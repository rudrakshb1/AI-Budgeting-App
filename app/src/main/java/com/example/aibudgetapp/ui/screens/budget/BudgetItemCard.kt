package com.example.aibudgetapp.ui.screens.budget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetItemCard(budget: Budget, onEdit: (Budget) -> Unit, onDelete: (String) -> Unit) {
    var showBottomSheet by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { showBottomSheet = true },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = budget.name, style = MaterialTheme.typography.titleMedium)
            Text(text = "Amount: \$${budget.amount}", style = MaterialTheme.typography.bodyMedium)

            // Only show category if not Yearly and not null/blank
            if (!budget.chosenType.equals("Yearly", ignoreCase = true) &&
                !budget.chosenCategory.isNullOrBlank()
            ) {
                Text(
                    text = "Category: ${budget.chosenCategory}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(text = "Type: ${budget.chosenType}", style = MaterialTheme.typography.bodySmall)
            Text(text = "From: ${budget.startDate}", style = MaterialTheme.typography.bodySmall)
            Text(text = "To: ${budget.endDate}", style = MaterialTheme.typography.bodySmall)
        }
    }
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        showBottomSheet = false
                        onEdit(budget)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit Budget")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        showBottomSheet = false
                        onDelete(budget.id)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete Budget")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
