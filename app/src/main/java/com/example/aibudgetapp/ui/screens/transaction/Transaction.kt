package com.example.aibudgetapp.ui.screens.transaction

data class Transaction(
    val id: String = "",
    val description: String = "",
    val amount: Double? = null,     // for receipts
    val debit: Double? = null,      // for bank statements
    val credit: Double? = null,     // for bank statements
    val balance: Double? = null,    // for bank statements
    var category: String = "",      // filled by user (never Uncategorized now)
    val date: String = "",
    val merchant: String = ""       // for receipts
)
