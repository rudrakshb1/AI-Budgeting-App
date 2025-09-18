package com.example.aibudgetapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.example.aibudgetapp.ui.screens.login.*
import com.example.aibudgetapp.ui.screens.screenContainer.ScreenContainer
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aibudgetapp.ui.screens.registration.RegistrationScreen
import com.example.aibudgetapp.ui.screens.registration.RegistrationViewModel




class MainActivity : ComponentActivity() {

    private val loginViewModel by viewModels<LoginViewModel>()
    private val registrationViewModel by viewModels<RegistrationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {

            var showRegister by remember { mutableStateOf(false) }

            val isLoggedIn by remember { derivedStateOf { loginViewModel.isLoggedIn } } //default: false
            val loginError by remember { derivedStateOf { loginViewModel.loginError } } //default: false
            val userName by remember { derivedStateOf { loginViewModel.userName } } //default: ""

            val isRegistered = registrationViewModel.isRegistered
            val registrationError = registrationViewModel.registerError

            LaunchedEffect(isRegistered) {
                if (isRegistered) {
                    showRegister = false
                    registrationViewModel.consumeRegistrationSuccess()
                }
            }

            if (isLoggedIn) {
                ScreenContainer(
                    userName = userName,
                    onLogout = { loginViewModel.logout() }
                )
            } else if (showRegister) {
                RegistrationScreen(
                    onRegister = { email, password, fn, ln -> registrationViewModel.register(email, password, fn, ln) },
                    onCancel = { showRegister = false },
                    registrationError = registrationError,
                    registrationErrorMessage = registrationViewModel.registerErrorMessage
                )
            } else {
                LoginScreen( //open LoginScreen if not loggedIn
                    onLogin = { id, password -> loginViewModel.login(id, password) },
                    onRegister = {showRegister=true},
                    loginError = loginError,
                    loginErrorMessage = loginViewModel.loginErrorMessage
                )
            }
        }
    }
}

