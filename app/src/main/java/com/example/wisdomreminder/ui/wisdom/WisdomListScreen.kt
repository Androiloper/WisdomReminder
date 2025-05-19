package com.example.wisdomreminder.ui.wisdom

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.wisdomreminder.R
import com.example.wisdomreminder.model.Wisdom
import com.example.wisdomreminder.ui.components.GlassCard
import com.example.wisdomreminder.ui.main.MainViewModel
import com.example.wisdomreminder.ui.theme.CosmicBlack
import com.example.wisdomreminder.ui.theme.CyberBlue
import com.example.wisdomreminder.ui.theme.DeepSpace
import com.example.wisdomreminder.ui.theme.ElectricGreen
import com.example.wisdomreminder.ui.theme.GlassSurface
import com.example.wisdomreminder.ui.theme.NebulaPurple
import com.example.wisdomreminder.ui.theme.NeonPink
import com.example.wisdomreminder.ui.theme.StarWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WisdomListScreen(
    onBackClick: () -> Unit,
    onWisdomClick: (Long) -> Unit,
    viewModel: MainViewModel
) {

    // Force a refresh when the screen opens
    LaunchedEffect(Unit) {
        Log.d("WisdomListScreen", "Screen opened - forcing refresh")
        viewModel.refreshData()
    }

    // Get the uiState from the viewModel
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        Log.d("WisdomListScreen", "UI State updated: $uiState")
    }


    // Extract wisdom lists from the current state
    val (activeWisdom, queuedWisdom, completedWisdom) = when (uiState) {
        is MainViewModel.WisdomUiState.Success -> {
            val successState = uiState as MainViewModel.WisdomUiState.Success
            Triple(successState.activeWisdom, successState.queuedWisdom, successState.completedWisdom)
        }
        else -> Triple(emptyList(), emptyList(), emptyList())
    }

    // Add debug logging
    LaunchedEffect(uiState) {
        when (uiState) {
            is MainViewModel.WisdomUiState.Success -> {
                val state = uiState as MainViewModel.WisdomUiState.Success
                Log.d("WisdomListScreen", "State updated: active=${state.activeWisdom.size}, " +
                        "queued=${state.queuedWisdom.size}, completed=${state.completedWisdom.size}")
            }
            else -> Log.d("WisdomListScreen", "State: $uiState")
        }
    }

    // In WisdomListScreen.kt
    LaunchedEffect(Unit) {
        viewModel.uiState.collect { state ->
            when (state) {
                is MainViewModel.WisdomUiState.Loading ->
                    Log.d("WisdomListScreen", "State: Loading")
                is MainViewModel.WisdomUiState.Error ->
                    Log.d("WisdomListScreen", "State: Error - ${state.message}")
                is MainViewModel.WisdomUiState.Success -> {
                    Log.d("WisdomListScreen", "State: Success - " +
                            "Active: ${state.activeWisdom.size}, " +
                            "Queued: ${state.queuedWisdom.size}, " +
                            "Completed: ${state.completedWisdom.size}")

                    // Log the actual items' IDs and text for detailed debugging
                    state.activeWisdom.forEachIndexed { index, wisdom ->
                        Log.d("WisdomListScreen", "Active #$index: ID=${wisdom.id}, Text='${wisdom.text.take(20)}...'")
                    }
                }
            }
        }


    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddWisdomDialog by remember { mutableStateOf(false) }

    val tabs = listOf("Active", "Queued", "Completed")



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
                            "WISDOM LIST",
                            style = MaterialTheme.typography.headlineLarge
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
            ) {
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search wisdom") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = StarWhite.copy(alpha = 0.7f)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = StarWhite.copy(alpha = 0.7f)
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberBlue,
                        unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite,
                        cursorColor = CyberBlue,
                        focusedContainerColor = GlassSurface.copy(alpha = 0.3f),
                        unfocusedContainerColor = GlassSurface.copy(alpha = 0.2f)
                    ),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true
                )

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = GlassSurface.copy(alpha = 0.3f),
                    contentColor = StarWhite,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = when (selectedTabIndex) {
                                0 -> ElectricGreen
                                1 -> NebulaPurple
                                else -> CyberBlue
                            }
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title.uppercase(),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            selectedContentColor = when (index) {
                                0 -> ElectricGreen
                                1 -> NebulaPurple
                                else -> CyberBlue
                            },
                            unselectedContentColor = StarWhite.copy(alpha = 0.7f)
                        )
                    }
                }

                // Content based on selected tab
                when (selectedTabIndex) {
                    0 -> ActiveWisdomList(
                        wisdom = activeWisdom,
                        onWisdomClick = onWisdomClick,
                        searchQuery = searchQuery
                    )
                    1 -> QueuedWisdomList(
                        wisdom = queuedWisdom,
                        onWisdomClick = onWisdomClick,
                        onActivate = { viewModel.activateWisdom(it) },
                        searchQuery = searchQuery
                    )
                    2 -> CompletedWisdomList(
                        wisdom = completedWisdom,
                        onWisdomClick = onWisdomClick,
                        onReactivate = { viewModel.activateWisdom(it) },
                        searchQuery = searchQuery
                    )
                }
            }


        }

        // Add Wisdom Dialog
        if (showAddWisdomDialog) {
            AddWisdomDialog(
                onDismiss = { showAddWisdomDialog = false },
                onSave = { text, source, category ->
                    viewModel.addWisdom(text, source, category)
                    showAddWisdomDialog = false


                    viewModel.refreshData()
                }
            )
        }
    }

    // Add this somewhere in your UI
    Button(
        onClick = { viewModel.debugDatabaseContents() },
        colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
    ) {
        Text("Debug DB")
    }
}

@Composable
fun ActiveWisdomList(
    wisdom: List<Wisdom>,
    onWisdomClick: (Long) -> Unit,
    searchQuery: String
) {
    // Add debug logging
    Log.d("WisdomList", "ActiveWisdomList received ${wisdom.size} items")
    wisdom.forEachIndexed { index, item ->
        Log.d("WisdomList", "Active item #$index: id=${item.id}, text='${item.text.take(20)}...'")
    }

    // Apply filtering
    val filteredWisdom = if (searchQuery.isEmpty()) {
        wisdom
    } else {
        wisdom.filter {
            it.text.contains(searchQuery, ignoreCase = true) ||
                    it.source.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    Log.d("WisdomList", "After filtering: ${filteredWisdom.size} active items remain")

    if (filteredWisdom.isEmpty()) {
        EmptyStateMessage(
            text = if (searchQuery.isEmpty())
                stringResource(R.string.empty_active)
            else
                "No active wisdom matching \"$searchQuery\""
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = filteredWisdom,
                key = { it.id } // Use stable ID as key
            ) { item ->
                // Simplified ActiveWisdomItem with more reliable animation
                ActiveWisdomItemSimplified(
                    wisdom = item,
                    onClick = { onWisdomClick(item.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp)) // FAB spacing
            }
        }
    }
}

@Composable
fun ActiveWisdomItemSimplified(
    wisdom: Wisdom,
    onClick: () -> Unit
) {



    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with day counter and progress
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
            val progress = (wisdom.currentDay.toFloat() / 21f).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .height(4.dp),
                color = ElectricGreen,
                trackColor = GlassSurface.copy(alpha = 0.3f)
            )

            // Wisdom text
            Text(
                text = wisdom.text,
                style = MaterialTheme.typography.bodyLarge,
                color = StarWhite,
                maxLines = 2,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Source and category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
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

                Text(
                    text = wisdom.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = StarWhite.copy(alpha = 0.7f)
                )
            }
        }
    }
}


@Composable
fun QueuedWisdomList(
    wisdom: List<Wisdom>,
    onWisdomClick: (Long) -> Unit,
    onActivate: (Long) -> Unit,
    searchQuery: String
) {
    // Add debug logging
    Log.d("WisdomList", "QueuedWisdomList received ${wisdom.size} items")
    wisdom.forEachIndexed { index, item ->
        Log.d("WisdomList", "Queued item #$index: id=${item.id}, text='${item.text.take(20)}...'")
    }

    // Apply filtering with a simpler approach
    val filteredWisdom = if (searchQuery.isEmpty()) {
        wisdom
    } else {
        wisdom.filter {
            it.text.contains(searchQuery, ignoreCase = true) ||
                    it.source.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    Log.d("WisdomList", "After filtering: ${filteredWisdom.size} queued items remain")

    if (filteredWisdom.isEmpty()) {
        EmptyStateMessage(
            text = if (searchQuery.isEmpty())
                stringResource(R.string.empty_queued)
            else
                "No queued wisdom matching \"$searchQuery\""
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = filteredWisdom,
                key = { it.id }
            ) { item ->
                // Simple implementation without complex animation
                QueuedWisdomItem(
                    wisdom = item,
                    onClick = { onWisdomClick(item.id) },
                    onActivate = { onActivate(item.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun QueuedWisdomItemSimplified(
    wisdom: Wisdom,
    onClick: () -> Unit,
    onActivate: () -> Unit
) {
    // Track activation state
    var isActivating by remember { mutableStateOf(false) }

    val animSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    var animationStarted by remember { mutableStateOf(false) }
    val animatedAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = animSpec,
        label = "alpha"
    )

    // Start animation after composition
    LaunchedEffect(Unit) {
        animationStarted = true
    }

    // Reset activation state after timeout
    LaunchedEffect(isActivating) {
        if (isActivating) {
            delay(3000) // Reset after 3 seconds if no state change
            isActivating = false
        }
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(animatedAlpha)
            .clickable(enabled = !isActivating) { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(NebulaPurple.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "QUEUED",
                        style = MaterialTheme.typography.bodySmall,
                        color = NebulaPurple
                    )
                }

                Button(
                    onClick = {
                        isActivating = true
                        onActivate()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActivating) NebulaPurple.copy(alpha = 0.6f) else NebulaPurple
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    enabled = !isActivating
                ) {
                    if (isActivating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = StarWhite,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ACTIVATING...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Activate",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ACTIVATE")
                    }
                }
            }

            // Wisdom text
            Text(
                text = wisdom.text,
                style = MaterialTheme.typography.bodyLarge,
                color = StarWhite,
                maxLines = 2,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Source and category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                if (wisdom.source.isNotBlank()) {
                    Text(
                        text = wisdom.source,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontStyle = FontStyle.Italic
                        ),
                        color = CyberBlue,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = wisdom.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = StarWhite.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun CompletedWisdomList(
    wisdom: List<Wisdom>,
    onWisdomClick: (Long) -> Unit,
    onReactivate: (Long) -> Unit,
    searchQuery: String
) {
    // Add debug logging
    Log.d("WisdomList", "CompletedWisdomList received ${wisdom.size} items")
    wisdom.forEachIndexed { index, item ->
        Log.d("WisdomList", "Completed item #$index: id=${item.id}, text='${item.text.take(20)}...'")
    }

    // Apply filtering with a simpler approach that avoids potential issues
    val filteredWisdom = wisdom.filter { item ->
        searchQuery.isEmpty() ||
                item.text.contains(searchQuery, ignoreCase = true) ||
                item.source.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true)
    }

    Log.d("WisdomList", "After filtering: ${filteredWisdom.size} completed items remain")

    if (filteredWisdom.isEmpty()) {
        EmptyStateMessage(
            text = if (searchQuery.isEmpty())
                stringResource(R.string.empty_completed)
            else
                "No completed wisdom matching \"$searchQuery\""
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(
                items = filteredWisdom,
                key = { _, item -> item.id }  // Stable key based on ID
            ) { index, item ->
                // Use explicit key for additional stability
                key(item.id) {
                    CompletedWisdomItemSimplified(
                        wisdom = item,
                        onClick = { onWisdomClick(item.id) },
                        onReactivate = { onReactivate(item.id) },
                        animationDelay = index * 50
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp)) // FAB spacing
            }
        }
    }
}

@Composable
fun CompletedWisdomItemSimplified(
    wisdom: Wisdom,
    onClick: () -> Unit,
    onReactivate: () -> Unit,
    animationDelay: Int = 0
) {
    // Track reactivation state
    var isReactivating by remember { mutableStateOf(false) }

    // Simplified animation with delayed start
    val animSpec = tween<Float>(durationMillis = 300, delayMillis = animationDelay)
    var animationStarted by remember { mutableStateOf(false) }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = animSpec,
        label = "alpha"
    )

    // Start animation after composition
    LaunchedEffect(Unit) {
        delay(50) // Short delay before starting animation
        animationStarted = true
    }

    // Reset reactivation state after timeout
    LaunchedEffect(isReactivating) {
        if (isReactivating) {
            delay(3000) // Reset after 3 seconds if no state change
            isReactivating = false
        }
    }

    // Compose the card with animation
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(animatedAlpha)
            .clickable(enabled = !isReactivating) { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(CyberBlue.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "COMPLETED",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberBlue
                    )
                }

                Button(
                    onClick = {
                        isReactivating = true
                        onReactivate()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isReactivating) CyberBlue.copy(alpha = 0.6f) else CyberBlue
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    enabled = !isReactivating
                ) {
                    if (isReactivating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = StarWhite,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("REACTIVATING...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Reactivate",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("REACTIVATE")
                    }
                }
            }

            // Wisdom text
            Text(
                text = wisdom.text,
                style = MaterialTheme.typography.bodyLarge,
                color = StarWhite,
                maxLines = 2,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Source and category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                if (wisdom.source.isNotBlank()) {
                    Text(
                        text = wisdom.source,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontStyle = FontStyle.Italic
                        ),
                        color = CyberBlue,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = wisdom.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = StarWhite.copy(alpha = 0.7f)
                )
            }

            // Completed date and total exposures
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
                Text(
                    text = "Completed: ${wisdom.dateCompleted?.format(dateFormatter) ?: "Unknown"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = StarWhite.copy(alpha = 0.5f)
                )

                Text(
                    text = "${wisdom.exposuresTotal} total exposures",
                    style = MaterialTheme.typography.bodySmall,
                    color = StarWhite.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun EmptyStateMessage(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = StarWhite,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(32.dp)
        )
    }
}

@Composable
fun ActiveWisdomItem(
    wisdom: Wisdom,
    onClick: () -> Unit,
    animationDelay: Int = 0
) {
    var visible by remember { mutableStateOf(true) }
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    LaunchedEffect(wisdom.id) {
        if (animationDelay > 0) {
            visible = false
            kotlinx.coroutines.delay(animationDelay.toLong())
            visible = true
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialOffsetY = { 100 }
        ),
        exit = fadeOut() + slideOutVertically()
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header with day counter and progress
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
                val progress = (wisdom.currentDay.toFloat() / 21f).coerceIn(0f, 1f)

                androidx.compose.material3.LinearProgressIndicator(
                    progress =  progress ,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .height(4.dp),
                    color = ElectricGreen,
                    trackColor = GlassSurface.copy(alpha = 0.3f)
                )

                // Wisdom text
                Text(
                    text = wisdom.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = StarWhite,
                    maxLines = 2,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Source and category
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
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

                    Text(
                        text = wisdom.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = StarWhite.copy(alpha = 0.7f)
                    )
                }

                // Stats row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Started: ${wisdom.startDate?.format(dateFormatter) ?: "Not started"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = StarWhite.copy(alpha = 0.5f)
                    )

                    Text(
                        text = "${wisdom.exposuresTotal} total exposures",
                        style = MaterialTheme.typography.bodySmall,
                        color = StarWhite.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun QueuedWisdomItem(
    wisdom: Wisdom,
    onClick: () -> Unit,
    onActivate: () -> Unit,
    animationDelay: Int = 0
) {
    var visible by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    LaunchedEffect(wisdom.id) {
        visible = false
        kotlinx.coroutines.delay(animationDelay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialOffsetY = { 100 }
        ),
        exit = fadeOut() + slideOutVertically()
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(NebulaPurple.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "QUEUED",
                            style = MaterialTheme.typography.bodySmall,
                            color = NebulaPurple
                        )
                    }

                    Button(
                        onClick = onActivate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NebulaPurple
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Activate",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ACTIVATE")
                    }
                }

                // Wisdom text
                Text(
                    text = wisdom.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = StarWhite,
                    maxLines = 2,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Source and category
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
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

                    Text(
                        text = wisdom.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = StarWhite.copy(alpha = 0.7f)
                    )
                }

                // Created date
                Text(
                    text = "Created: ${wisdom.dateCreated.format(dateFormatter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = StarWhite.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun CompletedWisdomItem(
    wisdom: Wisdom,
    onClick: () -> Unit,
    onReactivate: () -> Unit,
    animationDelay: Int = 0
) {
    var visible by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    LaunchedEffect(wisdom.id) {
        visible = false
        kotlinx.coroutines.delay(animationDelay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialOffsetY = { 100 }
        ),
        exit = fadeOut() + slideOutVertically()
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(CyberBlue.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "COMPLETED",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberBlue
                        )
                    }

                    Button(
                        onClick = onReactivate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberBlue
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Reactivate",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("REACTIVATE")
                    }
                }

                // Wisdom text
                Text(
                    text = wisdom.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = StarWhite,
                    maxLines = 2,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Source and category
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
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

                    Text(
                        text = wisdom.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = StarWhite.copy(alpha = 0.7f)
                    )
                }

                // Completed date and total exposures
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Completed: ${wisdom.dateCompleted?.format(dateFormatter) ?: "Unknown"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = StarWhite.copy(alpha = 0.5f)
                    )

                    Text(
                        text = "${wisdom.exposuresTotal} total exposures",
                        style = MaterialTheme.typography.bodySmall,
                        color = StarWhite.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

    /**
     * Debug helper function to directly check database content
     * Call this from a button in your UI for testing
     */
    @Composable
    fun DebugDatabaseButton(viewModel: MainViewModel) {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current

        Button(
            onClick = {
                scope.launch {
                    try {
                        // Get current state
                        val uiState = viewModel.uiState.value
                        if (uiState is MainViewModel.WisdomUiState.Success) {
                            val message = "UI State: Active=${uiState.activeWisdom.size}, " +
                                    "Queued=${uiState.queuedWisdom.size}, " +
                                    "Completed=${uiState.completedWisdom.size}"
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            Log.d("DEBUG", message)
                        } else {
                            Toast.makeText(context, "UI State: $uiState", Toast.LENGTH_LONG).show()
                            Log.d("DEBUG", "UI State: $uiState")
                        }

                        // Force refresh
                        viewModel.refreshData()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.e("DEBUG", "Error checking database", e)
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonPink
            ),
            modifier = Modifier.padding(16.dp)
        ) {
            Text("DEBUG DB")
        }


    }



}

