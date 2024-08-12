package no.uio.ifi.in2000.prosjekt.data.enTur

import no.uio.ifi.in2000.prosjekt.model.EnTur

class EnTurRepository {
    private val enTurDataSource = EnTurDataSource()

    suspend fun getEnTurAPI(text : String, noOfResults : Int = 10) : EnTur?{
        return enTurDataSource.getEnTurAutoComplete(text, noOfResults)
    }
}