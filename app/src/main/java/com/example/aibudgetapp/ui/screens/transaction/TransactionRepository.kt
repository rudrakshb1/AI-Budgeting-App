package com.example.aibudgetapp.ui.screens.transaction

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import android.util.Log
import com.google.firebase.firestore.Query
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
            transaction.date = transaction.date.toIsoDateString()

            // Create a new Firestore docRef so we know the ID
            val docRef = userTransactionRef().document()
            val txWithId = transaction.copy(id = docRef.id)

            docRef.set(txWithId)
                .addOnSuccessListener {
                    Log.d("REPOSITORY", "Firestore add: Success. ID=${docRef.id}")
                    onSuccess()
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
                            id = doc.id,
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

    fun String.toIsoDateString(): String {
        val DATE_FORMATS = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE,        // 2025-10-08
            DateTimeFormatter.ofPattern("d/M/yyyy"), // 8/10/2025
            DateTimeFormatter.ofPattern("dd/MM/yyyy")// 08/10/2025
        )
        for (fmt in DATE_FORMATS) {
            try {
                val parsed = LocalDate.parse(this.trim(), fmt)
                return parsed.toString()
            } catch (_: Exception) { }
        }
        return this
    }
}