package no.uio.ifi.in2000.prosjekt


import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import no.uio.ifi.in2000.prosjekt.data.bigDataCloud.BigDataCloudRepository
import no.uio.ifi.in2000.prosjekt.data.enTur.EnTurRepository
import no.uio.ifi.in2000.prosjekt.data.havvarsel.HavvarselRepository
import no.uio.ifi.in2000.prosjekt.data.metAlert.MetAlertRepository
import no.uio.ifi.in2000.prosjekt.data.oceanForecast.OceanForecastRepository
import no.uio.ifi.in2000.prosjekt.data.weatherForecast.LocationForecastRepository
import org.junit.Test

class ApiTest {
    private val bigDataCloudAPI: BigDataCloudRepository = BigDataCloudRepository()
    private val locationForecastAPI: LocationForecastRepository = LocationForecastRepository()
    private val enturAPI:EnTurRepository = EnTurRepository()
    private val metAlertAPI: MetAlertRepository = MetAlertRepository()
    private val havvarselAPI: HavvarselRepository = HavvarselRepository()
    private val oceanAPI: OceanForecastRepository = OceanForecastRepository()

    @Test
    fun testBigDataCloudCity() = runBlocking{
        // Arrange
        val lat = "59.920649"
        val lon = "10.786299"

        // Act
        val result = bigDataCloudAPI.getBigDataCloud(lat, lon)

        // Assert
        assertNotNull(result)
        assertEquals("Oslo", result?.city)
    }
    @Test
    fun testLocationForecast() = runBlocking{
        val lat = "59.920649"
        val lon = "10.786299"

        val result = locationForecastAPI.getLocationForecast(lat,lon, null)

        assertNotNull(result)
    }
/* This test checks if the suggestions are between one and ten in size*/
    @Test
    fun testEnturSuggestions() = runBlocking{
        val search = "Oslo luft"

        val result = enturAPI.getEnTurAPI(search)

        assertNotNull(result)
        assertTrue(result?.features?.size in 0..10)
    }
    @Test
    fun testMetAlert() = runBlocking {
        val lat = "59.920649"
        val lon = "10.786299"

        val result = metAlertAPI.getMetAlertCoordinates(lat,lon)
        assertNotNull(result)
    }
    @Test
    fun testHavvarsel() = runBlocking{
        val lat = "59.920649"
        val lon = "10.786299"
        val result = havvarselAPI.getHavarselTemperature(lat, lon)
        assertNotNull(result)
    }

    @Test
    fun testOceanForecast() = runBlocking{
        val lat = "59.920649"
        val lon = "10.786299"
        val result = oceanAPI.getOceanForecast(lat, lon)
        assertNotNull(result)
    }



}