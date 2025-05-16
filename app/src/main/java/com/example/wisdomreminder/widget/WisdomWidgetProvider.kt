package com.example.wisdomreminder.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.example.wisdomreminder.R
import com.example.wisdomreminder.data.db.WisdomReminderDatabase
import com.example.wisdomreminder.data.repository.WisdomRepository
import com.example.wisdomreminder.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Implementation of App Widget functionality.
 */
@AndroidEntryPoint
class WisdomWidgetProvider : AppWidgetProvider() {

    private val TAG = "WisdomWidgetProvider"
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Inject
    lateinit var wisdomRepository: WisdomRepository

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate: Updating ${appWidgetIds.size} widgets")

        // Launch a coroutine to update each widget
        coroutineScope.launch {
            // Get the active wisdom
            val activeWisdomList = wisdomRepository.getActiveWisdom().first()

            if (activeWisdomList.isNotEmpty()) {
                // Get the first active wisdom
                val activeWisdom = activeWisdomList.first()

                // Update all widgets
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId, activeWisdom.text,
                        activeWisdom.source, activeWisdom.currentDay)
                }
            } else {
                // No active wisdom, show empty state
                for (appWidgetId in appWidgetIds) {
                    updateAppWidgetEmpty(context, appWidgetManager, appWidgetId)
                }
            }
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
        Log.d(TAG, "onEnabled: First widget created")
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
        Log.d(TAG, "onDisabled: Last widget removed")
    }

    companion object {
        /**
         * Update a single app widget with active wisdom
         */
        @SuppressLint("RemoteViewLayout")
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            wisdomText: String,
            wisdomSource: String,
            currentDay: Int
        ) {
            Log.d("WisdomWidget", "Updating widget with wisdom: $wisdomText")

            // Create an Intent to launch MainActivity when widget is clicked
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Get the layout for the widget
            val views = RemoteViews(context.packageName, R.layout.widget_wisdom)

            // Set widget content
            views.setTextViewText(R.id.widget_wisdom_text, "\"$wisdomText\"")
            views.setTextViewText(R.id.widget_source, wisdomSource)
            views.setTextViewText(R.id.widget_day_counter, "DAY $currentDay/21")

            // Make the day counter visible
            views.setViewVisibility(R.id.widget_day_counter, View.VISIBLE)

            // Set the click intent
            views.setOnClickPendingIntent(R.id.widget_wisdom_text, pendingIntent)

            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        /**
         * Update widget to show empty state when no active wisdom
         */
        @SuppressLint("RemoteViewLayout")
        internal fun updateAppWidgetEmpty(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            // Create an Intent to launch MainActivity when widget is clicked
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Get the layout for the widget
            val views = RemoteViews(context.packageName, R.layout.widget_wisdom)

            // Set widget content for empty state
            views.setTextViewText(R.id.widget_wisdom_text, context.getString(R.string.widget_empty))
            views.setTextViewText(R.id.widget_source, "")

            // Hide the day counter
            views.setViewVisibility(R.id.widget_day_counter, View.GONE)

            // Set the click intent
            views.setOnClickPendingIntent(R.id.widget_wisdom_text, pendingIntent)

            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}