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



class MainActivity : ComponentActivity() {

    private val loginViewModel by viewModels<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isLoggedIn by remember { derivedStateOf { loginViewModel.isLoggedIn } } //default: false
            val loginError by remember { derivedStateOf { loginViewModel.loginError } } //default: false
            val userName by remember { derivedStateOf { loginViewModel.userName } } //default: ""

            if (isLoggedIn) {
                ScreenContainer( userName = userName) //open main page if loggedIn
            } else {
                LoginScreen( //open LoginScreen if not loggedIn
                    onLogin = { id, password -> loginViewModel.login(id, password) },
                    loginError = loginError
                )
            }
        }
    }
}

