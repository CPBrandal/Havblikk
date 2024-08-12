package no.uio.ifi.in2000.prosjekt.model

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class MetAlerts(
    val features: List<Warning?>? = null,
    val lang: String? = null,
    val lastChange: String? = null,
    val type: String? = null
)

@Serializable
data class Warning(
    val geometry: PolymorphicItem?,
    val properties: PropertiesA,
    val type: String?,
    @SerialName("when")
    val timeinterval: TimeInterval // må bruke serialname fordi "when" ikke kan være navn
)

@Serializable
enum class SectionType {
    @SerialName("Polygon")
    Polygon,

    @SerialName("MultiPolygon")
    MultiPolygon,
}

@Serializable(with = SectionSerializer::class)
sealed interface PolymorphicItem {
    @SerialName("type")
    val type : SectionType
}

@Serializable
data class PolygonSection(
    override val type: SectionType = SectionType.Polygon,
    @SerialName("coordinates")
    val coordinates: List<List<List<String?>>>,
) : PolymorphicItem

@Serializable
data class MultiPolygonSection(
    override val type: SectionType = SectionType.MultiPolygon,
    @SerialName("coordinates")
    val coordinates: List<List<List<List<String?>>>>
) : PolymorphicItem

@Serializable
data class PropertiesA(
    val MunicipalityId : String? = null,
    val administrativeId : String? = null,
    val area : String? = null,
    val awarenessResponse : String? = null,
    val awarenessSeriousness : String? = null,
    val awareness_level : String? = null,
    val awareness_type : String? = null,
    val certainty : String? = null,
    val consequences : String? = null,
    val county : List<String?>,
    val description : String? = null,
    val event : String? = null,
    val eventAwarenessName : String? = null,
    val geographicDomain : String? = null,
    val id : String? = null,
    val instruction : String? = null,
    val resources: List<Fil>,
    val riskMatrixColor : String? = null,
    val severity : String? = null,
    val title: String? = null,
    val type : String? = null,
)

@Serializable
data class Fil(
    val description: String?,
    val mimeType : String?,
    val uri : String?
)

@Serializable
data class TimeInterval(
    val interval : List<String?>
)

object SectionSerializer :
    JsonContentPolymorphicSerializer<PolymorphicItem>(
        PolymorphicItem::class
    ) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<PolymorphicItem> {
        return when (element.jsonObject["type"]?.jsonPrimitive?.content) {
            "Polygon" -> PolygonSection.serializer()
            "MultiPolygon" -> MultiPolygonSection.serializer()
            else -> throw Exception("ERROR: No Serializer found. Serialization failed.")
        }
    }
}