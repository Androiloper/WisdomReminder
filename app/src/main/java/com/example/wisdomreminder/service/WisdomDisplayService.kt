package com.example.wisdomreminder.service

import android.animation.ValueAnimator
import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager as SystemNotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences // Added for preferences
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.wisdomreminder.R
import com.example.wisdomreminder.data.repository.WisdomRepository // Should be IWisdomRepository if using interface
import com.example.wisdomreminder.model.Wisdom // Ensure Wisdom model is imported
import com.example.wisdomreminder.ui.main.MainActivity
import com.example.wisdomreminder.ui.main.MainViewModel
import com.example.wisdomreminder.ui.settings.UnlockScreenDisplayMode // Import the enum
// Import UnlockDisplayOrder if you use it from settings, not shown in this snippet but was in conceptual.
// import com.example.wisdomreminder.ui.settings.UnlockDisplayOrder
import com.example.wisdomreminder.util.SwipeDismissTouchListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@AndroidEntryPoint
class WisdomDisplayService : Service() {

    companion object {
        const val CHANNEL_ID = "wisdom_channel"
        const val NOTIFICATION_ID = 1
        const val TAG = "WisdomDisplayService"
        const val ACTION_START_SERVICE = "com.example.wisdomreminder.START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.example.wisdomreminder.STOP_SERVICE"
        const val ACTION_DISPLAY_WISDOM = "com.example.wisdomreminder.DISPLAY_WISDOM"
        const val EXTRA_WISDOM_ID = "wisdom_id"

        private val _isRunning = AtomicBoolean(false)
        val isServiceRunning: Boolean
            get() = _isRunning.get()
    }

    @Inject
    lateinit var wisdomRepository: WisdomRepository // Assuming direct injection of implementation for now

    @Inject
    lateinit var prefs: SharedPreferences

    private lateinit var windowManager: WindowManager
    private var wisdomView: View? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val screenReceiver = ScreenReceiver()
    private var lastDisplayTime = 0L
    private val displayCooldownMillis = 10000L
    private var animator: ValueAnimator? = null
    private var monitorJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        createNotificationChannel()
        try {
            startForeground(NOTIFICATION_ID, createNotification())
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
                addAction(ACTION_STOP_SERVICE)
                addAction(ACTION_DISPLAY_WISDOM)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(screenReceiver, filter, RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                registerReceiver(screenReceiver, filter)
            }

            _isRunning.set(true)
            notifyServiceStatus(true)
            startMonitorJob()
            Log.d(TAG, "WisdomDisplayService successfully started")
        } catch (e: Exception) {
            Log.e(TAG, "Service initialization failed: ${e.message}", e)
            _isRunning.set(false)
            notifyServiceStatus(false)
            stopSelf()
        }
    }

    private fun startMonitorJob() {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            try {
                while(true) {
                    notifyServiceStatus(true)
                    delay(30000)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in monitor job", e)
            }
        }
    }

    private fun notifyServiceStatus(isRunning: Boolean) {
        try {
            val intent = Intent(MainViewModel.ACTION_SERVICE_STATUS_CHANGE).apply {
                putExtra(MainViewModel.EXTRA_IS_RUNNING, isRunning)
            }
            sendBroadcast(intent)
            Log.d(TAG, "Service status broadcast sent: running=$isRunning")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send service status broadcast", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand: ${intent?.action}")
        when (intent?.action) {
            ACTION_START_SERVICE -> {
                Log.d(TAG, "Received START_SERVICE action")
                _isRunning.set(true)
                notifyServiceStatus(true)
            }
            ACTION_STOP_SERVICE -> {
                Log.d(TAG, "Received STOP_SERVICE action, stopping service")
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_DISPLAY_WISDOM -> {
                val wisdomId = intent.getLongExtra(EXTRA_WISDOM_ID, -1L)
                if (wisdomId != -1L) {
                    serviceScope.launch { displayWisdomById(wisdomId) }
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "Service onDestroy")
        monitorJob?.cancel()
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver: ${e.message}")
        }
        hideWisdom()
        serviceScope.cancel()
        _isRunning.set(false)
        notifyServiceStatus(false)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Wisdom Reminders", SystemNotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
                description = "Shows wisdom reminders throughout the day"
            }
            val notificationManager = getSystemService(SystemNotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): android.app.Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.service_notification_title))
            .setContentText(getString(R.string.service_notification_text))
            .setSmallIcon(R.drawable.ic_wisdom)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()
    }

    private suspend fun displayWisdomById(wisdomId: Long) {
        if (!canDisplayOverlay()) return

        wisdomRepository.getWisdomById(wisdomId).onSuccess { wisdom ->
            if (wisdom != null) {
                wisdomRepository.recordExposure(wisdomId)
                displayWisdomOnScreen(wisdom.text, wisdom.source)
            } else Log.e(TAG, "Wisdom not found: $wisdomId")
        }.onFailure { error -> Log.e(TAG, "Error retrieving wisdom: $wisdomId", error) }
    }

    private suspend fun showWisdomForUnlock() {
        if (!canDisplayOverlay()) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastDisplayTime < displayCooldownMillis) {
            Log.d(TAG, "Preventing wisdom display - cooldown active")
            return
        }
        lastDisplayTime = currentTime

        val displayModeName = prefs.getString("unlock_display_mode", UnlockScreenDisplayMode.ACTIVE_WISDOM.name) // Using the correct enum from settings
        val displayMode = UnlockScreenDisplayMode.valueOf(displayModeName ?: UnlockScreenDisplayMode.ACTIVE_WISDOM.name)

        // val orderPreference = prefs.getString("unlock_display_order", UnlockDisplayOrder.LINEAR.name) // If you implement this
        // val displayOrder = UnlockDisplayOrder.valueOf(orderPreference ?: UnlockDisplayOrder.LINEAR.name)

        try {
            var wisdomToShow: Wisdom? = null
            when (displayMode) {
                UnlockScreenDisplayMode.ACTIVE_WISDOM -> {
                    val activeWisdom = wisdomRepository.getActiveWisdom().first()
                    if (activeWisdom.isNotEmpty()) {
                        // Simple random for now, could be minBy exposuresToday
                        wisdomToShow = activeWisdom.random()
                    }
                }
                UnlockScreenDisplayMode.QUEUED_PLAYLIST -> { // This now refers to settings for sub-playlist
                    // TODO: Implement logic based on a more specific playlist type if chosen in settings
                    // For now, using favorites as a placeholder for "QUEUED_PLAYLIST"
                    val favoriteQueued = wisdomRepository.getFavoriteDisplayableWisdom().first() // Corrected method name
                    if (favoriteQueued.isNotEmpty()) {
                        wisdomToShow = favoriteQueued.random() // Simple random favorite
                    } else { // Fallback if no favorites
                        val activeWisdom = wisdomRepository.getActiveWisdom().first()
                        if (activeWisdom.isNotEmpty()) {
                            wisdomToShow = activeWisdom.random()
                        }
                    }
                    Log.d(TAG, "Display mode: Queued Playlist (using Favorites as placeholder). Item: ${wisdomToShow?.text?.take(20)}")
                }
                // Add cases for SEVEN_WISDOM_PLAYLIST, RANDOM_FROM_ALL_QUEUED if those are distinct settings
            }

            if (wisdomToShow != null) {
                wisdomRepository.recordExposure(wisdomToShow.id)
                displayWisdomOnScreen(wisdomToShow.text, wisdomToShow.source)
            } else {
                Log.d(TAG, "No wisdom found for display mode: $displayMode")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error showing wisdom for unlock: ${e.message}", e)
        }
    }


    private fun canDisplayOverlay(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Log.e(TAG, "Cannot show wisdom: no overlay permission")
                return false
            }
        }
        return true
    }


    private fun displayWisdomOnScreen(text: String, source: String) {
        mainHandler.post {
            try {
                animator?.cancel()

                if (wisdomView?.windowToken != null) { // Check if view is attached
                    try {
                        windowManager.removeView(wisdomView)
                    } catch (e: Exception) { Log.e(TAG, "Error removing existing view: ${e.message}") }
                }
                wisdomView = null // Clear reference

                val inflater = LayoutInflater.from(this)
                wisdomView = inflater.inflate(R.layout.wisdom_overlay, null)

                wisdomView?.findViewById<TextView>(R.id.wisdom_text)?.text = text
                wisdomView?.findViewById<TextView>(R.id.wisdom_source)?.text = source

                val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                }

                val resolvedFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                } else {
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                }


                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    windowType, resolvedFlags, PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.CENTER
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    }
                }

                wisdomView?.setOnTouchListener(
                    SwipeDismissTouchListener(
                        wisdomView!!,
                        object : SwipeDismissTouchListener.DismissCallbacks {
                            override fun onDismiss(view: View, direction: Int) {
                                hideWisdom()
                            }
                        }
                    )
                )

                Log.d(TAG, "Adding wisdom view to window")
                windowManager.addView(wisdomView, params)

                val textView = wisdomView?.findViewById<TextView>(R.id.wisdom_text)
                animator = ValueAnimator.ofFloat(0.8f, 1.0f).apply {
                    duration = 2000
                    repeatCount = ValueAnimator.INFINITE
                    repeatMode = ValueAnimator.REVERSE
                    addUpdateListener { animation -> textView?.alpha = animation.animatedValue as Float }
                    start()
                }

                serviceScope.launch {
                    delay(20000)
                    hideWisdom()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to add wisdom view: ${e.message}", e)
            }
        }
    }

    private fun hideWisdom() {
        mainHandler.post {
            animator?.cancel()
            animator = null
            wisdomView?.let { view ->
                try {
                    if (view.windowToken != null) { // Check if view is attached before removing
                        windowManager.removeView(view)
                        Log.d(TAG, "Wisdom view removed")
                    } else {
                        Log.d(TAG, "Wisdom view hideWisdom - was not attached or already removed.")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error removing wisdom view: ${e.message}")
                }
            }
            wisdomView = null
        }
    }

    private fun isScreenOnAndDeviceUnlocked(): Boolean {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val isScreenOn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            powerManager.isInteractive
        } else {
            @Suppress("DEPRECATION")
            powerManager.isScreenOn
        }
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val isDeviceLocked = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyguardManager.isDeviceLocked
        } else {
            @Suppress("DEPRECATION")
            keyguardManager.isKeyguardLocked
        }
        return isScreenOn && !isDeviceLocked
    }

    inner class ScreenReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            Log.d(TAG, "Screen event: $action")

            when (action) {
                Intent.ACTION_SCREEN_ON -> {
                    if (isScreenOnAndDeviceUnlocked()) {
                        serviceScope.launch {
                            delay(500L)
                            showWisdomForUnlock()
                        }
                    }
                }
                Intent.ACTION_USER_PRESENT -> {
                    if (isScreenOnAndDeviceUnlocked()) {
                        serviceScope.launch {
                            delay(1500L)
                            showWisdomForUnlock()
                        }
                    }
                }
                Intent.ACTION_SCREEN_OFF -> {
                    hideWisdom()
                }
                ACTION_STOP_SERVICE -> stopSelf()
            }
        }
    }
}