package no.uio.ifi.in2000.prosjekt.data.havvarsel

import no.uio.ifi.in2000.prosjekt.model.DataProjectionMain
import no.uio.ifi.in2000.prosjekt.model.Temperature

class HavvarselRepository {
    private val havvarselDataSrc = HavvarselDataSource()

    suspend fun getHavarselTemperature(longitude : String, latitude : String, depth: String? = null, before : String? = null, after: String? = null) : Temperature?{
        return havvarselDataSrc.getHavvarselTemp(longitude, latitude, depth, before, after)
    }

    suspend fun getHavvarselDataProjection(variables: List<String>, lon: String, lat: String, depth: String? = null, before : String? = null, after: String? = null) : DataProjectionMain?{
        return havvarselDataSrc.getHavvarselDataProjection(variables, lon, lat, depth, before, after)
    }
}