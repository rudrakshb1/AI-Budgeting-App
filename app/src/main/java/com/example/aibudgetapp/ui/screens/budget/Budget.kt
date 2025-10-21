package com.example.aibudgetapp.ui.screens.budget

data class Budget (
    val id: String,
    val name: String,
    // val selectedDate: Int,
    val chosenType: String,
   // val chosenCategory: String,
    val chosenCategory: String? = null,
    val amount: Double,
    val checked: Boolean,
    val startDate: String? = null,
    val endDate: String? = null
)