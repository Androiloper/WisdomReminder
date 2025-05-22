package com.example.wisdomreminder.ui.settings

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi // Added for clarity, though it was in your original
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Face // Changed for alarm icon
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
import com.example.wisdomreminder.ui.theme.*

@RequiresApi(Build.VERSION_CODES.S)
private fun hasExactAlarmPermission(context: Context): Boolean {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    return alarmManager.canScheduleExactAlarms()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel() // Ensure this line is exactly as here
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val alarmsEnabled by viewModel.alarmsEnabled.collectAsState()
    val morningAlarmEnabled by viewModel.morningAlarmEnabled.collectAsState()
    val morningAlarmTime by viewModel.morningAlarmTime.collectAsState()
    val eveningAlarmEnabled by viewModel.eveningAlarmEnabled.collectAsState()
    val eveningAlarmTime by viewModel.eveningAlarmTime.collectAsState()

    var showTimePickerDialog by remember { mutableStateOf(false) }
    var timePickerMode by remember { mutableStateOf("morning") }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val handleAlarmToggle: (Boolean) -> Unit = { isEnabled ->
        viewModel.setAlarmsEnabled(isEnabled)
        if (isEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasExactAlarmPermission(context)) {
                showPermissionDialog = true
            }
        }
    }

    LaunchedEffect(alarmsEnabled, context) { // Add context to LaunchedEffect key if needed for permission check
        if (alarmsEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasExactAlarmPermission(context)) {
                // Optionally re-prompt or update UI if permission is critical and not granted
                // For now, it relies on the toggle handler to show the dialog.
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace)
            .drawBehind {
                drawRect(brush = Brush.verticalGradient(colors = listOf(CosmicBlack, DeepSpace)))
                for (i in 0..100) {
                    val x = (Math.random() * size.width).toFloat()
                    val y = (Math.random() * size.height).toFloat()
                    val radius = (Math.random() * 2f + 0.5f).toFloat()
                    val alphaVal = (Math.random() * 0.8f + 0.2f).toFloat() // Renamed to avoid conflict
                    drawCircle(color = StarWhite.copy(alpha = alphaVal), radius = radius, center = Offset(x, y))
                }
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_wisdom),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.1f).blur(20.dp)
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentColor = StarWhite,
            topBar = {
                TopAppBar(
                    title = { Text("SETTINGS", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = StarWhite)
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

                SectionHeader(title = "NOTIFICATION SETTINGS")
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        FuturisticToggle(
                            text = "Show Wisdom Notifications",
                            isChecked = notificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                            icon = Icons.Rounded.Notifications
                        )
                        if (notificationsEnabled) {
                            Text(
                                "Regular notifications will show wisdom throughout the day",
                                style = MaterialTheme.typography.bodySmall,
                                color = StarWhite.copy(alpha = 0.7f),
                                modifier = Modifier.padding(start = 40.dp)
                            )
                        }
                    }
                }

                SectionHeader(title = "DAILY REMINDERS")
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        FuturisticToggle(
                            text = "Daily Wisdom Alarms",
                            isChecked = alarmsEnabled,
                            onCheckedChange = handleAlarmToggle,
                            icon = Icons.Rounded.Face
                        )
                        if (alarmsEnabled) {
                            Divider(color = GlassSurfaceLight, modifier = Modifier.padding(vertical = 8.dp))
                            AlarmTimeSetting(
                                label = "Morning Reflection",
                                isEnabled = morningAlarmEnabled,
                                time = morningAlarmTime,
                                onEnableChange = { viewModel.setMorningAlarmEnabled(it) },
                                onTimeClick = {
                                    timePickerMode = "morning"
                                    showTimePickerDialog = true
                                }
                            )
                            AlarmTimeSetting(
                                label = "Evening Reflection",
                                isEnabled = eveningAlarmEnabled,
                                time = eveningAlarmTime,
                                onEnableChange = { viewModel.setEveningAlarmEnabled(it) },
                                onTimeClick = {
                                    timePickerMode = "evening"
                                    showTimePickerDialog = true
                                }
                            )
                            Text(
                                "High-priority notification alarms at set times for daily reflection.",
                                style = MaterialTheme.typography.bodySmall,
                                color = StarWhite.copy(alpha = 0.7f),
                                modifier = Modifier.padding(start = 12.dp, top = 8.dp)
                            )
                        }
                    }
                }

                SectionHeader(title = "ABOUT")
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Wisdom Reminder v1.0", style = MaterialTheme.typography.titleMedium, color = ElectricGreen)
                        Text("The 21/21 Rule", style = MaterialTheme.typography.titleSmall, color = StarWhite)
                        Text(
                            "Internalizing wisdom through repetition: expose yourself to the same piece of wisdom 21 times over 21 days to make it a part of your thinking.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StarWhite.copy(alpha = 0.8f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { /* TODO: Open About Dialog or Info Screen */ },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberBlue)
                            ) {
                                Icon(Icons.Rounded.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("MORE INFO")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (showTimePickerDialog) {
            val initialTime = if (timePickerMode == "morning") parseTimeString(morningAlarmTime) else parseTimeString(eveningAlarmTime)
            TimePickerDialog(
                initialHour = initialTime.first,
                initialMinute = initialTime.second,
                title = if (timePickerMode == "morning") "Set Morning Time" else "Set Evening Time",
                onDismiss = { showTimePickerDialog = false },
                onTimeSelected = { hour, minute ->
                    val formattedTime = formatTimeString(hour, minute)
                    if (timePickerMode == "morning") {
                        viewModel.setMorningAlarmTime(formattedTime)
                    } else {
                        viewModel.setEveningAlarmTime(formattedTime)
                    }
                    showTimePickerDialog = false
                    Toast.makeText(context, "Time set to $formattedTime", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showPermissionDialog && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlertDialog(
                onDismissRequest = { showPermissionDialog = false },
                title = { Text("Permission Required") },
                text = { Text("This app needs exact alarm permission to schedule wisdom reminders at specific times. Please grant this permission in the next screen.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showPermissionDialog = false
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NebulaPurple)
                    ) { Text("Open Settings") }
                },
                dismissButton = { TextButton(onClick = { showPermissionDialog = false }) { Text("Later") } },
                containerColor = GlassSurfaceDark, // Ensure these colors are defined in your theme
                titleContentColor = ElectricGreen,
                textContentColor = StarWhite
            )
        }
    }
}

// SectionHeader, FuturisticToggle, AlarmTimeSetting, TimePickerDialog, NumberPicker,
// parseTimeString, formatTimeString, formatTimeDisplay functions should be here.
// (Re-adding them for completeness as they were mentioned as not repeated previously)

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = NeonPink, // Make sure NeonPink is defined in your theme
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
        label = "toggle_animation_scale" // Added label
    )

    Surface( // Use Surface for better click handling and semantics
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale) // Apply scale here
            .clip(MaterialTheme.shapes.medium) // Clip before background
            .background(GlassSurface.copy(alpha = if (isChecked) 0.7f else 0.4f))
            .clickable { onCheckedChange(!isChecked) } // Make the whole row clickable
            .padding(16.dp),
        color = Color.Transparent // Surface itself is transparent, background is on modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    contentDescription = null, // Content description could be text for accessibility
                    tint = if (isChecked) ElectricGreen else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                color = StarWhite
            )
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange, // Called by parent Row's clickable now
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
fun AlarmTimeSetting( // Re-added for completeness from previous context
    label: String,
    isEnabled: Boolean,
    time: String,
    onEnableChange: (Boolean) -> Unit,
    onTimeClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isEnabled,
                    onCheckedChange = onEnableChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = NebulaPurple,
                        uncheckedColor = StarWhite.copy(alpha = 0.6f),
                        checkmarkColor = StarWhite
                    )
                )
                Text(label, style = MaterialTheme.typography.bodyMedium, color = StarWhite)
            }
            if (isEnabled) {
                Text(
                    "Daily at $time",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElectricGreen,
                    modifier = Modifier.padding(start = 52.dp) // Align with Checkbox text
                )
            }
        }
        if (isEnabled) {
            Button(
                onClick = onTimeClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricGreen.copy(alpha = 0.2f),
                    contentColor = ElectricGreen
                )
            ) { Text("SET TIME") }
        }
    }
}


@Composable
fun TimePickerDialog( // Re-added for completeness
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
                containerColor = GlassSurfaceDark, // Make sure this color is defined
                contentColor = StarWhite
            ),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = ElectricGreen, modifier = Modifier.padding(bottom = 16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumberPicker(value = hour, onValueChange = { hour = it }, range = 0..23, format = { it.toString().padStart(2, '0') })
                    Text(":", style = MaterialTheme.typography.headlineLarge, color = ElectricGreen, modifier = Modifier.padding(horizontal = 16.dp))
                    NumberPicker(value = minute, onValueChange = { minute = it }, range = 0..59, step = 1, format = { it.toString().padStart(2, '0') })
                }
                Text(formatTimeDisplay(hour, minute), style = MaterialTheme.typography.bodyMedium, color = StarWhite.copy(alpha = 0.7f), modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = StarWhite.copy(alpha = 0.7f))) { Text("CANCEL") }
                    Button(onClick = { onTimeSelected(hour, minute) }, colors = ButtonDefaults.buttonColors(containerColor = NebulaPurple), modifier = Modifier.padding(start = 16.dp)) { Text("SET") }
                }
            }
        }
    }
}

@Composable
fun NumberPicker( // Re-added for completeness
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    step: Int = 1,
    format: (Int) -> String = { it.toString() }
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = { onValueChange( (value + step).coerceIn(range.first, range.last) ) }, // Simplified update
            modifier = Modifier.clip(CircleShape).background(NebulaPurple.copy(alpha = 0.2f))
        ) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase", tint = NebulaPurple) }

        Text(format(value), style = MaterialTheme.typography.headlineMedium, color = StarWhite, modifier = Modifier.padding(vertical = 8.dp))

        IconButton(
            onClick = { onValueChange( (value - step).coerceIn(range.first, range.last) ) }, // Simplified update
            modifier = Modifier.clip(CircleShape).background(NebulaPurple.copy(alpha = 0.2f))
        ) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease", tint = NebulaPurple) }
    }
}

private fun parseTimeString(timeString: String): Pair<Int, Int> { // Re-added
    return try {
        val parts = timeString.split(":")
        Pair(parts[0].toInt(), parts[1].toInt())
    } catch (e: Exception) { Pair(8, 0) }
}

private fun formatTimeString(hour: Int, minute: Int): String { // Re-added
    return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}

private fun formatTimeDisplay(hour: Int, minute: Int): String { // Re-added
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "$displayHour:${minute.toString().padStart(2, '0')} $amPm"
}