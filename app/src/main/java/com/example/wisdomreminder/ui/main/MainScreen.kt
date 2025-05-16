package com.example.wisdomreminder.ui.main

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.wisdomreminder.R
import com.example.wisdomreminder.model.Wisdom
import com.example.wisdomreminder.ui.components.GlassCard
import com.example.wisdomreminder.ui.components.StatCard
import com.example.wisdomreminder.ui.theme.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onSettingsClick: () -> Unit,
    onWisdomListClick: () -> Unit,
    onWisdomClick: (Long) -> Unit
) {
    val activeWisdom by viewModel.activeWisdom.observeAsState(emptyList())
    val queuedWisdom by viewModel.queuedWisdom.observeAsState(emptyList())
    val completedWisdom by viewModel.completedWisdom.observeAsState(emptyList())
    val activeWisdomCount by viewModel.activeWisdomCount.observeAsState(0)
    val completedWisdomCount by viewModel.completedWisdomCount.observeAsState(0)
    val serviceRunning by viewModel.serviceRunning.observeAsState(false)

    val context = LocalContext.current
    var showAddWisdomDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Cosmic background
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
                .blur(60.dp)
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentColor = StarWhite,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "WISDOM REMINDER",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = GlassSurface.copy(alpha = 0.5f),
                        titleContentColor = StarWhite
                    ),
                    actions = {
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = StarWhite
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddWisdomDialog = true },
                    containerColor = NebulaPurple,
                    contentColor = StarWhite
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Wisdom"
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Stats Cards Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        title = "ACTIVE",
                        value = activeWisdomCount.toString(),
                        icon = Icons.Default.PlayArrow,
                        color = ElectricGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "COMPLETED",
                        value = completedWisdomCount.toString(),
                        icon = Icons.Default.PlayArrow,
                        color = CyberBlue,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Main Section - Featured Active Wisdom
                Text(
                    text = "ACTIVE WISDOM",
                    style = MaterialTheme.typography.titleLarge,
                    color = ElectricGreen,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )

                if (activeWisdom.isEmpty()) {
                    // Empty state for active wisdom
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable { onWisdomListClick() }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.empty_active),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = StarWhite,
                                    textAlign = TextAlign.Center
                                )

                                Button(
                                    onClick = onWisdomListClick,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NebulaPurple
                                    ),
                                    modifier = Modifier.padding(top = 16.dp)
                                ) {
                                    Text("VIEW WISDOM")
                                }
                            }
                        }
                    }
                } else {
                    // Display featured wisdom
                    ActiveWisdomCard(
                        wisdom = activeWisdom.first(),
                        onClick = { onWisdomClick(it.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Queued Wisdom Section
                Text(
                    text = "QUEUED WISDOM",
                    style = MaterialTheme.typography.titleLarge,
                    color = NebulaPurple,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                if (queuedWisdom.isEmpty()) {
                    // Empty state for queued wisdom
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.empty_queued),
                                style = MaterialTheme.typography.bodyMedium,
                                color = StarWhite,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Display first few queued wisdom
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        queuedWisdom.take(3).forEach { wisdom ->
                            QueuedWisdomItem(
                                wisdom = wisdom,
                                onClick = { onWisdomClick(wisdom.id) },
                                onActivate = { viewModel.activateWisdom(wisdom.id) }
                            )
                        }

                        if (queuedWisdom.size > 3) {
                            OutlinedButton(
                                onClick = onWisdomListClick,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = NebulaPurple
                                ),
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(top = 8.dp)
                            ) {
                                Text("VIEW ALL (${queuedWisdom.size})")
                            }
                        }
                    }
                }

                // Debug buttons (for development only)
                if (queuedWisdom.isEmpty() && activeWisdom.isEmpty()) {
                    Button(
                        onClick = { viewModel.addSampleWisdom() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonPink
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 16.dp)
                    ) {
                        Text("ADD SAMPLE WISDOM")
                    }
                }

                // Service status indicator
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (serviceRunning) ElectricGreen.copy(alpha = 0.2f) else NeonPink.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (serviceRunning) ElectricGreen else NeonPink)
                        )

                        Text(
                            text = if (serviceRunning) "Wisdom service running" else "Wisdom service stopped",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (serviceRunning) ElectricGreen else NeonPink,
                            modifier = Modifier.padding(start = 8.dp)
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        TextButton(
                            onClick = {
                                if (serviceRunning) {
                                    viewModel.stopWisdomService(context)
                                } else {
                                    viewModel.checkAndRestartService(context)
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (serviceRunning) StarWhite else NeonPink
                            )
                        ) {
                            Text(if (serviceRunning) "STOP" else "START")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp)) // Bottom spacing for FAB
            }
        }
    }
}

@Composable
fun ActiveWisdomCard(
    wisdom: Wisdom,
    onClick: (Wisdom) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val progress = (wisdom.currentDay.toFloat() / 21f).coerceIn(0f, 1f)

    GlassCard(
        modifier = modifier
            .clickable { onClick(wisdom) }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with day counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(NeonPink.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "DAY ${wisdom.currentDay}/21",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonPink
                    )
                }

                Text(
                    text = "${wisdom.exposuresToday}/21 today",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElectricGreen
                )
            }

            // Progress indicator
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .height(6.dp),
                color = ElectricGreen,
                trackColor = GlassSurfaceLight
            )

            // Wisdom text
            Text(
                text = "\"${wisdom.text}\"",
                style = MaterialTheme.typography.bodyLarge,
                color = StarWhite,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Source and stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    if (wisdom.source.isNotBlank()) {
                        Text(
                            text = wisdom.source,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic
                            ),
                            color = CyberBlue
                        )
                    }

                    Text(
                        text = "Started: ${wisdom.startDate?.format(dateFormatter) ?: "Not started"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = StarWhite.copy(alpha = 0.7f)
                    )
                }

                Text(
                    text = "${wisdom.exposuresTotal} exposures",
                    style = MaterialTheme.typography.bodySmall,
                    color = StarWhite.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun QueuedWisdomItem(
    wisdom: Wisdom,
    onClick: () -> Unit,
    onActivate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = GlassSurface.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = wisdom.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StarWhite,
                    maxLines = 2
                )

                if (wisdom.source.isNotBlank()) {
                    Text(
                        text = wisdom.source,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontStyle = FontStyle.Italic
                        ),
                        color = CyberBlue,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = onActivate,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NebulaPurple
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("ACTIVATE")
            }
        }
    }
}