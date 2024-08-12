package no.uio.ifi.in2000.prosjekt.model

import kotlinx.serialization.Serializable

@Serializable
data class GeoNorge(
    val metadata : MetaDataGeoNorge? = null,
    val adresser : List<Adresse>? = null
)
@Serializable
data class MetaDataGeoNorge(
    val side : String? = null,
    val asciiKompatibel : Boolean? = null,
    val totaltAntallTreff : Int? = null,
    val viserTil : Int? = null,
    val sokeStreng : String? = null,
    val viserFra : String? = null,
    val treffPerSide : String? = null
)
@Serializable
data class Adresse(
    val adressenavn: String? = null,
    val adressetekst: String? = null,
    val adressetilleggsnavn: String? = null,
    val adressekode: Int? = null,
    val nummer: Int? = null,
    val bokstav: String? = null,
    val kommunenummer: String? = null,
    val kommunenavn: String? = null,
    val gardsnummer: Int? = null,
    val bruksnummer: Int? = null,
    val festenummer: Int? = null,
    val undernummer: Int? = null,
    val bruksenhetsnummer: List<String>? = null,
    val objtype: String? = null,
    val poststed: String? = null,
    val postnummer: String? = null,
    val adressetekstutenadressetilleggsnavn: String? = null,
    val stedfestingverifisert: Boolean? = null,
    val representasjonspunkt: Point? = null,
    val oppdateringsdato: String? = null
)
@Serializable
data class Point(
    val epsg: String? = null,
    val lat: Double? = null,
    val lon: Double? = null
)
