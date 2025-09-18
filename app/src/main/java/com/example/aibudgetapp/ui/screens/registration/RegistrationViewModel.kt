package com.example.aibudgetapp.ui.screens.registration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest


class RegistrationViewModel : ViewModel() {
    private var auth: FirebaseAuth = Firebase.auth

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
                    registerError = true
                    registerErrorMessage =
                        task.exception?.localizedMessage ?: "Registration failed."
                    return@addOnCompleteListener
                }

                val user = auth.currentUser
                val display = if (lastName.isNullOrBlank()) firstName else "$firstName $lastName"

                user?.updateProfile(userProfileChangeRequest { displayName = display })
                isRegistered = true
            }
    }
    fun consumeRegistrationSuccess() {
        isRegistered = false
    }
}