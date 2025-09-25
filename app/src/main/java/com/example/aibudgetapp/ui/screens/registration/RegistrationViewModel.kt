package com.example.aibudgetapp.ui.screens.registration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class RegistrationViewModel : ViewModel() {
    private var auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    var isRegistered by mutableStateOf(false)
        private set

    var registerError by mutableStateOf(false)
        private set

    var registerErrorMessage by mutableStateOf<String?>(null)
        private set

    fun register(email: String, password: String, firstName: String, lastName: String?) {
        registerError = false
        registerErrorMessage = null
        isRegistered = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    handleAuthError(task.exception)
                    return@addOnCompleteListener
                }

                val user = auth.currentUser ?: return@addOnCompleteListener
                val display = if (lastName.isNullOrBlank()) firstName else "$firstName $lastName"

                val req = UserProfileChangeRequest.Builder()
                    .setDisplayName(display)
                    .build()

                user.updateProfile(req)
                    .addOnFailureListener { e ->
                        registerError = true
                        registerErrorMessage = e.localizedMessage ?: "Failed to set display name."
                        writeUserDoc(user.uid, user.email.orEmpty(), display)
                    }
                    .addOnSuccessListener {
                        writeUserDoc(user.uid, user.email.orEmpty(), display)
                    }
            }
    }

    private fun handleAuthError(e: Exception?) {
        registerError = true
        registerErrorMessage = when (e) {
            is com.google.firebase.FirebaseNetworkException ->
                "Can't reach the server. Check your internet connection."
            is com.google.firebase.auth.FirebaseAuthUserCollisionException ->
                "An account already exists with this email."
            is com.google.firebase.auth.FirebaseAuthWeakPasswordException ->
                "Password is too weak. Try a stronger one."
            else -> e?.localizedMessage ?: "Registration failed. Please try again."
        }
    }

    private fun writeUserDoc(uid: String, email: String, displayName: String) {
        val data = hashMapOf(
            "uid" to uid,
            "email" to email,
            "displayName" to displayName,
            "createdAt" to FieldValue.serverTimestamp(),
            "lastLoginAt" to FieldValue.serverTimestamp()
        )

        db.collection("users").document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { isRegistered = true }
            .addOnFailureListener { e ->
                registerError = true
                registerErrorMessage = e.localizedMessage ?: "Failed to save profile."
            }
    }

    fun consumeRegistrationSuccess() {
        isRegistered = false
    }
}
