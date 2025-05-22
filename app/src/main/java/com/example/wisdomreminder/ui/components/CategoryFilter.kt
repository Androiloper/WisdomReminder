package com.example.wisdomreminder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.wisdomreminder.ui.theme.*
import com.example.wisdomreminder.ui.wisdom.CategoryDropdownItem // Assuming reuse

@Composable
fun CategoryFilter(
    allCategories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCategoryDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.Right // Keeps it to the right
    ) {
        Button(
            onClick = { showCategoryDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = NebulaPurple.copy(alpha = 0.7f)
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = "Filter by Category",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = selectedCategory ?: "All Categories",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    }

    if (showCategoryDialog) {
        FilterCategorySelectionDialog( // Renamed to avoid confusion with the dashboard one
            categories = listOf("All Categories") + allCategories.distinct().sorted(),
            currentSelectedCategory = selectedCategory ?: "All Categories",
            onCategorySelected = { category ->
                onCategorySelected(if (category == "All Categories") null else category)
                showCategoryDialog = false
            },
            onDismiss = { showCategoryDialog = false }
        )
    }
}

@Composable
private fun FilterCategorySelectionDialog(
    categories: List<String>,
    currentSelectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = GlassSurface.copy(alpha = 0.95f), // Slightly more opaque
                contentColor = StarWhite
            ),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Filter by Category",
                    style = MaterialTheme.typography.titleLarge, // Changed to titleLarge
                    color = ElectricGreen,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Box(modifier = Modifier.heightIn(max = 300.dp)) { // Constrain height for LazyColumn
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(categories) { category ->
                            CategoryDropdownItem( // Reusing the styled item
                                text = category,
                                isSelected = category == currentSelectedCategory,
                                onClick = { onCategorySelected(category) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("CLOSE", color = StarWhite.copy(alpha = 0.7f))
                }
            }
        }
    }
}
// If CategoryDropdownItem is not public from AddWisdomDialog.kt, you'll need its definition here too:
// @Composable
// fun CategoryDropdownItem(text: String, isSelected: Boolean, onClick: () -> Unit) { ... }