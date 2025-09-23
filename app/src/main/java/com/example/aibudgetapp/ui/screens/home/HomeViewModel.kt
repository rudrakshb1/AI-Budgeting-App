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
import java.time.temporal.TemporalAdjusters
import java.util.Locale

private const val WEEKS_PER_MONTH = 52.0 / 12.0

class HomeViewModel(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
): ViewModel() {

    var budgetError by mutableStateOf(false)
        private set

    var monthlyBudget by mutableStateOf(0)
        private set

    var transactionError by mutableStateOf(false)
        private set

    var monthlySpent by mutableStateOf(0.0)
        private set

    var monthlyListTransaction = mutableStateListOf<Double>()
        private set

    var monthLabels = mutableStateListOf<String>()
        private set

    var weeklyBudget by mutableStateOf(0)
        private set

    var weeklySpent by mutableStateOf(0.0)
        private set

    var weeklyListTransaction = mutableStateListOf<Double>()
        private set

    var weekLabels = mutableStateListOf<String>()
        private set

    fun getMonthlyBudget() {

        budgetError = false
        budgetRepository.getBudgets(
            onSuccess = { list ->
                monthlyBudget = list
                    .filter { it.chosenType.equals("monthly", ignoreCase = true) }
                    .sumOf { it.amount }
                weeklyBudget = list
                    .filter { it.chosenType.equals("weekly", ignoreCase = true) }
                    .sumOf { it.amount }

                monthlyBudget += (weeklyBudget * WEEKS_PER_MONTH).toInt()
            },
            onFailure = { e ->
                budgetError = true
            }
        )
    }

    fun getMonthlyTransaction(yearMonth: YearMonth) {
        transactionError = false
        transactionRepository.getTransactions(
            onSuccess = { list ->
                monthlySpent = list
                    .filter { it.date.contains(yearMonth.toString(), ignoreCase = true) }
                    .sumOf { (it.amount ?: 0.0) + (it.debit ?: 0.0) }
            },
            onFailure = { e ->
                transactionError = true
            }
        )
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

    fun getWeeklyBudget() {
        budgetError = false
        budgetRepository.getBudgets(
            onSuccess = { list ->
                weeklyBudget = list
                    .filter { it.chosenType.equals("weekly", ignoreCase = true) }
                    .sumOf { it.amount }
            },
            onFailure = { e ->
                budgetError = true
            }
        )
    }

    fun getWeeklyTransaction() {
        val today = LocalDate.now()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        transactionError = false
        transactionRepository.getTransactions(
            onSuccess = { list ->
                weeklySpent = list
                    .filter { tx ->
                        try {
                            val txDate = LocalDate.parse(tx.date, formatter)
                            !txDate.isBefore(weekStart) && !txDate.isAfter(weekEnd)
                        } catch (e: Exception) {
                            false
                        }
                    }
                    .sumOf { (it.amount ?: 0.0) + (it.debit ?: 0.0) }



            },
            onFailure = { e ->
                transactionError = true
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
