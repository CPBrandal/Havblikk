package no.uio.ifi.in2000.prosjekt.data.bigDataCloud

import no.uio.ifi.in2000.prosjekt.model.BigDataCloud

class BigDataCloudRepository {
    private val bigDataCloudDataSrc = BigDataCloudDataSource()

    suspend fun getBigDataCloud(lat : String, lon : String) : BigDataCloud?{
        return bigDataCloudDataSrc.getPlaceFromCoordinates(lat, lon)
    }
}