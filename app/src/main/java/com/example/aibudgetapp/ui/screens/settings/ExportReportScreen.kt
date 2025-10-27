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
import java.time.temporal.ChronoUnit

enum class ReportPeriod { WEEK, MONTH, YEAR }



private fun parseLocalDateOrNull(s: String?): LocalDate? =
    try { if (s.isNullOrBlank()) null else LocalDate.parse(s) } catch (_: Exception) { null }

private fun unitFor(type: String?): String = when (type?.lowercase()) {
    "weekly" -> "week"
    "monthly" -> "month"
    else -> "year"
}

/** derive N periods from start..end; min 1; uses existing dates + chosenType */
private fun periodsFor(type: String?, start: String?, end: String?): Int {
    val s = parseLocalDateOrNull(start)
    val e = parseLocalDateOrNull(end)
    if (s == null || e == null || e.isBefore(s)) return 1
    return when (type?.lowercase()) {
        "weekly"  -> (ChronoUnit.WEEKS.between(s, e).toInt() + 1).coerceAtLeast(1)
        "monthly" -> (ChronoUnit.MONTHS.between(s.withDayOfMonth(1), e.withDayOfMonth(1)).toInt() + 1).coerceAtLeast(1)
        "yearly"  -> (ChronoUnit.YEARS.between(s.withDayOfYear(1), e.withDayOfYear(1)).toInt() + 1).coerceAtLeast(1)
        else -> 1
    }
}


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


    LaunchedEffect(budgets) {
        val now = LocalDate.now()
        budgets.forEach { budget ->
            val endDate = runCatching { budget.endDate?.let { LocalDate.parse(it) } }.getOrNull()
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

            val csv = buildString {
                append("Period: $selectedPeriod\n\n")
                budgetsByPeriod.forEach { budget ->
                    val unit = unitFor(budget.chosenType)
                    val n = periodsFor(budget.chosenType, budget.startDate, budget.endDate)
                    val totalBudget = budget.amount * n

                    append("Budget: ${budget.name} (${budget.chosenType})\n")
                    if (!budget.chosenType.equals("Yearly", true) && !budget.chosenCategory.isNullOrBlank())
                        append("Category: ${budget.chosenCategory}\n")
                    append("From: ${budget.startDate} To: ${budget.endDate}\n")
                    append("Budget: $${"%.2f".format(budget.amount)} per $unit (Total: $${"%.2f".format(totalBudget)} for $n ${if (n==1) unit else "${unit}s"})\n")

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
                    val delta = totalBudget - spent
                    append("Total spending: $${"%.2f".format(spent)} (in budget period)\n")
                    if (delta >= 0) append("Remaining: $${"%.2f".format(delta)}\n")
                    else append("Over by: $${"%.2f".format(-delta)}\n")

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
                putExtra(Intent.EXTRA_EMAIL, arrayOf(""))
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
            val unit = unitFor(budget.chosenType)
            val n = periodsFor(budget.chosenType, start, end)
            val totalBudget = budget.amount * n

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
            val delta = totalBudget - spent

            Text("Budget: $${"%.2f".format(budget.amount)} per $unit (Total: $${"%.2f".format(totalBudget)} for $n ${if (n==1) unit else "${unit}s"})")
            Text("Total spending: $${"%.2f".format(spent)} (in budget period)")
            if (delta >= 0) {
                Text("Remaining: $${"%.2f".format(delta)}", color = MaterialTheme.colorScheme.primary)
            } else {
                Text("Over by: $${"%.2f".format(-delta)}", color = MaterialTheme.colorScheme.error)
            }

            Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("${budget.name} (${budget.chosenType})", style = MaterialTheme.typography.titleMedium)
                    if (!budget.chosenType.equals("Yearly", true) && !budget.chosenCategory.isNullOrBlank())
                        Text("Category: ${budget.chosenCategory}")
                    Text("From: ${budget.startDate}  To: ${budget.endDate}")
                    Text("Budget: $${"%.2f".format(budget.amount)} per $unit")
                    Text("Total budget: $${"%.2f".format(totalBudget)} (x$n ${if (n==1) unit else "${unit}s"})")
                    Text("Spent: $${"%.2f".format(spent)}")
                    if (delta >= 0)
                        Text("Remaining: $${"%.2f".format(delta)}", color = MaterialTheme.colorScheme.primary)
                    else
                        Text("Over by: $${"%.2f".format(-delta)}", color = MaterialTheme.colorScheme.error)
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
