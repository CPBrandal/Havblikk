package no.uio.ifi.in2000.prosjekt.ui.home

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import no.uio.ifi.in2000.prosjekt.data.bigDataCloud.BigDataCloudRepository
import no.uio.ifi.in2000.prosjekt.data.enTur.EnTurRepository
import no.uio.ifi.in2000.prosjekt.data.havvarsel.HavvarselRepository
import no.uio.ifi.in2000.prosjekt.data.weatherForecast.LocationForecastRepository
import no.uio.ifi.in2000.prosjekt.model.BigDataCloud
import no.uio.ifi.in2000.prosjekt.model.DataProjectionMain
import no.uio.ifi.in2000.prosjekt.model.FeaturesEnTur
import no.uio.ifi.in2000.prosjekt.model.WeatherData
import no.uio.ifi.in2000.prosjekt.ui.home.DataStoreManager.LOCATION_UI_STATE_KEY
import no.uio.ifi.in2000.prosjekt.ui.home.DataStoreManager.dataStore
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


object DataStoreManager {
    val LOCATION_UI_STATE_KEY = stringPreferencesKey("location_ui_state")
    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "Cities")
}

@Serializable
data class CombinedWeatherData(
    val weatherData: WeatherData?,
    val dataProjectionMain: DataProjectionMain?,
    val bigDataCloud: BigDataCloud?,
    val enTurLocationName: String? = null /*Optional field for storing the location name as fetched from the EnTur API.*/

)
@Serializable
data class LocationUIState(
    /*Holds a map where each key is a string representing a location coordinate pair, and the value is the combined weather data for that location*/
    val combinedDataMap: Map<String, CombinedWeatherData> = emptyMap(),
    /* Stores a pair representing the current location key and its associated combined weather data.*/
    val locationCombined : Pair<String, CombinedWeatherData?>? = null,
    val suggestion: List<FeaturesEnTur>? = emptyList()
)

val json = Json {
    serializersModule = SerializersModule {
        // If you have custom serializers, you can include them here
    }
    ignoreUnknownKeys = true // Useful for backward compatibility
    encodeDefaults = true
}
/*
I am serializing the data class into a JSON string for storing it in datastore
 and then deserializing it back into the same data class.
 */
fun serializeLocationUIState(state: LocationUIState): String {
    return json.encodeToString(LocationUIState.serializer(), state)
}

fun deserializeLocationUIState(serializedData: String): LocationUIState {
    return json.decodeFromString(LocationUIState.serializer(), serializedData)
}
/*
This extension function is used to save the location UI state to DataStore
 */
suspend fun Context.saveLocationUIState(state: LocationUIState) {
    val serializedState = serializeLocationUIState(state)
    dataStore.edit { preferences ->
        preferences[LOCATION_UI_STATE_KEY] = serializedState
    }
}
/*
Loading the location UI state from DataStore
 */
suspend fun Context.loadLocationUIState(): LocationUIState? {
    // This assumes you have an extension function or a utility to read from DataStore and deserialize
    val preferences = dataStore.data.first() // Use .first() to get the current value, be mindful of the thread it's called on
    val serializedState = preferences[LOCATION_UI_STATE_KEY]
    return if (serializedState != null) deserializeLocationUIState(serializedState) else null
}

class HomeScreenViewModel(application: Application): AndroidViewModel(application){
    private val locationForecastrepository: LocationForecastRepository = LocationForecastRepository()
    private val havvarselRepository: HavvarselRepository = HavvarselRepository()
    private  val bigDataCloudDataRepository: BigDataCloudRepository = BigDataCloudRepository()
    private val enTurRepository: EnTurRepository = EnTurRepository()
    private val _isPopupVisible = MutableStateFlow(false)
    val isPopupVisible: StateFlow<Boolean> = _isPopupVisible.asStateFlow()

    /*Holds the current UI state related to locations including maps, current location, and suggestions*/
    private val _locationUIstate = MutableStateFlow(LocationUIState())
    val locationUIState: StateFlow<LocationUIState> = _locationUIstate.asStateFlow()

    init {
        viewModelScope.launch {
            val appContext = getApplication<Application>()
            val storedState = appContext.loadLocationUIState()
            if(storedState?.combinedDataMap == null) {
                /* If no data is stored, fetches default weather data for predefined locations*/
                val locMap = hashMapOf<Pair<String, String>, String?>(
                    Pair("59.911075", "10.748128") to "Oslo",
                    Pair("60.391789", "5.326067") to "Bergen"
                )
                fetchWeatherData(locMap)
            }
            else{
                /*
                Checking if the user have allowed to use their location and if so calling the function
                fetchLocationWeatherData and getting the updated location data
                 */
                getLocationViewModel(appContext) { location ->
                    val lat = location.latitude.toString()
                    val lon = location.longitude.toString()
                    fetchLocationWeatherData(Pair(lat, lon))
                }
                /*
                Going through the stored data and updating all the data from the api
                 */
                val locationDataMap: Map<Pair<String, String>, String?> = storedState.combinedDataMap.map { (key, value) ->
                    val coordinates = key.split(",").let {
                        Pair(it[0].trim(), it[1].trim()) // Ensuring there is no extra whitespace
                    }
                    coordinates to value.enTurLocationName
                }.toMap()
                fetchWeatherData(locationDataMap)
            }
        }
    }
    /*Toggles the visibility state of the popup UI*/
    fun toggleVisibility(){
        _isPopupVisible.value = !_isPopupVisible.value
    }

    fun triggerSaveState(context: Context, state: LocationUIState) {
        viewModelScope.launch {
            context.saveLocationUIState(state)
        }
    }

    /*Fetches suggestions based on user input from the EnTur API*/
    fun fetchSuggestions(locationName: String){
        viewModelScope.launch {
            val suggestionsTemp = enTurRepository.getEnTurAPI(locationName)?.features
            _locationUIstate.update {currenState ->
                currenState.copy(suggestion = suggestionsTemp)
            }
        }
    }
    /*Clears any current suggestions, typically called when the user clears the search or closes the search UI.*/
    fun clearSuggestions(){
        viewModelScope.launch {
            _locationUIstate.update {currenState ->
                currenState.copy(suggestion = emptyList())
            }
        }
    }
    /*
    I want the if check to have an empty body here because i dont want to ask for location permissions every time
    they open the app.
     */
    private fun getLocationViewModel(
        context: Context,

        locationCallback: (Location) -> Unit // Callback function to receive location
    ) {
        val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)
        if ((ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED)
        ) {
        } else {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        /*
                        using a callback here because it is an asyncrounous operation and we dont want to block the main thread
                        If i would return here i will not get the data the same way
                         */
                        locationCallback(location) // Call the callback with location
                    }
                }
            return
        }
    }
    /*Fetches and updates weather data for a list/map of locations.*/
    private fun fetchWeatherData(locations: Map<Pair<String,String>, String?>){
        locations.forEach{(location, name) ->
            viewModelScope.launch {
                val weatherData = locationForecastrepository.getLocationForecast(location.first, location.second, null)
                val currentDateTime = Instant.now()
                val norwegianTimeZone = ZoneId.of("Europe/Oslo")
                val norwegianDateTime = currentDateTime.atZone(norwegianTimeZone).toLocalDateTime()
                val roundedDateTime = norwegianDateTime.withMinute(0).withSecond(0).withNano(0)
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

                val formattedNowDateTime = formatter.format(roundedDateTime)
                val norwegianPlusOneDateTime = roundedDateTime.plusHours(1)
                val formattedPlusOneDateTime = formatter.format(norwegianPlusOneDateTime)

                val seaTemperatureAndSalinity = havvarselRepository.getHavvarselDataProjection(listOf("temperature", "salinity"),location.second,location.first,null,formattedPlusOneDateTime,formattedNowDateTime)
                val bigDataCloudData = bigDataCloudDataRepository.getBigDataCloud(location.first, location.second)


                _locationUIstate.update {currentState ->
                    val updatedMap = currentState.combinedDataMap.toMutableMap()
                    val locationKey = "${location.first}, ${location.second}"

                    updatedMap[locationKey] = CombinedWeatherData(weatherData, seaTemperatureAndSalinity, bigDataCloudData, name)
                    currentState.copy(combinedDataMap = updatedMap)
                }
            }
        }
    }
    /*Fetches weather data for the user's current location.*/
    /*
    I am only fetching the city that you are in for your location.
     */
    fun fetchLocationWeatherData(location: Pair<String,String>) {
        viewModelScope.launch {
            val weatherData = locationForecastrepository.getLocationForecast(
                location.first,
                location.second,
                null
            )
            val currentDateTime = Instant.now()
            val norwegianTimeZone = ZoneId.of("Europe/Oslo")
            val norwegianDateTime = currentDateTime.atZone(norwegianTimeZone).toLocalDateTime()
            val roundedDateTime = norwegianDateTime.withMinute(0).withSecond(0).withNano(0)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

            val formattedNowDateTime = formatter.format(roundedDateTime)
            val norwegianPlusOneDateTime = roundedDateTime.plusHours(1)
            val formattedPlusOneDateTime = formatter.format(norwegianPlusOneDateTime)


            val seaTemperatureAndSalinity = havvarselRepository.getHavvarselDataProjection(
                listOf("temperature", "salinity"),
                location.second,
                location.first,
                null,
                formattedPlusOneDateTime,
                formattedNowDateTime
            )
            val bigDataCloudData =
                bigDataCloudDataRepository.getBigDataCloud(location.first, location.second)
            _locationUIstate.update {currentState ->
                val locationName = bigDataCloudDataRepository.getBigDataCloud(location.first, location.second)
                val locationKey = "${location.first}, ${location.second}"

                val myLoc = Pair(locationKey,CombinedWeatherData(weatherData, seaTemperatureAndSalinity, bigDataCloudData, locationName?.city))

                currentState.copy(locationCombined = myLoc)

            }
        }
    }
    /*Adds new weather data to the existing map of locations.*/
    private fun addWeatherData(locationKey: String, weatherData: CombinedWeatherData){
        _locationUIstate.update {currenState->
            val updatedMap = currenState.combinedDataMap.toMutableMap().apply {
                put(locationKey, weatherData)
            }
            currenState.copy(combinedDataMap = updatedMap)
        }
    }
    /*Adds a new location by its name using geographic coordinates and fetched weather data.*/
    fun addLocationByName(locationName: String, context: Context){
        viewModelScope.launch {

            val findSuggestion = _locationUIstate.value.suggestion?.find {
                (it.properties.label == locationName)
            }
            val cordinates = findSuggestion?.geometry?.coordinates
            val weatherData = locationForecastrepository.getLocationForecast(cordinates?.get(1).toString(), cordinates?.get(0).toString(), null)

            val nowWithOffset = ZonedDateTime.now(ZoneOffset.of("+01:00"))
            val oneHourLater = nowWithOffset.plusHours(1)
            val formattedOneHourLater = oneHourLater.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            val formattedNow = nowWithOffset.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

            val seaTemperatureAndSalinity = havvarselRepository.getHavvarselDataProjection(listOf("temperature", "salinity"),cordinates?.get(0).toString(),cordinates?.get(1).toString(),null,formattedOneHourLater,formattedNow)
            val bigDataCloudData = bigDataCloudDataRepository.getBigDataCloud(cordinates?.get(1).toString(), cordinates?.get(0).toString())

            addWeatherData("${cordinates?.get(1)}, ${cordinates?.get(0)}", CombinedWeatherData(weatherData, seaTemperatureAndSalinity, bigDataCloudData, findSuggestion?.properties?.label))
            triggerSaveState(context, locationUIState.value)
        }
    }
    /*Deletes a location from the current map by its coordinate key*/
    fun deleteLocation(locationCordinate: String){
        _locationUIstate.update {currenState ->
            val updatedMap = currenState.combinedDataMap.toMutableMap().apply {
                remove(locationCordinate)
            }
            currenState.copy(combinedDataMap = updatedMap)
        }
    }
}
