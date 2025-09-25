@file:OptIn(ExperimentalMaterial3Api::class)
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.example.aibudgetapp.ui.theme.LocalThemeController
import com.example.aibudgetapp.ui.theme.ThemeMode


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
    onConfirmEditName: (String) -> Unit,
    onDeleteAccount: () -> Unit,
) {

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val themeController = LocalThemeController.current

    val tiles = listOf(
        SettingTileData("Reminders", Icons.Filled.Notifications, onNavigateReminders),
        SettingTileData("Themes", Icons.Filled.DarkMode, {showThemeDialog = true}),
        SettingTileData("Passcode", Icons.Filled.Lock, onNavigatePasscode),
        SettingTileData("Reminders", Icons.Filled.Notifications, onNavigateReminders),
        SettingTileData("Export Data", Icons.Filled.Storage, onNavigateExport),
        SettingTileData("Uploaded Data", Icons.Filled.UploadFile, onNavigateUploads),
        SettingTileData("FAQ", Icons.Filled.HelpOutline, onNavigateFaq),
    )

    var showEdit by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(uiState.displayName) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    AssistChip(
                        onClick = {
                            newName = uiState.displayName
                            showEdit = true
                            onEditProfile()
                        },
                        label = { Text("Edit Profile") },
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) }
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(uiState.avatarInitials, fontSize = 16.sp)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(uiState.displayName, fontSize = 14.sp)
                    }
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    AssistChip(
                        onClick = onAddUser,
                        label = { Text("Add user") },
                        leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) }
                    )
                }
            }

            if (showEdit) {
                ModalBottomSheet(
                    onDismissRequest = { showEdit = false },
                    sheetState = sheetState
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Text("Edit profile", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            singleLine = true,
                            label = { Text("Display name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showEdit = false }) { Text("Cancel") }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                enabled = newName.isNotBlank(),
                                onClick = {
                                    onConfirmEditName(newName.trim())
                                    showEdit = false
                                }
                            ) { Text("Save") }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
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

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(120.dp)
                            .fillMaxWidth()
                            .clickable { showDeleteConfirm = true },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete Account",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                            Text(
                                text = "Delete account",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(onClick = onLogout) {
                    Text("Log Out")
                }
            }
        }

        if (showThemeDialog) {
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                title = { Text("Theme") },
                text = {
                    Column {
                        TextButton(onClick = {
                            themeController.mode = ThemeMode.FollowSystem
                            showThemeDialog = false
                        }) { Text("Follow system") }

                        TextButton(onClick = {
                            themeController.mode = ThemeMode.Light
                            showThemeDialog = false
                        }) { Text("Light") }

                        TextButton(onClick = {
                            themeController.mode = ThemeMode.Dark
                            showThemeDialog = false
                        }) { Text("Dark") }
                    }
                },
                confirmButton = {},
                dismissButton = {}
            )
        }



        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirm = false
                            onDeleteAccount()
                        }
                    ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Cancel")
                    }
                },
                title = {
                    Text("DELETE ACCOUNT?", color = MaterialTheme.colorScheme.error)
                },
                text = {
                    Text("This will permanently remove your account and data.")
                }
            )
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

