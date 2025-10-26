package com.example.aibudgetapp.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aibudgetapp.ui.screens.transaction.Transaction
import androidx.compose.ui.Alignment
import com.example.aibudgetapp.ui.screens.budget.Budget
import java.time.LocalDate
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.example.aibudgetapp.notifications.NotificationLog
import com.example.aibudgetapp.notifications.NotificationEvent

enum class ReportPeriod { WEEK, MONTH, YEAR }

@Composable
fun ExportReportScreen(
    transactions: List<Transaction>,
    budgets: List<Budget>,
    fetchBudgets: () -> Unit
) {
    LaunchedEffect(Unit) {
        fetchBudgets()
    }
    var selectedPeriod by remember { mutableStateOf("Weekly") }
    val periodOptions = listOf("Weekly", "Monthly", "Yearly")
    val context = LocalContext.current
    val budgetsByPeriod = budgets.filter { it.chosenType.equals(selectedPeriod, ignoreCase = true) }
    val now = LocalDate.now()

    //Notification trigger
    LaunchedEffect(budgets) {
        budgets.forEach { budget ->
            val end = budget.endDate?.takeIf { it.isNotBlank() }
            val endDate = runCatching { end?.let { LocalDate.parse(it) } }.getOrNull()
            if (endDate != null && !endDate.isAfter(now)) {
                if (NotificationLog.getAll(context).none { it.periodId == budget.id }) {
                    NotificationLog.log(
                        context,
                        NotificationEvent(
                            id = System.currentTimeMillis(),
                            label = budget.chosenType,
                            periodId = budget.id,
                            percent = 0.0,
                            spent = 0.0,
                            budget = budget.amount,
                            message = "Final report for your ${budget.chosenType.lowercase()} budget '${budget.name}' is ready!",
                            read = false
                        )
                    )
                }
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Budget Reports", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        // Period picker
        Row(verticalAlignment = Alignment.CenterVertically) {
            periodOptions.forEach { period ->
                FilterChip(
                    selected = selectedPeriod.equals(period, ignoreCase = true),
                    onClick = { selectedPeriod = period },
                    label = { Text(period) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            // Build CSV/text report string
            val csv = buildString {
                append("Period: $selectedPeriod\n\n")
                budgetsByPeriod.forEach { budget ->
                    append("Budget: ${budget.name} (${budget.chosenType})\n")
                    if (!budget.chosenType.equals("Yearly", true) && !budget.chosenCategory.isNullOrBlank())
                        append("Category: ${budget.chosenCategory}\n")
                    append("From: ${budget.startDate} To: ${budget.endDate}\n")
                    append("Budget Amount: ${"%.2f".format(budget.amount)}\n")
                    val budgetTxns = transactions.filter { tx ->
                        val txDate = try { LocalDate.parse(tx.date) } catch (_: Exception) { null }
                        val inRange = txDate != null &&
                                (budget.startDate == null || !txDate.isBefore(LocalDate.parse(budget.startDate))) &&
                                (budget.endDate == null || !txDate.isAfter(LocalDate.parse(budget.endDate)))
                        inRange && (
                                budget.chosenType.equals("Yearly", ignoreCase = true)
                                        || tx.category == budget.chosenCategory
                                )
                    }
                    val spent = budgetTxns.sumOf { it.amount ?: 0.0 }
                    append("Spent: ${"%.2f".format(spent)}\n")
                    append("Transactions:\n")
                    budgetTxns.forEach { tx ->
                        append("- ${tx.date}: ${tx.description} (${tx.amount})\n")
                    }
                    append("\n")
                }
            }
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, csv)
                putExtra(Intent.EXTRA_SUBJECT, "AI Budgeting App Report")
            }
            context.startActivity(Intent.createChooser(intent, "Share Report"))
        }) {
            Text("Share/Download Report")
        }

        if (budgetsByPeriod.isEmpty()) {
            Text("No $selectedPeriod budgets found.")
            return
        }

        budgetsByPeriod.forEach { budget ->
            val start = budget.startDate?.takeIf { it.isNotBlank() }
            val end = budget.endDate?.takeIf { it.isNotBlank() }
            val budgetTxns = transactions.filter { tx ->
                val txDate = try { LocalDate.parse(tx.date) } catch (_: Exception) { null }
                val inRange = txDate != null &&
                        (start == null || !txDate.isBefore(LocalDate.parse(start))) &&
                        (end == null || !txDate.isAfter(LocalDate.parse(end)))
                inRange && (
                        selectedPeriod.equals("Yearly", ignoreCase = true)
                                || tx.category == budget.chosenCategory
                        )
            }
            val spent = budgetTxns.sumOf { it.amount ?: 0.0 }
            val overspent = (spent - budget.amount).coerceAtLeast(0.0)
            val remaining = (budget.amount - spent).coerceAtLeast(0.0)

            Text("Budget: ${"%.2f".format(budget.amount)}")
            Text("Spent: ${"%.2f".format(spent)}")
            Text("Overspent: ${"%.2f".format(overspent)}", color = MaterialTheme.colorScheme.error)
            Text("Remaining: ${"%.2f".format(remaining)}", color = MaterialTheme.colorScheme.primary)

            Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("${budget.name} (${budget.chosenType})", style = MaterialTheme.typography.titleMedium)
                    if (!budget.chosenType.equals("Yearly", true) && !budget.chosenCategory.isNullOrBlank())
                        Text("Category: ${budget.chosenCategory}")
                    Text("From: ${budget.startDate}  To: ${budget.endDate}")
                    Text("Budget: $${budget.amount}")
                    Text("Spent: $spent")
                    if (overspent > 0)
                        Text("Overspent: $overspent", color = MaterialTheme.colorScheme.error)
                    else
                        Text("Remaining: $remaining", color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(6.dp))
                    Text("Matching Transactions:", style = MaterialTheme.typography.bodySmall)
                    if (budgetTxns.isEmpty())
                        Text("None")
                    else
                        budgetTxns.forEach { tx ->
                            Text("${tx.date} - ${tx.description}: ${tx.amount}", style = MaterialTheme.typography.bodySmall)
                        }
                }
            }
        }
    }
}
