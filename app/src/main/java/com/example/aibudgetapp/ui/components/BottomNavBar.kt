package com.example.aibudgetapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme


// TODO: Replace Material icons with custom SVGs, similar to the ones on Figma
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings

@Composable
fun BottomNavBar(
    onHomeClick: () -> Unit,
    onBudgetClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline,
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = { Icon(Icons.Filled.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.onSurface) },
                label = "Home",
                onClick = onHomeClick
            )
            NavItem(
                icon = { Icon(Icons.Filled.Add, contentDescription = "Budget", tint = MaterialTheme.colorScheme.onSurface) },
                label = "Budget",
                onClick = onBudgetClick
            )
            NavItem(
                icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface) },
                label = "Settings",
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
private fun NavItem(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon()
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomNavBarPreview() {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.weight(1f))
        BottomNavBar(
            onHomeClick = {},
            onBudgetClick = {},
            onSettingsClick = {}
        )
    }
}
