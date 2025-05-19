package com.example.wisdomreminder.ui.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wisdomreminder.data.repository.IWisdomRepository
import com.example.wisdomreminder.model.Wisdom
import com.example.wisdomreminder.service.WisdomDisplayService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

@HiltViewModel
class MainViewModel @Inject constructor(
    private val wisdomRepository: IWisdomRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val TAG = "MainViewModel"

    // Shared preferences for storing selected categories
    private val prefs: SharedPreferences = context.getSharedPreferences("wisdom_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // UI state using sealed class pattern
    sealed class WisdomUiState {
        object Loading : WisdomUiState()
        data class Success(
            val activeWisdom: List<Wisdom> = emptyList(),
            val queuedWisdom: List<Wisdom> = emptyList(),
            val completedWisdom: List<Wisdom> = emptyList(),
            val activeCount: Int = 0,
            val completedCount: Int = 0,
            val serviceRunning: Boolean = false,
            val allCategories: List<String> = emptyList(),
            val selectedCategories: List<String> = emptyList(),
            val categoryWisdom: Map<String, List<Wisdom>> = emptyMap()
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

    // Selected categories
    private val _selectedCategories = MutableStateFlow<List<String>>(emptyList())

    // Events/actions
    sealed class UiEvent {
        object WisdomAdded : UiEvent()
        object WisdomDeleted : UiEvent()
        object WisdomUpdated : UiEvent()
        object WisdomActivated : UiEvent()
        object CategoryAdded : UiEvent()
        object CategoryRemoved : UiEvent()
        data class Error(val message: String) : UiEvent()
    }

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    companion object {
        private const val SELECTED_CATEGORIES_KEY = "selected_categories"
    }


    // Data class for combined flow results - add this inside the MainViewModel class
    private data class CombinedFlowData(
        val active: List<Wisdom>,
        val queued: List<Wisdom>,
        val completed: List<Wisdom>,
        val activeCount: Int,
        val completedCount: Int,
        val allCategories: List<String>
    )

    init {
        Log.d(TAG, "MainViewModel initialized, triggering explicit data load")
        viewModelScope.launch {
            _uiState.value = WisdomUiState.Loading
            loadSelectedCategories()
            refreshData()
        }

        initializeDataCollection()
    }

    private fun loadSelectedCategories() {
        val categoriesJson = prefs.getString(SELECTED_CATEGORIES_KEY, null)
        if (categoriesJson != null) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                val categories: List<String> = gson.fromJson(categoriesJson, type)
                _selectedCategories.value = categories
                Log.d(TAG, "Loaded ${categories.size} selected categories from preferences")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading selected categories", e)
                _selectedCategories.value = emptyList()
            }
        }
    }

    private fun saveSelectedCategories(categories: List<String>) {
        try {
            val categoriesJson = gson.toJson(categories)
            prefs.edit().putString(SELECTED_CATEGORIES_KEY, categoriesJson).apply()
            Log.d(TAG, "Saved ${categories.size} selected categories to preferences")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving selected categories", e)
        }
    }

    private fun initializeDataCollection() {
        viewModelScope.launch {
            Log.d(TAG, "Starting to collect from repository flows")

            try {
                // Use the explicit Flow.combine() extension function with all flows as a single parameter
                combine(
                    flowOf(wisdomRepository.getActiveWisdom()),
                    flowOf(wisdomRepository.getQueuedWisdom()),
                    flowOf(wisdomRepository.getCompletedWisdom()),
                    flowOf(wisdomRepository.getActiveWisdomCount()),
                    flowOf(wisdomRepository.getCompletedWisdomCount()),
                    flowOf(wisdomRepository.getAllCategories())
                ) { flows ->
                    // Extract data from the flows array
                    val activeFlow = flows[0] as Flow<List<Wisdom>>
                    val queuedFlow = flows[1] as Flow<List<Wisdom>>
                    val completedFlow = flows[2] as Flow<List<Wisdom>>
                    val activeCountFlow = flows[3] as Flow<Int>
                    val completedCountFlow = flows[4] as Flow<Int>
                    val allCategoriesFlow = flows[5] as Flow<List<String>>

                    // Collect all the data
                    val active = activeFlow.first()
                    val queued = queuedFlow.first()
                    val completed = completedFlow.first()
                    val activeCount = activeCountFlow.first()
                    val completedCount = completedCountFlow.first()
                    val allCategories = allCategoriesFlow.first()

                    // Return a tuple-like data structure
                    Triple(
                        Triple(active, queued, completed),
                        Pair(activeCount, completedCount),
                        allCategories
                    )
                }.collect { result ->
                    // Destructure the complex result
                    val (wisdomTriple, countPair, allCategories) = result
                    val (active, queued, completed) = wisdomTriple
                    val (activeCount, completedCount) = countPair

                    val selectedCats = _selectedCategories.value

                    // Build category wisdom map
                    val categoryWisdomMap = mutableMapOf<String, List<Wisdom>>()

                    // Collect wisdom by each selected category
                    for (category in selectedCats) {
                        try {
                            val wisdomByCategory = wisdomRepository.getWisdomByCategory(category).first()
                            categoryWisdomMap[category] = wisdomByCategory
                        } catch (e: Exception) {
                            Log.e(TAG, "Error getting wisdom for category: $category", e)
                            categoryWisdomMap[category] = emptyList()
                        }
                    }

                    // Construct the final state
                    _uiState.value = WisdomUiState.Success(
                        activeWisdom = active,
                        queuedWisdom = queued,
                        completedWisdom = completed,
                        activeCount = activeCount,
                        completedCount = completedCount,
                        serviceRunning = WisdomDisplayService.isServiceRunning,
                        allCategories = allCategories,
                        selectedCategories = selectedCats,
                        categoryWisdom = categoryWisdomMap
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in data collection", e)
                _uiState.value = WisdomUiState.Error("Failed to load data: ${e.localizedMessage}")
            }
        }
    }

    // Add a category to display on the main screen
    fun addCategory(category: String) {
        viewModelScope.launch {
            try {
                val currentCategories = _selectedCategories.value.toMutableList()
                if (category !in currentCategories) {
                    currentCategories.add(category)
                    _selectedCategories.value = currentCategories
                    saveSelectedCategories(currentCategories)
                    _events.emit(UiEvent.CategoryAdded)
                    Log.d(TAG, "Added category: $category")

                    // Force refresh after category change
                    refreshData()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding category", e)
                _events.emit(UiEvent.Error("Failed to add category: ${e.localizedMessage}"))
            }
        }
    }

    // Remove a category from display
    fun removeCategory(category: String) {
        viewModelScope.launch {
            try {
                val currentCategories = _selectedCategories.value.toMutableList()
                if (currentCategories.remove(category)) {
                    _selectedCategories.value = currentCategories
                    saveSelectedCategories(currentCategories)
                    _events.emit(UiEvent.CategoryRemoved)
                    Log.d(TAG, "Removed category: $category")

                    // Force refresh after category change
                    refreshData()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error removing category", e)
                _events.emit(UiEvent.Error("Failed to remove category: ${e.localizedMessage}"))
            }
        }
    }

    // Get wisdom by ID
    fun getWisdomById(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = wisdomRepository.getWisdomById(id)

                result.fold(
                    onSuccess = { wisdom ->
                        _selectedWisdom.value = wisdom
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error getting wisdom by ID: $id", error)
                        _events.emit(UiEvent.Error("Could not load wisdom details"))
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception in getWisdomById", e)
                _events.emit(UiEvent.Error("An unexpected error occurred"))
            }
        }
    }

    // Add new wisdom
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
                    isActive = false,
                    dateCompleted = null
                )

                val result = wisdomRepository.addWisdom(wisdom)

                result.fold(
                    onSuccess = { id ->
                        if (id > 0) {
                            _events.emit(UiEvent.WisdomAdded)
                            Log.d(TAG, "Wisdom added successfully with ID: $id")
                            refreshData()
                            debugDatabaseContents()
                        } else {
                            _events.emit(UiEvent.Error("Failed to add wisdom (invalid ID)"))
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error adding wisdom", error)
                        _events.emit(UiEvent.Error("Failed to add wisdom: ${error.localizedMessage}"))
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception in addWisdom", e)
                _events.emit(UiEvent.Error("An unexpected error occurred while adding wisdom"))
            }
        }
    }

    // Update wisdom
    fun updateWisdom(wisdom: Wisdom) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = wisdomRepository.updateWisdom(wisdom)

                result.fold(
                    onSuccess = { success ->
                        _events.emit(UiEvent.WisdomUpdated)

                        // Update selected wisdom if it's the one being edited
                        if (_selectedWisdom.value?.id == wisdom.id) {
                            _selectedWisdom.value = wisdom
                        }

                        refreshData()
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error updating wisdom: ${wisdom.id}", error)
                        _events.emit(UiEvent.Error("Failed to update wisdom: ${error.localizedMessage}"))
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception in updateWisdom", e)
                _events.emit(UiEvent.Error("An unexpected error occurred while updating wisdom"))
            }
        }
    }

    // Delete wisdom
    fun deleteWisdom(wisdom: Wisdom) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = wisdomRepository.deleteWisdom(wisdom)

                result.fold(
                    onSuccess = { success ->
                        _events.emit(UiEvent.WisdomDeleted)

                        // Clear selected wisdom if it's the one being deleted
                        if (_selectedWisdom.value?.id == wisdom.id) {
                            _selectedWisdom.value = null
                        }

                        refreshData()
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error deleting wisdom: ${wisdom.id}", error)
                        _events.emit(UiEvent.Error("Failed to delete wisdom: ${error.localizedMessage}"))
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception in deleteWisdom", e)
                _events.emit(UiEvent.Error("An unexpected error occurred while deleting wisdom"))
            }
        }
    }

    // Activate wisdom
    fun activateWisdom(wisdomId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "ViewModel: Activating wisdom with ID: $wisdomId")

                // Check active count
                val currentActiveCount = (uiState.value as? WisdomUiState.Success)?.activeCount ?: 0
                if (currentActiveCount >= 3) {
                    _events.emit(UiEvent.Error("You can only have up to 3 active wisdom items at once"))
                    return@launch
                }

                // Activate wisdom using Result
                val result = wisdomRepository.activateWisdom(wisdomId)

                result.fold(
                    onSuccess = { success ->
                        _events.emit(UiEvent.WisdomActivated)

                        // Force refresh data with delay
                        delay(300) // Small delay to ensure transaction completes
                        Log.d(TAG, "Refreshing data after activation")
                        refreshData()

                        // Verify active wisdom status
                        val activeResult = wisdomRepository.getActiveWisdomDirect()
                        activeResult.onSuccess { activeItems ->
                            Log.d(TAG, "Direct verification: ${activeItems.size} active items")
                            activeItems.forEach {
                                Log.d(TAG, "Active item: id=${it.id}, text='${it.text.take(20)}...'")
                            }
                        }

                        // Update selected wisdom if it's the one being activated
                        if (_selectedWisdom.value?.id == wisdomId) {
                            getWisdomById(wisdomId)
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error activating wisdom: $wisdomId", error)
                        _events.emit(UiEvent.Error("Failed to activate wisdom: ${error.localizedMessage}"))
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception in activateWisdom", e)
                _events.emit(UiEvent.Error("An unexpected error occurred while activating wisdom"))
            }
        }
    }

    // Service management
    fun checkAndRestartService(context: Context) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                if (!WisdomDisplayService.isServiceRunning) {
                    startWisdomService(context)
                }

                // Update the UI state to reflect service status
                _uiState.value = (_uiState.value as? WisdomUiState.Success)?.copy(
                    serviceRunning = WisdomDisplayService.isServiceRunning
                ) ?: _uiState.value
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

            // Update UI state after a delay
            viewModelScope.launch {
                delay(500)
                _uiState.value = (_uiState.value as? WisdomUiState.Success)?.copy(
                    serviceRunning = WisdomDisplayService.isServiceRunning
                ) ?: _uiState.value
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

            // Update UI state after a delay
            viewModelScope.launch {
                delay(500)
                _uiState.value = (_uiState.value as? WisdomUiState.Success)?.copy(
                    serviceRunning = WisdomDisplayService.isServiceRunning
                ) ?: _uiState.value
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop service", e)
            viewModelScope.launch {
                _events.emit(UiEvent.Error("Failed to stop wisdom service"))
            }
        }
    }

    // Sample data method
    fun addSampleWisdom() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var successCount = 0
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
                    Wisdom(
                        text = "The quality of your life is determined by the quality of your questions.",
                        source = "Tony Robbins",
                        category = "Personal Development"
                    ),
                    Wisdom(
                        text = "Let no corrupt words proceed out of your mouth",
                        source = "Ephesians 4:29",
                        category = "General"
                    )
                )

                // Add each wisdom item
                for (wisdom in sampleWisdom) {
                    val result = wisdomRepository.addWisdom(wisdom)
                    result.onSuccess { successCount++ }
                }

                if (successCount > 0) {
                    _events.emit(UiEvent.WisdomAdded)
                    refreshData()
                } else {
                    _events.emit(UiEvent.Error("Failed to add sample wisdom"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding sample wisdom", e)
                _events.emit(UiEvent.Error("Failed to add sample wisdom"))
            }
        }
    }

    // Data refresh method
    fun refreshData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting data refresh")

                // Only show loading state if we don't already have data
                val currentState = _uiState.value
                if (currentState !is WisdomUiState.Success) {
                    _uiState.emit(WisdomUiState.Loading)
                }

                // Get fresh data with timeouts to prevent hangs
                withTimeout(5000) {
                    val activeItems = wisdomRepository.getActiveWisdom().first()
                    val queuedItems = wisdomRepository.getQueuedWisdom().first()
                    val completedItems = wisdomRepository.getCompletedWisdom().first()
                    val activeCount = wisdomRepository.getActiveWisdomCount().first()
                    val completedCount = wisdomRepository.getCompletedWisdomCount().first()
                    val allCategories = wisdomRepository.getAllCategories().first()
                    val selectedCats = _selectedCategories.value

                    // Get wisdom for each selected category
                    val categoryWisdomMap = mutableMapOf<String, List<Wisdom>>()
                    selectedCats.forEach { category ->
                        try {
                            val wisdomByCategory = wisdomRepository.getWisdomByCategory(category).first()
                            categoryWisdomMap[category] = wisdomByCategory
                        } catch (e: Exception) {
                            Log.e(TAG, "Error getting wisdom for category: $category", e)
                            categoryWisdomMap[category] = emptyList()
                        }
                    }

                    Log.d(TAG, "Refresh complete - Active: ${activeItems.size}, Queued: ${queuedItems.size}")

                    // Update UI state
                    _uiState.emit(WisdomUiState.Success(
                        activeWisdom = activeItems,
                        queuedWisdom = queuedItems,
                        completedWisdom = completedItems,
                        activeCount = activeCount,
                        completedCount = completedCount,
                        serviceRunning = WisdomDisplayService.isServiceRunning,
                        allCategories = allCategories,
                        selectedCategories = selectedCats,
                        categoryWisdom = categoryWisdomMap
                    ))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing data", e)
                _uiState.emit(WisdomUiState.Error("Failed to refresh data: ${e.localizedMessage}"))
            }
        }
    }

    // Debug method
    fun debugDatabaseContents() {
        viewModelScope.launch {
            try {
                // Check all wisdom in the database
                val allWisdom = wisdomRepository.getAllWisdom().first()
                Log.d(TAG, "DEBUG - All wisdom: ${allWisdom.size} items")
                allWisdom.forEach { wisdom ->
                    Log.d(TAG, "Wisdom: ${wisdom.id} - '${wisdom.text.take(20)}...' - Active: ${wisdom.isActive}")
                }

                // Check active wisdom directly
                val activeResult = wisdomRepository.getActiveWisdomDirect()
                activeResult.onSuccess { activeItems ->
                    Log.d(TAG, "DEBUG - Active wisdom direct: ${activeItems.size} items")
                    activeItems.forEach {
                        Log.d(TAG, "Active: ${it.id} - '${it.text.take(20)}...'")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error debugging database", e)
            }
        }
    }
}