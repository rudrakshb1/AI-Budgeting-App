package com.example.aibudgetapp.ui.screens.budget

import com.example.aibudgetapp.ui.screens.transaction.Transaction
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
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

    fun getBudgets(
        onSuccess: (List<Budget>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            userBudgetsRef()
                .orderBy("selecteddate", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener { snapshot ->
                    val list = snapshot.documents.map { doc ->
                        val data = doc.data ?: emptyMap<String, Any?>()
                        Budget(
                            name = data["name"] as? String ?: "",
                            selecteddate = (data["selecteddate"] as? Number)?.toInt() ?: 0,
                            chosentype = data["chosentype"] as? String ?: "",
                            chosencategory = data["chosencategory"] as? String ?: "",
                            amount = (data["amount"] as? Number)?.toInt() ?: 0,
                            checked = data["checked"] as? Boolean ?: false
                        )
                    }
                    onSuccess(list)
                }
                .addOnFailureListener(onFailure)
        } catch (e: IllegalStateException) {
            onFailure(e)
        }
    }




}