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

    private fun overlapDaysInclusive(
        s1: LocalDate, e1: LocalDate,
        s2: LocalDate, e2: LocalDate
    ): Int {
        val start = maxOf(s1, s2)
        val end   = minOf(e1, e2)
        if (end.isBefore(start)) return 0
        return (ChronoUnit.DAYS.between(start, end) + 1).toInt()
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
                val weeklyBudgets  = list.filter { it.chosenType.equals("weekly",  ignoreCase = true) }
                val monthlyBudgets = list.filter { it.chosenType.equals("monthly", ignoreCase = true) }

                for (i in 0 until 12) {
                    val anchor = LocalDate.now().minusWeeks(i.toLong())
                    val weekStart = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    val weekEnd   = weekStart.plusDays(6)

                    var sum = 0.0

                    weeklyBudgets.forEach { b ->
                        val s = LocalDate.parse(b.startDate, parseFmt)
                        val e = LocalDate.parse(b.endDate,   parseFmt)
                        val overlapDays = overlapDaysInclusive(weekStart, weekEnd, s, e)
                        if (overlapDays > 0) {
                            sum += b.amount.toDouble() * overlapDays / 7.0
                        }
                    }

                    monthlyBudgets.forEach { b ->
                        val s = LocalDate.parse(b.startDate, parseFmt)
                        val e = LocalDate.parse(b.endDate,   parseFmt)
                        sum += monthlyPortionForWeek(b.amount, s, e, weekStart, weekEnd)
                    }

                    weeklyBudgetList.add(0, sum.toInt())
                }
            },
            onFailure = {
                budgetError = true
            }
        )
    }

    private fun monthlyPortionForWeek(
        amount: Int,
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
