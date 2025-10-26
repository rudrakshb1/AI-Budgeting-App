package com.example.aibudgetapp.ui.screens.transaction

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class TransactionRepository {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

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
            .addOnFailureListener(onError)
    }

    private fun withTransactionsRef(
        onError: (Exception) -> Unit = {},
        block: (CollectionReference) -> Unit
    ) {
        fetchTargetUid(
            onResult = { targetUid ->
                if (targetUid == null) {
                    onError(IllegalStateException("targetUid null"))
                    return@fetchTargetUid
                }
                val ref = db.collection("users").document(targetUid).collection("transactions")
                block(ref)
            },
            onError = onError
        )
    }

    fun userTransactionsRef(
        onReady: (CollectionReference) -> Unit,
        onError: (Exception) -> Unit = {}
    ) = withTransactionsRef(onError, onReady)

    fun addTransaction(
        transaction: Transaction,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Log.d("REPOSITORY", "Firestore push: $transaction")
        try {
            transaction.date = transaction.date.toIsoDateString()

            withTransactionsRef(
                onError = onFailure
            ) { ref ->
                val docRef = ref.document()
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
            }
        } catch (e: DateTimeParseException) {
            onFailure(e)
        }
    }

    // Save transaction, then upload image and set receiptUrl (keeps addTransaction() unchanged)
    fun addTransactionWithImage(
        imageUri: android.net.Uri,
        transaction: Transaction,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            transaction.date = transaction.date.toIsoDateString()
            withTransactionsRef(
                onError = onFailure
            ) { ref ->
                val docRef = ref.document()
                val txWithId = transaction.copy(id = docRef.id)

                // 1) Save the transaction first (same behavior as addTransaction)
                docRef.set(txWithId)
                    .addOnSuccessListener {
                        // 2) After save succeeds, upload the image and set receiptUrl
                        fetchTargetUid(
                            onResult = { ownerUid ->
                                if (ownerUid == null) {
                                    onFailure(IllegalStateException("ownerUid null"))
                                    return@fetchTargetUid
                                }
                                val storageRef = com.google.firebase.storage.FirebaseStorage
                                    .getInstance()
                                    .reference
                                    .child("receipts/$ownerUid/${docRef.id}.jpg")

                                storageRef.putFile(imageUri)
                                    .continueWithTask { task ->
                                        if (!task.isSuccessful) {
                                            throw task.exception ?: RuntimeException("Upload failed")
                                        }
                                        storageRef.downloadUrl
                                    }
                                    .addOnSuccessListener { downloadUri ->
                                        docRef.update("receiptUrl", downloadUri.toString())
                                            .addOnSuccessListener { onSuccess() }
                                            .addOnFailureListener(onFailure)
                                    }
                                    .addOnFailureListener(onFailure)
                            },
                            onError = onFailure
                        )
                    }
                    .addOnFailureListener(onFailure)
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }


    fun getTransactions(
        onSuccess: (List<Transaction>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        withTransactionsRef(
            onError = onFailure
        ) { ref ->
            ref.orderBy("date", Query.Direction.DESCENDING)
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
                            receiptUrl = data["receiptUrl"] as? String
                        )
                    }
                    onSuccess(list)
                }
                .addOnFailureListener(onFailure)
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
        withTransactionsRef(
            onError = onFailure
        ) { ref ->
            ref.document(id)
                .delete()
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener(onFailure)
        }
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