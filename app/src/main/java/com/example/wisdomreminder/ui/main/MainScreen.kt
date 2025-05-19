package com.example.wisdomreminder.ui.main

import android.widget.Toast
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
import com.example.wisdomreminder.ui.components.*
import com.example.wisdomreminder.ui.theme.*
import com.example.wisdomreminder.ui.wisdom.AddWisdomDialog
import com.example.wisdomreminder.ui.wisdom.QueuedWisdomItem
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onSettingsClick: () -> Unit,
    onWisdomListClick: () -> Unit,
    onWisdomClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showAddWisdomDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Collect events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MainViewModel.UiEvent.WisdomAdded -> {
                    Toast.makeText(context, "Wisdom added successfully", Toast.LENGTH_SHORT).show()
                }
                is MainViewModel.UiEvent.WisdomActivated -> {
                    Toast.makeText(context, "Wisdom activated", Toast.LENGTH_SHORT).show()
                }
                is MainViewModel.UiEvent.CategoryAdded -> {
                    Toast.makeText(context, "Category added successfully", Toast.LENGTH_SHORT).show()
                }
                is MainViewModel.UiEvent.CategoryRemoved -> {
                    Toast.makeText(context, "Category removed", Toast.LENGTH_SHORT).show()
                }
                is MainViewModel.UiEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                else -> { /* Handle other events */ }
            }
        }
    }

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

        // Add cosmic particles animation
        CosmicAnimations.CosmicParticlesEffect(
            particleCount = 30,
            modifier = Modifier.fillMaxSize()
        )

        // Render UI based on state
        when (val state = uiState) {
            MainViewModel.WisdomUiState.Loading -> {
                // Show loading indicator
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ElectricGreen)
                }
            }

            is MainViewModel.WisdomUiState.Error -> {
                // Show error state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Error: ${state.message}",
                            color = NeonPink,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.refreshData() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NebulaPurple
                            )
                        ) {
                            Text("RETRY")
                        }
                    }
                }
            }

            is MainViewModel.WisdomUiState.Success -> {
                // Main content when data loaded successfully
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
                                // Add START/STOP button for the service
                                Button(
                                    onClick = {
                                        if (state.serviceRunning) {
                                            viewModel.stopWisdomService(context)
                                        } else {
                                            viewModel.checkAndRestartService(context)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (state.serviceRunning)
                                            NeonPink.copy(alpha = 0.8f)
                                        else
                                            ElectricGreen.copy(alpha = 0.8f)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(if (state.serviceRunning) "STOP" else "START")
                                }

                                IconButton(onClick = { onSettingsClick() }) {
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
                                value = state.activeCount.toString(),
                                icon = Icons.Default.PlayArrow,
                                color = ElectricGreen,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "COMPLETED",
                                value = state.completedCount.toString(),
                                icon = Icons.Default.PlayArrow,
                                color = CyberBlue,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Swipeable Wisdom Cards
                        // Combine all wisdom for swiping through
                        val allWisdom = state.activeWisdom + state.queuedWisdom + state.completedWisdom

                        Text(
                            text = "WISDOM EXPLORER",
                            style = MaterialTheme.typography.titleLarge,
                            color = NeonPink,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        // Fixed SwipeableWisdomCards with explicit type
                        SwipeableWisdomCards(
                            allWisdom = allWisdom,
                            onWisdomClick = { id: Long -> onWisdomClick(id) }
                        )

                        // Category Cards Section
                        Text(
                            text = "CATEGORY CARDS",
                            style = MaterialTheme.typography.titleLarge,
                            color = CyberBlue,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )

                        // Current category cards
                        if (state.selectedCategories.isEmpty()) {
                            // Empty state
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
                                        text = "Add category cards to view wisdom by category",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = StarWhite,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            // Display category cards
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                state.selectedCategories.forEach { category ->
                                    val wisdomList = state.categoryWisdom[category] ?: emptyList()
                                    // Fixed CategoryWisdomCard call with explicit types
                                    CategoryWisdomCard(
                                        category = category,
                                        wisdomList = wisdomList,
                                        onWisdomClick = { id: Long -> onWisdomClick(id) },
                                        onRemove = { viewModel.removeCategory(category) }
                                    )
                                }
                            }
                        }

                        // Add Category Button
                        var showCategorySelectionDialog by remember { mutableStateOf(false) }

                        Button(
                            onClick = { showCategorySelectionDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberBlue.copy(alpha = 0.8f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Category",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ADD CATEGORY CARD")
                        }

                        // Category Selection Dialog
                        if (showCategorySelectionDialog) {
                            CategorySelectionDialog(
                                availableCategories = state.allCategories,
                                selectedCategories = state.selectedCategories,
                                onDismiss = { showCategorySelectionDialog = false },
                                onCategorySelected = { category ->
                                    viewModel.addCategory(category)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Main Section - Featured Active Wisdom
                        Text(
                            text = "ACTIVE WISDOM",
                            style = MaterialTheme.typography.titleLarge,
                            color = ElectricGreen,
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        )

                        if (state.activeWisdom.isEmpty()) {
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
                                            onClick = { onWisdomListClick() },
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
                                wisdom = state.activeWisdom.first(),
                                onClick = { wisdom -> onWisdomClick(wisdom.id) },
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

                        if (state.queuedWisdom.isEmpty()) {
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
                                state.queuedWisdom.take(3).forEach { wisdom ->
                                    QueuedWisdomItem(
                                        wisdom = wisdom,
                                        onClick = { onWisdomClick(wisdom.id) },
                                        onActivate = { viewModel.activateWisdom(wisdom.id) }
                                    )
                                }

                                if (state.queuedWisdom.size > 3) {
                                    OutlinedButton(
                                        onClick = { onWisdomListClick() },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = NebulaPurple
                                        ),
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .padding(top = 8.dp)
                                    ) {
                                        Text("VIEW ALL (${state.queuedWisdom.size})")
                                    }
                                }
                            }
                        }

                        // ALL WISDOM Section
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "ALL WISDOM",
                            style = MaterialTheme.typography.titleLarge,
                            color = CyberBlue,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )

                        // Combine all wisdom for the ALL WISDOM section
                        Spacer(modifier = Modifier.height(16.dp))

                        AllWisdomSection(
                            allWisdom = state.activeWisdom + state.queuedWisdom + state.completedWisdom,
                            onWisdomClick = { wisdomId -> onWisdomClick(wisdomId) }
                        )

                        // Debug buttons (for development only)
                        if (state.queuedWisdom.isEmpty() && state.activeWisdom.isEmpty()) {
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

                        Spacer(modifier = Modifier.height(80.dp)) // Bottom spacing for FAB
                    }
                }

                // Add Wisdom Dialog
                if (showAddWisdomDialog) {
                    AddWisdomDialog(
                        onDismiss = { showAddWisdomDialog = false },
                        onSave = { text, source, category ->
                            viewModel.addWisdom(text, source, category)
                            showAddWisdomDialog = false
                        }
                    )
                }
            }
        }
    }
}