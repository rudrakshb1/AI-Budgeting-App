package com.example.aibudgetapp.ui.screens.budget

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class BudgetRepository {
    private val budgetdatabase = Firebase.firestore

    fun addBudget(budget: Budget, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val budgetmap = hashMapOf(
            "name" to budget.name,
            "selecteddate" to budget.selecteddate,
            "chosentype" to budget.chosentype,
            "chosencategory" to budget.chosencategory,
            "amount" to budget.amount,
            "checked" to budget.checked,
        )
        budgetdatabase.collection("budgets")
            .add(budgetmap)
            .addOnSuccessListener { onSuccess()}
            .addOnFailureListener { e -> onFailure(e)}
    }
}