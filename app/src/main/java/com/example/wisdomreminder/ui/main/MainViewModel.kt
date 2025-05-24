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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

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

    private val _selectedCategoriesForCards = MutableStateFlow<List<String>>(emptyList()) // For "MY DASHBOARD"

    private val _categoryForSevenWisdomPlaylist = MutableStateFlow<String?>(
        prefs.getString(CATEGORY_FOR_SEVEN_WISDOM_KEY, null)
    )
    val categoryForSevenWisdomPlaylist: StateFlow<String?> = _categoryForSevenWisdomPlaylist.asStateFlow()

    private val _mainScreenExplorerCategories = MutableStateFlow<List<String>>(
        emptyList()
    )
    val mainScreenExplorerCategories: StateFlow<List<String>> = _mainScreenExplorerCategories.asStateFlow()


    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    // This BroadcastReceiver listens for service status changes
    private val serviceStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_SERVICE_STATUS_CHANGE) {
                val isRunning = intent.getBooleanExtra(EXTRA_IS_RUNNING, false)
                Log.d(TAG, "Service status broadcast received: running=$isRunning")
                updateServiceRunningState(isRunning) // CALL TO THE FUNCTION
            }
        }
    }

    init {
        Log.d(TAG, "MainViewModel initialized.")
        val intentFilter = IntentFilter(ACTION_SERVICE_STATUS_CHANGE)
        // Register the receiver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            applicationContext.registerReceiver(serviceStatusReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            applicationContext.registerReceiver(serviceStatusReceiver, intentFilter)
        }
        loadSelectedCategoriesForCards()
        loadMainScreenExplorerCategories()
        initializeDataCollection()

        viewModelScope.launch {
            _categoryForSevenWisdomPlaylist.collectLatest { categoryName ->
                val currentSuccessState = _uiState.value as? WisdomUiState.Success
                if (currentSuccessState != null && currentSuccessState.selectedCategoryForSevenWisdom != categoryName) {
                    _uiState.value = currentSuccessState.copy(selectedCategoryForSevenWisdom = categoryName)
                }
            }
        }
    }

    companion object {
        private const val SELECTED_CATEGORIES_FOR_CARDS_KEY = "selected_categories_for_cards"
        private const val MAIN_SCREEN_EXPLORER_CATEGORIES_KEY = "main_screen_explorer_categories"
        const val ACTION_SERVICE_STATUS_CHANGE = "com.example.wisdomreminder.ACTION_SERVICE_STATUS_CHANGE"
        const val EXTRA_IS_RUNNING = "extra_is_running"
        const val DEFAULT_CATEGORY = "General"
        private const val CATEGORY_FOR_SEVEN_WISDOM_KEY = "category_for_seven_wisdom"
    }

    sealed class WisdomUiState {
        object Loading : WisdomUiState()
        data class Success(
            val allWisdomFlatList: List<Wisdom> = emptyList(),
            val activeWisdom: List<Wisdom> = emptyList(),
            val otherQueuedWisdom: List<Wisdom> = emptyList(),
            val favoriteQueuedWisdom: List<Wisdom> = emptyList(),
            val sevenWisdomPlaylist: List<Wisdom> = emptyList(),
            val completedWisdom: List<Wisdom> = emptyList(),
            val activeCount: Int = 0,
            val completedCount: Int = 0,
            val serviceRunning: Boolean = false,
            val allCategories: List<String> = emptyList(),
            val selectedCategoriesForCards: List<String> = emptyList(),
            val mainScreenExplorerCategories: List<String> = emptyList(),
            val categoryWisdomMap: Map<String, List<Wisdom>> = emptyMap(),
            val selectedCategoryForSevenWisdom: String? = null
        ) : WisdomUiState()
        data class Error(val message: String) : WisdomUiState()
    }

    sealed class UiEvent {
        object WisdomAdded : UiEvent()
        object WisdomDeleted : UiEvent()
        object WisdomUpdated : UiEvent()
        object WisdomActivated : UiEvent()
        object WisdomFavorited : UiEvent()
        object CategoryCardAdded : UiEvent()
        object CategoryCardRemoved : UiEvent()
        object MainScreenExplorerCategoryAdded : UiEvent()
        object MainScreenExplorerCategoryRemoved : UiEvent()
        object SevenWisdomCategoryChanged : UiEvent()
        data class CategoryOperationSuccess(val message: String) : UiEvent()
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
        val categoriesJson = prefs.getString(SELECTED_CATEGORIES_FOR_CARDS_KEY, null)
        if (categoriesJson != null) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                _selectedCategoriesForCards.value = gson.fromJson(categoriesJson, type) ?: emptyList()
            } catch (e: Exception) { _selectedCategoriesForCards.value = emptyList() }
        } else { _selectedCategoriesForCards.value = emptyList() }
    }

    private fun saveSelectedCategoriesForCards(categories: List<String>) {
        val categoriesJson = gson.toJson(categories)
        prefs.edit().putString(SELECTED_CATEGORIES_FOR_CARDS_KEY, categoriesJson).apply()
    }

    private fun loadMainScreenExplorerCategories() {
        val categoriesJson = prefs.getString(MAIN_SCREEN_EXPLORER_CATEGORIES_KEY, null)
        if (categoriesJson != null) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                _mainScreenExplorerCategories.value = gson.fromJson(categoriesJson, type) ?: emptyList()
                Log.d(TAG, "Loaded mainScreenExplorerCategories: ${_mainScreenExplorerCategories.value}")
            } catch (e: Exception) { _mainScreenExplorerCategories.value = emptyList() }
        } else { _mainScreenExplorerCategories.value = emptyList() }
    }

    private fun saveMainScreenExplorerCategories(categories: List<String>) {
        val categoriesJson = gson.toJson(categories)
        prefs.edit().putString(MAIN_SCREEN_EXPLORER_CATEGORIES_KEY, categoriesJson).apply()
        Log.d(TAG, "Saved mainScreenExplorerCategories: $categories")
    }

    fun addCategoryToMainScreenExplorers(category: String) {
        viewModelScope.launch {
            val currentList = _mainScreenExplorerCategories.value.toMutableList()
            if (!currentList.contains(category)) {
                currentList.add(category)
                _mainScreenExplorerCategories.value = currentList.toList()
                saveMainScreenExplorerCategories(_mainScreenExplorerCategories.value)
                _events.emit(UiEvent.MainScreenExplorerCategoryAdded)
            }
        }
    }

    fun removeCategoryFromMainScreenExplorers(category: String) {
        viewModelScope.launch {
            val currentList = _mainScreenExplorerCategories.value.toMutableList()
            if (currentList.remove(category)) {
                _mainScreenExplorerCategories.value = currentList.toList()
                saveMainScreenExplorerCategories(_mainScreenExplorerCategories.value)
                _events.emit(UiEvent.MainScreenExplorerCategoryRemoved)
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
            val allWisdomFlatListFlow = wisdomRepository.getAllWisdom()
            val activeWisdomFlow = wisdomRepository.getActiveWisdom()
            val strictlyQueuedWisdomFlow = wisdomRepository.getStrictlyQueuedWisdom()
            val favoriteDisplayableWisdomFlow = wisdomRepository.getFavoriteDisplayableWisdom()
            val completedWisdomFlow = wisdomRepository.getCompletedWisdom()
            val activeWisdomCountFlow = wisdomRepository.getActiveWisdomCount()
            val completedWisdomCountFlow = wisdomRepository.getCompletedWisdomCount()
            val allCategoriesFlow = wisdomRepository.getAllCategories()

            val sevenWisdomPlaylistFlow = _categoryForSevenWisdomPlaylist.flatMapLatest { categoryName ->
                if (categoryName != null) {
                    wisdomRepository.getDisplayableWisdomByCategory(categoryName)
                        .map { list -> list.take(7) }
                } else { flowOf(emptyList<Wisdom>()) }
            }.catch { e -> Log.e(TAG, "Error in sevenWisdomPlaylistFlow", e); emit(emptyList<Wisdom>()) }

            combine(
                allWisdomFlatListFlow,
                activeWisdomFlow,
                strictlyQueuedWisdomFlow,
                favoriteDisplayableWisdomFlow,
                sevenWisdomPlaylistFlow,
                completedWisdomFlow,
                activeWisdomCountFlow,
                completedWisdomCountFlow,
                allCategoriesFlow,
                _selectedCategoriesForCards,
                _categoryForSevenWisdomPlaylist,
                _mainScreenExplorerCategories
            ) { values ->
                val currentServiceStatus = (_uiState.value as? WisdomUiState.Success)?.serviceRunning ?: WisdomDisplayService.isServiceRunning
                val currentCategoryMap = (_uiState.value as? WisdomUiState.Success)?.categoryWisdomMap ?: emptyMap()

                @Suppress("UNCHECKED_CAST")
                WisdomUiState.Success(
                    allWisdomFlatList = values[0] as List<Wisdom>,
                    activeWisdom = values[1] as List<Wisdom>,
                    otherQueuedWisdom = values[2] as List<Wisdom>,
                    favoriteQueuedWisdom = values[3] as List<Wisdom>,
                    sevenWisdomPlaylist = values[4] as List<Wisdom>,
                    completedWisdom = values[5] as List<Wisdom>,
                    activeCount = values[6] as Int,
                    completedCount = values[7] as Int,
                    allCategories = (values[8] as List<String>).distinct().sorted(),
                    selectedCategoriesForCards = values[9] as List<String>,
                    mainScreenExplorerCategories = values[11] as List<String>,
                    categoryWisdomMap = currentCategoryMap,
                    selectedCategoryForSevenWisdom = values[10] as String?
                )
            }.catch { e ->
                Log.e(TAG, "initializeDataCollection (combine): Exception", e)
                _uiState.value = WisdomUiState.Error("Failed to load data: ${e.localizedMessage ?: "Unknown error"}")
            }.collectLatest { successState ->
                _uiState.value = successState
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
                    _uiState.value = currentSuccessState.copy(categoryWisdomMap = emptyMap())
                }
                return@launch
            }

            val newCategoryWisdomMap = mutableMapOf<String, List<Wisdom>>()
            var mapChanged = false

            selectedCategories.forEach { category ->
                try {
                    val wisdomForCategory = wisdomRepository.getDisplayableWisdomByCategory(category).first()
                    newCategoryWisdomMap[category] = wisdomForCategory
                    if (currentSuccessState.categoryWisdomMap[category] != wisdomForCategory) {
                        mapChanged = true
                    }
                } catch (e: Exception) {
                    newCategoryWisdomMap[category] = emptyList()
                    if (currentSuccessState.categoryWisdomMap.containsKey(category)) mapChanged = true
                }
            }

            val keysDiffer = currentSuccessState.categoryWisdomMap.keys != newCategoryWisdomMap.keys
            val sizeDiffer = currentSuccessState.categoryWisdomMap.size != newCategoryWisdomMap.size

            if (mapChanged || keysDiffer || sizeDiffer) {
                _uiState.value = currentSuccessState.copy(
                    categoryWisdomMap = newCategoryWisdomMap.toMap()
                )
            }
        }
    }

    // DEFINITION OF THE FUNCTION
    private fun updateServiceRunningState(isRunning: Boolean) {
        viewModelScope.launch(Dispatchers.Main) {
            val currentUiState = _uiState.value
            if (currentUiState is WisdomUiState.Success) {
                if (currentUiState.serviceRunning != isRunning) {
                    _uiState.value = currentUiState.copy(serviceRunning = isRunning)
                }
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
                        _events.emit(UiEvent.Error("Could not load wisdom details: ${error.message}"))
                    }
                )
            } catch (e: Exception) {
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentMaxOrderIndex = (uiState.value as? WisdomUiState.Success)?.otherQueuedWisdom
                    ?.filter { it.category == wisdomCategory }
                    ?.maxOfOrNull { it.orderIndex } ?: -1

                val wisdom = Wisdom(
                    text = text,
                    source = source,
                    category = wisdomCategory,
                    dateCreated = LocalDateTime.now(),
                    orderIndex = currentMaxOrderIndex + 1
                )
                wisdomRepository.addWisdom(wisdom).fold(
                    onSuccess = { _events.emit(UiEvent.WisdomAdded) },
                    onFailure = { error ->
                        _events.emit(UiEvent.Error("Failed to add wisdom: ${error.localizedMessage ?: "Unknown error"}"))
                    }
                )
            } catch (e: Exception) {
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
                        _events.emit(UiEvent.Error("Failed to update wisdom: ${error.localizedMessage}"))
                    }
                )
            } catch (e: Exception) {
                _events.emit(UiEvent.Error("An unexpected error occurred: ${e.localizedMessage}"))
            }
        }
    }
    fun deleteWisdom(wisdom: Wisdom) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (wisdom.category == _categoryForSevenWisdomPlaylist.value) {
                }
                wisdomRepository.deleteWisdom(wisdom).fold(
                    onSuccess = {
                        _events.emit(UiEvent.WisdomDeleted)
                        if (_selectedWisdom.value?.id == wisdom.id) {
                            _selectedWisdom.value = null
                        }
                    },
                    onFailure = { error ->
                        _events.emit(UiEvent.Error("Failed to delete wisdom: ${error.localizedMessage}"))
                    }
                )
            } catch (e: Exception) {
                _events.emit(UiEvent.Error("An unexpected error occurred: ${e.localizedMessage}"))
            }
        }
    }
    fun activateWisdom(wisdomId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                wisdomRepository.activateWisdom(wisdomId).fold(
                    onSuccess = {
                        _events.emit(UiEvent.WisdomActivated)
                        if (_selectedWisdom.value?.id == wisdomId) {
                            getWisdomById(wisdomId)
                        }
                    },
                    onFailure = { error ->
                        _events.emit(UiEvent.Error("Failed to activate wisdom: ${error.localizedMessage}"))
                    }
                )
            } catch (e: Exception) {
                _events.emit(UiEvent.Error("An unexpected error occurred: ${e.localizedMessage}"))
            }
        }
    }
    fun toggleFavoriteStatus(wisdomId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val wisdomResult = wisdomRepository.getWisdomById(wisdomId)
            wisdomResult.fold(
                onSuccess = { currentWisdom ->
                    if (currentWisdom != null) {
                        val newFavoriteStatus = !currentWisdom.isFavorite
                        wisdomRepository.updateFavoriteStatus(wisdomId, newFavoriteStatus).fold(
                            onSuccess = {
                                _events.emit(UiEvent.WisdomFavorited)
                                if (_selectedWisdom.value?.id == wisdomId) {
                                    _selectedWisdom.value = currentWisdom.copy(isFavorite = newFavoriteStatus)
                                }
                            },
                            onFailure = { error ->
                                _events.emit(UiEvent.Error("Failed to update favorite status: ${error.localizedMessage}"))
                            }
                        )
                    } else {
                        _events.emit(UiEvent.Error("Could not find wisdom to update favorite status."))
                    }
                },
                onFailure = { error ->
                    _events.emit(UiEvent.Error("Could not fetch wisdom to update favorite: ${error.message}"))
                }
            )
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            Log.d(TAG, "refreshData called. Re-initializing data collection.")
            initializeDataCollection()
        }
    }
    fun checkAndRestartService(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val isCurrentlyRunning = WisdomDisplayService.isServiceRunning
            updateServiceRunningState(isCurrentlyRunning) // CALL TO THE FUNCTION
            if (!isCurrentlyRunning) {
                startService(context)
            }
        }
    }
    fun startService(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val serviceIntent = Intent(context, WisdomDisplayService::class.java).apply {
                    action = WisdomDisplayService.ACTION_START_SERVICE
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } catch (e: Exception) {
                updateServiceRunningState(false) // Ensure state is updated on failure
                _events.emit(UiEvent.Error("Failed to start wisdom service: ${e.localizedMessage}"))
            }
        }
    }
    fun stopWisdomService(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val serviceIntent = Intent(context, WisdomDisplayService::class.java).apply {
                    action = WisdomDisplayService.ACTION_STOP_SERVICE
                }
                context.startService(serviceIntent)
            } catch (e: Exception) {
                _events.emit(UiEvent.Error("Failed to stop wisdom service: ${e.localizedMessage}"))
            }
        }
    }
    fun addSampleWisdom() { /* ... */ }
    fun renameWisdomCategory(oldName: String, newName: String) { /* ... */ }
    fun clearWisdomCategory(categoryName: String) { /* ... */ }
    fun setCategoryForSevenWisdomPlaylist(categoryName: String?) { /* ... */ }
    fun debugDatabaseContents() { /* ... */ }

}