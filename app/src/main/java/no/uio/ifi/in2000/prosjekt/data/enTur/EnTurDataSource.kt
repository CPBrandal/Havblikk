package no.uio.ifi.in2000.prosjekt.data.enTur

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.prosjekt.model.EnTur

class EnTurDataSource {
    private val client = HttpClient(CIO){
        defaultRequest {
            url("https://api.entur.io/geocoder/v1/autocomplete?")
            header("ET-Client-Name", "app-for-havvarsel.no")
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
    Gets out a list of "features" which are lists of names of places, also a variable
    Have to replace some norwegian letters with their english counterparts and replace spaces with %20
    Have set the number of results to 10, this could be anything
     */
    suspend fun getEnTurAutoComplete( text : String, noOfResults : Int = 10) : EnTur?{ //"format: 2024-03-19T23:00:00.000+01:00"

        val newText = text.lowercase()
            .replace(" ", "%20")
            .replace("ø", "oe")
            .replace("å", "aa")
            .replace("æ", "ae")

        return try {
            client.get("https://api.entur.io/geocoder/v1/autocomplete?text=$newText&size=$noOfResults&lang=no").body()
        } catch (e: Exception) {
            // Handle the exception (log, throw a custom exception, etc.)
            println("Error during HTTP request for EnTur: $e")
            null
        }
    }
}
