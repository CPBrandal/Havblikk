package no.uio.ifi.in2000.prosjekt.ui.commonUIUtils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.yml.charts.common.model.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.prosjekt.data.oceanForecast.OceanForecastRepository
import no.uio.ifi.in2000.prosjekt.model.OceanForecastData
import no.uio.ifi.in2000.prosjekt.model.OceanTimeSeries

/* Data class for ocean forcast data */
data class OceanForecastUiState(
    val oceanForecastData: OceanForecastData? = OceanForecastData(),
    var oceanWeatherList: List<OceanTimeSeries?> = emptyList()
)

class OceanForecastViewModel(coords: String): ViewModel(){
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _waveMap = MutableStateFlow<Map<String, Double>>(emptyMap())
    val waveMap: StateFlow<Map<String, Double>> = _waveMap

    private val liste = coords.split(",")
    val lat = liste[0].trim()
    val lon = liste[1].trim()
    private val oceanForecastRepo = OceanForecastRepository()

    private val _oFUiState = MutableStateFlow(OceanForecastUiState())
    val oFUiState: StateFlow<OceanForecastUiState> = _oFUiState.asStateFlow()

    var startHour = 0
    init {
        getOceanData(lat, lon)
    }

    private fun getOceanData(lat : String, lon : String){
        toggleLoading()
        viewModelScope.launch {
            _oFUiState.update {
                it.copy(
                    oceanForecastData = oceanForecastRepo.getOceanForecast(lat,lon)
                )
            }
            setStartHour()
            makeWeatherList(0)
            waveHeight()
            toggleLoading()
        }
    }

    private fun toggleLoading() {
        _isLoading.value = !_isLoading.value
    }

    /* Defines the first hour we get data for */
    private fun setStartHour(){
        if(_oFUiState.value.oceanForecastData?.properties?.timeseries?.isEmpty() == true){
            return
        }
        startHour = (_oFUiState.value.oceanForecastData?.properties?.timeseries?.get(0)?.time)?.substring(11, 13)?.toIntOrNull() ?: 0
    }

    /* Creates a map of <Hour, Wave Height> for up to 24 hours */
    private fun waveHeight(){
        if(_oFUiState.value.oceanForecastData?.properties?.timeseries?.isEmpty() == true){
            return
        }
        val map = mutableMapOf<String, Double>()
        var max = 0.0
        var maxTid = 0
        val currentHour = getCurrentHour()
        /* There is an offset between the start hour (first hour we get data for) and the current hour
        *  so we calculate for it */
        if(currentHour >= startHour){
            repeat(24-startHour){
                val tid = (_oFUiState.value.oceanForecastData?.properties?.timeseries?.get(it)?.time)?.substring(11, 13)?.toIntOrNull() ?: 0
                val height = _oFUiState.value.oceanForecastData?.properties?.timeseries?.get(it)?.data?.instant?.details?.get("sea_surface_wave_height") ?: -1.0
                map[tid.toString()] = height /* Avoid nullpointerexception */
                if(height >= max){
                    max = height
                    maxTid = tid
                }
            }
        } else {
            repeat(24 - currentHour) {
                val tid = (_oFUiState.value.oceanForecastData?.properties?.timeseries?.get(24-startHour+currentHour+it)?.time)?.substring(11, 13)?.toIntOrNull() ?: 0
                val height = _oFUiState.value.oceanForecastData?.properties?.timeseries?.get(24-startHour+currentHour+it)?.data?.instant?.details?.get("sea_surface_wave_height") ?: -1.0
                map[tid.toString()] = height /* Avoid nullpointerexception */
                if(height >= max){
                    max = height
                    maxTid = tid
                }
            }
        }

        map["max"] = max /* Adds max height */
        map["tid"] = maxTid.toDouble() /* Adds the hour for the max */
        _waveMap.value = map
    }

    fun makeWeatherList(offset: Int){
        toggleLoading()
        if(_oFUiState.value.oceanForecastData?.properties?.timeseries?.isEmpty() == true){
            toggleLoading()
            return
        }
        val currentHour = getCurrentHour()
        /* There is a difference between the start hour (first hour we get data for) and the current hour
        *  so we calculate for it. Also we calculate for offset, being next day (24), or the day after (48) */
        val list = when (offset) {
            0 -> when(currentHour >= startHour) {
                true -> (startHour..23).map { index -> _oFUiState.value.oceanForecastData?.properties?.timeseries?.get(index-startHour) }
                else -> (0..23-currentHour).map { index -> _oFUiState.value.oceanForecastData?.properties?.timeseries?.get(24-startHour+index) }
            }
            else -> when(currentHour >= startHour) {
                true -> (0..23).map { index -> _oFUiState.value.oceanForecastData?.properties?.timeseries?.get(index + offset - startHour) }
                else -> (0..23).map { index -> _oFUiState.value.oceanForecastData?.properties?.timeseries?.get(24-startHour+index+offset) }
            }
        }
        _oFUiState.value.oceanWeatherList = list
        toggleLoading()
    }

    fun linechartMaker(offset: Int, variable: String): List<Point> {
        if(_oFUiState.value.oceanForecastData?.properties?.timeseries?.isEmpty() == true){
            val pointData = (0..23).map { index -> Point(index.toFloat(), 0.0f) }
            return pointData
        }
        val currentHour = getCurrentHour()
        /* There is a difference between the start hour (first hour we get data for) and the current hour
        *  so we calculate for it. Also we calculate for offset, being next day (24), or the day after (48) */
        val pointData = when (offset) {
            0 -> when(currentHour >= startHour) {
                true -> (startHour..23).map { index ->
                    val temperature = (oFUiState.value.oceanForecastData?.properties?.timeseries
                        ?.get(index-startHour)?.data?.instant?.details
                        ?.get(variable) ?: 0).toFloat()
                    Point(index.toFloat(), temperature)
                }
                else -> (0..23-currentHour).map { index ->
                    val temperature = (_oFUiState.value.oceanForecastData?.properties?.timeseries
                        ?.get(24-startHour+index)?.data?.instant?.details
                        ?.get(variable) ?: 0).toFloat()
                    Point(index.toFloat(), temperature)
                }
            }
            else -> when(currentHour >= startHour) {
                true -> (0..23).map { index ->
                    val temperature = (_oFUiState.value.oceanForecastData?.properties?.timeseries
                        ?.get(index + offset - startHour)?.data?.instant?.details
                        ?.get(variable)?: 0).toFloat()

                    Point(index.toFloat(), temperature)
                }
                else -> (0..23).map { index ->
                    val temperature = (_oFUiState.value.oceanForecastData?.properties?.timeseries
                        ?.get(24-startHour+index+offset)?.data?.instant?.details
                        ?.get(variable)?: 0).toFloat()

                    Point(index.toFloat(), temperature)
                }
            }
        }
        return pointData
    }
}