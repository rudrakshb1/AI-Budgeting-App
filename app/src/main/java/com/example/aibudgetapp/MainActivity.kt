package com.example.aibudgetapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.aibudgetapp.ui.screens.login.*
import com.example.aibudgetapp.ui.screens.screenContainer.ScreenContainer
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aibudgetapp.data.AccountRepository
import com.example.aibudgetapp.ui.screens.registration.RegistrationScreen
import com.example.aibudgetapp.ui.screens.registration.RegistrationViewModel
import com.example.aibudgetapp.ui.screens.settings.SettingsViewModel
import com.example.aibudgetapp.ui.screens.settings.SettingsViewModelFactory
import com.example.aibudgetapp.ui.theme.AIBudgetAppTheme
import com.example.aibudgetapp.ui.theme.LocalThemeController
import com.example.aibudgetapp.ui.theme.ThemeController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID


class MainActivity : ComponentActivity() {

    private val loginViewModel by viewModels<LoginViewModel>()
    private val registrationViewModel by viewModels<RegistrationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {

            val themeController = remember { ThemeController() }

            CompositionLocalProvider(LocalThemeController provides themeController) {
                AIBudgetAppTheme(mode = themeController.mode) {

                    var showRegister by remember { mutableStateOf(false) }

                    val isLoggedIn by remember { derivedStateOf { loginViewModel.isLoggedIn } } //default:false
                    val loginError by remember { derivedStateOf { loginViewModel.loginError } } //default:false

                    val isRegistered = registrationViewModel.isRegistered
                    val registrationError = registrationViewModel.registerError

                    if (isLoggedIn) {
                        val sessionKey by rememberSaveable(isLoggedIn) {
                            mutableStateOf(UUID.randomUUID().toString())
                        }

                        val repo = remember(sessionKey) {
                            AccountRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
                        }
                        Log.d("MainActivity", "uid: ${repo.currentUid()}, displayName: ${repo.currentDisplayName()}")
                        val settingsViewModel: SettingsViewModel =
                            viewModel(
                                key = "settings-$sessionKey",
                                factory = SettingsViewModelFactory(repo)
                            )

                        ScreenContainer(
                            settingsViewModel = settingsViewModel,
                            onLogout = { loginViewModel.logout() }
                        )
                    } else if (showRegister) {
                        RegistrationScreen(
                            onRegister = { email, password, fn, ln -> registrationViewModel.register(email, password, fn, ln) },
                            onCancel = { showRegister = false },
                            registrationError = registrationError,
                            registrationErrorMessage = registrationViewModel.registerErrorMessage,
                            isRegistered = isRegistered,
                            onSuccessAcknowledged = {
                                showRegister = false
                                registrationViewModel.consumeRegistrationSuccess()
                            }
                        )
                    } else {
                        LoginScreen(
                            onLogin = {
                                      id, password -> loginViewModel.login(id, password)

                                  },
                            onRegister = { showRegister = true },
                            loginError = loginError,
                            loginErrorMessage = loginViewModel.loginErrorMessage
                        )
                    }
                }
            }
        }
    }
}

