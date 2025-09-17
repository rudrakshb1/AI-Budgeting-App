package com.example.aibudgetapp.ui.screens.settings

import com.example.aibudgetapp.ui.screens.settings.SettingsUiState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    bottomBar: @Composable (() -> Unit)? = null,
    onMenu: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onAddUser: () -> Unit = {},
    onNavigatePasscode: () -> Unit = {},
    onNavigateReminders: () -> Unit = {},
    onNavigateExport: () -> Unit = {},
    onNavigateUploads: () -> Unit = {},
    onNavigateFaq: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val tiles = listOf(
        SettingTileData("Passcode", Icons.Filled.Lock, onNavigatePasscode),
        SettingTileData("Reminders", Icons.Filled.Notifications, onNavigateReminders),
        SettingTileData("Export Data", Icons.Filled.Storage, onNavigateExport),
        SettingTileData("Uploaded Data", Icons.Filled.UploadFile, onNavigateUploads),
        SettingTileData("FAQ", Icons.Filled.HelpOutline, onNavigateFaq),
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = "AI Budgeting App",
                onMenu = onMenu
            )
        },
        bottomBar = { bottomBar?.invoke() }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = onEditProfile,
                    label = { Text("Edit profile") },
                    leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) }
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(uiState.avatarInitials, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(uiState.displayName, fontSize = 12.sp)
                }

                AssistChip(
                    onClick = onAddUser,
                    label = { Text("Add user") },
                    leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) }
                )
            }

            Spacer(Modifier.height(12.dp))

            LazyVerticalGrid(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tiles) { tile -> SettingTile(tile) }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(onClick = onLogout) {
                    Text("Log out")
                }
            }
        }
    }
}


private data class SettingTileData(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun AppTopBar(
    title: String,
    onMenu: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenu) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun SettingTile(data: SettingTileData) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .height(120.dp)
            .fillMaxWidth()
            .clickable { data.onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(data.icon, contentDescription = data.title)
            }
            Text(
                text = data.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun PreviewSettings() {
    MaterialTheme {
        SettingsScreen(
            uiState = SettingsUiState(displayName = "Preview User", avatarInitials = "P")
        )
    }
}

