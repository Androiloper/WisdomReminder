package com.example.wisdomreminder.ui.main

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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
            val allWisdom: List<Wisdom> = emptyList(),
            val categories: List<String> = emptyList()
        ) : WisdomUiState()
        data class Error(val message: String) : WisdomUiState()
    }

    // Private mutable state
    private val _uiState = MutableStateFlow<WisdomUiState>(WisdomUiState.Loading)

    // Public immutable state
    val uiState = _uiState.asStateFlow()

    // Selected wisdom for detail view
    private val _selectedWisdom = MutableStateFlow<Wisdom?>(null)
    val selectedWisdom = _selectedWisdom.asStateFlow()

    // Events/actions
    sealed class UiEvent {
        object WisdomAdded : UiEvent()
        object WisdomDeleted : UiEvent()
        object WisdomUpdated : UiEvent()
        data class Error(val message: String) : UiEvent()
    }

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    init {
        Log.d(TAG, "MainViewModel initialized")
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.value = WisdomUiState.Loading

                // Collect all wisdom and categories
                wisdomRepository.getAllWisdom().collectLatest { allWisdom ->
                    val categories = wisdomRepository.getAllCategories().asStateFlow().value ?: emptyList()

                    _uiState.value = WisdomUiState.Success(
                        allWisdom = allWisdom,
                        categories = categories
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading data", e)
                _uiState.value = WisdomUiState.Error("Failed to load data: ${e.message}")
            }
        }
    }

    // Get wisdom by ID
    fun getWisdomById(id: Long) {
        viewModelScope.launch {
            wisdomRepository.getWisdomById(id).onSuccess { wisdom ->
                _selectedWisdom.value = wisdom
            }.onFailure { error ->
                _events.emit(UiEvent.Error("Could not load wisdom details"))
            }
        }
    }

    // Add new wisdom
    fun addWisdom(title: String, text: String, category: String) {
        if (title.isBlank() || text.isBlank()) return

        viewModelScope.launch {
            try {
                val wisdom = Wisdom(
                    title = title,
                    text = text,
                    category = category
                )

                wisdomRepository.addWisdom(wisdom).onSuccess { id ->
                    _events.emit(UiEvent.WisdomAdded)
                    loadData() // Reload data to reflect changes
                }.onFailure { error ->
                    _events.emit(UiEvent.Error("Failed to add wisdom: ${error.message}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in addWisdom", e)
                _events.emit(UiEvent.Error("An unexpected error occurred"))
            }
        }
    }

    // Update wisdom
    fun updateWisdom(wisdom: Wisdom) {
        viewModelScope.launch {
            wisdomRepository.updateWisdom(wisdom).onSuccess {
                _events.emit(UiEvent.WisdomUpdated)

                // Update selected wisdom if it's being edited
                if (_selectedWisdom.value?.id == wisdom.id) {
                    _selectedWisdom.value = wisdom
                }

                loadData() // Reload data to reflect changes
            }.onFailure { error ->
                _events.emit(UiEvent.Error("Failed to update wisdom: ${error.message}"))
            }
        }
    }

    // Delete wisdom
    fun deleteWisdom(wisdom: Wisdom) {
        viewModelScope.launch {
            wisdomRepository.deleteWisdom(wisdom).onSuccess {
                _events.emit(UiEvent.WisdomDeleted)

                // Clear selected wisdom if it's being deleted
                if (_selectedWisdom.value?.id == wisdom.id) {
                    _selectedWisdom.value = null
                }

                loadData() // Reload data to reflect changes
            }.onFailure { error ->
                _events.emit(UiEvent.Error("Failed to delete wisdom: ${error.message}"))
            }
        }
    }

    // Add sample data for testing
    fun addSampleWisdom() {
        viewModelScope.launch {
            val sampleWisdom = listOf(
                Wisdom(
                    title = "Quality of Questions",
                    text = "The quality of your life is determined by the quality of your questions",
                    category = "Personal Development"
                ),
                Wisdom(
                    title = "Habits and Excellence",
                    text = "We are what we repeatedly do. Excellence, then, is not an act, but a habit",
                    category = "Philosophy"
                ),
                Wisdom(
                    title = "Focus and Growth",
                    text = "What you stay focused on will grow",
                    category = "Mindfulness"
                )
            )

            var successCount = 0
            for (wisdom in sampleWisdom) {
                wisdomRepository.addWisdom(wisdom).onSuccess {
                    successCount++
                }
            }

            if (successCount > 0) {
                _events.emit(UiEvent.WisdomAdded)
                loadData() // Reload data
            }
        }
    }

    // Force refresh data
    fun refreshData() {
        loadData()
    }
}