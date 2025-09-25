package com.example.aibudgetapp.ui.screens.screenContainer

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aibudgetapp.ui.components.BottomNavBar
import com.example.aibudgetapp.ui.screens.home.HomeScreen
import com.example.aibudgetapp.ui.screens.settings.SettingsScreen
import com.example.aibudgetapp.ui.screens.budget.BudgetScreen
import com.example.aibudgetapp.ui.screens.transaction.AddTransactionScreen
import com.example.aibudgetapp.ui.screens.budget.BudgetOverviewScreen
import com.example.aibudgetapp.ui.theme.AIBudgetAppTheme
import com.example.aibudgetapp.ui.screens.transaction.ReceiptFlowScreen
import com.example.aibudgetapp.ui.screens.transaction.AddTransactionViewModel
import com.example.aibudgetapp.ui.screens.transaction.AddTransactionViewModelFactory
import com.example.aibudgetapp.ui.screens.transaction.TransactionRepository
import com.example.aibudgetapp.ui.screens.settings.SettingsViewModel
import com.example.aibudgetapp.ui.screens.screenContainer.ScreenContainerViewModel
import com.example.aibudgetapp.data.AccountRepository
import com.example.aibudgetapp.ui.screens.settings.SettingsViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

sealed class Screen {
    object HOME : Screen()
    object ADDTRANSACTION : Screen()
    object SETTINGS : Screen()
    object BUDGETOVERVIEW : Screen()
    object BUDGET : Screen()
    data class RECEIPTFLOW(val uri: Uri) : Screen()
}

@Composable
fun ScreenContainer(userName: String, onLogout: () -> Unit) {
    val screenContainerViewModel: ScreenContainerViewModel = viewModel()

    // Use the Factory to create ViewModel with repo dependency
    val addTransactionViewModel: AddTransactionViewModel = viewModel(
        factory = AddTransactionViewModelFactory(TransactionRepository())
    )

    AIBudgetAppTheme {
        Scaffold(
            bottomBar = {
                BottomNavBar(
                    onHomeClick = { screenContainerViewModel.navigateTo(Screen.HOME) },
                    onBudgetClick = { screenContainerViewModel.navigateTo(Screen.BUDGETOVERVIEW) },
                    onSettingsClick = { screenContainerViewModel.navigateTo(Screen.SETTINGS) }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (val screen = screenContainerViewModel.currentScreen) {
                    Screen.HOME -> HomeScreen(
                        userName = userName,
                        screenContainerViewModel = screenContainerViewModel
                    )

                    Screen.ADDTRANSACTION -> AddTransactionScreen(
                        onReceiptPicked = { uri ->
                            screenContainerViewModel.navigateTo(Screen.RECEIPTFLOW(uri))
                        }
                    )

                    Screen.SETTINGS -> {
                        // Repo + VM factory wiring for Settings
                        val repo = remember {
                            AccountRepository(
                                auth = FirebaseAuth.getInstance(),
                                db = FirebaseFirestore.getInstance()
                            )
                        }
                        val settingsViewModel: SettingsViewModel =
                            viewModel(factory = SettingsViewModelFactory(repo))

                        SettingsScreen(
                            uiState = settingsViewModel.uiState,
                            onAddUser = settingsViewModel::onAddUserClick,
                            onLogout = onLogout,
                            onConfirmEditName = { newName ->
                                settingsViewModel.onEditProfileConfirm(newName)
                            },
                            onDeleteAccount = {
                                settingsViewModel.deleteAccount(
                                    onSuccess = { onLogout() }, // after deletion, return to login
                                    onError = { /* TODO: show snackbar/toast */ }
                                )
                            }
                        )
                    }

                    Screen.BUDGETOVERVIEW -> BudgetOverviewScreen(
                        onAddBudgetClick = { screenContainerViewModel.navigateTo(Screen.BUDGET) }
                    )

                    Screen.BUDGET -> BudgetScreen(
                        onBackClick = { screenContainerViewModel.navigateTo(Screen.BUDGETOVERVIEW) }
                    )

                    is Screen.RECEIPTFLOW -> ReceiptFlowScreen(
                        imageUri = screen.uri,
                        addTransactionViewModel = addTransactionViewModel,
                        onComplete = { screenContainerViewModel.navigateTo(Screen.ADDTRANSACTION) }
                    )
                }
            }
        }
    }
}
