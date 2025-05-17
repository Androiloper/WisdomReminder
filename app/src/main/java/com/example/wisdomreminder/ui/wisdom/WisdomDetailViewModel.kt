package com.example.wisdomreminder.ui.wisdom

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wisdomreminder.data.repository.WisdomRepository
import com.example.wisdomreminder.model.Wisdom
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WisdomDetailViewModel @Inject constructor(
    private val wisdomRepository: WisdomRepository
) : ViewModel() {
    private val TAG = "WisdomDetailViewModel"

    // State for wisdom detail
    sealed class DetailState {
        object Loading : DetailState()
        data class Success(val wisdom: Wisdom) : DetailState()
        data class Error(val message: String) : DetailState()
    }

    private val _state = MutableStateFlow<DetailState>(DetailState.Loading)
    val state = _state.asStateFlow()

    // Events
    sealed class DetailEvent {
        object Deleted : DetailEvent()
        object Updated : DetailEvent()
        object Activated : DetailEvent()
        data class Error(val message: String) : DetailEvent()
    }

    private val _events = MutableSharedFlow<DetailEvent>()
    val events = _events.asSharedFlow()

    fun loadWisdom(id: Long) {
        viewModelScope.launch {
            _state.value = DetailState.Loading
            try {
                val wisdomResult = wisdomRepository.getWisdomById(id)
                wisdomResult.fold(
                    onSuccess = { wisdom ->
                        if (wisdom != null) {
                            _state.value = DetailState.Success(wisdom)
                        } else {
                            _state.value = DetailState.Error("Wisdom not found")
                            _events.emit(DetailEvent.Error("Wisdom not found"))
                        }
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Error loading wisdom: $id", e)
                        _state.value = DetailState.Error("Failed to load wisdom")
                        _events.emit(DetailEvent.Error("Failed to load wisdom: ${e.localizedMessage}"))
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in loadWisdom: $id", e)
                _state.value = DetailState.Error("Failed to load wisdom")
                _events.emit(DetailEvent.Error("Failed to load wisdom: ${e.localizedMessage}"))
            }
        }
    }

    fun updateWisdom(wisdom: Wisdom) {
        viewModelScope.launch {
            try {
                wisdomRepository.updateWisdom(wisdom)
                _state.value = DetailState.Success(wisdom)
                _events.emit(DetailEvent.Updated)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating wisdom: ${wisdom.id}", e)
                _events.emit(DetailEvent.Error("Failed to update wisdom: ${e.localizedMessage}"))
            }
        }
    }

    fun deleteWisdom(wisdom: Wisdom) {
        viewModelScope.launch {
            try {
                wisdomRepository.deleteWisdom(wisdom)
                _events.emit(DetailEvent.Deleted)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting wisdom: ${wisdom.id}", e)
                _events.emit(DetailEvent.Error("Failed to delete wisdom: ${e.localizedMessage}"))
            }
        }
    }

    fun activateWisdom(wisdomId: Long) {
        viewModelScope.launch {
            try {
                wisdomRepository.activateWisdom(wisdomId)
                // Reload the wisdom to get updated state
                loadWisdom(wisdomId)
                _events.emit(DetailEvent.Activated)
            } catch (e: Exception) {
                Log.e(TAG, "Error activating wisdom: $wisdomId", e)
                _events.emit(DetailEvent.Error("Failed to activate wisdom: ${e.localizedMessage}"))
            }
        }
    }

    fun recordExposure(wisdomId: Long) {
        viewModelScope.launch {
            try {
                wisdomRepository.recordExposure(wisdomId)
                // Reload the wisdom to get updated exposure count
                loadWisdom(wisdomId)
            } catch (e: Exception) {
                Log.e(TAG, "Error recording exposure: $wisdomId", e)
                _events.emit(DetailEvent.Error("Failed to record exposure"))
            }
        }
    }
}