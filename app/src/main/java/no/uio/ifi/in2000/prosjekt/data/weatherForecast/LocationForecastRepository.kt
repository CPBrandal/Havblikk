package no.uio.ifi.in2000.prosjekt.data.weatherForecast

import no.uio.ifi.in2000.prosjekt.model.WeatherData

class LocationForecastRepository {
    private val locationForeCastDataSrc = LocationForecastDataSource()
    suspend fun getLocationForecast(lat : String, lon : String, alt : String? = null) : WeatherData?{
        return locationForeCastDataSrc.getLocationForecast(lat, lon, alt)
    }
}