package com.example.wisdomreminder.service

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wisdomreminder.service.WisdomDisplayService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ServiceViewModel @Inject constructor() : ViewModel() {
    private val TAG = "ServiceViewModel"

    // Service status
    private val _serviceStatus = MutableStateFlow(ServiceStatus.UNKNOWN)
    val serviceStatus = _serviceStatus.asStateFlow()

    // Events
    sealed class ServiceEvent {
        object Started : ServiceEvent()
        object Stopped : ServiceEvent()
        data class Error(val message: String) : ServiceEvent()
    }

    private val _events = MutableSharedFlow<ServiceEvent>()
    val events = _events.asSharedFlow()

    // Service status
    enum class ServiceStatus {
        RUNNING,
        STOPPED,
        UNKNOWN
    }

    init {
        // Initialize service status
        _serviceStatus.value = if (WisdomDisplayService.isServiceRunning) {
            ServiceStatus.RUNNING
        } else {
            ServiceStatus.STOPPED
        }
    }

    fun checkServiceStatus() {
        _serviceStatus.value = if (WisdomDisplayService.isServiceRunning) {
            ServiceStatus.RUNNING
        } else {
            ServiceStatus.STOPPED
        }
    }

    fun startService(context: Context) {
        viewModelScope.launch {
            try {
                val serviceIntent = Intent(context, WisdomDisplayService::class.java).apply {
                    action = WisdomDisplayService.ACTION_START_SERVICE
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }

                // Give service time to start
                delay(500)
                checkServiceStatus()

                if (_serviceStatus.value == ServiceStatus.RUNNING) {
                    _events.emit(ServiceEvent.Started)
                } else {
                    _events.emit(ServiceEvent.Error("Service failed to start"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start service", e)
                _events.emit(ServiceEvent.Error("Failed to start service: ${e.localizedMessage}"))
                _serviceStatus.value = ServiceStatus.STOPPED
            }
        }
    }

    fun stopService(context: Context) {
        viewModelScope.launch {
            try {
                val serviceIntent = Intent(context, WisdomDisplayService::class.java).apply {
                    action = WisdomDisplayService.ACTION_STOP_SERVICE
                }
                context.startService(serviceIntent)

                // Give service time to stop
                delay(500)
                checkServiceStatus()

                if (_serviceStatus.value == ServiceStatus.STOPPED) {
                    _events.emit(ServiceEvent.Stopped)
                } else {
                    _events.emit(ServiceEvent.Error("Service failed to stop"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop service", e)
                _events.emit(ServiceEvent.Error("Failed to stop service: ${e.localizedMessage}"))
            }
        }
    }
}