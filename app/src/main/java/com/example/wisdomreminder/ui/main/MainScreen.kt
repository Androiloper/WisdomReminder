package com.example.wisdomreminder.ui.main

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.wisdomreminder.R
import com.example.wisdomreminder.model.Wisdom
import com.example.wisdomreminder.ui.components.*
import com.example.wisdomreminder.ui.theme.*
import com.example.wisdomreminder.ui.wisdom.AddWisdomDialog
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.foundation.horizontalScroll


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onSettingsClick: () -> Unit,
    onNavigateToWisdomList: (defaultTab: String?) -> Unit,
    onWisdomClick: (Long) -> Unit,
    onNavigateToManageCategories: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showAddWisdomDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val localTAG = "MainScreen"
    var showCategorySelectionDialog by remember { mutableStateOf(false) } // State for dialog visibility


    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MainViewModel.UiEvent.WisdomAdded -> {
                    Toast.makeText(context, "Wisdom added successfully", Toast.LENGTH_SHORT).show()
                }
                is MainViewModel.UiEvent.WisdomActivated -> {
                    Toast.makeText(context, "Wisdom activated", Toast.LENGTH_SHORT).show()
                }
                is MainViewModel.UiEvent.CategoryCardAdded -> {
                    Toast.makeText(context, "Category card added to dashboard", Toast.LENGTH_SHORT).show()
                }
                is MainViewModel.UiEvent.CategoryCardRemoved -> {
                    Toast.makeText(context, "Category card removed from dashboard", Toast.LENGTH_SHORT).show()
                }
                is MainViewModel.UiEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                is MainViewModel.UiEvent.WisdomDeleted -> {
                    Toast.makeText(context, "Wisdom deleted", Toast.LENGTH_SHORT).show()
                }
                is MainViewModel.UiEvent.WisdomUpdated -> {
                    Toast.makeText(context, "Wisdom updated", Toast.LENGTH_SHORT).show()
                }
                is MainViewModel.UiEvent.CategoryOperationSuccess -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is MainViewModel.UiEvent.WisdomFavorited -> {
                    Toast.makeText(context, "Favorite status updated", Toast.LENGTH_SHORT).show()
                }
                is MainViewModel.UiEvent.SevenWisdomCategoryChanged -> {
                    Toast.makeText(context, "7 Wisdom Playlist category updated", Toast.LENGTH_SHORT).show()
                }
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
            modifier = Modifier.fillMaxSize().alpha(0.1f).blur(60.dp)
        )
        CosmicAnimations.CosmicParticlesEffect(particleCount = 30, modifier = Modifier.fillMaxSize())

        when (val state = uiState) {
            MainViewModel.WisdomUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ElectricGreen)
                }
            }
            is MainViewModel.WisdomUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                        Text("Error: ${state.message}", color = NeonPink, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refreshData() }, colors = ButtonDefaults.buttonColors(containerColor = NebulaPurple)) {
                            Text("RETRY")
                        }
                    }
                }
            }
            is MainViewModel.WisdomUiState.Success -> {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    contentColor = StarWhite,
                    topBar = {
                        TopAppBar(
                            title = { Text("WISDOM REMINDER", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)) },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = GlassSurface.copy(alpha = 0.5f), titleContentColor = StarWhite),
                            actions = {
                                Button(
                                    onClick = {
                                        if (state.serviceRunning) viewModel.stopWisdomService(context)
                                        else viewModel.startService(context)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (state.serviceRunning) NeonPink.copy(alpha = 0.8f) else ElectricGreen.copy(alpha = 0.8f)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) { Text(if (state.serviceRunning) "STOP SVC" else "START SVC") }
                                IconButton(onClick = { onSettingsClick() }) {
                                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = StarWhite)
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showAddWisdomDialog = true }, containerColor = NebulaPurple, contentColor = StarWhite) {
                            Icon(Icons.Default.Add, contentDescription = "Add Wisdom")
                        }
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        HorizontalNavButtons(
                            buttons = listOf(
                                NavButtonInfo("WISDOM", onClick = { onNavigateToWisdomList("all") }, color = CyberBlue),
                                NavButtonInfo("CATEGORY", onClick = onNavigateToManageCategories, color = ElectricGreen),
                                NavButtonInfo("QUEUE", onClick = { onNavigateToWisdomList("queued") }, color = NeonPink),
                                NavButtonInfo("DASHBOARD+", onClick = { showCategorySelectionDialog = true }, color = AccentOrange) // New button
                            )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                StatCard("ACTIVE", state.activeCount.toString(), Icons.Default.PlayArrow, ElectricGreen, Modifier.weight(1f))
                                StatCard("COMPLETED", state.completedCount.toString(), Icons.Default.PlayArrow, CyberBlue, Modifier.weight(1f))
                            }

                            val allWisdomItems = remember(
                                state.activeWisdom,
                                state.otherQueuedWisdom,
                                state.favoriteQueuedWisdom,
                                state.sevenWisdomPlaylist,
                                state.completedWisdom
                            ) {
                                (state.activeWisdom +
                                        state.otherQueuedWisdom +
                                        state.favoriteQueuedWisdom +
                                        state.sevenWisdomPlaylist +
                                        state.completedWisdom
                                        ).distinctBy { it.id }
                            }

                            if (allWisdomItems.isNotEmpty()) {
                                SwipeableWisdomCards(
                                    allWisdom = allWisdomItems,
                                    onWisdomClick = onWisdomClick
                                )
                            }

                            Text(
                                text = "WISDOM EXPLORER",
                                style = MaterialTheme.typography.titleLarge,
                                color = NeonPink,
                                modifier = Modifier.padding(top = 16.dp, bottom = 0.dp)
                            )
                            CategoryExplorerCard(
                                allWisdom = allWisdomItems,
                                selectedCategory = state.selectedExplorerCategory,
                                allCategories = state.allCategories,
                                onWisdomClick = onWisdomClick,
                                onCategorySelected = { category ->
                                    viewModel.setSelectedExplorerCategory(category)
                                }
                            )

                            Text(
                                text = "MY DASHBOARD",
                                style = MaterialTheme.typography.titleLarge,
                                color = ElectricGreen,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                            if (state.selectedCategoriesForCards.isEmpty()) {
                                GlassCard(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Add category cards to your dashboard using the 'DASHBOARD+' button above.", style = MaterialTheme.typography.bodyMedium, color = StarWhite.copy(alpha = 0.7f), textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                                    }
                                }
                            } else {
                                Log.d(localTAG, "Dashboard rendering. Selected categories: ${state.selectedCategoriesForCards}. CategoryWisdomMap keys: ${state.categoryWisdomMap.keys}")
                                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    state.selectedCategoriesForCards.forEach { category ->
                                        val wisdomListForCategory = state.categoryWisdomMap[category] ?: emptyList()
                                        Log.d(localTAG, "Dashboard Card: Category='${category}', Items=${wisdomListForCategory.size}, Wisdoms: ${wisdomListForCategory.joinToString { it.text.take(15) }}")
                                        CategoryWisdomCard(
                                            category = category,
                                            wisdomList = wisdomListForCategory,
                                            onWisdomClick = onWisdomClick,
                                            onRemove = { viewModel.removeCategoryCard(category) }
                                        )
                                    }
                                }
                            }

                            // The old "ADD CATEGORY TO DASHBOARD" button is now removed from here.

                            if (state.activeWisdom.isNotEmpty()) {
                                Text(
                                    text = "FEATURED ACTIVE WISDOM",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = ElectricGreen,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                )
                                ActiveWisdomCard(
                                    wisdom = state.activeWisdom.first(),
                                    onClick = { wisdom -> onWisdomClick(wisdom.id) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else if (allWisdomItems.isEmpty()){
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth().height(150.dp).clickable { showAddWisdomDialog = true },
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("No wisdom yet. Add your first one!", style = MaterialTheme.typography.bodyLarge, color = StarWhite, textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                            }

                            if (allWisdomItems.isNotEmpty()) {
                                AllWisdomSection(
                                    allWisdom = allWisdomItems,
                                    onWisdomClick = onWisdomClick
                                )
                            }

                            if (allWisdomItems.isEmpty() && state.activeWisdom.isEmpty()) {
                                Button(
                                    onClick = { viewModel.addSampleWisdom() },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp)
                                ) { Text("ADD SAMPLE WISDOM") }
                            }

                            Button(
                                onClick = { viewModel.debugDatabaseContents() },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                            ) {
                                Text("DEBUG DB")
                            }

                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }

                if (showAddWisdomDialog) {
                    AddWisdomDialog(
                        allExistingCategories = state.allCategories,
                        onDismiss = { showAddWisdomDialog = false },
                        onSave = { text, source, category ->
                            viewModel.addWisdom(text, source, category)
                            showAddWisdomDialog = false
                        }
                    )
                }
                // This dialog is now triggered by the "DASHBOARD+" button in HorizontalNavButtons
                if (showCategorySelectionDialog) {
                    val availableCategoriesForDialog = state.allCategories.filterNot { it in state.selectedCategoriesForCards }
                    val allCategoriesFromState = state.allCategories

                    CategorySelectionDialog(
                        availableCategories = availableCategoriesForDialog,
                        allCategoriesInSystem = allCategoriesFromState,
                        selectedCategoriesOnDashboard = state.selectedCategoriesForCards,
                        onDismiss = { showCategorySelectionDialog = false },
                        onCategorySelected = { category ->
                            viewModel.addCategoryCard(category)
                            showCategorySelectionDialog = false
                        }
                    )
                }
            }
        }
    }
}