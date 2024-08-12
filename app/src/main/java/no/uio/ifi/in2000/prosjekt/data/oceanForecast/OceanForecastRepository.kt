package no.uio.ifi.in2000.prosjekt.data.oceanForecast

import no.uio.ifi.in2000.prosjekt.model.OceanForecastData

class OceanForecastRepository{
    private val oceanForecastDataSrc = OceanForecastDataSource()

    suspend fun getOceanForecast(lat : String, lon : String): OceanForecastData? {
        return oceanForecastDataSrc.getOceanForecast(lat, lon)
    }
}