package com.example.wisdomreminder.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val wisdomRepository: IWisdomRepository,
    @ApplicationContext applicationContext: Context
) : ViewModel() {
    private val TAG = "MainViewModel"

    // Use Application Context for SharedPreferences to avoid memory leaks
    private val prefs: SharedPreferences = applicationContext.getSharedPreferences("wisdom_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Service status broadcast receiver
    private val serviceStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_SERVICE_STATUS_CHANGE) {
                val isRunning = intent.getBooleanExtra(EXTRA_IS_RUNNING, false)
                Log.d(TAG, "Service status broadcast received: running=$isRunning")
                updateServiceRunningState(isRunning)
            }
        }
    }

    // Register the receiver when the ViewModel is initialized
    init {
        applicationContext.registerReceiver(
            serviceStatusReceiver,
            IntentFilter(ACTION_SERVICE_STATUS_CHANGE),
            Context.RECEIVER_NOT_EXPORTED
        )
    }

    companion object {
        private const val SELECTED_CATEGORIES_FOR_CARDS_KEY = "selected_categories_for_cards"
        const val ACTION_SERVICE_STATUS_CHANGE = "com.example.wisdomreminder.ACTION_SERVICE_STATUS_CHANGE"
        const val EXTRA_IS_RUNNING = "extra_is_running"
    }

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
            val selectedCategoriesForCards: List<String> = emptyList(),
            val categoryWisdomMap: Map<String, List<Wisdom>> = emptyMap()
        ) : WisdomUiState()
        data class Error(val message: String) : WisdomUiState()
    }

    sealed class UiEvent {
        object WisdomAdded : UiEvent()
        object WisdomDeleted : UiEvent()
        object WisdomUpdated : UiEvent()
        object WisdomActivated : UiEvent()
        object CategoryCardAdded : UiEvent()
        object CategoryCardRemoved : UiEvent()
        data class Error(val message: String) : UiEvent()
    }

    private val _uiState = MutableStateFlow<WisdomUiState>(WisdomUiState.Loading)
    val uiState: StateFlow<WisdomUiState> = _uiState.asStateFlow()

    private val _selectedWisdom = MutableStateFlow<Wisdom?>(null)
    val selectedWisdom: StateFlow<Wisdom?> = _selectedWisdom.asStateFlow()

    private val _selectedCategoriesForCards = MutableStateFlow<List<String>>(emptyList())

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    private var dataCollectionJob: Job? = null
    private var categoryDataJob: Job? = null

    init {
        Log.d(TAG, "MainViewModel initialized")
        loadSelectedCategoriesForCards()
        initializeDataCollection()
    }

    override fun onCleared() {
        Log.d(TAG, "MainViewModel onCleared")
        dataCollectionJob?.cancel()
        categoryDataJob?.cancel()
        super.onCleared()
    }

    private fun loadSelectedCategoriesForCards() {
        viewModelScope.launch {
            val categoriesJson = prefs.getString(SELECTED_CATEGORIES_FOR_CARDS_KEY, null)
            if (categoriesJson != null) {
                try {
                    val type = object : TypeToken<List<String>>() {}.type
                    val categories: List<String> = gson.fromJson(categoriesJson, type)
                    _selectedCategoriesForCards.value = categories
                    Log.d(TAG, "Loaded ${categories.size} selected categories for cards: $categories")
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading selected categories for cards", e)
                    _selectedCategoriesForCards.value = emptyList()
                }
            } else {
                Log.d(TAG, "No selected categories for cards found in preferences.")
                _selectedCategoriesForCards.value = emptyList()
            }
        }
    }

    private fun saveSelectedCategoriesForCards(categories: List<String>) {
        viewModelScope.launch {
            try {
                val categoriesJson = gson.toJson(categories)
                prefs.edit().putString(SELECTED_CATEGORIES_FOR_CARDS_KEY, categoriesJson).apply()
                Log.d(TAG, "Saved ${categories.size} selected categories for cards: $categories")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving selected categories for cards", e)
            }
        }
    }

    private fun initializeDataCollection() {
        dataCollectionJob?.cancel()
        categoryDataJob?.cancel()

        Log.d(TAG, "Initializing data collection.")
        if (_uiState.value !is WisdomUiState.Success || (_uiState.value as? WisdomUiState.Success)?.activeWisdom?.isEmpty() == true) {
            _uiState.value = WisdomUiState.Loading
        }

        // Main data collection job
        dataCollectionJob = viewModelScope.launch {
            // Create a list of flows
            val flowList = listOf(
                wisdomRepository.getActiveWisdom(),
                wisdomRepository.getQueuedWisdom(),
                wisdomRepository.getCompletedWisdom(),
                wisdomRepository.getActiveWisdomCount(),
                wisdomRepository.getCompletedWisdomCount(),
                wisdomRepository.getAllCategories(),
                _selectedCategoriesForCards
            )

            // Use the combine method that takes an iterable of flows and returns an array of results
            combine(flowList) { values: Array<Any> ->
                // Cast each value to its expected type
                val active = values[0] as List<Wisdom>
                val queued = values[1] as List<Wisdom>
                val completed = values[2] as List<Wisdom>
                val activeCount = values[3] as Int
                val completedCount = values[4] as Int
                val allCategories = values[5] as List<String>
                val selectedCategories = values[6] as List<String>

                Log.d(TAG, "Combine transform block. Active: ${active.size}, Queued: ${queued.size}, SelectedCardCats: ${selectedCategories.size}")

                // Get initial state without category map
                WisdomUiState.Success(
                    activeWisdom = active,
                    queuedWisdom = queued,
                    completedWisdom = completed,
                    activeCount = activeCount,
                    completedCount = completedCount,
                    serviceRunning = WisdomDisplayService.isServiceRunning,
                    allCategories = allCategories,
                    selectedCategoriesForCards = selectedCategories,
                    categoryWisdomMap = emptyMap()
                )
            }.catch { e ->
                Log.e(TAG, "Exception in data collection combine block", e)
                _uiState.value = WisdomUiState.Error("Failed to load data: ${e.localizedMessage ?: "Unknown error"}")
            }.collectLatest { successState ->
                _uiState.value = successState
                Log.d(TAG, "UI State updated to Success. Active: ${successState.activeWisdom.size}, Service: ${successState.serviceRunning}")

                // Trigger category data collection
                updateCategoryData()
            }
        }
    }

// Add the right import for the right version of combine


    // Helper class to hold main data
    private data class MainData(
        val active: List<Wisdom>,
        val queued: List<Wisdom>,
        val completed: List<Wisdom>,
        val activeCount: Int,
        val completedCount: Int,
        val allCategories: List<String>
    )

    // Separate job for category data to avoid slowing down main UI updates
    private fun updateCategoryData() {
        categoryDataJob?.cancel()
        categoryDataJob = viewModelScope.launch {
            try {
                val selectedCategories = _selectedCategoriesForCards.value
                if (selectedCategories.isEmpty()) {
                    return@launch
                }

                Log.d(TAG, "Updating category data for: $selectedCategories")
                val categoryWisdomMap = mutableMapOf<String, List<Wisdom>>()

                // Process each category
                for (category in selectedCategories) {
                    try {
                        val wisdomForCategory = wisdomRepository.getWisdomByCategory(category).first()
                        categoryWisdomMap[category] = wisdomForCategory
                        Log.d(TAG, "Fetched ${wisdomForCategory.size} items for category '$category'")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching wisdom for category '$category'", e)
                        categoryWisdomMap[category] = emptyList()
                    }
                }

                // Update the UI state with the new category map
                (_uiState.value as? WisdomUiState.Success)?.let { currentState ->
                    _uiState.value = currentState.copy(categoryWisdomMap = categoryWisdomMap)
                    Log.d(TAG, "Updated UI state with category map containing ${categoryWisdomMap.size} categories")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating category data", e)
            }
        }
    }

    fun addCategoryCard(category: String) {
        viewModelScope.launch {
            val currentCategories = _selectedCategoriesForCards.value.toMutableList()
            if (!currentCategories.contains(category)) {
                currentCategories.add(category)
                _selectedCategoriesForCards.value = currentCategories.toList()
                saveSelectedCategoriesForCards(_selectedCategoriesForCards.value)
                _events.emit(UiEvent.CategoryCardAdded)
            }
        }
    }

    fun removeCategoryCard(category: String) {
        viewModelScope.launch {
            val currentCategories = _selectedCategoriesForCards.value.toMutableList()
            if (currentCategories.remove(category)) {
                _selectedCategoriesForCards.value = currentCategories.toList()
                saveSelectedCategoriesForCards(_selectedCategoriesForCards.value)
                _events.emit(UiEvent.CategoryCardRemoved)

                // Update UI state to remove this category from the map
                (_uiState.value as? WisdomUiState.Success)?.let { currentState ->
                    val updatedMap = currentState.categoryWisdomMap.toMutableMap()
                    updatedMap.remove(category)
                    _uiState.value = currentState.copy(categoryWisdomMap = updatedMap)
                }
            }
        }
    }

    fun getWisdomById(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = wisdomRepository.getWisdomById(id)
                result.fold(
                    onSuccess = { wisdom -> _selectedWisdom.value = wisdom },
                    onFailure = { error ->
                        Log.e(TAG, "Error getting wisdom by ID: $id", error)
                        _events.emit(UiEvent.Error("Could not load wisdom details: ${error.message}"))
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception in getWisdomById", e)
                _events.emit(UiEvent.Error("An unexpected error occurred: ${e.message}"))
            }
        }
    }

    fun addWisdom(text: String, source: String, category: String) {
        if (text.isBlank()) {
            viewModelScope.launch { _events.emit(UiEvent.Error("Wisdom text cannot be empty")) }
            return
        }
        Log.d(TAG, "Attempting to add wisdom: Text='${text.take(20)}...', Source='$source', Category='$category'")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val wisdom = Wisdom(
                    text = text,
                    source = source,
                    category = category.ifBlank { "General" },
                    dateCreated = LocalDateTime.now()
                )
                val result = wisdomRepository.addWisdom(wisdom)
                result.fold(
                    onSuccess = { id ->
                        Log.d(TAG, "Wisdom added with ID: $id")
                        _events.emit(UiEvent.WisdomAdded)
                        // Trigger category data update if this wisdom belongs to a selected category
                        if (_selectedCategoriesForCards.value.contains(wisdom.category)) {
                            updateCategoryData()
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error adding wisdom", error)
                        _events.emit(UiEvent.Error("Failed to add wisdom: ${error.localizedMessage ?: "Unknown error"}"))
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception in addWisdom", e)
                _events.emit(UiEvent.Error("An unexpected error occurred while adding wisdom: ${e.localizedMessage ?: "Unknown error"}"))
            }
        }
    }

    fun updateWisdom(wisdom: Wisdom) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                wisdomRepository.updateWisdom(wisdom).fold(
                    onSuccess = {
                        _events.emit(UiEvent.WisdomUpdated)
                        if (_selectedWisdom.value?.id == wisdom.id) {
                            _selectedWisdom.value = wisdom
                        }
                        // Update category data if needed
                        if (_selectedCategoriesForCards.value.contains(wisdom.category)) {
                            updateCategoryData()
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error updating wisdom: ${wisdom.id}", error)
                        _events.emit(UiEvent.Error("Failed to update wisdom: ${error.localizedMessage}"))
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception in updateWisdom", e)
                _events.emit(UiEvent.Error("An unexpected error occurred: ${e.localizedMessage}"))
            }
        }
    }

    fun deleteWisdom(wisdom: Wisdom) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                wisdomRepository.deleteWisdom(wisdom).fold(
                    onSuccess = {
                        _events.emit(UiEvent.WisdomDeleted)
                        if (_selectedWisdom.value?.id == wisdom.id) {
                            _selectedWisdom.value = null
                        }
                        // Update category data if needed
                        if (_selectedCategoriesForCards.value.contains(wisdom.category)) {
                            updateCategoryData()
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error deleting wisdom: ${wisdom.id}", error)
                        _events.emit(UiEvent.Error("Failed to delete wisdom: ${error.localizedMessage}"))
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception in deleteWisdom", e)
                _events.emit(UiEvent.Error("An unexpected error occurred: ${e.localizedMessage}"))
            }
        }
    }

    fun activateWisdom(wisdomId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUiStateValue = _uiState.value
            if (currentUiStateValue is WisdomUiState.Success && currentUiStateValue.activeCount >= 3) {
                _events.emit(UiEvent.Error("You can only have up to 3 active wisdom items at once"))
                return@launch
            }

            Log.d(TAG, "ViewModel: Activating wisdom with ID: $wisdomId")
            try {
                wisdomRepository.activateWisdom(wisdomId).fold(
                    onSuccess = {
                        _events.emit(UiEvent.WisdomActivated)
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
                _events.emit(UiEvent.Error("An unexpected error occurred: ${e.localizedMessage}"))
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            Log.d(TAG, "Manual refresh triggered. Re-initializing data collection.")
            initializeDataCollection()
        }
    }

    private fun updateServiceRunningState(isRunning: Boolean) {
        viewModelScope.launch(Dispatchers.Main) {
            Log.d(TAG, "Updating service running state to: $isRunning")
            (_uiState.value as? WisdomUiState.Success)?.let { currentState ->
                if (currentState.serviceRunning != isRunning) {
                    _uiState.value = currentState.copy(serviceRunning = isRunning)
                }
            }
        }
    }

    fun checkAndRestartService(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isCurrentlyRunning = WisdomDisplayService.isServiceRunning
                Log.d(TAG, "Checking service. Currently running: $isCurrentlyRunning")

                if (!isCurrentlyRunning) {
                    Log.d(TAG, "Service not running, attempting to start.")
                    startWisdomService(context)
                } else {
                    // Update UI to match actual service state
                    updateServiceRunningState(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking/restarting service status", e)
                _events.emit(UiEvent.Error("Failed to check/restart service status"))
            }
        }
    }

    fun startWisdomService(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting WisdomDisplayService")
                val serviceIntent = Intent(context, WisdomDisplayService::class.java).apply {
                    action = WisdomDisplayService.ACTION_START_SERVICE
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }

                Log.d(TAG, "WisdomDisplayService start requested")

                // Service will broadcast its status when started
                // We'll update UI state when we receive that broadcast
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start service", e)
                updateServiceRunningState(false)
                _events.emit(UiEvent.Error("Failed to start wisdom service: ${e.localizedMessage}"))
            }
        }
    }

    fun stopWisdomService(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Stopping WisdomDisplayService")
                val serviceIntent = Intent(context, WisdomDisplayService::class.java).apply {
                    action = WisdomDisplayService.ACTION_STOP_SERVICE
                }
                context.startService(serviceIntent)
                Log.d(TAG, "WisdomDisplayService stop requested")

                // Service will broadcast its status when stopped
                // We'll update UI state when we receive that broadcast
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop service", e)
                _events.emit(UiEvent.Error("Failed to stop wisdom service: ${e.localizedMessage}"))
            }
        }
    }

    fun addSampleWisdom() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sampleWisdom = listOf(
                    Wisdom(text = "What you stay focused on will grow", source = "Law of Attraction", category = "Personal Development"),
                    Wisdom(text = "We are what we repeatedly do. Excellence, then, is not an act, but a habit.", source = "Aristotle", category = "Philosophy"),
                    Wisdom(text = "The quality of your life is determined by the quality of your questions.", source = "Tony Robbins", category = "Personal Development"),
                    Wisdom(text = "Let no corrupt words proceed out of your mouth", source = "Ephesians 4:29", category = "General")
                )
                var successCount = 0
                for (wisdom in sampleWisdom) {
                    wisdomRepository.addWisdom(wisdom).onSuccess { successCount++ }
                }
                if (successCount > 0) {
                    _events.emit(UiEvent.WisdomAdded)
                    // Update category data since we might have added wisdom to selected categories
                    updateCategoryData()
                } else {
                    _events.emit(UiEvent.Error("Failed to add sample wisdom"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding sample wisdom", e)
                _events.emit(UiEvent.Error("Failed to add sample wisdom: ${e.localizedMessage}"))
            }
        }
    }

    fun debugDatabaseContents() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allWisdom = wisdomRepository.getAllWisdom().first()
                Log.d(TAG, "DEBUG - All wisdom in DB: ${allWisdom.size} items")
                allWisdom.forEach { wisdom ->
                    Log.d(TAG, "DB Wisdom: ID=${wisdom.id}, Text='${wisdom.text.take(20)}...', Active=${wisdom.isActive}, Queued=${!wisdom.isActive && wisdom.dateCompleted == null}")
                }
                val activeResult = wisdomRepository.getActiveWisdomDirect().getOrNull() ?: emptyList()
                Log.d(TAG, "DEBUG - Active wisdom direct from DB: ${activeResult.size} items")
                activeResult.forEach { Log.d(TAG, "DB Active: ID=${it.id}, Text='${it.text.take(20)}...'") }
            } catch (e: Exception) {
                Log.e(TAG, "Error debugging database", e)
            }
        }
    }
}