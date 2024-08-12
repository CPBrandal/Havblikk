package no.uio.ifi.in2000.prosjekt.data.oceanForecast

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.prosjekt.model.OceanForecastData

class OceanForecastDataSource {
    private val client = HttpClient(CIO){
        defaultRequest {
            url("https://gw-uio.intark.uh-it.no/in2000/")
            header("X-Gravitee-API-Key", "ea84d4ab-11b1-4c33-b336-6a884067c35e") // API-key to proxy server
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }
    /*
    Ocean forecast from MET
     */
    suspend fun getOceanForecast(latitude : String, longitude : String): OceanForecastData? {
        val coordinates = "lat=$latitude&lon=$longitude"
        return try {
            client.get("weatherapi/oceanforecast/2.0/complete?$coordinates").body()
        } catch (e: Exception) {
            // Handle the exception (log, throw a custom exception, etc.)
            println("Error during HTTP request for locationforecast: $e")
            null
        }
    }
}