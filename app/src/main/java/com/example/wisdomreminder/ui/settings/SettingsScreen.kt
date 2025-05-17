package com.example.wisdomreminder.ui.settings

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.wisdomreminder.R
import com.example.wisdomreminder.ui.components.GlassCard
import com.example.wisdomreminder.ui.main.MainViewModel
import com.example.wisdomreminder.ui.theme.*

// Non-composable function to check alarm permission
@androidx.annotation.RequiresApi(Build.VERSION_CODES.S)
private fun checkExactAlarmPermission(context: Context): Boolean {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    return !alarmManager.canScheduleExactAlarms()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,

   // viewModel: SettingsViewModel = hiltViewModel()

) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()


    // Settings state
    var notificationsEnabled by remember { mutableStateOf(true) }
    var alarmsEnabled by remember { mutableStateOf(true) }
    var morningAlarmEnabled by remember { mutableStateOf(true) }
    var morningAlarmTime by remember { mutableStateOf("08:00") }
    var eveningAlarmEnabled by remember { mutableStateOf(true) }
    var eveningAlarmTime by remember { mutableStateOf("20:00") }

    var showTimePickerDialog by remember { mutableStateOf(false) }
    var timePickerMode by remember { mutableStateOf("morning") }
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Handler for alarm toggle with permission check
    val handleAlarmToggle: (Boolean) -> Unit = { isEnabled ->
        alarmsEnabled = isEnabled
        if (isEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkExactAlarmPermission(context)) {
                showPermissionDialog = true
            }
        }
    }

    // Background with cosmic theme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace)
            .drawBehind {
                // Create a gradient background with stars
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            CosmicBlack,
                            DeepSpace
                        )
                    )
                )

                // Small cosmic particles
                for (i in 0..100) {
                    val x = (Math.random() * size.width).toFloat()
                    val y = (Math.random() * size.height).toFloat()
                    val radius = (Math.random() * 2f + 0.5f).toFloat()
                    val alpha = (Math.random() * 0.8f + 0.2f).toFloat()

                    drawCircle(
                        color = StarWhite.copy(alpha = alpha),
                        radius = radius,
                        center = Offset(x, y)
                    )
                }
            }
    ) {
        // Nebula effect in the background
        Image(
            painter = painterResource(id = R.drawable.ic_wisdom),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.1f)
                .blur(20.dp)
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentColor = StarWhite,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "SETTINGS",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = StarWhite
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = GlassSurface.copy(alpha = 0.5f),
                        titleContentColor = StarWhite,
                        navigationIconContentColor = StarWhite
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Notification Settings Section
                SectionHeader(title = "NOTIFICATION SETTINGS")

                // Notification Settings Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Enable Notifications
                        FuturisticToggle(
                            text = "Show Wisdom Notifications",
                            isChecked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                            icon = Icons.Rounded.Notifications
                        )

                        if (notificationsEnabled) {
                            Text(
                                text = "Regular notifications will show wisdom throughout the day",
                                style = MaterialTheme.typography.bodySmall,
                                color = StarWhite.copy(alpha = 0.7f),
                                modifier = Modifier.padding(start = 40.dp)
                            )
                        }
                    }
                }

                // Alarm Settings Section
                SectionHeader(title = "DAILY REMINDERS")

                // Alarm Settings Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Enable Alarms - Using the handler function
                        FuturisticToggle(
                            text = "Daily Wisdom Alarms",
                            isChecked = alarmsEnabled,
                            onCheckedChange = handleAlarmToggle,
                            icon = Icons.Rounded.Face // Using a generic alarm icon
                        )

                        if (alarmsEnabled) {
                            Divider(
                                color = GlassSurfaceLight,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            // Morning Alarm Toggle and Time
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = morningAlarmEnabled,
                                            onCheckedChange = {
                                                morningAlarmEnabled = it
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = NebulaPurple,
                                                uncheckedColor = StarWhite.copy(alpha = 0.6f)
                                            )
                                        )

                                        Text(
                                            text = "Morning Reflection",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = StarWhite
                                        )
                                    }

                                    if (morningAlarmEnabled) {
                                        Text(
                                            text = "Daily at $morningAlarmTime",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ElectricGreen,
                                            modifier = Modifier.padding(start = 52.dp)
                                        )
                                    }
                                }

                                if (morningAlarmEnabled) {
                                    Button(
                                        onClick = {
                                            timePickerMode = "morning"
                                            showTimePickerDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ElectricGreen.copy(alpha = 0.2f),
                                            contentColor = ElectricGreen
                                        )
                                    ) {
                                        Text("SET TIME")
                                    }
                                }
                            }

                            // Evening Alarm Toggle and Time
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = eveningAlarmEnabled,
                                            onCheckedChange = {
                                                eveningAlarmEnabled = it
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = NebulaPurple,
                                                uncheckedColor = StarWhite.copy(alpha = 0.6f)
                                            )
                                        )

                                        Text(
                                            text = "Evening Reflection",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = StarWhite
                                        )
                                    }

                                    if (eveningAlarmEnabled) {
                                        Text(
                                            text = "Daily at $eveningAlarmTime",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ElectricGreen,
                                            modifier = Modifier.padding(start = 52.dp)
                                        )
                                    }
                                }

                                if (eveningAlarmEnabled) {
                                    Button(
                                        onClick = {
                                            timePickerMode = "evening"
                                            showTimePickerDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ElectricGreen.copy(alpha = 0.2f),
                                            contentColor = ElectricGreen
                                        )
                                    ) {
                                        Text("SET TIME")
                                    }
                                }
                            }

                            Text(
                                text = "High-priority notification alarms at set times for daily reflection",
                                style = MaterialTheme.typography.bodySmall,
                                color = StarWhite.copy(alpha = 0.7f),
                                modifier = Modifier.padding(start = 12.dp, top = 8.dp)
                            )
                        }
                    }
                }

                // About Section
                SectionHeader(title = "ABOUT")

                // About Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Wisdom Reminder v1.0",
                            style = MaterialTheme.typography.titleMedium,
                            color = ElectricGreen
                        )

                        Text(
                            text = "The 21/21 Rule",
                            style = MaterialTheme.typography.titleSmall,
                            color = StarWhite
                        )

                        Text(
                            text = "Internalizing wisdom through repetition: expose yourself to the same piece of wisdom 21 times over 21 days to make it a part of your thinking.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StarWhite.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { /* Open About Dialog */ },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberBlue
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("MORE INFO")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Time picker dialog
        if (showTimePickerDialog) {
            val initialTime = if (timePickerMode == "morning") {
                parseTimeString(morningAlarmTime)
            } else {
                parseTimeString(eveningAlarmTime)
            }

            TimePickerDialog(
                initialHour = initialTime.first,
                initialMinute = initialTime.second,
                title = if (timePickerMode == "morning") "Set Morning Time" else "Set Evening Time",
                onDismiss = { showTimePickerDialog = false },
                onTimeSelected = { hour, minute ->
                    val formattedTime = formatTimeString(hour, minute)
                    if (timePickerMode == "morning") {
                        morningAlarmTime = formattedTime
                    } else {
                        eveningAlarmTime = formattedTime
                    }
                    showTimePickerDialog = false

                    // You would update these in your ViewModel/preferences
                    Toast.makeText(context, "Time set to $formattedTime", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Permission dialog
        if (showPermissionDialog && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlertDialog(
                onDismissRequest = { showPermissionDialog = false },
                title = { Text("Permission Required") },
                text = {
                    Text("This app needs exact alarm permission to schedule wisdom reminders at specific times. Please grant this permission in the next screen.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showPermissionDialog = false
                            // Direct user to exact alarm settings
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                            intent.data = Uri.parse("package:${context.packageName}")
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NebulaPurple
                        )
                    ) {
                        Text("Open Settings")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionDialog = false }) {
                        Text("Later")
                    }
                },
                containerColor = GlassSurfaceDark,
                titleContentColor = ElectricGreen,
                textContentColor = StarWhite
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = NeonPink,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun FuturisticToggle(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val scale by animateFloatAsState(
        targetValue = if (isChecked) 1.03f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "toggle animation"
    )

    Surface(
        modifier = Modifier
            .scale(scale)
            .fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(GlassSurface.copy(alpha = if (isChecked) 0.7f else 0.4f))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with glow effect
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                (if (isChecked) ElectricGreen else Color.Gray).copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isChecked) ElectricGreen else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Toggle text
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                color = StarWhite
            )

            // Custom switch
            Switch(
                checked = isChecked,
                onCheckedChange = { onCheckedChange(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NebulaPurple,
                    checkedTrackColor = ElectricGreen.copy(alpha = 0.3f),
                    checkedBorderColor = ElectricGreen,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.DarkGray.copy(alpha = 0.3f),
                    uncheckedBorderColor = Color.Gray
                )
            )
        }
    }
}

@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    title: String,
    onDismiss: () -> Unit,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    var hour by remember { mutableStateOf(initialHour) }
    var minute by remember { mutableStateOf(initialMinute) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = GlassSurfaceDark,
                contentColor = StarWhite
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = ElectricGreen,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Time selection with NumberPickers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour picker
                    NumberPicker(
                        value = hour,
                        onValueChange = { hour = it },
                        range = 0..23,
                        format = { value -> value.toString().padStart(2, '0') }
                    )

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineLarge,
                        color = ElectricGreen,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Minute picker
                    NumberPicker(
                        value = minute,
                        onValueChange = { minute = it },
                        range = 0..59,
                        step = 5,
                        format = { value -> value.toString().padStart(2, '0') }
                    )
                }

                // Format display
                Text(
                    text = formatTimeDisplay(hour, minute),
                    style = MaterialTheme.typography.bodyMedium,
                    color = StarWhite.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = StarWhite.copy(alpha = 0.7f)
                        )
                    ) {
                        Text("CANCEL")
                    }

                    Button(
                        onClick = { onTimeSelected(hour, minute) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NebulaPurple
                        ),
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Text("SET")
                    }
                }
            }
        }
    }
}

@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    step: Int = 1,
    format: (Int) -> String = { it.toString() }
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = {
                val newValue = if (value + step <= range.last) value + step else range.first
                onValueChange(newValue)
            },
            modifier = Modifier
                .clip(CircleShape)
                .background(NebulaPurple.copy(alpha = 0.2f))
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Increase",
                tint = NebulaPurple
            )
        }

        Text(
            text = format(value),
            style = MaterialTheme.typography.headlineMedium,
            color = StarWhite,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        IconButton(
            onClick = {
                val newValue = if (value - step >= range.first) value - step else range.last
                onValueChange(newValue)
            },
            modifier = Modifier
                .clip(CircleShape)
                .background(NebulaPurple.copy(alpha = 0.2f))
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Decrease",
                tint = NebulaPurple
            )
        }
    }
}

// Helper functions
private fun parseTimeString(timeString: String): Pair<Int, Int> {
    return try {
        val parts = timeString.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        Pair(hour, minute)
    } catch (e: Exception) {
        Pair(8, 0) // Default to 8:00 AM
    }
}

private fun formatTimeString(hour: Int, minute: Int): String {
    return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}

private fun formatTimeDisplay(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "$displayHour:${minute.toString().padStart(2, '0')} $amPm"
}