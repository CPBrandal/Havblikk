package no.uio.ifi.in2000.prosjekt.model

import kotlinx.serialization.Serializable

@Serializable
data class EnTur(
    val features : List<FeaturesEnTur>? = null,
    val geometry: GeometryEnTur? = null
)
@Serializable
data class FeaturesEnTur(
    val geometry : GeometryEnTur,
    val properties : PropertiesEnTur
)

@Serializable
data class GeometryEnTur(
    val coordinates : List<String>
)

@Serializable
data class PropertiesEnTur(
    val label : String
)