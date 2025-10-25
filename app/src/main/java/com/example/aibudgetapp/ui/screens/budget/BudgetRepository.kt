package com.example.aibudgetapp.ui.screens.budget

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore

class BudgetRepository {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    /**
     * Fetch the targetUid field stored in the current user's profile document.
     * This is the UID under which budgets will actually be stored.
     */
    private fun fetchTargetUid(
        onResult: (String?) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            onError(IllegalStateException("Not logged in"))
            return
        }

        db.collection("users").document(currentUid).get()
            .addOnSuccessListener { snap ->
                val targetUid = snap.getString("uid")
                if (targetUid.isNullOrBlank()) {
                    onError(IllegalStateException("targetUid not found in user profile"))
                } else {
                    onResult(targetUid)
                }
            }
            .addOnFailureListener { e -> onError(e) }
    }

    /**
     * Helper to work with the budgets collection.
     * Ensures we have the correct targetUid before accessing the collection.
     */
    private fun withBudgetsRef(
        onError: (Exception) -> Unit = {},
        block: (CollectionReference) -> Unit
    ) {
        fetchTargetUid(
            onResult = { targetUid ->
                if (targetUid == null) {
                    onError(IllegalStateException("targetUid null"))
                    return@fetchTargetUid
                }
                val ref = db.collection("users").document(targetUid).collection("budgets")
                block(ref)
            },
            onError = onError
        )
    }

    /**
     * Public version of budgetsRef if you want direct access.
     */
    fun userBudgetsRef(
        onReady: (CollectionReference) -> Unit,
        onError: (Exception) -> Unit = {}
    ) = withBudgetsRef(onError, onReady)

    /**
     * Add a new budget document under the targetUid.
     */
    fun addBudget(
        budget: Budget,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Log.d("REPOSITORY", "Firestore push: $budget")

        val sanitized = if (budget.chosenType.equals("Yearly", true)) {
            budget.copy(chosenCategory = null)
        } else budget

        val map = hashMapOf(
            "id" to sanitized.id,
            "name" to sanitized.name,
            "chosentype" to sanitized.chosenType,
            "amount" to sanitized.amount,
            "checked" to sanitized.checked,
            "startDate" to (sanitized.startDate ?: ""),
            "endDate" to (sanitized.endDate ?: "")
        ).apply {
            sanitized.chosenCategory?.let { put("chosencategory", it) }
        }

        withBudgetsRef(
            onError = onFailure
        ) { ref ->
            ref.add(map)
                .addOnSuccessListener { docRef ->
                    val newId = docRef.id
                    docRef.update("id", newId)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener(onFailure)
                }
                .addOnFailureListener(onFailure)
        }
    }

    /**
     * Get all budgets for the target user.
     */
    fun getBudgets(
        onSuccess: (List<Budget>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        withBudgetsRef(
            onError = onFailure
        ) { ref ->
            ref.get()
                .addOnSuccessListener { snapshot: QuerySnapshot ->
                    val list = snapshot.documents.map { doc ->
                        val data = doc.data ?: emptyMap<String, Any?>()
                        Budget(
                            id = (data["id"] as? String) ?: doc.id,
                            name = data["name"] as? String ?: "",
                            chosenType = data["chosentype"] as? String ?: "",
                            chosenCategory = (data["chosencategory"] as? String)?.ifBlank { null },
                            amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                            checked = data["checked"] as? Boolean ?: false,
                            startDate = data["startDate"] as? String,
                            endDate = data["endDate"] as? String
                        )
                    }
                    Log.d("REPO", "getBudgets: found ${list.size} budgets")
                    onSuccess(list)
                }
                .addOnFailureListener(onFailure)
        }
    }

    /**
     * Get budgets filtered by category.
     */
    fun getBudgetCategory(
        category: String,
        onSuccess: (List<Budget>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        withBudgetsRef(
            onError = onFailure
        ) { ref ->
            ref.whereEqualTo("chosencategory", category)
                .get()
                .addOnSuccessListener { snapshot ->
                    val results = snapshot.documents.map { res ->
                        val cate = res.data ?: emptyMap<String, Any?>()
                        Budget(
                            id = (cate["id"] as? String) ?: res.id,
                            name = cate["name"] as? String ?: "",
                            chosenType = cate["chosentype"] as? String ?: "",
                            chosenCategory = (cate["chosencategory"] as? String)?.ifBlank { null },
                            amount = (cate["amount"] as? Number)?.toDouble() ?: 0.0,
                            checked = cate["checked"] as? Boolean ?: false,
                            startDate = cate["startDate"] as? String,
                            endDate = cate["endDate"] as? String
                        )
                    }
                    Log.d("REPO", "getBudgetCategory: found ${results.size} budgets")
                    onSuccess(results)
                }
                .addOnFailureListener(onFailure)
        }
    }

    /**
     * Delete a budget by its ID.
     */
    fun deleteBudget(
        id: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (id.isBlank()) {
            onFailure(IllegalArgumentException("Missing id for delete"))
            return
        }

        withBudgetsRef(
            onError = onFailure
        ) { ref ->
            ref.document(id)
                .delete()
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener(onFailure)
        }
    }
}