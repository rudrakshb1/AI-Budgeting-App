package com.example.aibudgetapp.ui.screens.transaction

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Tx(
    val id: String,
    val amount: Double,
    val category: String,
    val createdAt: Timestamp? = null
)

class AddTransactionViewModel : ViewModel() {

    var receiptUri: String? = null
        private set

    private val _transactions = MutableStateFlow<List<Tx>>(emptyList())
    val transactions: StateFlow<List<Tx>> = _transactions

    fun setReceipt(uri: String?) { receiptUri = uri }

    fun saveTransaction(
        amount: Double,
        category: String,
        receiptUrl: String? = null,
        onDone: (Boolean) -> Unit = {}
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) { onDone(false); return }

        //TODO: make transaction box clickable and store this inside
        val db = FirebaseFirestore.getInstance()
        val tx = hashMapOf(
            "amount" to amount,
            "category" to category,
            "receiptUrl" to receiptUrl,
            "createdAt" to Timestamp.now()
        )

        db.collection("users").document(uid)
            .collection("transactions")
            .add(tx)
            .addOnSuccessListener { ref ->
                Log.d("TX", "Write OK id=${ref.id}")
                onDone(true)
            }
            .addOnFailureListener { e ->
                Log.e("TX", "Write FAIL", e)
                onDone(false)
            }
    }

    fun startListeningTransactions() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(uid)
            .collection("transactions")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) return@addSnapshotListener
                _transactions.value = snap.documents.map { d ->
                    Tx(
                        id = d.id,
                        amount = (d.get("amount") as? Number)?.toDouble() ?: 0.0,
                        category = d.getString("category") ?: "Unknown",
                        createdAt = d.getTimestamp("createdAt")
                    )
                }
            }
    }

    fun deleteTransaction(docId: String, onDone: (Boolean) -> Unit = {}) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) { onDone(false); return }

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid)
            .collection("transactions").document(docId)
            .delete()
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { e -> Log.e("TX", "Delete FAIL", e); onDone(false) }
    }

    //TODO: fix this into AddTransactionScreen.kt
    fun totalsByCategory(): Map<String, Double> =
        transactions.value
            .groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.amount } }
}
