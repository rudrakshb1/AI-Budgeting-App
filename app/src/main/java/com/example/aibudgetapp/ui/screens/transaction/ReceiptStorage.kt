package com.example.aibudgetapp.ui.screens.transaction

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

object ReceiptStorage {
    private val storage = FirebaseStorage.getInstance()

    fun pathFor(ownerUid: String, txnId: String) =
        "receipts/$ownerUid/$txnId.jpg"

    suspend fun uploadAndGetUrl(localUri: Uri, ownerUid: String, txnId: String): String {
        val ref = storage.reference.child(pathFor(ownerUid, txnId))
        ref.putFile(localUri).await()
        return ref.downloadUrl.await().toString()
    }
}

