package no.uio.ifi.in2000.prosjekt.model

import kotlinx.serialization.Serializable

@Serializable
data class BigDataCloud(
    val latitude : Double? = null,
    val lookupSource : String? = null,
    val longitude : Double? = null,
    val localityLanguageRequested : String? = null,
    val continent : String? = null,
    val continentCode : String? = null,
    val countryName : String? = null,
    val countryCode : String? = null,
    val principalSubdivision : String? = null,
    val principalSubdivisionCode : String? = null,
    val city : String? = null,
    val locality : String? = null,
    val postcode : String? = null,
    val plusCode : String? = null
)
