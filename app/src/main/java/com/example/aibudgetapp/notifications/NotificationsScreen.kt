package com.example.aibudgetapp.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var items by remember { mutableStateOf(NotificationLog.getAll(ctx)) }
    var showConfirmClear by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        NotificationLog.markAllRead(ctx)
        items = NotificationLog.getAll(ctx)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (items.isNotEmpty()) {
                        IconButton(onClick = { showConfirmClear = true }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Clear all")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->

        // ADDED: confirm dialog for "Clear all"
        if (showConfirmClear) {
            AlertDialog(
                onDismissRequest = { showConfirmClear = false },
                title = { Text("Clear all notifications?") },
                text  = { Text("This will remove all saved notifications.") },
                confirmButton = {
                    TextButton(onClick = {
                        NotificationLog.clearAll(ctx)     // ADDED
                        items = NotificationLog.getAll(ctx) // ADDED: refresh UI
                        showConfirmClear = false
                    }) { Text("Clear all") }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmClear = false }) { Text("Cancel") }
                }
            )
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No notifications yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(items, key = { it.id }) { e ->
                    ListItem(
                        headlineContent = { Text(e.message) },
                        supportingContent = { Text("${e.label} • ${e.periodId}") },
                        overlineContent = {
                            Text(DateFormat.getDateTimeInstance().format(Date(e.id)))
                        },
                        trailingContent = {
                            IconButton(
                                onClick = {
                                    val removed = NotificationLog.deleteOne(ctx, e.id)
                                    if (removed) {
                                        items = NotificationLog.getAll(ctx)
                                    }
                                }
                            ) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
