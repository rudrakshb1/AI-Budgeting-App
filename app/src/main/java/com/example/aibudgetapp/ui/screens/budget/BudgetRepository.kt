package com.example.aibudgetapp.ui.screens.budget

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore

class BudgetRepository {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private fun userBudgetsRef() = db
        .collection("users")
        .document(auth.currentUser?.uid ?: throw IllegalStateException("Not logged in"))
        .collection("budgets")

    fun addBudget(budget: Budget, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        Log.d("REPOSITORY", "Firestore push: $budget")
        val map = hashMapOf(
            "id" to budget.id,
            "name" to budget.name,
            // "selecteddate" to budget.selectedDate,
            "chosentype" to budget.chosenType,
            "chosencategory" to budget.chosenCategory,
            "amount" to budget.amount,
            "checked" to budget.checked,
            "startDate" to (budget.startDate ?: ""),
            "endDate" to (budget.endDate ?: "")
        )
        userBudgetsRef()
            .add(map)
            .addOnSuccessListener { ref ->
                val newId = ref.id
                ref.update("id", newId)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener(onFailure) }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun getBudgets(
        onSuccess: (List<Budget>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            userBudgetsRef()
                .get()
                .addOnSuccessListener { snapshot ->
                    val list = snapshot.documents.map { doc ->
                        val data = doc.data ?: emptyMap<String, Any?>()
                        Budget(
                            id = (data["id"] as? String) ?: doc.id,
                            name = data["name"] as? String ?: "",
                            // selectedDate = (data["selecteddate"] as? Number)?.toInt() ?: 0,
                            chosenType = data["chosentype"] as? String ?: "",
                            chosenCategory = data["chosencategory"] as? String ?: "",
                            amount = (data["amount"] as? Number)?.toInt() ?: 0,
                            checked = data["checked"] as? Boolean ?: false,
                            startDate = data["startDate"] as? String,
                            endDate = data["endDate"] as? String
                        )
                    }
                    Log.d("REPO", "getBudgets: found ${list.size} budgets")

                    onSuccess(list)
                }
                .addOnFailureListener(onFailure)
        } catch (e: IllegalStateException) {
            onFailure(e)
        }
    }

    fun deleteBudget(
        id: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (id.isBlank()) {
            onFailure(IllegalArgumentException("Missing id for delete"))
            return
        }
        userBudgetsRef()
            .document(id)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }
    fun getbudgetcategory(
        category: String,
    onSuccess: (List<Budget>) -> Unit,
        onFailure: (Exception) -> Unit
    ){
        try {

            userBudgetsRef()
            .whereEqualTo("chosencategory", category)
            .get()
            .addOnSuccessListener { snapshot ->
                val results = snapshot.documents.map { res ->
                    val cate = res.data ?: emptyMap<String, Any?>()
                    Budget(
                        id = (cate["id"] as? String) ?: res.id,
                        name = cate["name"] as? String ?: "",
                        chosenType = cate["chosentype"] as? String ?: "",
                        chosenCategory = cate["chosencategory"] as? String ?: "",
                        amount = (cate["amount"] as? Number)?.toInt() ?: 0,
                        checked = cate["checked"] as? Boolean ?: false,
                        startDate = cate["startDate"] as? String,
                        endDate = cate["endDate"] as? String
                    )
                }
                Log.d("REPO", "getBudgets: found ${results.size} budgets")


                onSuccess(results)
            }
            .addOnFailureListener(onFailure)
    } catch (e: IllegalStateException) {
        onFailure(e)
    }
}







}