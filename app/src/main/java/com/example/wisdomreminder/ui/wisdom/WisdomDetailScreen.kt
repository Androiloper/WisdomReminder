package com.example.wisdomreminder.ui.wisdom

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.wisdomreminder.R
import com.example.wisdomreminder.model.Wisdom
import com.example.wisdomreminder.ui.components.GlassCard
import com.example.wisdomreminder.ui.main.MainViewModel
import com.example.wisdomreminder.ui.theme.CosmicBlack
import com.example.wisdomreminder.ui.theme.CyberBlue
import com.example.wisdomreminder.ui.theme.DeepSpace
import com.example.wisdomreminder.ui.theme.ElectricGreen
import com.example.wisdomreminder.ui.theme.GlassSurface
import com.example.wisdomreminder.ui.theme.GlassSurfaceDark
import com.example.wisdomreminder.ui.theme.NebulaPurple
import com.example.wisdomreminder.ui.theme.NeonPink
import com.example.wisdomreminder.ui.theme.StarWhite
import kotlinx.coroutines.flow.first
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WisdomDetailScreen(
    onBackClick: () -> Unit,
    wisdomId: Long,
    viewModel: MainViewModel
) {
    // Get the uiState from the viewModel
    val uiState by viewModel.uiState.collectAsState()

    // Extract wisdom lists from the current state
    val wisdomLists = when (uiState) {
        is MainViewModel.WisdomUiState.Success -> {
            val state = uiState as MainViewModel.WisdomUiState.Success
            Triple(state.activeWisdom, state.queuedWisdom, state.completedWisdom)
        }
        else -> Triple(emptyList(), emptyList(), emptyList())
    }

    // Destructure the lists
    val (activeWisdom, queuedWisdom, completedWisdom) = wisdomLists

    // Now proceed with the rest of your code
    val allWisdom = activeWisdom + queuedWisdom + completedWisdom
    val wisdom = allWisdom.find { it.id == wisdomId }

    // Fetch the wisdom details if not already loaded
    LaunchedEffect(wisdomId) {
        viewModel.getWisdomById(wisdomId)
    }

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
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
                            wisdom?.category?.uppercase() ?: "WISDOM DETAIL",
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
                    ),
                    actions = {
                        // Edit action
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = StarWhite
                            )
                        }

                        // Delete action
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = NeonPink
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                if (wisdom != null && !wisdom.isActive) {
                    FloatingActionButton(
                        onClick = { viewModel.activateWisdom(wisdom.id) },
                        containerColor = NebulaPurple,
                        contentColor = StarWhite
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Activate Wisdom"
                        )
                    }
                }
            }
        ) { paddingValues ->
            if (wisdom == null) {
                // Wisdom not found
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Wisdom not found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = StarWhite
                    )
                }
            } else {
                // Wisdom detail
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Status badge
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
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }

                    if (wisdom.isActive) {
                        // Progress section for active wisdom
                        GlassCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "21/21 RULE PROGRESS",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ElectricGreen
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Progress indicators
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    // Day progress
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(CircleShape)
                                                .background(GlassSurfaceDark),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(70.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        brush = Brush.radialGradient(
                                                            colors = listOf(
                                                                NeonPink.copy(alpha = 0.7f),
                                                                NeonPink.copy(alpha = 0.1f)
                                                            )
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${wisdom.currentDay}",
                                                    style = MaterialTheme.typography.displayMedium,
                                                    color = StarWhite
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "DAYS",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = NeonPink
                                        )

                                        Text(
                                            text = "of 21",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = StarWhite.copy(alpha = 0.7f)
                                        )
                                    }

                                    // Today's exposures
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(CircleShape)
                                                .background(GlassSurfaceDark),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(70.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        brush = Brush.radialGradient(
                                                            colors = listOf(
                                                                ElectricGreen.copy(alpha = 0.7f),
                                                                ElectricGreen.copy(alpha = 0.1f)
                                                            )
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${wisdom.exposuresToday}",
                                                    style = MaterialTheme.typography.displayMedium,
                                                    color = StarWhite
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "TODAY",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = ElectricGreen
                                        )

                                        Text(
                                            text = "of 21",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = StarWhite.copy(alpha = 0.7f)
                                        )
                                    }

                                    // Total exposures
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(CircleShape)
                                                .background(GlassSurfaceDark),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(70.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        brush = Brush.radialGradient(
                                                            colors = listOf(
                                                                CyberBlue.copy(alpha = 0.7f),
                                                                CyberBlue.copy(alpha = 0.1f)
                                                            )
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${wisdom.exposuresTotal}",
                                                    style = MaterialTheme.typography.displayMedium,
                                                    color = StarWhite
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "TOTAL",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = CyberBlue
                                        )

                                        Text(
                                            text = "exposures",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = StarWhite.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Daily exposures progress bar
                                Text(
                                    text = "Today's Exposures: ${wisdom.exposuresToday}/21",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StarWhite.copy(alpha = 0.8f)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                val dailyProgress = (wisdom.exposuresToday.toFloat() / 21f).coerceIn(0f, 1f)
                                androidx.compose.material3.LinearProgressIndicator(
                                    progress =  dailyProgress ,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp),
                                    color = ElectricGreen,
                                    trackColor = GlassSurface.copy(alpha = 0.3f)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Days progress bar
                                Text(
                                    text = "Days Completed: ${wisdom.currentDay}/21",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StarWhite.copy(alpha = 0.8f)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                val dayProgress = (wisdom.currentDay.toFloat() / 21f).coerceIn(0f, 1f)
                                androidx.compose.material3.LinearProgressIndicator(
                                    progress =  dayProgress ,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp),
                                    color = NeonPink,
                                    trackColor = GlassSurface.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }

                    // Wisdom content
                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "WISDOM",
                                style = MaterialTheme.typography.titleMedium,
                                color = NebulaPurple
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "\"${wisdom.text}\"",
                                style = MaterialTheme.typography.headlineLarge,
                                color = StarWhite,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (wisdom.source.isNotBlank()) {
                                Text(
                                    text = "— ${wisdom.source}",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontStyle = FontStyle.Italic
                                    ),
                                    color = CyberBlue,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Details card
                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "DETAILS",
                                style = MaterialTheme.typography.titleMedium,
                                color = CyberBlue
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' h:mm a")

                            DetailRow(
                                label = "Category",
                                value = wisdom.category,
                                labelColor = CyberBlue
                            )

                            DetailRow(
                                label = "Created",
                                value = wisdom.dateCreated.format(dateFormatter),
                                labelColor = CyberBlue
                            )

                            if (wisdom.startDate != null) {
                                DetailRow(
                                    label = "Started",
                                    value = wisdom.startDate.format(dateFormatter),
                                    labelColor = CyberBlue
                                )
                            }

                            if (wisdom.dateCompleted != null) {
                                DetailRow(
                                    label = "Completed",
                                    value = wisdom.dateCompleted.format(dateFormatter),
                                    labelColor = CyberBlue
                                )
                            }

                            DetailRow(
                                label = "Status",
                                value = when {
                                    wisdom.isActive -> "Active (Day ${wisdom.currentDay}/21)"
                                    wisdom.dateCompleted != null -> "Completed"
                                    else -> "Queued"
                                },
                                labelColor = CyberBlue
                            )

                            if (wisdom.isActive || wisdom.dateCompleted != null) {
                                DetailRow(
                                    label = "Total Exposures",
                                    value = wisdom.exposuresTotal.toString(),
                                    labelColor = CyberBlue
                                )
                            }
                        }
                    }

                    // Actions card for non-active wisdom
                    if (!wisdom.isActive) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "ACTIONS",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ElectricGreen
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { viewModel.activateWisdom(wisdom.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NebulaPurple
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = if (wisdom.dateCompleted != null) "REACTIVATE" else "ACTIVATE",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Activating this wisdom will begin the 21/21 rule process.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StarWhite.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp)) // Bottom spacing for FAB
                }

                // Edit dialog
                if (showEditDialog) {
                    EditWisdomDialog(
                        wisdom = wisdom,
                        onDismiss = { showEditDialog = false },
                        onSave = { text, source, category ->
                            viewModel.updateWisdom(
                                wisdom.copy(
                                    text = text,
                                    source = source,
                                    category = category
                                )
                            )
                            showEditDialog = false
                        }
                    )
                }

                // Delete confirmation dialog
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = {
                            Text("Delete Wisdom")
                        },
                        text = {
                            Text("Are you sure you want to delete this wisdom? This action cannot be undone.")
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.deleteWisdom(wisdom)
                                    showDeleteDialog = false
                                    onBackClick()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonPink
                                )
                            ) {
                                Text("DELETE")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showDeleteDialog = false }
                            ) {
                                Text("CANCEL")
                            }
                        }
                    )
                }
            }
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

    // Available categories
    val categories = listOf("General", "Personal", "Professional", "Health", "Relationships", "Philosophy", "Motivation")
    var showCategoryDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = GlassSurface,
                contentColor = StarWhite
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "EDIT WISDOM",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = CyberBlue
                )

                // Wisdom text field
                OutlinedTextField(
                    value = wisdomText,
                    onValueChange = { wisdomText = it },
                    label = { Text("Wisdom Text") },
                    placeholder = { Text("Enter your wisdom") },
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

                // Source field
                OutlinedTextField(
                    value = wisdomSource,
                    onValueChange = { wisdomSource = it },
                    label = { Text("Source (Optional)") },
                    placeholder = { Text("Author, book, etc.") },
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

                // Category field with dropdown
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                            TextButton(onClick = { showCategoryDropdown = !showCategoryDropdown }) {
                                Text(
                                    text = if (showCategoryDropdown) "Close" else "Select",
                                    color = CyberBlue
                                )
                            }
                        }
                    )

                    // Category dropdown
                    if (showCategoryDropdown) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                .background(GlassSurface.copy(alpha = 0.8f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                categories.forEach { category ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                            .background(
                                                if (category == wisdomCategory)
                                                    CyberBlue.copy(alpha = 0.2f)
                                                else
                                                    Color.Transparent
                                            )
                                            .padding(12.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        TextButton(
                                            onClick = {
                                                wisdomCategory = category
                                                showCategoryDropdown = false
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = category,
                                                color = if (category == wisdomCategory)
                                                    CyberBlue
                                                else
                                                    StarWhite,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text("CANCEL")
                    }

                    Button(
                        onClick = {
                            if (wisdomText.isNotBlank()) {
                                onSave(
                                    wisdomText.trim(),
                                    wisdomSource.trim(),
                                    wisdomCategory.trim()
                                )
                            }
                        },
                        enabled = wisdomText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberBlue
                        )
                    ) {
                        Text("UPDATE")
                    }
                }
            }
        }
    }
}