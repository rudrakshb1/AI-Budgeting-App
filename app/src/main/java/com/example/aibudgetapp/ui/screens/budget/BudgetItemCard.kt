package com.example.aibudgetapp.ui.screens.budget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BudgetItemCard(budget: Budget) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = budget.name, style = MaterialTheme.typography.titleMedium)
            Text(text = "Amount: \$${budget.amount}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Category: ${budget.chosenCategory}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Type: ${budget.chosenType}", style = MaterialTheme.typography.bodySmall)
            // Text(text = "Date: ${budget.selectedDate}", style = MaterialTheme.typography.bodySmall)
            Text(text = "From: ${budget.startDate}", style = MaterialTheme.typography.bodySmall)
            Text(text = "To: ${budget.endDate}", style = MaterialTheme.typography.bodySmall)
        }
    }
}