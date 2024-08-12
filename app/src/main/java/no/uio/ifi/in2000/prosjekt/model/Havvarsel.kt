package no.uio.ifi.in2000.prosjekt.model

import kotlinx.serialization.Serializable
//windcurrentprojection
@Serializable
data class WindCurrentProjection(
    val data : List<DataWindCurrent>? = null,
    val metadata: List<Variables>? = null
)
@Serializable
data class DataWindCurrent(
    val coords : Coords? = null,
    val current : Current? = null,
    val wind : Wind? = null,

)
@Serializable
data class Coords(
    val lat : String? = null,
    val lon : String? = null
)
@Serializable
data class Wind(
    val direction : String? = null,
    val strength : String? = null
)
@Serializable
data class Current(
    val direction : String? = null,
    val strength : String? = null
)


//Timevalues
@Serializable
data class TimeValues(
    val timeValues : List<String>? = null //Alle mulige tidspunkter det er forecast for
)

//Depths
@Serializable
data class Depths(
    val depthItems : List<DepthObjects>? = null
)

@Serializable
data class DepthObjects( //for Depths
    val depthIndex : String? = null,
    val depthValue : String? = null
)

//Dataprojection
@Serializable
data class DataProjectionMain(
    val closestGridPoint : ClosestGridPoint? = null,
    val closestGridPointWithData : ClosestGridPointWithData? = null,
    val data : List<DataProjection>? = null,
    val metadata : List<Variables>? = null,
    val queryPoint : QueryPoint? = null
)
@Serializable
data class DataProjection(
    val data : List<DataProjectionKV>? = null,
    val rawTime: String? = null
)
@Serializable
data class DataProjectionKV(
    val key : String? = null,
    val value : String? = null
)
//Temperature og dataprojection
@Serializable
data class Temperature(
    val variables: List<Variables>? = null,
    val queryPoint: QueryPoint? = null,
    val closestGridPoint: ClosestGridPoint? = null,
    val closestGridPointWithData: ClosestGridPointWithData? = null,


)
@Serializable
data class ClosestGridPoint( //Temperatur og dataprojection
    val distance: String? = null,
    val lat: String? = null,
    val lon: String? = null,
    val x: String? = null,
    val y: String? = null
)
@Serializable
data class ClosestGridPointWithData( //Temperatur og dataprojection
    val distance: String? = null,
    val lat: String? = null,
    val lon: String? = null,
    val x: String? = null,
    val y: String? = null
)
@Serializable
data class QueryPoint( //Temperatur og dataprojection
    val lat: String? = null,
    val lon: String? = null
)
@Serializable
data class Variables( //Temperatur og dataprojection
    val data: List<DataHavVarsel>? = null, //Hvert element i listen representerer temperatur for en ny time,
    //eks index 1 er 1 time etter klokken 00:00
    val dimensions: List<String?>? = null,
    val variableName : String? = null,
    val metadata : List<MetadataHavvarsel>? = null
)
@Serializable
data class DataHavVarsel( //Temperatur
    val rawTime: String? = null,
    val value: String? = null,

)
@Serializable
data class MetadataHavvarsel( //Temperatur
    val key : String? = null,
    val value : String? = null
)


