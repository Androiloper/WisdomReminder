package com.example.wisdomreminder.ui.settings

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Info // For unlock screen setting
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
import androidx.compose.ui.graphics.vector.ImageVector
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
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val alarmsEnabled by viewModel.alarmsEnabled.collectAsState()
    val morningAlarmEnabled by viewModel.morningAlarmEnabled.collectAsState()
    val morningAlarmTime by viewModel.morningAlarmTime.collectAsState()
    val eveningAlarmEnabled by viewModel.eveningAlarmEnabled.collectAsState()
    val eveningAlarmTime by viewModel.eveningAlarmTime.collectAsState()
    val unlockScreenDisplayMode by viewModel.unlockScreenDisplayMode.collectAsState()


    var showTimePickerDialog by remember { mutableStateOf(false) }
    var timePickerMode by remember { mutableStateOf("morning") }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showUnlockModeDialog by remember { mutableStateOf(false) }


    val handleAlarmToggle: (Boolean) -> Unit = { isEnabled ->
        viewModel.setAlarmsEnabled(isEnabled)
        if (isEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasExactAlarmPermission(context)) {
                showPermissionDialog = true
            }
        }
    }

    LaunchedEffect(alarmsEnabled, morningAlarmEnabled, eveningAlarmEnabled, context) {
        if (alarmsEnabled && (morningAlarmEnabled || eveningAlarmEnabled) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasExactAlarmPermission(context)) {
                // Reminder if permission is still needed
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
                    val alphaVal = (Math.random() * 0.8f + 0.2f).toFloat()
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
                    title = { Text("SETTINGS", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = StarWhite) },
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

                SectionHeader(title = "GENERAL NOTIFICATIONS")
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
                                "Regular notifications will show wisdom throughout the day.",
                                style = MaterialTheme.typography.bodySmall,
                                color = StarWhite.copy(alpha = 0.7f), // Explicit color
                                modifier = Modifier.padding(start = 40.dp)
                            )
                        }
                    }
                }

                SectionHeader(title = "DAILY REMINDER ALARMS")
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
                            AlarmTimeSetting( // Line 153 error might be related to Text inside this
                                label = "Morning Reflection",
                                isEnabled = morningAlarmEnabled,
                                time = morningAlarmTime,
                                onEnableChange = { viewModel.setMorningAlarmEnabled(it) },
                                onTimeClick = {
                                    timePickerMode = "morning"
                                    showTimePickerDialog = true
                                }
                            )
                            AlarmTimeSetting( // Line 173 error might be related to Text inside this
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
                                color = StarWhite.copy(alpha = 0.7f), // Explicit color
                                modifier = Modifier.padding(start = 12.dp, top = 8.dp)
                            )
                        }
                    }
                }

                SectionHeader(title = "UNLOCK SCREEN DISPLAY") // Approx Line 190
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        SettingItem(
                            icon = Icons.Rounded.Info,
                            title = "Wisdom on Unlock",
                            subtitle = "Choose what wisdom appears when you unlock your phone.",
                            onClick = { showUnlockModeDialog = true }
                        ) {
                            Text(
                                text = when (unlockScreenDisplayMode) {
                                    UnlockScreenDisplayMode.ACTIVE_WISDOM -> "Active Wisdom"
                                    UnlockScreenDisplayMode.QUEUED_PLAYLIST -> "Queued Playlist"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = ElectricGreen // Explicit color
                            )
                        }
                        Text(
                            "Note: 'Queued Playlist' functionality is conceptual and will pick random active wisdom for now. Full playlist selection will be added later.",
                            style = MaterialTheme.typography.bodySmall,
                            color = StarWhite.copy(alpha = 0.6f), // Explicit color
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }


                SectionHeader(title = "ABOUT") // Approx Line 208
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Wisdom Reminder v1.0", style = MaterialTheme.typography.titleMedium, color = ElectricGreen)
                        Text("The 21/21 Rule", style = MaterialTheme.typography.titleSmall, color = StarWhite)
                        Text( // Approx Line 215 error
                            "Internalizing wisdom through repetition: expose yourself to the same piece of wisdom 21 times over 21 days to make it a part of your thinking.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StarWhite.copy(alpha = 0.8f) // Explicit color
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    Toast.makeText(context, "More info coming soon!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberBlue)
                            ) {
                                Icon(Icons.Rounded.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("MORE INFO", color = LocalContentColor.current) // Use LocalContentColor for Button Text
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
                    Toast.makeText(context, "${if (timePickerMode == "morning") "Morning" else "Evening"} alarm set to $formattedTime", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showPermissionDialog && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlertDialog(
                onDismissRequest = { showPermissionDialog = false },
                title = { Text("Permission Required", color = ElectricGreen) },
                text = { Text("This app needs permission to schedule exact alarms for wisdom reminders at specific times. Please grant this permission in the app settings.", color = StarWhite) },
                confirmButton = {
                    Button(
                        onClick = {
                            showPermissionDialog = false
                            try {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open settings. Please grant permission manually.", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NebulaPurple)
                    ) { Text("Open Settings", color = StarWhite) }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionDialog = false }) {
                        Text("Later", color = StarWhite.copy(alpha = 0.7f))
                    }
                },
                containerColor = GlassSurfaceDark,
                tonalElevation = 12.dp
            )
        }
        if (showUnlockModeDialog) {
            UnlockScreenModeDialog( // Approx Line 243 error might be related to Text inside this
                currentMode = unlockScreenDisplayMode,
                onModeSelected = {
                    viewModel.setUnlockScreenDisplayMode(it)
                    showUnlockModeDialog = false
                },
                onDismiss = { showUnlockModeDialog = false }
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) { // This was fine, no changes needed based on error
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = NeonPink,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    brush = Brush.radialGradient(colors = listOf(NebulaPurple.copy(alpha = 0.15f), Color.Transparent)),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = NebulaPurple, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = StarWhite) // Explicit color
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = StarWhite.copy(alpha = 0.7f)) // Explicit color
            }
        }
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(16.dp))
            trailingContent()
        }
    }
}


@Composable
fun UnlockScreenModeDialog(
    currentMode: UnlockScreenDisplayMode,
    onModeSelected: (UnlockScreenDisplayMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Unlock Screen Display", color = ElectricGreen) },
        text = {
            Column {
                Text("Choose which wisdom to display when you unlock your phone:", color = StarWhite) // Explicit color
                Spacer(Modifier.height(16.dp))
                UnlockScreenDisplayMode.values().forEach { mode ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onModeSelected(mode) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (mode == currentMode),
                            onClick = { onModeSelected(mode) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = ElectricGreen,
                                unselectedColor = StarWhite.copy(alpha = 0.7f)
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text( // Error was around here (line 243 in previous version)
                            text = when (mode) {
                                UnlockScreenDisplayMode.ACTIVE_WISDOM -> "Currently Active Wisdom"
                                UnlockScreenDisplayMode.QUEUED_PLAYLIST -> "Wisdom from Queue (Playlist)"
                            },
                            color = StarWhite // Explicit color
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("CLOSE", color = StarWhite.copy(alpha = 0.7f)) }
        },
        containerColor = GlassSurfaceDark
    )
}

@Composable
fun FuturisticToggle(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector
) {
    val scale by animateFloatAsState(
        targetValue = if (isChecked) 1.0f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "toggle_animation_scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(MaterialTheme.shapes.medium)
            .background(GlassSurface.copy(alpha = if (isChecked) 0.6f else 0.3f))
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            (if (isChecked) ElectricGreen else MoonGray).copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (isChecked) ElectricGreen else MoonGray,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            color = StarWhite // Explicit color
        )
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NebulaPurple,
                checkedTrackColor = ElectricGreen.copy(alpha = 0.4f),
                checkedBorderColor = ElectricGreen.copy(alpha = 0.6f),
                uncheckedThumbColor = MoonGray,
                uncheckedTrackColor = GlassSurfaceLight.copy(alpha = 0.5f),
                uncheckedBorderColor = MoonGray.copy(alpha = 0.5f)
            )
        )
    }
}


@Composable
fun AlarmTimeSetting(
    label: String,
    isEnabled: Boolean,
    time: String,
    onEnableChange: (Boolean) -> Unit,
    onTimeClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp), // Approx line 145-150
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isEnabled,
                onCheckedChange = onEnableChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = NebulaPurple,
                    uncheckedColor = StarWhite.copy(alpha = 0.6f),
                    checkmarkColor = StarWhite
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge, color = StarWhite, modifier = Modifier.weight(1f)) // Line ~153 (Error Reported Here)

            if (isEnabled) {
                Button( // Line ~157
                    onClick = onTimeClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricGreen.copy(alpha = 0.2f),
                        contentColor = ElectricGreen // This is the key for Text color
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(time, style = MaterialTheme.typography.bodyMedium, color = ElectricGreen) // Explicitly set color
                }
            }
        }
        if (isEnabled) {
            Text( // Line ~172-173 (Error Reported Here)
                "Daily alarm set for $time",
                style = MaterialTheme.typography.bodySmall,
                color = StarWhite.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 48.dp)
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
    var hour by remember { mutableIntStateOf(initialHour) }
    var minute by remember { mutableIntStateOf(initialMinute) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = GlassSurfaceDark,
                contentColor = StarWhite
            ),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = ElectricGreen, modifier = Modifier.padding(bottom = 24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumberPicker(value = hour, onValueChange = { hour = it }, range = 0..23, format = { it.toString().padStart(2, '0') })
                    Text(":", style = MaterialTheme.typography.headlineLarge, color = ElectricGreen, modifier = Modifier.padding(horizontal = 20.dp))
                    NumberPicker(value = minute, onValueChange = { minute = it }, range = 0..59, step = 1, format = { it.toString().padStart(2, '0') })
                }
                Text(
                    formatTimeDisplay(hour, minute),
                    style = MaterialTheme.typography.bodyLarge,
                    color = StarWhite.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = StarWhite.copy(alpha = 0.7f))) { Text("CANCEL", color = StarWhite.copy(alpha = 0.7f)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onTimeSelected(hour, minute) },
                        colors = ButtonDefaults.buttonColors(containerColor = NebulaPurple),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                    ) { Text("SET", color = StarWhite) }
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
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(
            onClick = { onValueChange( (value + step).coerceIn(range.first, range.last) ) },
            modifier = Modifier.size(40.dp).clip(CircleShape).background(NebulaPurple.copy(alpha = 0.2f))
        ) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase", tint = NebulaPurple) }

        Text(format(value), style = MaterialTheme.typography.headlineMedium, color = StarWhite, modifier = Modifier.padding(vertical = 4.dp))

        IconButton(
            onClick = { onValueChange( (value - step).coerceIn(range.first, range.last) ) },
            modifier = Modifier.size(40.dp).clip(CircleShape).background(NebulaPurple.copy(alpha = 0.2f))
        ) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease", tint = NebulaPurple) }
    }
}

private fun parseTimeString(timeString: String): Pair<Int, Int> {
    return try {
        val parts = timeString.split(":")
        Pair(parts[0].toInt(), parts[1].toInt())
    } catch (e: Exception) { Pair(8, 0) }
}

private fun formatTimeString(hour: Int, minute: Int): String {
    return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}

private fun formatTimeDisplay(hour: Int, minute: Int): String {
    val amPm = if (hour < 12 || hour == 24) "AM" else "PM"
    val displayHour = when {
        hour == 0 || hour == 12 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "$displayHour:${minute.toString().padStart(2, '0')} $amPm"
}