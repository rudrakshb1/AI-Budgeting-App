package com.example.aibudgetapp.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
class AccountRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    fun currentDisplayName(): String = auth.currentUser?.displayName.orEmpty()
    fun currentPhotoUri(): Uri? = auth.currentUser?.photoUrl

    suspend fun updateDisplayName(newName: String) {
        val user = auth.currentUser ?: error("Not logged in")
        val req = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()

        user.updateProfile(req).await() // Auth
        db.collection("users").document(user.uid)            // Firestore mirror
            .set(mapOf("displayName" to newName), SetOptions.merge())
            .await()
    }

    suspend fun updateProfilePhoto(photo: Uri?) {
        val user = auth.currentUser ?: error("Not logged in")
        val req = UserProfileChangeRequest.Builder()
            .setPhotoUri(photo)
            .build()

        user.updateProfile(req).await() // Auth
        db.collection("users").document(user.uid)            // Firestore mirror
            .set(mapOf("photoUri" to photo), SetOptions.merge())
            .await()
    }


    suspend fun deleteAccount(): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("Not logged in"))
        val uid = user.uid
        return try {
            suspend fun deleteSubcollection(name: String) {
                val docs = db.collection("users").document(uid).collection(name).get().await()
                for (d in docs.documents) d.reference.delete().await()
            }

            runCatching { deleteSubcollection("budgets") }
            runCatching { deleteSubcollection("transactions") }

            db.collection("users").document(uid).delete().await()
            user.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun ensureUserProfileDoc() {
        val user = auth.currentUser ?: return
        val doc = db.collection("users").document(user.uid)

        val snap = doc.get().await()
        val base = mutableMapOf<String, Any>(
            "uid" to user.uid,
            "email" to (user.email ?: ""),
            "displayName" to (user.displayName ?: ""),
            "lastLoginAt" to FieldValue.serverTimestamp()
        )
        if (!snap.exists()) {
            base["createdAt"] = FieldValue.serverTimestamp()
        }

        // DO NOT DELETE - this merges users data
        doc.set(base, SetOptions.merge()).await()
    }
}
