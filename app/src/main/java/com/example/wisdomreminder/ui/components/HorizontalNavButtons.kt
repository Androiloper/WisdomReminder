package com.example.wisdomreminder.ui.components // Or directly in MainScreen.kt initially

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wisdomreminder.ui.theme.CyberBlue
import com.example.wisdomreminder.ui.theme.ElectricGreen
import com.example.wisdomreminder.ui.theme.GlassSurface
import com.example.wisdomreminder.ui.theme.NebulaPurple
import com.example.wisdomreminder.ui.theme.StarWhite

// Definition for NavButtonInfo and HorizontalNavButtons (can be moved to a components file)
// Definition for NavButtonInfo (remains the same)
data class NavButtonInfo(
    val label: String,
    val onClick: () -> Unit,
    val color: Color
)

@Composable
fun HorizontalNavButtons( // Updated to use GlassCard style
    buttons: List<NavButtonInfo>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(Color.Transparent) // Row itself is transparent, cards will have background
            .padding(horizontal = 8.dp, vertical = 10.dp), // Padding for the entire row
        horizontalArrangement = Arrangement.spacedBy(10.dp) // Spacing between card-buttons
    ) {
        buttons.forEach { buttonInfo ->
            GlassCard( // Use GlassCard for each button
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium) // Ensure clipping for clickable
                    .clickable(onClick = buttonInfo.onClick)
                    .defaultMinSize(minHeight = 42.dp) // Ensure a good tap height
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 10.dp), // Internal padding for text
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = buttonInfo.label.uppercase(),
                        style = MaterialTheme.typography.bodySmall, // Consistent text style
                        fontWeight = FontWeight.Bold,
                        color = buttonInfo.color // Use the button's specific color for the text
                    )
                }
            }
        }
    }
}