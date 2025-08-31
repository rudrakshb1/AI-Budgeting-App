package com.example.aibudgetapp.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.auth

class LoginViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    var isLoggedIn by mutableStateOf(false)
        private set

    var loginError by mutableStateOf(false)
        private set

    var userName by mutableStateOf("") // after Login, save userName
        private set

    var loginErrorMessage by mutableStateOf<String?>(null)
        private set

    private fun mapAuthErrorShort(e: Exception): String = when (e) {
        is FirebaseAuthInvalidCredentialsException,
        is FirebaseAuthInvalidUserException -> "Invalid email or password."
        is FirebaseNetworkException -> "Network error. Check your connection."
        else -> "Something went wrong. Please try again."
    }

    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login success
                    val user = auth.currentUser
                    isLoggedIn = true
                    loginError = false
                    loginErrorMessage = null
                    userName = user?.email ?: ""
                } else {
                    // Login failed
                    isLoggedIn = false
                    loginError = true
                    loginErrorMessage = mapAuthErrorShort(task.exception ?: Exception())
                }
            }
    }

    fun logout() {
        auth.signOut()
        isLoggedIn = false
        userName = ""
        loginError = false
        loginErrorMessage = null
    }
}
