package com.example.wisdomreminder.ui.wisdom

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack // Keep for TopAppBar
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
// Removed: import androidx.compose.material.icons.filled.ArrowDropDown
// Removed: import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.res.painterResource // Ensure this is present
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.wisdomreminder.R // Ensure R is imported for drawables
import com.example.wisdomreminder.model.Wisdom
import com.example.wisdomreminder.ui.components.GlassCard
import com.example.wisdomreminder.ui.main.MainViewModel
import com.example.wisdomreminder.ui.navigation.Screen
import com.example.wisdomreminder.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WisdomDetailScreen(
    navController: NavHostController, // Added NavController
    initialWisdomId: Long,
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val mainUiState by viewModel.uiState.collectAsState()

    // State to hold the list of wisdom items in the current category and the current index
    val categoryWisdomData by produceState<Pair<List<Wisdom>, Int>?>(
        initialValue = null,
        key1 = initialWisdomId,
        key2 = mainUiState
    ) {
        // This block will re-execute if initialWisdomId or mainUiState changes
        val currentWisdom = when (val state = mainUiState) {
            is MainViewModel.WisdomUiState.Success -> state.allWisdomFlatList.find { it.id == initialWisdomId }
            else -> null
        }

        if (currentWisdom != null) {
            val categoryItems = when (val state = mainUiState) {
                is MainViewModel.WisdomUiState.Success -> state.allWisdomFlatList
                    .filter { it.category.equals(currentWisdom.category, ignoreCase = true) }
                    // Sort by orderIndex, then by dateCreated for consistent ordering
                    .sortedWith(compareBy({ it.orderIndex }, { it.dateCreated }))
                else -> emptyList()
            }
            val currentIndex = categoryItems.indexOfFirst { it.id == initialWisdomId }
            if (currentIndex != -1) {
                value = Pair(categoryItems, currentIndex)
            } else {
                value = Pair(listOf(currentWisdom), 0)
            }
        } else if (mainUiState is MainViewModel.WisdomUiState.Loading) {
            value = null // Still loading
        } else {
            val errorWisdom = Wisdom(id = -1, text = "Wisdom not found or error loading.", category = "Error")
            value = Pair(listOf(errorWisdom),0)
        }
    }


    if (categoryWisdomData == null || mainUiState is MainViewModel.WisdomUiState.Loading) {
        Box(modifier = Modifier.fillMaxSize().background(DeepSpace), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ElectricGreen)
        }
        return
    }

    val (categoryWisdomList, initialPageIndex) = categoryWisdomData!!

    if (categoryWisdomList.isEmpty() || initialPageIndex == -1) {
        Box(modifier = Modifier.fillMaxSize().background(DeepSpace), contentAlignment = Alignment.Center) {
            Text("Wisdom item not found in its category.", color = NeonPink, style = MaterialTheme.typography.bodyLarge)
        }
        return
    }


    val pagerState = rememberPagerState(
        initialPage = initialPageIndex,
        pageCount = { categoryWisdomList.size }
    )

    LaunchedEffect(pagerState, categoryWisdomList, initialWisdomId) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collectLatest { settledPage ->
                if (categoryWisdomList.isNotEmpty() && settledPage < categoryWisdomList.size) {
                    val newWisdomId = categoryWisdomList[settledPage].id
                    if (newWisdomId != initialWisdomId) {
                        navController.navigate(Screen.WisdomDetail.createRoute(newWisdomId)) {
                            popUpTo(Screen.WisdomDetail.route) {
                                inclusive = true
                                saveState = false
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    }
                }
            }
    }

    LaunchedEffect(initialWisdomId, categoryWisdomList.size) {
        if (categoryWisdomList.isNotEmpty() && initialPageIndex != -1 && pagerState.currentPage != initialPageIndex && pagerState.pageCount == categoryWisdomList.size) {
            pagerState.scrollToPage(initialPageIndex)
        }
    }


    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        key = { pageIndex -> categoryWisdomList[pageIndex].id }
    ) { pageIndex ->
        if (pageIndex >= 0 && pageIndex < categoryWisdomList.size) {
            val currentWisdomInPager = categoryWisdomList[pageIndex]
            WisdomItemDetailContent(
                wisdom = currentWisdomInPager,
                viewModel = viewModel,
                onBackClick = onBackClick,
                isFirstInCategory = pageIndex == 0,
                isLastInCategory = pageIndex == categoryWisdomList.size - 1
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(DeepSpace), contentAlignment = Alignment.Center) {
                Text("Error loading wisdom for this page.", color = NeonPink)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WisdomItemDetailContent(
    wisdom: Wisdom,
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    isFirstInCategory: Boolean,
    isLastInCategory: Boolean
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

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
                    val alpha = (Math.random() * 0.8f + 0.2f).toFloat()
                    drawCircle(color = StarWhite.copy(alpha = alpha), radius = radius, center = Offset(x, y))
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
                    title = {
                        Text(
                            wisdom.category.uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = StarWhite)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = GlassSurface.copy(alpha = 0.5f),
                        titleContentColor = StarWhite,
                        navigationIconContentColor = StarWhite
                    ),
                    actions = {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = StarWhite)
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NeonPink)
                        }
                    }
                )
            },
            floatingActionButton = {
                if (!wisdom.isActive) {
                    FloatingActionButton(
                        onClick = { viewModel.activateWisdom(wisdom.id) },
                        containerColor = NebulaPurple,
                        contentColor = StarWhite
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Activate Wisdom")
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val (statusColor, statusText) = when {
                        wisdom.isActive -> NeonPink to "DAY ${wisdom.currentDay}/21"
                        wisdom.dateCompleted != null -> CyberBlue to "COMPLETED"
                        else -> NebulaPurple to "QUEUED"
                    }
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(statusColor.copy(alpha = 0.2f))
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(text = statusText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = statusColor)
                    }
                }

                if (wisdom.isActive) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("21/21 RULE PROGRESS", style = MaterialTheme.typography.titleMedium, color = ElectricGreen)
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ProgressCircle("DAYS", wisdom.currentDay.toString(), NeonPink)
                                ProgressCircle("TODAY", wisdom.exposuresToday.toString(), ElectricGreen)
                                ProgressCircle("TOTAL", wisdom.exposuresTotal.toString(), CyberBlue, "exposures")
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Today's Exposures: ${wisdom.exposuresToday}/21", style = MaterialTheme.typography.bodySmall, color = StarWhite.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress =  (wisdom.exposuresToday.toFloat() / 21f).coerceIn(0f, 1f) ,
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = ElectricGreen,
                                trackColor = GlassSurface.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Days Completed: ${wisdom.currentDay}/21", style = MaterialTheme.typography.bodySmall, color = StarWhite.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress =  (wisdom.currentDay.toFloat() / 21f).coerceIn(0f, 1f) ,
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = NeonPink,
                                trackColor = GlassSurface.copy(alpha = 0.3f)
                            )
                        }
                    }
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                        Text("WISDOM", style = MaterialTheme.typography.titleMedium, color = NebulaPurple)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("\"${wisdom.text}\"", style = MaterialTheme.typography.headlineLarge, color = StarWhite, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(16.dp))
                        if (wisdom.source.isNotBlank()) {
                            Text("— ${wisdom.source}", style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic), color = CyberBlue, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                        Text("DETAILS", style = MaterialTheme.typography.titleMedium, color = CyberBlue)
                        Spacer(modifier = Modifier.height(16.dp))
                        // Corrected DateTimeFormatter pattern
                        val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' h:mm a") }
                        DetailRow("Category", wisdom.category, CyberBlue)
                        DetailRow("Created", wisdom.dateCreated.format(dateFormatter), CyberBlue)
                        wisdom.startDate?.let { DetailRow("Started", it.format(dateFormatter), CyberBlue) }
                        wisdom.dateCompleted?.let { DetailRow("Completed", it.format(dateFormatter), CyberBlue) }
                        DetailRow("Status", when {
                            wisdom.isActive -> "Active (Day ${wisdom.currentDay}/21)"
                            wisdom.dateCompleted != null -> "Completed"
                            else -> "Queued"
                        }, CyberBlue)
                        if (wisdom.isActive || wisdom.dateCompleted != null) {
                            DetailRow("Total Exposures", wisdom.exposuresTotal.toString(), CyberBlue)
                        }
                    }
                }

                if (!wisdom.isActive) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ACTIONS", style = MaterialTheme.typography.titleMedium, color = ElectricGreen)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.activateWisdom(wisdom.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = NebulaPurple),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (wisdom.dateCompleted != null) "REACTIVATE" else "ACTIVATE", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Activating this wisdom will begin the 21/21 rule process.", style = MaterialTheme.typography.bodySmall, color = StarWhite.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }

            if (showEditDialog) {
                EditWisdomDialog(
                    wisdom = wisdom,
                    onDismiss = { showEditDialog = false },
                    onSave = { text, source, category ->
                        viewModel.updateWisdom(wisdom.copy(text = text, source = source, category = category))
                        showEditDialog = false
                    }
                )
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Wisdom") },
                    text = { Text("Are you sure you want to delete this wisdom? This action cannot be undone.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteWisdom(wisdom)
                                showDeleteDialog = false
                                onBackClick()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                        ) { Text("DELETE") }
                    },
                    dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("CANCEL") } }
                )
            }
        }
    }
}

@Composable
private fun ProgressCircle(label: String, value: String, color: Color, subLabel: String = "of 21") {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(GlassSurfaceDark),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(70.dp).clip(CircleShape)
                    .background(brush = Brush.radialGradient(colors = listOf(color.copy(alpha = 0.7f), color.copy(alpha = 0.1f)))),
                contentAlignment = Alignment.Center
            ) {
                Text(value, style = MaterialTheme.typography.displayMedium, color = StarWhite)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = color)
        if (label != "TOTAL") {
            Text(subLabel, style = MaterialTheme.typography.bodySmall, color = StarWhite.copy(alpha = 0.7f))
        } else {
            Text(subLabel, style = MaterialTheme.typography.bodySmall, color = StarWhite.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    labelColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = labelColor
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = StarWhite
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWisdomDialog(
    wisdom: Wisdom,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var wisdomText by remember { mutableStateOf(wisdom.text) }
    var wisdomSource by remember { mutableStateOf(wisdom.source) }
    var wisdomCategory by remember { mutableStateOf(wisdom.category) }
    val categories = listOf("General", "Personal", "Professional", "Health", "Relationships", "Philosophy", "Motivation")
    var showCategoryDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = GlassSurface, contentColor = StarWhite),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("EDIT WISDOM", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = CyberBlue)
                OutlinedTextField(
                    value = wisdomText,
                    onValueChange = { wisdomText = it },
                    label = { Text("Wisdom Text") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberBlue,
                        unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite,
                        cursorColor = CyberBlue,
                        focusedLabelColor = CyberBlue,
                        unfocusedLabelColor = StarWhite.copy(alpha = 0.7f)
                    ),
                    minLines = 3
                )
                OutlinedTextField(
                    value = wisdomSource,
                    onValueChange = { wisdomSource = it },
                    label = { Text("Source (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberBlue,
                        unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite,
                        cursorColor = CyberBlue,
                        focusedLabelColor = CyberBlue,
                        unfocusedLabelColor = StarWhite.copy(alpha = 0.7f)
                    ),
                    singleLine = true
                )
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = wisdomCategory,
                        onValueChange = { wisdomCategory = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberBlue,
                            unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                            focusedTextColor = StarWhite,
                            unfocusedTextColor = StarWhite,
                            cursorColor = CyberBlue,
                            focusedLabelColor = CyberBlue,
                            unfocusedLabelColor = StarWhite.copy(alpha = 0.7f)
                        ),
                        singleLine = true,
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showCategoryDropdown = !showCategoryDropdown }) {
                                Icon(
                                    painter = if (showCategoryDropdown) painterResource(id = R.drawable.ic_arrow_drop_up) else painterResource(id = R.drawable.ic_arrow_drop_down),
                                    contentDescription = "Toggle Categories",
                                    tint = CyberBlue
                                )
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.8f).background(GlassSurfaceDark.copy(alpha = 0.95f))
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category, color = if (category == wisdomCategory) CyberBlue else StarWhite) },
                                onClick = {
                                    wisdomCategory = category
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.5f)), modifier = Modifier.padding(end = 16.dp)) { Text("CANCEL") }
                    Button(
                        onClick = { if (wisdomText.isNotBlank()) onSave(wisdomText.trim(), wisdomSource.trim(), wisdomCategory.trim()) },
                        enabled = wisdomText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberBlue)
                    ) { Text("UPDATE") }
                }
            }
        }
    }
}
