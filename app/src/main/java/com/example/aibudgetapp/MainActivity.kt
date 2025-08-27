package com.example.aibudgetapp

import android.os.Bundle
import com.example.aibudgetapp.ui.screens.login.LoginScreen
import com.example.aibudgetapp.ui.screens.screenContainer.ScreenContainer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aibudgetapp.ui.screens.login.LoginViewModel

class MainActivity : ComponentActivity() {
    private val loginViewModel by viewModels<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isLoggedIn by loginViewModel.isLoggedIn.collectAsStateWithLifecycle()
            val loginError by loginViewModel.loginError.collectAsStateWithLifecycle()
            val userName   by loginViewModel.userName.collectAsStateWithLifecycle()

            if (isLoggedIn) {
                ScreenContainer(userName = userName)
            } else {
                LoginScreen(
                    onLogin = { id, pass -> loginViewModel.login(id, pass) },
                    loginError = loginError
                )
            }
        }
    }
}
