package com.example.aibudgetapp.notifications


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var items by remember { mutableStateOf(NotificationLog.getAll(ctx)) }

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
                }
            )
        }
    ) { innerPadding ->
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
                items(items) { e ->
                    ListItem(
                        headlineContent = { Text(e.message) },
                        supportingContent = { Text("${e.label} • ${e.periodId}") },
                        overlineContent = {
                            Text(DateFormat.getDateTimeInstance().format(Date(e.id)))
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
