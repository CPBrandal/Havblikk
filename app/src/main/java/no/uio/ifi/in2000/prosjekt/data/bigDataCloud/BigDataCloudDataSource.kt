package no.uio.ifi.in2000.prosjekt.data.bigDataCloud

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.prosjekt.model.BigDataCloud



class BigDataCloudDataSource {
    private val client = HttpClient(CIO){
        defaultRequest {
            url("https://api.bigdatacloud.net/data/reverse-geocode-client?")
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
    place can be either a city or a country
     */
    suspend fun getPlaceFromCoordinates(lat : String, lon : String) : BigDataCloud? {
        return try {
            client.get("https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=$lat&longitude=$lon")
                .body()
        } catch (e: Exception) {
            println("Error during HTTP request for metAlerts: $e")
            return null
        }
    }
}
