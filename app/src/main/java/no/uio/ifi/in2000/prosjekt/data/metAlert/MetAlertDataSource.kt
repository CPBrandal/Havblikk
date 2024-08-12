package no.uio.ifi.in2000.prosjekt.data.metAlert

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.prosjekt.model.MetAlerts


class MetAlertDataSource {
    private val client = HttpClient(CIO){
        defaultRequest {
            url("https://gw-uio.intark.uh-it.no/in2000/")
            header("X-Gravitee-API-Key", "ea84d4ab-11b1-4c33-b336-6a884067c35e") // API-key til proxy server
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
    Getting metalerts for the marine area
     */
    suspend fun getMetAlerts(): MetAlerts? {
        return try {
            client.get("weatherapi/metalerts/2.0/current.json?geographicDomain=marine").body()
        } catch (e: Exception) {
            println("Error during HTTP request for metAlerts: $e")
            return null
        }
    }

    /*
    Getting metalerts for a specific location
     */
    suspend fun getMetAlertsCoordinates(lat : String, lon: String): MetAlerts? {
        return try {
            client.get("weatherapi/metalerts/2.0/current.json?lat=$lat&lon=$lon").body()
        } catch (e: Exception) {
            println("Error during HTTP request for metAlerts: $e")
            return null
        }
    }
}
