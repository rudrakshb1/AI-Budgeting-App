package com.example.aibudgetapp.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.aibudgetapp.ui.screens.budget.BudgetRepository
import com.example.aibudgetapp.ui.screens.transaction.TransactionRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

class HomeViewModel(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
): ViewModel() {

    var budgetError by mutableStateOf(false)
        private set

    var monthlyBudgetList = mutableStateListOf<Int>()
        private set

    var transactionError by mutableStateOf(false)
        private set

    var monthlyListTransaction = mutableStateListOf<Double>()
        private set

    var monthLabels = mutableStateListOf<String>()
        private set

    var weeklyBudgetList = mutableStateListOf<Int>()
        private set

    var weeklyListTransaction = mutableStateListOf<Double>()
        private set

    var weekLabels = mutableStateListOf<String>()
        private set

    fun getMonthlyBudgetList() {
        val parseFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        monthlyBudgetList.clear()
        budgetError = false

        budgetRepository.getBudgets(
            onSuccess = { list ->
                val monthlyBudgets = list.filter { it.chosenType.equals("monthly", ignoreCase = true) }
                val weeklyBudgets  = list.filter { it.chosenType.equals("weekly",  ignoreCase = true) }

                for (i in 0 until 12) {
                    val yearMonth   = YearMonth.now().minusMonths(i.toLong())
                    val startDate   = yearMonth.atDay(1)
                    val endDate     = yearMonth.atEndOfMonth()
                    val daysInMonth = (ChronoUnit.DAYS.between(startDate, endDate) + 1).toInt()

                    var sum = 0.0

                    monthlyBudgets.forEach { budget ->
                        val bStart = LocalDate.parse(budget.startDate, parseFmt)
                        val bEnd   = LocalDate.parse(budget.endDate,   parseFmt)

                        val overlapDays = overlapDaysInclusive(bStart, bEnd, startDate, endDate)
                        if (overlapDays > 0) {
                            sum += budget.amount.toDouble() * overlapDays / daysInMonth
                        }
                    }

                    weeklyBudgets.forEach { budget ->
                        val bStart = LocalDate.parse(budget.startDate, parseFmt)
                        val bEnd   = LocalDate.parse(budget.endDate,   parseFmt)

                        val overlapDays = overlapDaysInclusive(bStart, bEnd, startDate, endDate)
                        if (overlapDays > 0) {
                            sum += budget.amount.toDouble() * overlapDays / 7.0
                        }
                    }

                    monthlyBudgetList.add(0, sum.toInt())
                }
            },
            onFailure = {
                budgetError = true
            }
        )
    }

    private fun overlapDaysInclusive(s1: LocalDate, e1: LocalDate, s2: LocalDate, e2: LocalDate): Int {
        if (e1.isBefore(s2) || e2.isBefore(s1)) return 0
        val start = if (s1.isAfter(s2)) s1 else s2
        val end   = if (e1.isBefore(e2)) e1 else e2
        return (ChronoUnit.DAYS.between(start, end) + 1).toInt().coerceAtLeast(0)
    }

    fun get12MonthlyTransaction() {
        transactionError = false
        monthlyListTransaction.clear()
        monthLabels.clear()

        for (i in 0 until 12) {
            val yearMonth = YearMonth.now().minusMonths(i.toLong())
            monthLabels.add(0, yearMonth.month.toString().take(3))
            transactionRepository.getTransactions(
                onSuccess = { list ->
                    val sum = list
                        .filter { it.date.contains(yearMonth.toString(), ignoreCase = true) }
                        .sumOf { (it.amount ?: 0.0) + (it.debit ?: 0.0) }


                    monthlyListTransaction.add(0, sum)
                },
                onFailure = { e ->
                    transactionError = true
                }
            )
        }
    }

    fun getWeeklyBudgetList() {
        val parseFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        weeklyBudgetList.clear()
        budgetError = false

        budgetRepository.getBudgets(
            onSuccess = { list ->
                val budgets = list.filter { it.chosenType.equals("weekly", ignoreCase = true) }

                for (i in 0 until 12) {
                    val anchor = LocalDate.now().minusWeeks(i.toLong())
                    val weekStart = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    val weekEnd = weekStart.plusDays(6)

                    var sum = 0.0

                    budgets.forEach { budget ->
                        val start = LocalDate.parse(budget.startDate, parseFmt)
                        val end   = LocalDate.parse(budget.endDate, parseFmt)

                        if (end.isBefore(weekStart) || start.isAfter(weekEnd)) return@forEach

                        val overlapStart = if (start.isAfter(weekStart)) start else weekStart
                        val overlapEnd   = if (end.isBefore(weekEnd)) end else weekEnd

                        val overlapDays = overlapDaysInclusive(overlapStart, overlapEnd, start, end)

                        if (overlapDays > 0) {
                            sum += budget.amount * overlapDays / 7.0
                        }
                    }

                    weeklyBudgetList.add(0, sum.toInt())
                }
            },
            onFailure = {
                budgetError = true
            }
        )
    }

    fun get12WeeklyTransaction() {
        transactionError = false
        weeklyListTransaction.clear()
        weekLabels.clear()

        val parseFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val labelFmtM = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH) // e.g., "Sep 2"

        for (i in 0 until 12) {
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


                    weeklyListTransaction.add(0, sum)
                },
                onFailure = { _ ->
                    transactionError = true
                }
            )
        }
    }
}
