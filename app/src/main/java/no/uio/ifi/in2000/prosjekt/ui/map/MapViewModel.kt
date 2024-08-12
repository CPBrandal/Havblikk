package no.uio.ifi.in2000.prosjekt.ui.map

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.mapbox.geojson.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/* Data class to hold center and zoom level */
data class CameraSettings(
    var center: Point = Point.fromLngLat(60.0, 10.0),
    var zoom: Double = 10.0
)

/* Viewmodel for the maps zoom and center state */
class MapViewModel(context: Context) : ViewModel() {
    /* Hold state for center and zoom */
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("MapSettings", Context.MODE_PRIVATE)

    private var _camera = MutableStateFlow(CameraSettings())
    var camera: StateFlow<CameraSettings> = _camera

    init {
        val cameraSettings = loadCameraSettings()
        _camera.value = cameraSettings
    }

    fun updateMap(center: Point, zoom: Double) {
        val currentSettings = _camera.value.copy(center = center, zoom = zoom)
        saveCameraSettings(currentSettings)
        _camera.value = currentSettings
    }

    private fun loadCameraSettings(): CameraSettings {
        val centerLat = sharedPreferences.getFloat("center_lat", 59.9f).toDouble()
        val centerLon = sharedPreferences.getFloat("center_lon", 10.7f).toDouble()
        val zoom = sharedPreferences.getFloat("zoom", 10f).toDouble()
        val center = Point.fromLngLat(centerLon, centerLat)
        return CameraSettings(center, zoom)
    }

    private fun saveCameraSettings(cameraSettings: CameraSettings) {
        sharedPreferences.edit().apply {
            putFloat("center_lat", cameraSettings.center.latitude().toFloat())
            putFloat("center_lon", cameraSettings.center.longitude().toFloat())
            putFloat("zoom", cameraSettings.zoom.toFloat())
            apply()
        }
    }
}
