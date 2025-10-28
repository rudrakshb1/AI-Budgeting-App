package com.example.aibudgetapp.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.aibudgetapp.constants.BudgetType
import com.example.aibudgetapp.constants.DATE_FORMAT
import com.example.aibudgetapp.constants.DATE_FORMAT_MMM_D
import com.example.aibudgetapp.constants.DAYS_PER_WEEK
import com.example.aibudgetapp.ui.screens.budget.BudgetRepository
import com.example.aibudgetapp.ui.screens.transaction.TransactionRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.round

class HomeViewModel(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
): ViewModel() {

    var budgetError by mutableStateOf(false)
        private set

    var budgetErrorMessage by mutableStateOf<String?>(null)
        private set

    var monthlyBudgetList = mutableStateListOf<Double>()
        private set

    var transactionError by mutableStateOf(false)
        private set

    var transactionErrorMessage by mutableStateOf<String?>(null)
        private set

    var monthlyTransactionList = mutableStateListOf<Double>()
        private set

    var monthLabels = mutableStateListOf<String>()
        private set

    var weeklyBudgetList = mutableStateListOf<Double>()
        private set

    var weeklyTransactionList = mutableStateListOf<Double>()
        private set

    var weekLabels = mutableStateListOf<String>()
        private set

    /**
     * Build the last [historyLength] months of budget totals.
     * Rule: allocate each budget proportionally to a month by overlap days.
     * - MONTHLY budgets: (amount * overlapDays / daysInMonth)
     * - WEEKLY budgets : (amount * overlapDays / 7)
     */
    fun getMonthlyBudgetList(
        historyLength : Int = 12
    ) {
        val parseFmt = DateTimeFormatter.ofPattern(DATE_FORMAT)
        monthlyBudgetList.clear()
        budgetError = false

        budgetRepository.getBudgets(
            onSuccess = { list ->
                val monthlyBudgets = list.filter { it.chosenType.equals(BudgetType.MONTHLY.value, ignoreCase = true) }
                val weeklyBudgets  = list.filter { it.chosenType.equals(BudgetType.WEEKLY.value,  ignoreCase = true) }

                for (i in 0 until historyLength) {
                    val yearMonth   = YearMonth.now().minusMonths(i.toLong())
                    val startDate   = yearMonth.atDay(1)
                    val endDate     = yearMonth.atEndOfMonth()
                    val daysInMonth = yearMonth.lengthOfMonth()

                    var sum = 0.0

                    // Allocate each MONTHLY budget into this month by overlapping days.
                    monthlyBudgets.forEach { budget ->
                        val bStart = LocalDate.parse(budget.startDate, parseFmt)
                        val bEnd   = LocalDate.parse(budget.endDate,   parseFmt)

                        val overlapDays = overlapDaysInclusive(bStart, bEnd, startDate, endDate)
                        if (overlapDays > 0) {
                            sum += budget.amount.toDouble() * overlapDays / daysInMonth
                        }
                    }

                    // Allocate each WEEKLY budget into this month by overlapping days.
                    weeklyBudgets.forEach { budget ->
                        val bStart = LocalDate.parse(budget.startDate, parseFmt)
                        val bEnd   = LocalDate.parse(budget.endDate,   parseFmt)

                        val overlapDays = overlapDaysInclusive(bStart, bEnd, startDate, endDate)
                        if (overlapDays > 0) {
                            sum += budget.amount.toDouble() * overlapDays / DAYS_PER_WEEK
                        }
                    }

                    // Keep 2-decimal precision for chart display
                    monthlyBudgetList.add(0, round(sum*100)/100)
                }
            },
            onFailure = { e ->
                budgetError = true
                budgetErrorMessage = e.message ?: "Unknown error while fetching budgets"
            }
        )
    }

    /**
     * Calculate inclusive overlap in days between two date ranges (s1..e1) and (s2..e2).
     *
     * Parameters:
     * @param s1 start of first range (e.g., budget start)
     * @param e1 end of first range   (e.g., budget end)
     * @param s2 start of second range (e.g., current month start)
     * @param e2 end of second range   (e.g., current month end)
     *
     * Returns:
     *  - The number of overlapping days (inclusive of both boundaries).
     *  - For example, a 1-day budget still counts as 1 day overlap.
     */
    private fun overlapDaysInclusive(
        s1: LocalDate, e1: LocalDate,
        s2: LocalDate, e2: LocalDate
    ): Int {
        val start = maxOf(s1, s2)
        val end   = minOf(e1, e2)
        if (end.isBefore(start)) return 0
        return (ChronoUnit.DAYS.between(start, end) + 1).toInt()
    }


    fun get12MonthlyTransaction(
        historyLength : Int = 12
    ) {
        transactionError = false
        monthlyTransactionList.clear()
        monthLabels.clear()

        for (i in 0 until historyLength) {
            val yearMonth = YearMonth.now().minusMonths(i.toLong())
            monthLabels.add(0, yearMonth.month.toString().take(3))
            transactionRepository.getTransactions(
                onSuccess = { list ->
                    val sum = list
                        .filter { it.date.contains(yearMonth.toString(), ignoreCase = true) }
                        .sumOf { (it.amount ?: 0.0) + (it.debit ?: 0.0) }


                    monthlyTransactionList.add(0, round(sum*100)/100)
                },
                onFailure = { e ->
                    transactionError = true
                    transactionErrorMessage = e.message ?: "Unknown error while fetching transactions"
                }
            )
        }
    }

    fun getWeeklyBudgetList(
        historyLength : Int = 12
    ) {
        val parseFmt = DateTimeFormatter.ofPattern(DATE_FORMAT)
        weeklyBudgetList.clear()
        budgetError = false

        budgetRepository.getBudgets(
            onSuccess = { list ->
                val weeklyBudgets  = list.filter { it.chosenType.equals(BudgetType.WEEKLY.value,  ignoreCase = true) }
                val monthlyBudgets = list.filter { it.chosenType.equals(BudgetType.MONTHLY.value, ignoreCase = true) }

                for (i in 0 until historyLength) {
                    val anchor = LocalDate.now().minusWeeks(i.toLong())
                    val weekStart = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    val weekEnd   = weekStart.plusDays(6)

                    var sum = 0.0

                    weeklyBudgets.forEach { b ->
                        val s = LocalDate.parse(b.startDate, parseFmt)
                        val e = LocalDate.parse(b.endDate,   parseFmt)
                        val overlapDays = overlapDaysInclusive(weekStart, weekEnd, s, e)
                        if (overlapDays > 0) {
                            sum += b.amount.toDouble() * overlapDays / DAYS_PER_WEEK
                        }
                    }

                    monthlyBudgets.forEach { b ->
                        val s = LocalDate.parse(b.startDate, parseFmt)
                        val e = LocalDate.parse(b.endDate,   parseFmt)
                        sum += monthlyPortionForWeek(b.amount, s, e, weekStart, weekEnd)
                    }

                    weeklyBudgetList.add(0, round(sum*100)/100)
                }
            },
            onFailure = { e ->
                budgetError = true
                budgetErrorMessage = e.message ?: "Unknown error while fetching budgets"
            }
        )
    }

    private fun monthlyPortionForWeek(
        amount: Double,
        budgetStart: LocalDate,
        budgetEnd: LocalDate,
        weekStart: LocalDate,
        weekEnd: LocalDate
    ): Double {
        val start = maxOf(budgetStart, weekStart)
        val end   = minOf(budgetEnd, weekEnd)
        if (end.isBefore(start)) return 0.0

        var d = start
        var acc = 0.0
        while (!d.isAfter(end)) {
            val dim = YearMonth.from(d).lengthOfMonth()
            acc += amount.toDouble() / dim
            d = d.plusDays(1)
        }
        return acc
    }

    fun get12WeeklyTransaction(
        historyLength: Int = 12
    ) {
        transactionError = false
        weeklyTransactionList.clear()
        weekLabels.clear()

        val parseFmt = DateTimeFormatter.ofPattern(DATE_FORMAT)
        val labelFmtM = DateTimeFormatter.ofPattern(DATE_FORMAT_MMM_D, Locale.ENGLISH) // e.g., "Sep 2"

        for (i in 0 until historyLength) {
            val anchor = LocalDate.now().minusWeeks(i.toLong())
            val weekStart = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val weekEnd = weekStart.plusDays(6)

            val label = weekStart.format(labelFmtM)

            weekLabels.add(0, label)

            transactionRepository.getTransactions(
                onSuccess = { list ->
                    val sum = list
                        .filter { tx ->
                            try {
                                val txDate = LocalDate.parse(tx.date, parseFmt)
                                !txDate.isBefore(weekStart) && !txDate.isAfter(weekEnd)
                            } catch (_: Exception) {
                                false
                            }
                        }
                        .sumOf { (it.amount ?: 0.0) + (it.debit ?: 0.0) }


                    weeklyTransactionList.add(0, round(sum*100)/100)
                },
                onFailure = { e ->
                    transactionError = true
                    transactionErrorMessage = e.message ?: "Unknown error while fetching transactions"
                }
            )
        }
    }
}
