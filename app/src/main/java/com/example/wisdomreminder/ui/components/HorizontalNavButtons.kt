package com.example.wisdomreminder.ui.components // Or directly in MainScreen.kt initially

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wisdomreminder.ui.theme.CyberBlue
import com.example.wisdomreminder.ui.theme.ElectricGreen
import com.example.wisdomreminder.ui.theme.GlassSurface
import com.example.wisdomreminder.ui.theme.NebulaPurple
import com.example.wisdomreminder.ui.theme.StarWhite

// Definition for NavButtonInfo and HorizontalNavButtons (can be moved to a components file)
data class NavButtonInfo(
    val label: String,
    val onClick: () -> Unit,
    val color: Color
)

@Composable
fun HorizontalNavButtons(
    buttons: List<NavButtonInfo>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(GlassSurface.copy(alpha = 0.2f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp) // Slightly reduced spacing if many buttons
    ) {
        buttons.forEach { buttonInfo ->
            Button(
                onClick = buttonInfo.onClick,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonInfo.color.copy(alpha = 0.9f),
                    contentColor = StarWhite
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp) // Adjusted padding
            ) {
                Text(
                    text = buttonInfo.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall, // Adjusted for potentially smaller buttons if more are added
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}