package com.example.wisdomreminder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border // New import
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn // New import
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.wisdomreminder.ui.wisdom.CategoryDropdownItem // Import if you made it public, or redefine here


@Composable
fun CategorySelectionDialog(
    availableCategories: List<String>,
    allCategoriesInSystem: List<String>,
    selectedCategoriesOnDashboard: List<String>,
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
                .padding(16.dp), // Dialog padding
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp) // Internal padding for card content
            ) {
                Text(
                    text = "ADD CATEGORY TO DASHBOARD", // Title
                    style = MaterialTheme.typography.headlineSmall,
                    color = NeonPink
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Choose a category to add:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StarWhite.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box( // Box to constrain LazyColumn height
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false) // Allow it to take space but not push buttons
                        .heightIn(max = 250.dp) // Max height for the list area
                        .border(1.dp, NebulaPurple.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(8.dp) // Padding inside the bordered box
                    ) {
                        if (availableCategories.isEmpty()) {
                            item {
                                Text(
                                    text = if (allCategoriesInSystem.isEmpty()) {
                                        "No categories defined yet. Add wisdom with categories first."
                                    } else {
                                        "All available categories are already on your dashboard."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = StarWhite.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            items(availableCategories) { category ->
                                // Using the same CategoryDropdownItem for consistent UI
                                CategoryDropdownItem(
                                    text = category,
                                    isSelected = category == categoryToAdd,
                                    onClick = { categoryToAdd = category }
                                )
                            }
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
                    ) { Text("CANCEL") }
                    Button(
                        onClick = {
                            categoryToAdd?.let { onCategorySelected(it) }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NebulaPurple),
                        enabled = categoryToAdd != null,
                        modifier = Modifier.padding(start = 16.dp)
                    ) { Text("ADD") }
                }
            }
        }
    }
}