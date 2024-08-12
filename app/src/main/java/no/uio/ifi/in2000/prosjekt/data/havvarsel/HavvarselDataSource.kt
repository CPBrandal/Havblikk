package no.uio.ifi.in2000.prosjekt.data.havvarsel

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.prosjekt.model.DataProjectionMain
import no.uio.ifi.in2000.prosjekt.model.Temperature
import no.uio.ifi.in2000.prosjekt.model.TimeValues
import no.uio.ifi.in2000.prosjekt.model.WindCurrentProjection
import java.net.URLEncoder

class HavvarselDataSource {
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
    //timevalues
    suspend fun getHavvarselTimeValues() : TimeValues?{ //"2024-03-19T23:00:00.000+01:00"
        return try {
            client.get("https://api.havvarsel.no/apis/duapi/havvarsel/v2/times").body()
        } catch (e: Exception) {
            // Handle the exception (log, throw a custom exception, etc.)
            println("Error during HTTP request for locationforecast: $e")
            null
        }
    }
    //dataprojection

    //Format: "2024-03-19T23:00:00.000+01:00"
    /*
    have to convert to UTF-8 and replace symbols to get the correct url because symbols such as + and : are not allowed
     */
    suspend fun getHavvarselDataProjection(variables: List<String>, lon: String, lat: String, depth: String? = null, before : String? = null, after: String? = null) : DataProjectionMain?{
        val variablesString = variables.joinToString(separator = "%2C")
        var fullAPIString = "https://api.havvarsel.no/apis/duapi/havvarsel/v2/dataprojection/$variablesString/$lon/$lat?"


        if(after != null) {
            val encodedString = withContext(Dispatchers.IO) {
                URLEncoder.encode(after, "UTF-8")
            }
                .replace("+", "%20")
                .replace(":", "%3A")
            fullAPIString += "after=$encodedString"
        }
        if(before != null) {
            val encodedString = withContext(Dispatchers.IO) {
                URLEncoder.encode(before, "UTF-8")
            }
                .replace("+", "%20")
                .replace(":", "%3A")
            fullAPIString += "&before=$encodedString"
        }
        if(depth != null) fullAPIString += "&depth=$depth"


        return try {
            client.get(fullAPIString).body()
        } catch (e: Exception) {
            // Handle the exception (log, throw a custom exception, etc.)
            println("Error during HTTP request for DataProjection: $e")
            null
        }
    }
    //Temperatur
    //"2024-03-19T23:00:00.000+01:00"
    suspend fun getHavvarselTemp(longitude : String, latitude : String, depth: String? = null, before : String? = null, after: String? = null) : Temperature?{ //for å få havvarrsel sitt temperatur api
        val coordinates = "$longitude/$latitude"
        var fullAPIString = "https://api.havvarsel.no/apis/duapi/havvarsel/v2/temperatureprojection/$coordinates?"

        if(after != null) {
            val encodedString = URLEncoder.encode(after, "UTF-8")
                .replace("+", "%20")
                .replace(":", "%3A")
            fullAPIString += "after=$encodedString"
        }
        if(before != null) {
            val encodedString = URLEncoder.encode(before, "UTF-8")
                .replace("+", "%20")
                .replace(":", "%3A")
            fullAPIString += "&before=$encodedString"
        }
        if(depth != null) fullAPIString += "&depth=$depth"

        return try {
            client.get(fullAPIString).body()
        } catch (e: Exception) {
            // Handle the exception (log, throw a custom exception, etc.)
            println("Error during HTTP request for HavvarselTemperature: $e")
            null
        }
    }

    //WindCurrentProjection
    suspend fun getHavvarselWindCurrent(lonCorner1 : String, latCorner1 : String, lonCorner2 : String, latCorner2 : String, time : String) : WindCurrentProjection?{ //Time skrives yy-mm-dd
        val information = "$lonCorner1/$latCorner1/$lonCorner2/$latCorner2/${time}T00%3A00%3A00.000%2B01%3A00"
        return try {
            client.get("https://api.havvarsel.no/apis/duapi/havvarsel/v2/windandcurrentprojection/$information").body()
        } catch (e: Exception) {
            // Handle the exception (log, throw a custom exception, etc.)
            println("Error during HTTP request for WindAndCurrentProjection: $e")
            null
        }
    }
}
