package com.example.aibudgetapp.ui.screens.registration

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch

class RegistrationViewModel : ViewModel() {
    private var auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Registration UI state flags
    var isRegistered by mutableStateOf(false)
        private set

    var registerError by mutableStateOf(false)
        private set

    var registerErrorMessage by mutableStateOf<String?>(null)
        private set

    /**
     * Self-registration flow (signs the app into the newly created user).
     * Steps:
     *  1) Create auth user
     *  2) Update display name
     *  3) Write Firestore user profile doc
     */
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

    /**
     * Maps common Firebase auth exceptions to friendly messages.
     */
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

    /**
     * Creates/merges the Firestore user profile document.
     */
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

    /**
     * Resets the success flag after the UI has shown success feedback.
     */
    fun consumeRegistrationSuccess() {
        isRegistered = false
    }

    /**
     * Owner-initiated “Add user” flow (delegation model).
     *
     * This feature is designed to create a completely separate Firebase Auth account
     * (new email + password), while immediately linking that account’s Firestore profile
     * to the owner’s uid.
     *
     * By saving the owner’s uid into the new profile doc, the new account effectively
     * becomes a “joined account” — it can access the owner’s budgets/transactions
     * under the shared rules, so both users work on the same data space.
     */
    fun addUser(email: String, password: String, firstName: String, lastName: String?, uid: String) {
        registerError = false
        registerErrorMessage = null
        isRegistered = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    handleAuthError(task.exception)
                    return@addOnCompleteListener
                }

                val user = task.result?.user ?: return@addOnCompleteListener
                val display = if (lastName.isNullOrBlank()) firstName else "$firstName $lastName"

                viewModelScope.launch {
                    try {
                        val repo = com.example.aibudgetapp.data.AccountRepository(
                            auth = com.google.firebase.auth.FirebaseAuth.getInstance(),
                            db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        )
                        repo.ensureUserProfileDoc(user, uid, display)
                        Log.d("AddUser", "User added")
                    } catch (_: Exception) {
                    }
                }
            }
    }
}
