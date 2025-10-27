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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aibudgetapp.ui.screens.settings.ChangePasswordScreen
import com.google.firebase.auth.EmailAuthProvider


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

                        val navController = rememberNavController()
                        NavHost(navController = navController, startDestination = "app") {

                            composable("app") {
                                ScreenContainer(
                                    settingsViewModel = settingsViewModel,
                                    onLogout = { loginViewModel.logout() },
                                    onNavigatePasscode = { navController.navigate("change-password") }
                                )
                            }

                            composable("change-password") {
                                ChangePasswordScreen(
                                    onBack = { navController.popBackStack() },
                                    onPasswordChanged = { currentPassword, newPassword, onResult ->
                                        val user = FirebaseAuth.getInstance().currentUser
                                        val email = user?.email
                                        if (user == null || email.isNullOrBlank()) {
                                            onResult(false, "No signed-in user.")
                                            return@ChangePasswordScreen
                                        }
                                        val cred = EmailAuthProvider.getCredential(email, currentPassword)
                                        user.reauthenticate(cred)
                                            .addOnSuccessListener {
                                                user.updatePassword(newPassword)
                                                    .addOnSuccessListener {
                                                        onResult(true, "Password updated.")
                                                    }
                                                    .addOnFailureListener { e ->
                                                        onResult(false, e.localizedMessage ?: "Update failed.")
                                                    }
                                            }
                                            .addOnFailureListener { e ->
                                                onResult(false, e.localizedMessage ?: "Reauthentication failed.")
                                            }
                                    }
                                )
                            }
                        }
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

