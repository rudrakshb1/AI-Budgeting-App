package com.example.aibudgetapp.ui.screens.transaction

data class Transaction(
    val id: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val date: String = "",
    val merchant: String = ""
)
