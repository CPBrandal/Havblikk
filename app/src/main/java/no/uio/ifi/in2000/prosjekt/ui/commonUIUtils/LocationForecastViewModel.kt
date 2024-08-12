package no.uio.ifi.in2000.prosjekt.ui.commonUIUtils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.yml.charts.common.model.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.prosjekt.data.weatherForecast.LocationForecastRepository
import no.uio.ifi.in2000.prosjekt.model.TimeSeries
import no.uio.ifi.in2000.prosjekt.model.WeatherData

data class LocationForecastUiState(
    val weatherData: WeatherData? = WeatherData(),
    var weatherList: List<TimeSeries?> = emptyList()
)
/* Viewmodel for locationforecast, can be used by any screen */
class LocationForecastViewModel(coords: String): ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val liste = coords.split(",")
    val lat = liste[0].trim()
    val lon = liste[1].trim()
    private val locationForecastRepo = LocationForecastRepository()

    private val _lFUiState = MutableStateFlow(LocationForecastUiState())
    val lFUiState: StateFlow<LocationForecastUiState> = _lFUiState.asStateFlow()

    var startHour = 0

    init {
        getNewData(lat, lon)
    }

    private fun getNewData(lat: String, lon: String) {
        toggleLoading()
        viewModelScope.launch {
            _lFUiState.update {
                it.copy(
                    weatherData = locationForecastRepo.getLocationForecast(lat, lon)
                )
            }
            setStartHour()
            makeWeatherList(0)
            toggleLoading()
        }
    }

    private fun toggleLoading() {
        _isLoading.value = !_isLoading.value
    }

    /* Get the the hour of the first item we get data about */
    private fun setStartHour() {
        if(_lFUiState.value.weatherData?.properties?.timeseries?.isEmpty() == true){
            return
        }
        startHour = (_lFUiState.value.weatherData?.properties?.timeseries?.get(0)?.time)?.substring(11, 13)
                ?.toIntOrNull() ?: 0
    }

    fun makeWeatherList(offset: Int){
        toggleLoading()
        if(_lFUiState.value.weatherData?.properties?.timeseries?.isEmpty() == true){
            toggleLoading()
            return
        }
        val currentHour = getCurrentHour()
        /* There is a difference between the start hour (first hour we get data for) and the current hour
        *  so we calculate for it. Also we calculate for offset, being next day (24), or the day after (48) */
        val list = when (offset) {
            0 -> when(currentHour >= startHour) {
                true -> (startHour..23).map { index -> _lFUiState.value.weatherData?.properties?.timeseries?.get(index-startHour) }
                else -> (0..23-startHour).map { index -> _lFUiState.value.weatherData?.properties?.timeseries?.get(24-startHour+index) }
            }
            else -> when(currentHour >= startHour) {
                true -> (0..23).map { index -> _lFUiState.value.weatherData?.properties?.timeseries?.get(index-startHour+offset) }
                false -> (0..23).map { index -> _lFUiState.value.weatherData?.properties?.timeseries?.get(24-startHour+index+offset) }
            }
        }
        _lFUiState.value.weatherList = list
        toggleLoading()
    }

    fun linechartMaker(offset: Int, variable: String): List<Point> {
        if(_lFUiState.value.weatherData?.properties?.timeseries?.isEmpty() == true){
            val pointData = (0..23).map { index -> Point(index.toFloat(), 0.0f) }
            return pointData
        }
        val currentHour = getCurrentHour()
        /* There is a difference between the start hour (first hour we get data for) and the current hour
        *  so we calculate for it. Also we calculate for offset, being next day (24), or the day after (48) */
        val pointData = when (offset) {
            0 -> when(currentHour >= startHour) {
                true -> (startHour..23).map { index ->
                    val temperature = (_lFUiState.value.weatherData?.properties?.timeseries
                        ?.get(index-startHour)?.data?.instant?.details
                        ?.get(variable)?:0).toFloat()
                    Point(index.toFloat(), temperature)}
                else -> (0..23).map { index ->
                    val temperature = (_lFUiState.value.weatherData?.properties?.timeseries
                        ?.get(24-startHour+index)?.data?.instant?.details
                        ?.get(variable)?:0).toFloat()

                    Point(index.toFloat(), temperature)
                }
            } /* Add the offset, 24 or 48 hours */
            else -> when(currentHour >= startHour) {
                true -> (0..23).map { index ->
                    val temperature = (_lFUiState.value.weatherData?.properties?.timeseries
                        ?.get(index-startHour+offset)?.data?.instant?.details
                        ?.get(variable)?:0).toFloat()
                    Point(index.toFloat(), temperature)}
                else -> (0..23).map { index ->
                    val temperature = (_lFUiState.value.weatherData?.properties?.timeseries
                        ?.get(24-startHour+index+offset)?.data?.instant?.details
                        ?.get(variable)?:0).toFloat()
                    Point(index.toFloat(), temperature)
                }
            }
        }
        return pointData
    }
}