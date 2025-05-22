package com.example.wisdomreminder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.wisdomreminder.ui.theme.CyberBlue
import com.example.wisdomreminder.ui.theme.GlassSurface
import com.example.wisdomreminder.ui.theme.NebulaPurple
import com.example.wisdomreminder.ui.theme.NeonPink
import com.example.wisdomreminder.ui.theme.StarWhite

/**
 * Dialog for selecting a category to display as a card
 */
@Composable
fun CategorySelectionDialog(
    availableCategories: List<String>, // Categories that can be added (not already on dashboard)
    allCategoriesInSystem: List<String>, // All unique categories from the database
    selectedCategoriesOnDashboard: List<String>, // Categories currently on the dashboard
    onDismiss: () -> Unit,
    onCategorySelected: (String) -> Unit
) {
    var categoryToAdd by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = GlassSurface,
                contentColor = StarWhite
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "SELECT CATEGORY",
                    style = MaterialTheme.typography.headlineSmall,
                    color = NeonPink
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Choose a category to add to your dashboard:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StarWhite.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                ) {
                    if (availableCategories.isEmpty()) {
                        item {
                            Text(
                                text = if (allCategoriesInSystem.isEmpty()) {
                                    "No categories defined in the app. Add wisdom with categories first."
                                } else {
                                    "All available categories are already on your dashboard."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = StarWhite.copy(alpha = 0.6f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(availableCategories) { category ->
                            CategoryItem(
                                category = category,
                                isSelected = category == categoryToAdd,
                                onSelect = { categoryToAdd = category }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
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
                        onClick = {
                            categoryToAdd?.let { onCategorySelected(it) }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NebulaPurple
                        ),
                        enabled = categoryToAdd != null,
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Text("ADD")
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryItem( // This can remain private if only used here
    category: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(MaterialTheme.shapes.small)
            .background(
                if (isSelected) NeonPink.copy(alpha = 0.2f)
                else NebulaPurple.copy(alpha = 0.1f)
            )
            .clickable(onClick = onSelect)
            .padding(16.dp)
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) NeonPink else CyberBlue
        )
    }
}