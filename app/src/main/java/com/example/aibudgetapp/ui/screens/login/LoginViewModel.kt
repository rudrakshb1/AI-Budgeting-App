package com.example.aibudgetapp.ui.screens.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel : ViewModel() {
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _loginError = MutableStateFlow(false)
    val loginError: StateFlow<Boolean> = _loginError

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName

    fun login(id: String, password: String) {
        if (id == "admin" && password == "1234") {
            _isLoggedIn.value = true
            _loginError.value = false
            _userName.value = id
        } else {
            _loginError.value = true
        }
    }
}
