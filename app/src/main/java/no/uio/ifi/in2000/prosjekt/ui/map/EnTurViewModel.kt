package no.uio.ifi.in2000.prosjekt.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.prosjekt.data.enTur.EnTurRepository
import no.uio.ifi.in2000.prosjekt.ui.home.LocationUIState


class EnTurViewModel : ViewModel() {
    private val enTurRepository: EnTurRepository = EnTurRepository()

    private val _locationUIstate = MutableStateFlow(LocationUIState())
    val locationUIState: StateFlow<LocationUIState> = _locationUIstate.asStateFlow()

    private val _isPopupVisible = MutableStateFlow(false) /* Provides information about whether the popup is visible or not */
    val isPopupVisible: StateFlow<Boolean> = _isPopupVisible.asStateFlow()

    /* Function to get suggestions, and update the UI state based on the location*/
    fun fetchSuggestions(locationName: String){
        viewModelScope.launch {
            val suggestionsTemp = enTurRepository.getEnTurAPI(locationName)?.features
            Log.d("Suggestions", "Fetched ${suggestionsTemp?.size} suggestions")
            //_suggestionsUIstate.value = suggestionsTemp
            _locationUIstate.update {currenState ->
                currenState.copy(suggestion = suggestionsTemp)
            }
        }
    }

    /* Clears the list of suggestions */
    fun clearSuggestions(){
        viewModelScope.launch {
            _locationUIstate.update {currenState ->
                currenState.copy(suggestion = emptyList())
            }

        }
    }

    fun toggleVisibility(){
        _isPopupVisible.value = !_isPopupVisible.value
    }
}