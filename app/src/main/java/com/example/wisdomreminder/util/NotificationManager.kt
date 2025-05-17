package com.example.wisdomreminder.util

import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.preference.PreferenceManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.wisdomreminder.R
import com.example.wisdomreminder.data.repository.WisdomRepository
import com.example.wisdomreminder.model.Wisdom
import com.example.wisdomreminder.receiver.NotificationReceiver
import com.example.wisdomreminder.ui.main.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wisdomRepository: WisdomRepository
) {
    companion object {
        const val CHANNEL_ID_REMINDERS = "wisdom_reminders"
        const val CHANNEL_ID_ALARMS = "wisdom_alarms"
        private const val NOTIFICATION_ID_BASE = 1000
        private const val REQUEST_CODE_BASE = 2000
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    // Set up notification channels for Android O and above
    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Regular reminders channel (lower priority)
            val reminderChannel = NotificationChannel(
                CHANNEL_ID_REMINDERS,
                "Wisdom Reminders",
                android.app.NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Regular wisdom reminders throughout the day"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(false)
                setShowBadge(false)
            }

            // Alarm notifications channel (higher priority)
            val alarmChannel = NotificationChannel(
                CHANNEL_ID_ALARMS,
                "Wisdom Alarms",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Scheduled wisdom alarms at specific times"
                enableLights(true)
                lightColor = Color.MAGENTA
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
                setShowBadge(true)
            }

            // Register the channels
            val notificationManager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as android.app.NotificationManager

            notificationManager.createNotificationChannels(listOf(reminderChannel, alarmChannel))
        }
    }

    /**
     * Show a standard wisdom reminder notification
     */
    suspend fun showWisdomNotification(wisdom: Wisdom) {
        if (!prefs.getBoolean("notifications_enabled", true)) {
            Timber.d("Notifications disabled, skipping")
            return
        }

        // Create notification intent
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("wisdom_id", wisdom.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_BASE + wisdom.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Mark as read action
        val markReadIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.example.wisdomreminder.MARK_READ"
            putExtra("wisdom_id", wisdom.id)
        }

        val markReadPendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_BASE + wisdom.id.toInt() + 100,
            markReadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
            .setSmallIcon(R.drawable.ic_wisdom)
            .setContentTitle("Wisdom Reminder")
            .setContentText(wisdom.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(wisdom.text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(context, R.color.nebula_purple))
            .addAction(
                R.drawable.ic_check,
                "Mark as Read",
                markReadPendingIntent
            )

        if (wisdom.source.isNotBlank()) {
            builder.setSubText(wisdom.source)
        }

        // Show the notification
        val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as android.app.NotificationManager

        notificationManager.notify(
            NOTIFICATION_ID_BASE + wisdom.id.toInt(),
            builder.build()
        )

        // Record the exposure
        wisdomRepository.recordExposure(wisdom.id)
    }

    /**
     * Show a high-priority alarm notification
     */
    suspend fun showAlarmNotification(wisdom: Wisdom, alarmTime: String) {
        if (!prefs.getBoolean("alarms_enabled", true)) {
            Timber.d("Alarms disabled, skipping")
            return
        }

        // Intent for opening the app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("wisdom_id", wisdom.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_BASE + wisdom.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze action
        val snoozeIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.example.wisdomreminder.SNOOZE_ALARM"
            putExtra("wisdom_id", wisdom.id)
        }

        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_BASE + wisdom.id.toInt() + 200,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create and show notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_ALARMS)
            .setSmallIcon(R.drawable.ic_wisdom)
            .setContentTitle("$alarmTime Wisdom Reflection")
            .setContentText(wisdom.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(wisdom.text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(context, R.color.neon_pink))
            .addAction(
                R.drawable.ic_snooze,
                "Snooze (15 min)",
                snoozePendingIntent
            )
            .setFullScreenIntent(pendingIntent, true)

        if (wisdom.source.isNotBlank()) {
            builder.setSubText(wisdom.source)
        }

        // Use a different random ID to avoid overriding regular notifications
        val notificationId = NOTIFICATION_ID_BASE + wisdom.id.toInt() + 500

        val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as android.app.NotificationManager

        notificationManager.notify(notificationId, builder.build())

        // Record the exposure
        wisdomRepository.recordExposure(wisdom.id)
    }
}