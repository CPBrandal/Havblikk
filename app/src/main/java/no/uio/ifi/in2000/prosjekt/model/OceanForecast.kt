package no.uio.ifi.in2000.prosjekt.model

import kotlinx.serialization.Serializable
@Serializable
data class OceanForecastData(
    val type: String? = null,
    val geometry: OceanGeometry? = null,
    val properties: OceanProperties? = null
)

@Serializable
data class OceanGeometry(
    val type: String? = null,
    val coordinates: List<Float> // index 0 = longitude, index 1 = latitude, index 2 = altitude
)

@Serializable
data class OceanProperties(
    val meta: OceanMeta? = null,
    val timeseries: List<OceanTimeSeries?>
)

@Serializable
data class OceanMeta(
    val updated_at: String?,
    val units: Map<String?,String?>
)

@Serializable
data class OceanTimeSeries(
    val time: String? = null,
    val data: OceanTimeSeriesData? = null
)

@Serializable
data class OceanTimeSeriesData(
    val instant: OceanInstantData? = null,
    val next_12_hours: OceanNext12HoursData? = null,
    val next_1_hours: OceanNext1HourData? = null,
    val next_6_hours: OceanNext6HoursData? = null
)
@Serializable
data class OceanInstantData(
    val details: Map<String, Double>
)
@Serializable
data class OceanNext12HoursData(
    val summary: Map<String?, String?>?,
    val details: Map<String?, Double?>?
)
@Serializable
data class OceanNext1HourData(
    val summary: Map<String?, String?>,
        val details: Map<String, Double>
)
@Serializable
data class OceanNext6HoursData(
    val summary: Map<String?, String?>,
    val details: Map<String, Double>
)