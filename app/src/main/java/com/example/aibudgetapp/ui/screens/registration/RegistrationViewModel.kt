package com.example.aibudgetapp.ui.screens.registration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class RegistrationViewModel : ViewModel() {
    private var auth: FirebaseAuth = Firebase.auth

    var isRegistered by mutableStateOf(false)
        private set

    var registerError by mutableStateOf(false)
        private set

    var registerErrorMessage by mutableStateOf<String?>(null)
        private set

    fun register(email: String, password: String) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Registration success
                    isRegistered = true
                    registerError = false
                    registerErrorMessage = null

                    val user = auth.currentUser
                    // TODO: You can save user information to Firestore or other databases if needed.

                } else {
                    // Registration failed
                    registerError = true
                    registerErrorMessage = task.exception?.localizedMessage
                    // Log the exception to debug the reason for failure.
                    task.exception?.printStackTrace()
                }
            }
    }
    fun consumeRegistrationSuccess() {
        isRegistered = false
    }
}