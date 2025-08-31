package com.example.aibudgetapp.ui.screens.registration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class RegistrationViewModel : ViewModel() {

    var isRegistered by mutableStateOf(false)
        private set

    var registerError by mutableStateOf(false)
        private set


    fun register(email: String, password: String) {
        //TODO: Change this code to actual registration
        if (email == "admin@gmail.com" && password == "1234") {
            isRegistered = true
            registerError = false

        } else {
            registerError = true
        }
    }
    fun consumeRegistrationSuccess() {
        isRegistered = false
    }
}