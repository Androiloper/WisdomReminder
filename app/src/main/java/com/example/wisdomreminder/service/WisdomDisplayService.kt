package com.example.wisdomreminder.service

import android.animation.ValueAnimator
import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WisdomDisplayService : Service() {

    companion object {
        const val CHANNEL_ID = "wisdom_channel"
        const val NOTIFICATION_ID = 1
        const val TAG = "WisdomDisplayService"

        // Action and extras
        const val ACTION_START_SERVICE = "com.example.wisdomreminder.START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.example.wisdomreminder.STOP_SERVICE"
        const val ACTION_DISPLAY_WISDOM = "com.example.wisdomreminder.DISPLAY_WISDOM"
        const val EXTRA_WISDOM_ID = "wisdom_id"

        // Flag to track if service is running
        @Volatile
        var isServiceRunning = false
    }

    @Inject
    lateinit var wisdomRepository: WisdomRepository

    private lateinit var windowManager: WindowManager
    private var wisdomView: View? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val screenReceiver = ScreenReceiver()
    private var lastDisplayTime = 0L // Track when the last wisdom was shown

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
            registerReceiver(screenReceiver, filter, RECEIVER_NOT_EXPORTED)

            isServiceRunning = true
        } catch (e: Exception) {
            Log.e(TAG, "Service initialization failed: ${e.message}", e)
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand: ${intent?.action}")

        when (intent?.action) {
            ACTION_START_SERVICE -> {
                Log.d(TAG, "Received START_SERVICE action")
            }
            ACTION_STOP_SERVICE -> {
                Log.d(TAG, "Received STOP_SERVICE action, stopping service")
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_DISPLAY_WISDOM -> {
                // Display wisdom
                val wisdomId = intent.getLongExtra(EXTRA_WISDOM_ID, -1L)
                if (wisdomId != -1L) {
                    serviceScope.launch {
                        displayWisdomById(wisdomId)
                    }
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy")
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver: ${e.message}")
        }
        hideWisdom()
        serviceScope.cancel()
        isServiceRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Wisdom Reminders",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
                description = "Shows wisdom reminders throughout the day"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): android.app.Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
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
        if (!Settings.canDrawOverlays(this)) {
            Log.e(TAG, "Cannot show wisdom: no overlay permission")
            return
        }

        // Prevent showing wisdom too frequently
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastDisplayTime < 2000) {
            Log.d(TAG, "Preventing wisdom display - too soon after last display")
            return
        }

        lastDisplayTime = currentTime

        // Get the wisdom by ID
        val wisdom = wisdomRepository.getWisdomById(wisdomId)
        if (wisdom != null) {
            // Record exposure
            wisdomRepository.recordExposure(wisdomId)

            // Display the wisdom
            displayWisdom(wisdom.text, wisdom.source)
        } else {
            Log.e(TAG, "Wisdom not found: $wisdomId")
        }
    }

    private fun displayWisdom(text: String, source: String) {
        try {
            // Remove any existing view first
            if (wisdomView != null) {
                try {
                    windowManager.removeView(wisdomView)
                } catch (e: Exception) {
                    Log.e(TAG, "Error removing existing view: ${e.message}")
                }
                wisdomView = null
            }

            val inflater = LayoutInflater.from(this)
            wisdomView = inflater.inflate(R.layout.wisdom_overlay, null)

            val wisdomText = wisdomView?.findViewById<TextView>(R.id.wisdom_text)
            val wisdomSource = wisdomView?.findViewById<TextView>(R.id.wisdom_source)

            wisdomText?.text = text
            wisdomSource?.text = source

            // Set up window parameters
            val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }

            var flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
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
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                windowType,
                flags,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
                flags = flags or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }

            Log.d(TAG, "Adding wisdom view to window")
            windowManager.addView(wisdomView, params)

            // Add animation effect
            val textView = wisdomText
            val valueAnimator = ValueAnimator.ofFloat(0.8f, 1.0f)
            valueAnimator.duration = 2000
            valueAnimator.repeatCount = ValueAnimator.INFINITE
            valueAnimator.repeatMode = ValueAnimator.REVERSE
            valueAnimator.addUpdateListener { animator ->
                val value = animator.animatedValue as Float
                textView?.alpha = value
            }
            valueAnimator.start()

            // Auto-hide after 20 seconds
            serviceScope.launch {
                delay(20000)
                valueAnimator.cancel()
                hideWisdom()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add wisdom view: ${e.message}", e)
            e.printStackTrace()
        }
    }

    private fun hideWisdom() {
        try {
            wisdomView?.let { view ->
                mainHandler.post {
                    try {
                        windowManager.removeView(view)
                        Log.d(TAG, "Wisdom view removed")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error removing wisdom view: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing wisdom view: ${e.message}")
        }
        wisdomView = null
    }

    private fun isScreenOn(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            powerManager.isInteractive
        } else {
            @Suppress("DEPRECATION")
            (getSystemService(Context.POWER_SERVICE) as android.os.PowerManager).isScreenOn
        }
    }

    private fun isDeviceUnlocked(): Boolean {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return !keyguardManager.isKeyguardLocked
    }

    inner class ScreenReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            Log.d(TAG, "Screen event: $action")

            when (action) {
                Intent.ACTION_SCREEN_ON -> {
                    // When screen turns on, schedule a wisdom display
                    serviceScope.launch {
                        delay(1000) // Small delay to ensure screen is fully on
                        showRandomWisdom()
                    }
                }
                Intent.ACTION_USER_PRESENT -> {
                    // When device is unlocked, schedule a wisdom display
                    serviceScope.launch {
                        delay(1500) // Slightly longer delay for unlock
                        showRandomWisdom()
                    }
                }
                Intent.ACTION_SCREEN_OFF -> {
                    hideWisdom()
                }
                ACTION_STOP_SERVICE -> {
                    stopSelf()
                }
                ACTION_DISPLAY_WISDOM -> {
                    // This is handled in onStartCommand
                }
            }
        }
    }

    private suspend fun showRandomWisdom() {
        try {
            // Get all active wisdom
            val activeWisdom = wisdomRepository.getActiveWisdom().first()

            if (activeWisdom.isNotEmpty()) {
                // Choose a random wisdom
                val randomWisdom = activeWisdom.random()

                // Record exposure
                wisdomRepository.recordExposure(randomWisdom.id)

                // Display it
                displayWisdom(randomWisdom.text, randomWisdom.source)
            } else {
                Log.d(TAG, "No active wisdom to display")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing random wisdom: ${e.message}", e)
        }
    }
}