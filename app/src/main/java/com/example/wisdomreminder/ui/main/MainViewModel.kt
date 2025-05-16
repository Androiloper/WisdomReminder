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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val wisdomRepository: WisdomRepository
) : ViewModel() {
    private val TAG = "MainViewModel"

    // Wisdom lists by status
    val activeWisdom = wisdomRepository.getActiveWisdom().asLiveData()
    val queuedWisdom = wisdomRepository.getQueuedWisdom().asLiveData()
    val completedWisdom = wisdomRepository.getCompletedWisdom().asLiveData()

    // Currently editing wisdom
    private val _editingWisdom = MutableLiveData<Wisdom?>(null)
    val editingWisdom: LiveData<Wisdom?> = _editingWisdom

    // Service status tracking
    private val _serviceRunning = MutableLiveData<Boolean>(false)
    val serviceRunning: LiveData<Boolean> = _serviceRunning

    // Statistics
    val activeWisdomCount = wisdomRepository.getActiveWisdomCount().asLiveData()
    val completedWisdomCount = wisdomRepository.getCompletedWisdomCount().asLiveData()

    init {
        // Initialize service status
        _serviceRunning.value = WisdomDisplayService.isServiceRunning
    }

    /**
     * Add a new wisdom item
     */
    fun addWisdom(text: String, source: String, category: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            try {
                val wisdom = Wisdom(
                    text = text,
                    source = source,
                    category = category,
                    dateCreated = LocalDateTime.now()
                )
                val id = wisdomRepository.addWisdom(wisdom)
                Log.d(TAG, "Added wisdom with ID: $id")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding wisdom", e)
            }
        }
    }

    /**
     * Delete a wisdom item
     */
    fun deleteWisdom(wisdom: Wisdom) {
        viewModelScope.launch {
            try {
                wisdomRepository.deleteWisdom(wisdom)
                Log.d(TAG, "Deleted wisdom: ${wisdom.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting wisdom", e)
            }
        }
    }

    /**
     * Update an existing wisdom
     */
    fun updateWisdom(wisdom: Wisdom) {
        viewModelScope.launch {
            try {
                wisdomRepository.updateWisdom(wisdom)
                Log.d(TAG, "Updated wisdom: ${wisdom.id}")
                // Clear editing state
                _editingWisdom.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error updating wisdom", e)
            }
        }
    }

    /**
     * Set the wisdom that's being edited
     */
    fun setEditingWisdom(wisdom: Wisdom?) {
        _editingWisdom.value = wisdom
    }

    /**
     * Activate a wisdom to start the 21/21 rule
     */
    fun activateWisdom(wisdomId: Long) {
        viewModelScope.launch {
            try {
                wisdomRepository.activateWisdom(wisdomId)
                Log.d(TAG, "Activated wisdom: $wisdomId")
            } catch (e: Exception) {
                Log.e(TAG, "Error activating wisdom", e)
            }
        }
    }

    /**
     * Record an exposure manually
     */
    fun recordExposure(wisdomId: Long) {
        viewModelScope.launch {
            try {
                wisdomRepository.recordExposure(wisdomId)
                Log.d(TAG, "Recorded exposure for wisdom: $wisdomId")
            } catch (e: Exception) {
                Log.e(TAG, "Error recording exposure", e)
            }
        }
    }

    /**
     * Check and restart the wisdom display service if needed
     */
    fun checkAndRestartService(context: Context) {
        viewModelScope.launch {
            // Update internal tracking
            _serviceRunning.value = WisdomDisplayService.isServiceRunning

            // If service is not running but should be, restart it
            if (!WisdomDisplayService.isServiceRunning) {
                startWisdomService(context)
            }
        }
    }

    /**
     * Start the wisdom display service
     */
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
            Log.d(TAG, "Started wisdom service")
            _serviceRunning.value = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start service", e)
            _serviceRunning.value = false
        }
    }

    /**
     * Stop the wisdom display service
     */
    fun stopWisdomService(context: Context) {
        try {
            val serviceIntent = Intent(context, WisdomDisplayService::class.java).apply {
                action = WisdomDisplayService.ACTION_STOP_SERVICE
            }
            context.startService(serviceIntent)
            Log.d(TAG, "Requested service stop")
            _serviceRunning.value = false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop service", e)
        }
    }

    /**
     * Add sample wisdom entries for testing
     */
    fun addSampleWisdom() {
        viewModelScope.launch {
            addWisdom(
                "What you stay focused on will grow",
                "Law of Attraction",
                "Personal Development"
            )

            addWisdom(
                "We are what we repeatedly do. Excellence, then, is not an act, but a habit.",
                "Aristotle",
                "Philosophy"
            )

            addWisdom(
                "The quality of your life is determined by the quality of your questions",
                "Tony Robbins",
                "Personal Development"
            )

            addWisdom(
                "You don't have to be great to start, but you have to start to be great",
                "Zig Ziglar",
                "Motivation"
            )

            addWisdom(
                "Knowledge is knowing what to say. Wisdom is knowing when to say it.",
                "Anonymous",
                "Communication"
            )
        }
    }
}