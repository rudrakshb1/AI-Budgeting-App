package com.example.aibudgetapp.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(onBack: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Frequently Asked Questions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FaqItem(
                question = "How do I add a new transaction?",
                answer = "Tap the '+' button on your dashboard, then enter the transaction details."
            )
            FaqItem(
                question = "How can I edit or delete a budget?",
                answer = "Go to your Budget Overview, select the budget, and tap 'Edit' or 'Delete'."
            )
            FaqItem(
                question = "How do I set up a budget?",
                answer = "Navigate to the 'Budgets' tab, tap 'Create Budget', and define your spending limits."
            )
            FaqItem(
                question = "Is my financial data secure?",
                answer = "Yes, all your data is encrypted and securely stored using Firebase Authentication and Firestore."
            )
            FaqItem(
                question = "How do I change my password?",
                answer = "Go to Settings then, Passcode and follow the on-screen instructions."
            )
        }
    }
}

@Composable
fun FaqItem(question: String, answer: String) {
    Column {
        Text(
            text = question,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            fontSize = 18.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = answer,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
