package com.example.aibudgetapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.example.aibudgetapp.ui.screens.login.*
import com.example.aibudgetapp.ui.screens.screenContainer.ScreenContainer
import com.example.aibudgetapp.ui.screens.DocumentUploadScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


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
            //  ONLY YOU USE THIS!
            val showUploadTestScreen = false // Set to true ONLY to test DocumentUploadScreen

            if (showUploadTestScreen) {
                // Displays  DocumentUploadScreen for isolated testing.
                DocumentUploadScreen()
            } else {
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
}



