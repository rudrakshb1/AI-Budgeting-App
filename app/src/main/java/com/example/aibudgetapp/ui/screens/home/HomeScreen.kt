package com.example.aibudgetapp.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aibudgetapp.notifications.NotificationLog
import com.example.aibudgetapp.ui.components.LineChart
import com.example.aibudgetapp.ui.screens.budget.BudgetRepository
import com.example.aibudgetapp.ui.screens.screenContainer.Screen
import com.example.aibudgetapp.ui.screens.screenContainer.ScreenContainerViewModel
import com.example.aibudgetapp.ui.screens.settings.SettingsUiState
import com.example.aibudgetapp.ui.screens.transaction.TransactionRepository
import com.example.aibudgetapp.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.example.aibudgetapp.notifications.ThresholdNotifier
import kotlin.math.round
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.time.Year

@Composable
private fun EnsureNotifPermission() {
    if (Build.VERSION.SDK_INT >= 33) {
        val context = LocalContext.current
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { /* granted/denied — no-op */ }

        LaunchedEffect(Unit) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun HomeScreen(
    uiState: SettingsUiState,
    screenContainerViewModel: ScreenContainerViewModel,
    onBellClick: () -> Unit = {}

) {
    EnsureNotifPermission()
    val homeViewModel = remember { HomeViewModel(BudgetRepository(), TransactionRepository()) }

    LaunchedEffect(Unit) {
        homeViewModel.getMonthlyBudgetList()
        homeViewModel.getWeeklyBudgetList()
        homeViewModel.get12MonthlyTransaction()
        homeViewModel.get12WeeklyTransaction()
    }

    val budgetError = homeViewModel.budgetError
    val budgetErrorMessage = homeViewModel.budgetErrorMessage
    val transactionError = homeViewModel.transactionError
    val transactionErrorMessage = homeViewModel.transactionErrorMessage
    val monthlyBudget = homeViewModel.monthlyBudgetList.lastOrNull() ?: 0.0
    val monthlyBudgetList = homeViewModel.monthlyBudgetList
    val monthlySpent = homeViewModel.monthlyTransactionList.lastOrNull() ?: 0.0
    val monthly12Spent = homeViewModel.monthlyTransactionList
    val monthLabels = homeViewModel.monthLabels
    val weeklyBudget = homeViewModel.weeklyBudgetList.lastOrNull() ?: 0.0
    val weeklyBudgetList = homeViewModel.weeklyBudgetList
    val weeklySpent = homeViewModel.weeklyTransactionList.lastOrNull() ?: 0.0
    val weekly12Spent = homeViewModel.weeklyTransactionList
    val weekLabels = homeViewModel.weekLabels
    val context = LocalContext.current
    val now = LocalDate.now()
    val monthId = now.format(DateTimeFormatter.ofPattern("yyyy-MM"))
    val wf = WeekFields.ISO
    val weekId = "${now.get(wf.weekBasedYear())}-W${now.get(wf.weekOfWeekBasedYear())}"

    // fire threshold checks when values change
    LaunchedEffect(monthlyBudget, monthlySpent, weeklyBudget, weeklySpent) {
        ThresholdNotifier.backfillIfNeeded(
            context, "Weekly", weekId, weeklySpent, weeklyBudget.toDouble()
        )
        ThresholdNotifier.backfillIfNeeded(
            context, "Monthly", monthId, monthlySpent, monthlyBudget.toDouble()
        )
        ThresholdNotifier.maybeNotifyCrossing(
            context, "Weekly", weekId, weeklySpent, weeklyBudget.toDouble()
        )
        ThresholdNotifier.maybeNotifyCrossing(
            context, "Monthly", monthId, monthlySpent, monthlyBudget.toDouble()
        )
    }

    //YEARLY threshold check (single yearly budget)
    val yearId = LocalDate.now().year.toString()

    LaunchedEffect(Unit) {
        val uid = Firebase.auth.currentUser?.uid ?: return@LaunchedEffect
        val db = Firebase.firestore

        // 1) read the (single) Yearly budget
        db.collection("users").document(uid).collection("budgets")
            .whereEqualTo("chosentype", "Yearly")
            .limit(1)
            .get()
            .addOnSuccessListener { budSnap ->
                // get amount as Double (handles Number or String)
                val yearlyBudget = budSnap.documents.firstOrNull()?.get("amount")?.let {
                    when (it) {
                        is Number -> it.toDouble()
                        is String -> it.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }
                } ?: 0.0

                android.util.Log.d("YearlyCheck", "Budget doc found=${budSnap.size()}; yearlyBudget=$yearlyBudget")

                // 2) sum YTD spending (your app counts only NEGATIVE totals as "spent")
                val start = "$yearId-01-01"
                val end = "$yearId-12-31"

                db.collection("users").document(uid).collection("transactions")
                    .whereGreaterThanOrEqualTo("date", start)
                    .whereLessThanOrEqualTo("date", end)
                    .get()
                    .addOnSuccessListener { txSnap ->
                        fun num(v: Any?): Double = when (v) {
                            is Number -> v.toDouble()
                            is String -> v.toDoubleOrNull() ?: 0.0
                            else -> 0.0
                        }

                        val yearlySpent = txSnap.documents.sumOf { d ->
                            val a = num(d.get("amount"))
                            val debit = num(d.get("debit"))
                            kotlin.math.abs(a) + kotlin.math.abs(debit)
                        }


                        val pct = if (yearlyBudget > 0.0) (yearlySpent / yearlyBudget) * 100.0 else 0.0
                        android.util.Log.d("YearlyCheck", "Tx count=${txSnap.size()}; yearlySpent=$yearlySpent; pct=$pct")

                        // 3) write backfill + maybe fire the OS notification (once per year)
                        ThresholdNotifier.backfillIfNeeded(
                            context = context,
                            label = "Yearly",
                            periodId = yearId,
                            spent = yearlySpent,
                            budget = yearlyBudget
                        )
                        val posted = ThresholdNotifier.maybeNotifyCrossing(
                            context = context,
                            label = "Yearly",
                            periodId = yearId,
                            spent = yearlySpent,
                            budget = yearlyBudget
                        )
                        android.util.Log.d("YearlyCheck", "maybeNotifyYearly posted=$posted")
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.e("YearlyCheck", "Failed to load yearly transactions", e)
                    }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("YearlyCheck", "Failed to load yearly budget", e)
            }
    }



    var badgeCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        // refresh when entering Home; opening Notifications screen will mark as read
        badgeCount = NotificationLog.getUnreadCount(context)
    }

    AIBudgetAppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .offset(y = (-32).dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 0.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp
                )
            ) {
                item {
                    Spacer(Modifier.height(48.dp))
                }
                item {
                    Greeting(
                        name = uiState.displayName,
                        modifier = Modifier.fillMaxWidth(),
                        badgeCount = badgeCount,
                        onBellClick = onBellClick
                    )
                }
                if (budgetError || transactionError) {
                    val errorText = when {
                        budgetError && transactionError ->
                            (budgetErrorMessage ?: "Failed to load budgets.") + "\n" +
                                    (transactionErrorMessage ?: "Failed to load transactions.")

                        budgetError -> budgetErrorMessage ?: "Failed to load budgets."
                        else -> transactionErrorMessage ?: "Failed to load transactions."
                    }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Text(
                                text = errorText,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                } else {
                    item {
                        Text(
                            "📊 Monthly Overview",
                            modifier = Modifier.padding(vertical = 16.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                        val monthlyRemaining = round((monthlyBudget - monthlySpent) * 100) / 100
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Total Budget: $$monthlyBudget")
                                Text("Total Spent: $$monthlySpent")
                                Text("Remaining: $$monthlyRemaining")
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                    item {
                        LineChart(
                            values = monthly12Spent,
                            compareValues = monthlyBudgetList,
                            xLabels = monthLabels,
                            title = "Monthly Spendings",
                        )
                        Spacer(Modifier.height(24.dp))
                    }

                    item {
                        Text("📊 Weekly Overview", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        val weeklyRemaining = round((weeklyBudget - weeklySpent) * 100) / 100
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Total Budget: $$weeklyBudget")
                                Text("Total Spent: $$weeklySpent")
                                Text("Remaining: $$weeklyRemaining")
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                    item {
                        LineChart(
                            values = weekly12Spent,
                            compareValues = weeklyBudgetList,
                            xLabels = weekLabels,
                            title = "Weekly Spendings"
                        )
                        Spacer(Modifier.height(48.dp))
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                    Button(
                        onClick = { screenContainerViewModel.navigateTo(Screen.ADDTRANSACTION) }
                    ) { Text("+") }
                }
            }
        }
    }

        @Composable
        fun Greeting(
            name: String,
            modifier: Modifier = Modifier,
            badgeCount: Int = 0,
            onBellClick: () -> Unit = {}
        ) {
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Welcome, $name!",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.titleLarge
                )
                BadgedBox(
                    badge = {
                        if (badgeCount > 0) {
                            Badge { Text(badgeCount.coerceAtMost(9).toString()) }
                        }
                    }
                ) {
                    IconButton(onClick = onBellClick) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications"
                        )
                    }
                }
            }
        }
