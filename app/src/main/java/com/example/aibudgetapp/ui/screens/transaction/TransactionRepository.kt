package com.example.aibudgetapp.ui.screens.transaction

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import android.util.Log
import com.google.firebase.firestore.Query

class TransactionRepository {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private fun userTransactionRef() = db
        .collection("users")
        .document(auth.currentUser?.uid ?: throw IllegalStateException("Not logged in"))
        .collection("transactions")

    fun addTransaction(transaction: Transaction, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        Log.d("REPOSITORY", "Firestore push: $transaction")
        val map = hashMapOf(
            "id" to transaction.id,
            "description" to transaction.description,
            "amount" to transaction.amount,
            "category" to transaction.category,
            "date" to transaction.date,
        )
        userTransactionRef()
            .add(map)
            .addOnSuccessListener { ref ->
                val newId = ref.id
                ref.update("id", newId)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener(onFailure)
            }
            .addOnFailureListener(onFailure)
    }

    fun getTransactions(
        onSuccess: (List<Transaction>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            userTransactionRef()
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { snapshot ->
                    val list = snapshot.documents.map { doc ->
                        val data = doc.data ?: emptyMap<String, Any?>()
                        Transaction(
                            id = (data["id"] as? String) ?: doc.id,
                            description = data["description"] as? String ?: "",
                            amount = when (val v = data["amount"]) {
                                is Number -> v.toDouble()
                                else -> 0.0
                            },
                            category = data["category"] as? String ?: "",
                            date = data["date"] as? String ?: "",
                        )
                    }
                    onSuccess(list)
                }
                .addOnFailureListener(onFailure)
        } catch (e: IllegalStateException) {
            onFailure(e)
        }
    }

    fun deleteTransaction(
        id: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (id.isBlank()) {
            onFailure(IllegalArgumentException("Missing id for delete"))
            return
        }
        userTransactionRef()
            .document(id)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }

}