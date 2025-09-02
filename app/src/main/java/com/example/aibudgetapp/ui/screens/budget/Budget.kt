package com.example.aibudgetapp.ui.screens.budget

data class Budget (
    val name: String,
    val selecteddate: Int,
    val chosentype: String,
    val chosencategory: String,
    val amount: Int,
    val checked: Boolean,
)