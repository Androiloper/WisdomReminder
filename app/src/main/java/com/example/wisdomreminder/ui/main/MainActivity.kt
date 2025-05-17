package com.example.wisdomreminder.ui.main

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.wisdomreminder.R
import com.example.wisdomreminder.ui.navigation.AppNavigation
import com.example.wisdomreminder.ui.theme.WisdomReminderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    private val viewModel: MainViewModel by viewModels()



    // Activity result launcher for overlay permission
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Check if permission was granted after returning
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                Log.d(TAG, "Overlay permission granted, starting service")
                viewModel.checkAndRestartService(this)
            } else {
                Log.d(TAG, "Overlay permission denied")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity onCreate")

        // Ensure permissions are checked on startup
        checkRequiredPermissions()

        setContent {
            WisdomReminderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(mainViewModel = viewModel)
                }
            }
        }

        // Monitor service status
        viewModel.checkAndRestartService(this)
    }

    override fun onResume() {
        super.onResume()
        // Check service when activity is resumed
        viewModel.checkAndRestartService(this)
    }

    private fun checkRequiredPermissions() {
        // For Android 13+ (API 33+), check notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                Log.d(TAG, "Notification permission granted: $isGranted")
            }

            // Request notification permission if needed
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // Check overlay permission
        ensureOverlayPermission()
    }

    private fun ensureOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Log.d(TAG, "Overlay permission not granted, showing dialog")
                AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage(getString(R.string.permission_overlay_required))
                    .setPositiveButton("Grant Permission") { _, _ ->
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                        overlayPermissionLauncher.launch(intent)
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } else {
                Log.d(TAG, "Overlay permission already granted")
            }
        }
    }
}