package com.example.aibudgetapp.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {
    var isLoggedIn by mutableStateOf(false)
        private set

    var loginError by mutableStateOf(false)
        private set

    var userName by mutableStateOf("") // after Login, save userName
        private set

    fun login(id: String, password: String) {
        //TODO: Change this code to actual login
        if (id == "admin" && password == "1234") {
            isLoggedIn = true
            loginError = false
            userName = id //TODO: Change this to username
        } else {
            loginError = true
        }
    }
}
