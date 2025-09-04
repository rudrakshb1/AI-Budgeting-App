package com.example.aibudgetapp.ui.screens.transaction

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class TransactionRepository {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private fun userTransactionRef() = db
        .collection("users")
        .document(auth.currentUser?.uid ?: throw IllegalStateException("Not logged in"))
        .collection("transactions")

    fun addTransaction(transaction: Transaction, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
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
}