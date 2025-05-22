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
import kotlinx.coroutines.flow.Flow // Ensure Flow is imported
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine // Crucial: ensure this is the ONLY combine import for flows
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

// ... other imports ...
//import kotlinx.coroutines.flow.StateFlow // Make sure this is imported
import kotlinx.coroutines.flow.asStateFlow // Make sure this is imported
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
//import java.time.LocalDateTime
//import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val wisdomRepository: IWisdomRepository,
    @ApplicationContext private val applicationContext: Context
) : ViewModel() {
    private val TAG = "MainViewModel"

    private val prefs: SharedPreferences = applicationContext.getSharedPreferences("wisdom_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _uiState = MutableStateFlow<WisdomUiState>(WisdomUiState.Loading)
    val uiState: StateFlow<WisdomUiState> = _uiState.asStateFlow()

    private val _selectedWisdom = MutableStateFlow<Wisdom?>(null)
    val selectedWisdom: StateFlow<Wisdom?> = _selectedWisdom.asStateFlow()

    private val _selectedCategoriesForCards = MutableStateFlow<List<String>>(emptyList())

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    private val serviceStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_SERVICE_STATUS_CHANGE) {
                val isRunning = intent.getBooleanExtra(EXTRA_IS_RUNNING, false)
                Log.d(TAG, "Service status broadcast received: running=$isRunning")
                updateServiceRunningState(isRunning)
            }
        }
    }

    init {
        Log.d(TAG, "MainViewModel initialized")
        val intentFilter = IntentFilter(ACTION_SERVICE_STATUS_CHANGE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            applicationContext.registerReceiver(
                serviceStatusReceiver,
                intentFilter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            applicationContext.registerReceiver(serviceStatusReceiver, intentFilter)
        }
        loadSelectedCategoriesForCards()
        initializeDataCollection()
    }

    companion object {
        private const val SELECTED_CATEGORIES_FOR_CARDS_KEY = "selected_categories_for_cards"
        const val ACTION_SERVICE_STATUS_CHANGE = "com.example.wisdomreminder.ACTION_SERVICE_STATUS_CHANGE"
        const val EXTRA_IS_RUNNING = "extra_is_running"
        const val DEFAULT_CATEGORY = "General" // Define default category
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
        data class CategoryOperationSuccess(val message: String) : UiEvent() // For rename/delete
        data class Error(val message: String) : UiEvent()
    }

    private var dataCollectionJob: Job? = null
    private var categoryDataJob: Job? = null


    override fun onCleared() {
        Log.d(TAG, "MainViewModel onCleared")
        applicationContext.unregisterReceiver(serviceStatusReceiver)
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
                    _selectedCategoriesForCards.value = gson.fromJson(categoriesJson, type) ?: emptyList()
                    Log.d(TAG, "loadSelectedCategories: Loaded ${_selectedCategoriesForCards.value.size} categories for dashboard: ${_selectedCategoriesForCards.value}")
                } catch (e: Exception) {
                    Log.e(TAG, "loadSelectedCategories: Error loading categories for dashboard", e)
                    _selectedCategoriesForCards.value = emptyList()
                }
            } else {
                Log.d(TAG, "loadSelectedCategories: No categories found in prefs for dashboard.")
                _selectedCategoriesForCards.value = emptyList()
            }
        }
    }

    private fun saveSelectedCategoriesForCards(categories: List<String>) {
        viewModelScope.launch {
            try {
                val categoriesJson = gson.toJson(categories)
                prefs.edit().putString(SELECTED_CATEGORIES_FOR_CARDS_KEY, categoriesJson).apply()
                Log.d(TAG, "saveSelectedCategories: Saved ${categories.size} categories for dashboard: $categories")
            } catch (e: Exception) {
                Log.e(TAG, "saveSelectedCategories: Error saving categories for dashboard", e)
            }
        }
    }

    private fun initializeDataCollection() {
        dataCollectionJob?.cancel()
        Log.d(TAG, "initializeDataCollection: Starting data collection...")
        if (_uiState.value !is WisdomUiState.Success) {
            _uiState.value = WisdomUiState.Loading
        }

        dataCollectionJob = viewModelScope.launch {
            val activeWisdomFlow = wisdomRepository.getActiveWisdom()
            val queuedWisdomFlow = wisdomRepository.getQueuedWisdom()
            val completedWisdomFlow = wisdomRepository.getCompletedWisdom()
            val activeWisdomCountFlow = wisdomRepository.getActiveWisdomCount()
            val completedWisdomCountFlow = wisdomRepository.getCompletedWisdomCount()
            val allCategoriesFlow = wisdomRepository.getAllCategories()
            // val selectedCategoriesForCardsFlow = _selectedCategoriesForCards.asStateFlow() // Correct, using asStateFlow()

            combine(
                activeWisdomFlow, queuedWisdomFlow, completedWisdomFlow,
                activeWisdomCountFlow, completedWisdomCountFlow, allCategoriesFlow,
                _selectedCategoriesForCards // Directly use the MutableStateFlow here for combine
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                WisdomUiState.Success(
                    activeWisdom = values[0] as List<Wisdom>,
                    queuedWisdom = values[1] as List<Wisdom>,
                    completedWisdom = values[2] as List<Wisdom>,
                    activeCount = values[3] as Int,
                    completedCount = values[4] as Int,
                    allCategories = (values[5] as List<String>).distinct().sorted(), // Ensure distinct and sorted
                    selectedCategoriesForCards = values[6] as List<String>,
                    serviceRunning = (_uiState.value as? WisdomUiState.Success)?.serviceRunning ?: WisdomDisplayService.isServiceRunning,
                    categoryWisdomMap = (_uiState.value as? WisdomUiState.Success)?.categoryWisdomMap ?: emptyMap()
                )
            }.catch { e ->
                Log.e(TAG, "initializeDataCollection (combine): Exception", e)
                _uiState.value = WisdomUiState.Error("Failed to load data: ${e.localizedMessage ?: "Unknown error"}")
            }.collectLatest { successState ->
                _uiState.value = successState
                Log.d(TAG, "initializeDataCollection (collectLatest): New Success state. Selected dashboard categories: ${successState.selectedCategoriesForCards}")
                updateCategoryData()
            }
        }
    }

    private fun updateCategoryData() {
        categoryDataJob?.cancel()
        categoryDataJob = viewModelScope.launch(Dispatchers.IO) {
            val currentSuccessState = _uiState.value as? WisdomUiState.Success ?: return@launch
            val selectedCategories = currentSuccessState.selectedCategoriesForCards

            if (selectedCategories.isEmpty()) {
                if (currentSuccessState.categoryWisdomMap.isNotEmpty()) {
                    Log.d(TAG, "updateCategoryData: No categories selected for dashboard, clearing map.")
                    _uiState.value = currentSuccessState.copy(categoryWisdomMap = emptyMap())
                }
                return@launch
            }

            Log.d(TAG, "updateCategoryData: Updating for dashboard categories: $selectedCategories")
            val newCategoryWisdomMap = mutableMapOf<String, List<Wisdom>>()
            var mapChanged = false

            selectedCategories.forEach { category ->
                try {
                    val wisdomForCategory = wisdomRepository.getWisdomByCategory(category).first()
                    newCategoryWisdomMap[category] = wisdomForCategory
                    if (currentSuccessState.categoryWisdomMap[category] != wisdomForCategory) {
                        mapChanged = true
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "updateCategoryData: Error fetching wisdom for card category '$category'", e)
                    newCategoryWisdomMap[category] = emptyList()
                    if (currentSuccessState.categoryWisdomMap.containsKey(category)) mapChanged = true
                }
            }
            Log.d(TAG, "updateCategoryData: currentSuccessState.categoryWisdomMap before update (keys): ${currentSuccessState.categoryWisdomMap.keys}, Size: ${currentSuccessState.categoryWisdomMap.size}")
            Log.d(TAG, "updateCategoryData: newCategoryWisdomMap to be set (keys): ${newCategoryWisdomMap.keys}, Size: ${newCategoryWisdomMap.size}")
            Log.d(TAG, "updateCategoryData: newCategoryWisdomMap content (counts): ${newCategoryWisdomMap.mapValues { it.value.size }}")

            val keysDiffer = currentSuccessState.categoryWisdomMap.keys != newCategoryWisdomMap.keys
            val sizeDiffer = currentSuccessState.categoryWisdomMap.size != newCategoryWisdomMap.size
            val contentActuallyChanged = mapChanged || keysDiffer || sizeDiffer

            if (contentActuallyChanged) {
                Log.d(TAG, "updateCategoryData: Map data changed (mapChanged=$mapChanged, keysDiffer=$keysDiffer, sizeDiffer=$sizeDiffer). Updating UI state.")
                _uiState.value = currentSuccessState.copy(categoryWisdomMap = newCategoryWisdomMap.toMap())
            } else {
                Log.d(TAG, "updateCategoryData: Map data seems unchanged. No map-specific UI update.")
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
                Log.d(TAG, "addCategoryCard: Added '$category'. Current selection: ${_selectedCategoriesForCards.value}")
                _events.emit(UiEvent.CategoryCardAdded)
            } else {
                Log.d(TAG, "addCategoryCard: Category '$category' already present.")
            }
        }
    }

    fun removeCategoryCard(category: String) {
        viewModelScope.launch {
            val currentCategories = _selectedCategoriesForCards.value.toMutableList()
            if (currentCategories.remove(category)) {
                _selectedCategoriesForCards.value = currentCategories.toList()
                saveSelectedCategoriesForCards(_selectedCategoriesForCards.value)
                Log.d(TAG, "removeCategoryCard: Removed '$category'. Current selection: ${_selectedCategoriesForCards.value}")
                _events.emit(UiEvent.CategoryCardRemoved)
            } else {
                Log.d(TAG, "removeCategoryCard: Category '$category' not found in selection.")
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
                        Log.e(TAG, "getWisdomById: Error for ID $id", error)
                        _events.emit(UiEvent.Error("Could not load wisdom details: ${error.message}"))
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "getWisdomById: Exception for ID $id", e)
                _events.emit(UiEvent.Error("An unexpected error occurred: ${e.message}"))
            }
        }
    }

    fun addWisdom(text: String, source: String, category: String) {
        if (text.isBlank()) {
            viewModelScope.launch { _events.emit(UiEvent.Error("Wisdom text cannot be empty")) }
            return
        }
        val wisdomCategory = category.ifBlank { DEFAULT_CATEGORY }
        Log.d(TAG, "addWisdom: Text='${text.take(20)}...', Source='$source', Category='$wisdomCategory'")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val wisdom = Wisdom(
                    text = text,
                    source = source,
                    category = wisdomCategory,
                    dateCreated = LocalDateTime.now()
                )
                val result = wisdomRepository.addWisdom(wisdom)
                result.fold(
                    onSuccess = { id ->
                        Log.d(TAG, "addWisdom: Success, ID=$id. Category='${wisdom.category}'")
                        _events.emit(UiEvent.WisdomAdded)
                    },
                    onFailure = { error ->
                        Log.e(TAG, "addWisdom: Error", error)
                        _events.emit(UiEvent.Error("Failed to add wisdom: ${error.localizedMessage ?: "Unknown error"}"))
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "addWisdom: Exception", e)
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
                            getWisdomById(wisdom.id)
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "updateWisdom: Error for ID ${wisdom.id}", error)
                        _events.emit(UiEvent.Error("Failed to update wisdom: ${error.localizedMessage}"))
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "updateWisdom: Exception for ID ${wisdom.id}", e)
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
                    },
                    onFailure = { error ->
                        Log.e(TAG, "deleteWisdom: Error for ID ${wisdom.id}", error)
                        _events.emit(UiEvent.Error("Failed to delete wisdom: ${error.localizedMessage}"))
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "deleteWisdom: Exception for ID ${wisdom.id}", e)
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

            try {
                wisdomRepository.activateWisdom(wisdomId).fold(
                    onSuccess = {
                        _events.emit(UiEvent.WisdomActivated)
                        if (_selectedWisdom.value?.id == wisdomId) {
                            getWisdomById(wisdomId)
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "activateWisdom: Error for ID $wisdomId", error)
                        _events.emit(UiEvent.Error("Failed to activate wisdom: ${error.localizedMessage}"))
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "activateWisdom: Exception for ID $wisdomId", e)
                _events.emit(UiEvent.Error("An unexpected error occurred: ${e.localizedMessage}"))
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            Log.d(TAG, "refreshData: Manual refresh triggered.")
            initializeDataCollection()
        }
    }

    private fun updateServiceRunningState(isRunning: Boolean) {
        viewModelScope.launch(Dispatchers.Main) {
            val currentUiState = _uiState.value
            if (currentUiState is WisdomUiState.Success) {
                if (currentUiState.serviceRunning != isRunning) {
                    _uiState.value = currentUiState.copy(serviceRunning = isRunning)
                    Log.d(TAG, "updateServiceRunningState: UI serviceRunning state updated to: $isRunning")
                }
            }
        }
    }

    fun checkAndRestartService(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val isCurrentlyRunning = WisdomDisplayService.isServiceRunning
            updateServiceRunningState(isCurrentlyRunning)

            if (!isCurrentlyRunning) {
                Log.d(TAG, "checkAndRestartService: Service not running, attempting to start.")
                startService(context)
            } else {
                Log.d(TAG, "checkAndRestartService: Service is already running.")
            }
        }
    }

    fun startService(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "startService: Requesting WisdomDisplayService START")
                val serviceIntent = Intent(context, WisdomDisplayService::class.java).apply {
                    action = WisdomDisplayService.ACTION_START_SERVICE
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "startService: Failed to start", e)
                updateServiceRunningState(false)
                _events.emit(UiEvent.Error("Failed to start wisdom service: ${e.localizedMessage}"))
            }
        }
    }

    fun stopWisdomService(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "stopWisdomService: Requesting WisdomDisplayService STOP")
                val serviceIntent = Intent(context, WisdomDisplayService::class.java).apply {
                    action = WisdomDisplayService.ACTION_STOP_SERVICE
                }
                context.startService(serviceIntent)
            } catch (e: Exception) {
                Log.e(TAG, "stopWisdomService: Failed to stop", e)
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
                    Wisdom(text = "Let no corrupt words proceed out of your mouth", source = "Ephesians 4:29", category = DEFAULT_CATEGORY)
                )
                var successCount = 0
                for (wisdom in sampleWisdom) {
                    wisdomRepository.addWisdom(wisdom).onSuccess { successCount++ }
                }
                if (successCount > 0) {
                    _events.emit(UiEvent.WisdomAdded)
                } else {
                    _events.emit(UiEvent.Error("Failed to add sample wisdom"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "addSampleWisdom: Error", e)
                _events.emit(UiEvent.Error("Failed to add sample wisdom: ${e.localizedMessage}"))
            }
        }
    }

    // --- New Category Management ViewModel Functions ---
    fun renameWisdomCategory(oldName: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (oldName.isBlank() || newName.isBlank() || oldName.equals(newName, ignoreCase = true)) {
                _events.emit(UiEvent.Error("Invalid category names for rename."))
                return@launch
            }
            // Optional: Check if newName already exists and prompt user if they want to merge.
            // For simplicity, this implementation will just rename. If newName exists, items will be merged.
            wisdomRepository.renameCategory(oldName, newName).fold(
                onSuccess = { count ->
                    Log.d(TAG, "Category '$oldName' renamed to '$newName', $count items affected.")
                    _events.emit(UiEvent.CategoryOperationSuccess("Category '$oldName' renamed to '$newName'"))
                    // The allCategories flow will refresh from the DB.
                    // If the renamed category was on the dashboard, update its card data.
                    if (_selectedCategoriesForCards.value.contains(oldName)) {
                        val updatedDashboardCategories = _selectedCategoriesForCards.value.map { if (it == oldName) newName else it }.distinct()
                        _selectedCategoriesForCards.value = updatedDashboardCategories
                        saveSelectedCategoriesForCards(updatedDashboardCategories)
                    }
                },
                onFailure = {
                    Log.e(TAG, "Failed to rename category '$oldName'", it)
                    _events.emit(UiEvent.Error("Failed to rename category: ${it.localizedMessage}"))
                }
            )
        }
    }

    fun clearWisdomCategory(categoryName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (categoryName.isBlank() || categoryName.equals(DEFAULT_CATEGORY, ignoreCase = true)) {
                _events.emit(UiEvent.Error("Cannot clear the default 'General' category or a blank category."))
                return@launch
            }
            wisdomRepository.clearCategoryItemsToGeneral(categoryName, DEFAULT_CATEGORY).fold(
                onSuccess = { count ->
                    Log.d(TAG, "Items from category '$categoryName' reassigned to '$DEFAULT_CATEGORY'. $count items affected.")
                    _events.emit(UiEvent.CategoryOperationSuccess("Items from '$categoryName' moved to '$DEFAULT_CATEGORY'."))
                    // The allCategories flow will refresh.
                    // If the cleared category was on the dashboard, remove it.
                    if (_selectedCategoriesForCards.value.contains(categoryName)) {
                        removeCategoryCard(categoryName) // This will also save and trigger UI update for dashboard
                    }
                },
                onFailure = {
                    Log.e(TAG, "Failed to clear category '$categoryName'", it)
                    _events.emit(UiEvent.Error("Failed to clear category: ${it.localizedMessage}"))
                }
            )
        }
    }


    fun debugDatabaseContents() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allWisdom = wisdomRepository.getAllWisdom().first()
                Log.d(TAG, "debugDatabaseContents: === All Wisdom in DB (${allWisdom.size}) ===")
                allWisdom.forEach { wisdom ->
                    Log.d(TAG, "DB Item: ID=${wisdom.id}, Text='${wisdom.text.take(30)}...', Cat='${wisdom.category}', Active=${wisdom.isActive}, Queued=${!wisdom.isActive && wisdom.dateCompleted == null}")
                }
                Log.d(TAG, "debugDatabaseContents: === End of All Wisdom ===")

                (_uiState.value as? WisdomUiState.Success)?.let {
                    Log.d(TAG, "debugDatabaseContents: === Current UI State ===")
                    Log.d(TAG, "Active: ${it.activeWisdom.size}, Queued: ${it.queuedWisdom.size}, Completed: ${it.completedWisdom.size}")
                    Log.d(TAG, "All Categories from DB: ${it.allCategories}")
                    Log.d(TAG, "Selected Dashboard Categories (from _selectedCategoriesForCards): ${it.selectedCategoriesForCards}")
                    Log.d(TAG, "Category Wisdom Map (Counts): ${it.categoryWisdomMap.mapValues { entry -> entry.value.size }}")
                    it.categoryWisdomMap.forEach{ (cat, list) ->
                        Log.d(TAG, "Map details for '$cat': ${list.joinToString { w -> w.text.take(15) }}")
                    }
                    Log.d(TAG, "debugDatabaseContents: === End of UI State ===")
                }
            } catch (e: Exception) {
                Log.e(TAG, "debugDatabaseContents: Error", e)
            }
        }
    }
}