package com.example.aibudgetapp.ui.screens.transaction

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import android.util.Log
import com.google.firebase.firestore.Query
import java.time.LocalDate
import java.time.format.DateTimeParseException

class TransactionRepository {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private fun userTransactionRef() = db
        .collection("users")
        .document(auth.currentUser?.uid ?: throw IllegalStateException("Not logged in"))
        .collection("transactions")

    fun addTransaction(
        transaction: Transaction,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Log.d("REPOSITORY", "Firestore push: $transaction")
        try {
            LocalDate.parse(transaction.date)
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
                    Log.d("REPOSITORY", "Firestore add: Success. Ref=$ref, NewID=$newId")
                    ref.update("id", newId)
                        .addOnSuccessListener {
                            Log.d("REPOSITORY", "ID field updated in Firestore to $newId")
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            Log.e("REPOSITORY", "Failed to update Firestore ID: ${e.message}")
                            onFailure(e)
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("REPOSITORY", "Failed Firestore add: ${e.message}")
                    onFailure(e)
                }
        } catch (e: DateTimeParseException) {
            onFailure(e)
        }
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