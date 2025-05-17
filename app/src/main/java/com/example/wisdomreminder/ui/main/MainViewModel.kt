package com.example.wisdomreminder.ui.main

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.wisdomreminder.data.repository.WisdomRepository
import com.example.wisdomreminder.model.Wisdom
import com.example.wisdomreminder.service.WisdomDisplayService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val wisdomRepository: WisdomRepository
) : ViewModel() {
    private val TAG = "MainViewModel"

    // UI state using sealed class pattern
    sealed class WisdomUiState {
        object Loading : WisdomUiState()
        data class Success(
            val activeWisdom: List<Wisdom> = emptyList(),
            val queuedWisdom: List<Wisdom> = emptyList(),
            val completedWisdom: List<Wisdom> = emptyList(),
            val activeCount: Int = 0,
            val completedCount: Int = 0,
            val serviceRunning: Boolean = false
        ) : WisdomUiState()
        data class Error(val message: String) : WisdomUiState()
    }

    // Private mutable state
    private val _uiState = MutableStateFlow<WisdomUiState>(WisdomUiState.Loading)

    // Public immutable state
    val uiState = _uiState.asStateFlow()

    // Single wisdom for detail view
    private val _selectedWisdom = MutableStateFlow<Wisdom?>(null)
    val selectedWisdom = _selectedWisdom.asStateFlow()

    // Events/actions
    sealed class UiEvent {
        object WisdomAdded : UiEvent()
        object WisdomDeleted : UiEvent()
        object WisdomUpdated : UiEvent()
        object WisdomActivated : UiEvent()
        data class Error(val message: String) : UiEvent()
    }

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    init {
        // Log which repository methods are being called
        Log.d(TAG, "MainViewModel initialized, collecting wisdom data")

        // Collect all wisdom data streams and combine them
        viewModelScope.launch {

            Log.d(TAG, "Starting to collect from repository flows")
            Log.d(TAG, "getActiveWisdom() called")
            Log.d(TAG, "getQueuedWisdom() called")
            Log.d(TAG, "getCompletedWisdom() called")
            Log.d(TAG, "getActiveWisdomCount() called")

            combine(
                wisdomRepository.getActiveWisdom(),
                wisdomRepository.getQueuedWisdom(),
                wisdomRepository.getCompletedWisdom(),
                wisdomRepository.getActiveWisdomCount(),
                wisdomRepository.getCompletedWisdomCount(),
                // Use shareIn to avoid duplicate flow collection
            ) { active, queued, completed, activeCount, completedCount ->
                WisdomUiState.Success(
                    activeWisdom = active,
                    queuedWisdom = queued,
                    completedWisdom = completed,
                    activeCount = activeCount,
                    completedCount = completedCount,
                    serviceRunning = WisdomDisplayService.isServiceRunning
                )
            }.catch { error ->
                Log.e(TAG, "Error collecting wisdom data", error)
                _uiState.value = WisdomUiState.Error(
                    message = error.message ?: "Unknown error occurred"
                )
                _events.emit(UiEvent.Error(error.message ?: "Unknown error occurred"))
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun getWisdomById(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val wisdom = wisdomRepository.getWisdomById(id)
                _selectedWisdom.value = wisdom
            } catch (e: Exception) {
                Log.e(TAG, "Error getting wisdom by ID: $id", e)
                _events.emit(UiEvent.Error("Could not load wisdom details"))
            }
        }
    }

    fun addWisdom(text: String, source: String, category: String) {
        if (text.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val wisdom = Wisdom(
                    text = text,
                    source = source,
                    category = category,
                    dateCreated = LocalDateTime.now()
                )
                val id = wisdomRepository.addWisdom(wisdom)
                if (id > 0) {
                    _events.emit(UiEvent.WisdomAdded)
                } else {
                    _events.emit(UiEvent.Error("Failed to add wisdom"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding wisdom", e)
                _events.emit(UiEvent.Error("Failed to add wisdom: ${e.localizedMessage}"))
            }
        }
    }

    fun updateWisdom(wisdom: Wisdom) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                wisdomRepository.updateWisdom(wisdom)
                _events.emit(UiEvent.WisdomUpdated)

                // Update selected wisdom if it's the one being edited
                if (_selectedWisdom.value?.id == wisdom.id) {
                    _selectedWisdom.value = wisdom
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating wisdom: ${wisdom.id}", e)
                _events.emit(UiEvent.Error("Failed to update wisdom: ${e.localizedMessage}"))
            }
        }
    }

    fun deleteWisdom(wisdom: Wisdom) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                wisdomRepository.deleteWisdom(wisdom)
                _events.emit(UiEvent.WisdomDeleted)

                // Clear selected wisdom if it's the one being deleted
                if (_selectedWisdom.value?.id == wisdom.id) {
                    _selectedWisdom.value = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting wisdom: ${wisdom.id}", e)
                _events.emit(UiEvent.Error("Failed to delete wisdom: ${e.localizedMessage}"))
            }
        }
    }

    fun activateWisdom(wisdomId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Check if we already have too many active wisdom items
                val currentActiveCount = (uiState.value as? WisdomUiState.Success)?.activeCount ?: 0
                if (currentActiveCount >= 3) {
                    _events.emit(UiEvent.Error("You can only have up to 3 active wisdom items at once"))
                    return@launch
                }

                wisdomRepository.activateWisdom(wisdomId)
                _events.emit(UiEvent.WisdomActivated)

                // Update selected wisdom if it's the one being activated
                if (_selectedWisdom.value?.id == wisdomId) {
                    getWisdomById(wisdomId) // Refresh the data
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error activating wisdom: $wisdomId", e)
                _events.emit(UiEvent.Error("Failed to activate wisdom: ${e.localizedMessage}"))
            }
        }
    }

    // Service management with error handling
    fun checkAndRestartService(context: Context) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                if (!WisdomDisplayService.isServiceRunning) {
                    startWisdomService(context)
                }

                // Update the UI state to reflect service status
                _uiState.update {
                    if (it is WisdomUiState.Success) {
                        it.copy(serviceRunning = WisdomDisplayService.isServiceRunning)
                    } else {
                        it
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking service status", e)
                _events.emit(UiEvent.Error("Failed to check service status"))
            }
        }
    }

    private fun startWisdomService(context: Context) {
        try {
            val serviceIntent = Intent(context, WisdomDisplayService::class.java).apply {
                action = WisdomDisplayService.ACTION_START_SERVICE
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

            // Update UI state after a delay to allow service to start
            viewModelScope.launch {
                delay(500) // Short delay
                _uiState.update {
                    if (it is WisdomUiState.Success) {
                        it.copy(serviceRunning = WisdomDisplayService.isServiceRunning)
                    } else {
                        it
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start service", e)
            viewModelScope.launch {
                _events.emit(UiEvent.Error("Failed to start wisdom service"))
            }
        }
    }

    fun stopWisdomService(context: Context) {
        try {
            val serviceIntent = Intent(context, WisdomDisplayService::class.java).apply {
                action = WisdomDisplayService.ACTION_STOP_SERVICE
            }
            context.startService(serviceIntent)

            // Update UI state after a delay to allow service to stop
            viewModelScope.launch {
                delay(500) // Short delay
                _uiState.update {
                    if (it is WisdomUiState.Success) {
                        it.copy(serviceRunning = WisdomDisplayService.isServiceRunning)
                    } else {
                        it
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop service", e)
            viewModelScope.launch {
                _events.emit(UiEvent.Error("Failed to stop wisdom service"))
            }
        }
    }

    // Helper function for sample data with proper error handling
    fun addSampleWisdom() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sampleWisdom = listOf(
                    Wisdom(
                        text = "What you stay focused on will grow",
                        source = "Law of Attraction",
                        category = "Personal Development"
                    ),
                    Wisdom(
                        text = "We are what we repeatedly do. Excellence, then, is not an act, but a habit.",
                        source = "Aristotle",
                        category = "Philosophy"
                    ),
                    // Other sample wisdom items...
                )

                // Use a transaction if possible
                sampleWisdom.forEach { wisdom ->
                    wisdomRepository.addWisdom(wisdom)
                }

                _events.emit(UiEvent.WisdomAdded)
            } catch (e: Exception) {
                Log.e(TAG, "Error adding sample wisdom", e)
                _events.emit(UiEvent.Error("Failed to add sample wisdom"))
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.value = WisdomUiState.Loading

            try {
                // Re-init the data collection
                // This will be collected by the existing Flow collectors
                // and _uiState will be updated when new data is available

                // If you need to explicitly force a refresh:
                combine(
                    wisdomRepository.getActiveWisdom(),
                    wisdomRepository.getQueuedWisdom(),
                    wisdomRepository.getCompletedWisdom(),
                    wisdomRepository.getActiveWisdomCount(),
                    wisdomRepository.getCompletedWisdomCount()
                ) { active, queued, completed, activeCount, completedCount ->
                    WisdomUiState.Success(
                        activeWisdom = active,
                        queuedWisdom = queued,
                        completedWisdom = completed,
                        activeCount = activeCount,
                        completedCount = completedCount,
                        serviceRunning = WisdomDisplayService.isServiceRunning
                    )
                }.first() // Use first() to get just one emission
                    .let { state ->
                        _uiState.value = state
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing data", e)
                _uiState.value = WisdomUiState.Error("Failed to refresh data: ${e.localizedMessage}")
            }
        }
    }

    // Add this to MainViewModel
    fun debugDatabaseContents() {
        viewModelScope.launch {
            try {
                // Check all wisdom in the database
                val allWisdom = wisdomRepository.getAllWisdom().first()
                Log.d(TAG, "DEBUG - All wisdom: ${allWisdom.size} items")
                allWisdom.forEach { wisdom ->
                    Log.d(TAG, "Wisdom: ${wisdom.id} - '${wisdom.text}' - Active: ${wisdom.isActive}")
                }

                // Check queued wisdom
                val queued = wisdomRepository.getQueuedWisdom().first()
                Log.d(TAG, "DEBUG - Queued wisdom: ${queued.size} items")

                // Check active wisdom
                val active = wisdomRepository.getActiveWisdom().first()
                Log.d(TAG, "DEBUG - Active wisdom: ${active.size} items")

                // Check completed wisdom
                val completed = wisdomRepository.getCompletedWisdom().first()
                Log.d(TAG, "DEBUG - Completed wisdom: ${completed.size} items")

            } catch (e: Exception) {
                Log.e(TAG, "Error debugging database", e)
            }
        }
    }
}