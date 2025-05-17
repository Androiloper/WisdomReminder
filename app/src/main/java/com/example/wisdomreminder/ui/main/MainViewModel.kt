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

    // Enhanced addWisdom with explicit refresh and better logging:
    fun addWisdom(text: String, source: String, category: String) {
        if (text.isBlank()) return

        Log.d(TAG, "Adding wisdom: $text, $source, $category")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val wisdom = Wisdom(
                    text = text,
                    source = source,
                    category = category,
                    dateCreated = LocalDateTime.now(),
                    isActive = false,  // Explicitly set to false to ensure it goes to queued
                    dateCompleted = null  // Explicitly set to null
                )

                val id = wisdomRepository.addWisdom(wisdom)
                Log.d(TAG, "Wisdom added with ID: $id")

                if (id > 0) {
                    _events.emit(UiEvent.WisdomAdded)

                    // IMPORTANT: Force refresh data after adding
                    Log.d(TAG, "Forcing data refresh after adding wisdom")
                    refreshData()

                    // Debug to verify data is properly updated
                    debugDatabaseContents()
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
                Log.d(TAG, "ViewModel: Activating wisdom with ID: $wisdomId")

                // Check active count (existing code)
                val currentActiveCount = (uiState.value as? WisdomUiState.Success)?.activeCount ?: 0
                if (currentActiveCount >= 3) {
                    _events.emit(UiEvent.Error("You can only have up to 3 active wisdom items at once"))
                    return@launch
                }


                // Activate wisdom
                wisdomRepository.activateWisdom(wisdomId)
                _events.emit(UiEvent.WisdomActivated)

                // Force reload with delay to ensure database has completed the transaction
                delay(100) // Small delay to ensure transaction completes
                Log.d(TAG, "Refreshing data after activation")

                // After activation, check directly what's active in the database
                val activeItems = wisdomRepository.getActiveWisdomDirect()
                Log.d(TAG, "After activation, direct query shows ${activeItems.size} active items")
                activeItems.forEach {
                    Log.d(TAG, "Active item: id=${it.id}, text='${it.text}', isActive=${it.isActive}")
                }

                refreshData()

                // Debug database state
                debugDatabaseContents()

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

    // Enhanced refreshData to be more reliable:
    fun refreshData() {
        Log.d(TAG, "Explicitly refreshing data")
        viewModelScope.launch {
            _uiState.value = WisdomUiState.Loading

            try {
                // Get fresh data directly by calling first() on each flow
                Log.d(TAG, "Getting fresh data from repository")
                val activeItems = wisdomRepository.getActiveWisdom().first()
                val queuedItems = wisdomRepository.getQueuedWisdom().first()
                val completedItems = wisdomRepository.getCompletedWisdom().first()
                val activeCount = wisdomRepository.getActiveWisdomCount().first()
                val completedCount = wisdomRepository.getCompletedWisdomCount().first()

                Log.d(TAG, "Refresh complete - Active: ${activeItems.size}, Queued: ${queuedItems.size}, Completed: ${completedItems.size}")

                // Update UI state with the fresh data
                _uiState.value = WisdomUiState.Success(
                    activeWisdom = activeItems,
                    queuedWisdom = queuedItems,
                    completedWisdom = completedItems,
                    activeCount = activeCount,
                    completedCount = completedCount,
                    serviceRunning = WisdomDisplayService.isServiceRunning
                )
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

