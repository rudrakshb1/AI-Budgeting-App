package com.example.aibudgetapp.ui.screens.budget

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class BudgetRepository {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private fun userBudgetsRef() = db
        .collection("users")
        .document(auth.currentUser?.uid ?: throw IllegalStateException("Not logged in"))
        .collection("budgets")

    fun addBudget(budget: Budget, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val map = hashMapOf(
            "name" to budget.name,
            "selecteddate" to budget.selecteddate,
            "chosentype" to budget.chosentype,
            "chosencategory" to budget.chosencategory,
            "amount" to budget.amount,
            "checked" to budget.checked,
        )
        userBudgetsRef()
            .add(map)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}