package com.example.wisdomreminder.service

import android.animation.ValueAnimator
import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager as SystemNotificationManager // aliased
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager // added
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.wisdomreminder.R
import com.example.wisdomreminder.data.repository.WisdomRepository
import com.example.wisdomreminder.ui.main.MainActivity
import com.example.wisdomreminder.ui.main.MainViewModel
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

        // Service status tracking with AtomicBoolean for thread safety
        private val _isRunning = AtomicBoolean(false)
        val isServiceRunning: Boolean
            get() = _isRunning.get()
    }

    @Inject
    lateinit var wisdomRepository: WisdomRepository

    private lateinit var windowManager: WindowManager
    private var wisdomView: View? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val screenReceiver = ScreenReceiver()
    private var lastDisplayTime = 0L
    private val displayCooldownMillis = 10000L // Cooldown of 10 seconds for random display
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
                addAction(Intent.ACTION_USER_PRESENT) // Device unlocked
                addAction(ACTION_STOP_SERVICE)
                addAction(ACTION_DISPLAY_WISDOM)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(screenReceiver, filter, RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                registerReceiver(screenReceiver, filter)
            }

            // Set service as running and notify
            _isRunning.set(true)
            notifyServiceStatus(true)

            // Start monitoring job to periodically update status
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
                    delay(30000) // Send status update every 30 seconds
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
                // Set status and notify
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

        // Cancel all jobs first
        monitorJob?.cancel()

        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver: ${e.message}")
        }

        hideWisdom() // Ensure animator is cancelled
        serviceScope.cancel()

        // Set service as not running and notify
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
                wisdomRepository.recordExposure(wisdomId) // Record exposure first
                displayWisdomOnScreen(wisdom.text, wisdom.source)
            } else Log.e(TAG, "Wisdom not found: $wisdomId")
        }.onFailure { error -> Log.e(TAG, "Error retrieving wisdom: $wisdomId", error) }
    }

    private suspend fun showRandomWisdom() {
        if (!canDisplayOverlay()) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastDisplayTime < displayCooldownMillis) {
            Log.d(TAG, "Preventing wisdom display - cooldown active")
            return
        }
        lastDisplayTime = currentTime

        try {
            val activeWisdom = wisdomRepository.getActiveWisdom().first()
            if (activeWisdom.isNotEmpty()) {
                val randomWisdom = activeWisdom.minByOrNull { it.exposuresToday } ?: activeWisdom.random()
                wisdomRepository.recordExposure(randomWisdom.id)
                displayWisdomOnScreen(randomWisdom.text, randomWisdom.source)
            } else Log.d(TAG, "No active wisdom to display randomly")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing random wisdom: ${e.message}", e)
        }
    }

    private fun canDisplayOverlay(): Boolean {
        if (!Settings.canDrawOverlays(this)) {
            Log.e(TAG, "Cannot show wisdom: no overlay permission")
            // Optionally, send a broadcast or event to MainActivity to request permission
            return false
        }
        return true
    }


    private fun displayWisdomOnScreen(text: String, source: String) {
        mainHandler.post {
            try {
                animator?.cancel()

                if (wisdomView != null) {
                    try { if(wisdomView?.isAttachedToWindow == true) windowManager.removeView(wisdomView) } // Check if attached
                    catch (e: Exception) { Log.e(TAG, "Error removing existing view: ${e.message}") }
                    wisdomView = null
                }

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

                // Corrected: 'if' as an expression needs an 'else' branch
                val resolvedFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                } else {
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or // Deprecated in P, but needed for older
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                }


                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    windowType, resolvedFlags, PixelFormat.TRANSLUCENT // Use resolvedFlags
                ).apply {
                    gravity = Gravity.CENTER
                    // FLAG_ALT_FOCUSABLE_IM is removed as it's often problematic with overlays
                    // and FLAG_NOT_FOCUSABLE should handle most keyboard interactions.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    }
                }

                // Corrected: Explicitly define lambda parameters for DismissCallbacks
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
                    if (view.isAttachedToWindow) {
                        windowManager.removeView(view)
                        Log.d(TAG, "Wisdom view removed")
                    } else {
                        Log.d(TAG, "Wisdom view removed on else")  // temporary fix need to be review
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
        // isDeviceLocked is more reliable than isKeyguardLocked for modern Android.
        // isKeyguardSecure tells if a PIN/Pattern/Password is set.
        // isDeviceLocked tells if the device is currently locked.
        val isDeviceLocked = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyguardManager.isDeviceLocked
        } else {
            @Suppress("DEPRECATION")
            keyguardManager.isKeyguardLocked // Fallback for older versions
        }

        return isScreenOn && !isDeviceLocked
    }

    inner class ScreenReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            Log.d(TAG, "Screen event: $action")

            when (action) {
                Intent.ACTION_SCREEN_ON -> {
                    // Wait for USER_PRESENT to confirm unlock, or if screen just turns on but was not locked.
                    if (isScreenOnAndDeviceUnlocked()) {
                        serviceScope.launch {
                            delay(500L) // Short delay
                            showRandomWisdom()
                        }
                    }
                }
                Intent.ACTION_USER_PRESENT -> {
                    if (isScreenOnAndDeviceUnlocked()) { // Double check, though USER_PRESENT implies it.
                        serviceScope.launch {
                            delay(1500L)
                            showRandomWisdom()
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