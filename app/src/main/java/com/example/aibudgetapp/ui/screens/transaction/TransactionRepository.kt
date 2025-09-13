package com.example.aibudgetapp.ui.screens.transaction

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import android.util.Log

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
        )
        userTransactionRef()
            .add(map)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun getTransactions(
        onSuccess: (List<Transaction>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            userTransactionRef()
                // .orderBy("date")
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
                            category = data["category"] as? String ?: ""
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