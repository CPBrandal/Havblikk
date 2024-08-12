package no.uio.ifi.in2000.prosjekt.model

import kotlinx.serialization.Serializable

@Serializable
data class WeatherData(
    val type: String? = null,
    val geometry: Geometry? = null,
    val properties: Properties? = null
)
@Serializable
data class Geometry(
    val type: String? = null,
    val coordinates: List<Float> // index 0 = longitude, index 1 = latitude, index 2 = altitude
)
@Serializable
data class Properties(
    val meta: Meta? = null,
    val timeseries: List<TimeSeries?>
)
@Serializable
data class Meta(
    val updated_at: String?,
    val units: Map<String?,String?>
)

@Serializable
data class TimeSeries(
    val time: String? = null,
    val data: TimeSeriesData? = null
)
@Serializable
data class TimeSeriesData(
    val instant: InstantData? = null,
    val next_12_hours: Next12HoursData? = null,
    val next_1_hours: Next1HourData? = null,
    val next_6_hours: Next6HoursData? = null
)
@Serializable
data class InstantData(
    val details: Map<String, Double>
)
@Serializable
data class Next12HoursData(
    val summary: Map<String?, String?>?,
    val details: Map<String?, Double?>?
)
@Serializable
data class Next1HourData(
    val summary: Map<String?, String?>,
    val details: Map<String, Double>
)
@Serializable
data class Next6HoursData(
    val summary: Map<String?, String?>,
    val details: Map<String, Double>
)
//@Serializable
//data class Details(
//    val air_pressure_at_sea_level: Double? = null,
//    val air_temperature: Double? = null,
//    val cloud_area_fraction: Double? = null,
//    val cloud_area_fraction_high: Double? = null,
//    val cloud_area_fraction_low: Double? = null,
//    val cloud_area_fraction_medium: Double? = null,
//    val dew_point_temperature: Double? = null,
//    val fog_area_fraction: Double? = null,
//    val relative_humidity: Double? = null,
//    val wind_from_direction: Double? = null,
//    val wind_speed: Double? = null,
//    val wind_speed_of_gust: Double? = null
//) {
//    fun print(){
//        println(air_pressure_at_sea_level)
//        println(air_temperature)
//        println(cloud_area_fraction_high)
//        println(dew_point_temperature)
//        println(relative_humidity)
//        println(wind_speed)
//        println(wind_speed_of_gust)
//    }
//}
//@Serializable
//data class Details2(
//    val air_temperature_max: Double? = null,
//    val air_temperature_min: Double? = null,
//    val precipitation_amount: Double? = null,
//    val precipitation_amount_max: Double? = null,
//    val precipitation_amount_min: Double? = null,
//    val probability_of_precipitation: Double? = null,
//    val probability_of_thunder: Double? = null,
//    val ultraviolet_index_clear_sky_max: Double? = null
//)

