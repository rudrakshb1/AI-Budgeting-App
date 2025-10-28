package com.example.aibudgetapp.ui.screens.screenContainer

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aibudgetapp.ui.components.BottomNavBar
import com.example.aibudgetapp.ui.screens.home.HomeScreen
import com.example.aibudgetapp.ui.screens.settings.SettingsScreen
import com.example.aibudgetapp.ui.screens.budget.BudgetScreen
import com.example.aibudgetapp.ui.screens.transaction.AddTransactionScreen
import com.example.aibudgetapp.ui.screens.budget.BudgetOverviewScreen
import com.example.aibudgetapp.ui.screens.chatbot.ChatbotScreen
import com.example.aibudgetapp.ui.theme.AIBudgetAppTheme
import com.example.aibudgetapp.ui.screens.transaction.ReceiptFlowScreen
import com.example.aibudgetapp.ui.screens.transaction.AddTransactionViewModel
import com.example.aibudgetapp.ui.screens.transaction.AddTransactionViewModelFactory
import com.example.aibudgetapp.ui.screens.transaction.TransactionRepository
import com.example.aibudgetapp.ui.screens.settings.SettingsViewModel
import com.example.aibudgetapp.notifications.NotificationsScreen
import com.example.aibudgetapp.ui.screens.registration.RegistrationViewModel
import com.example.aibudgetapp.ui.screens.settings.RemindersScreen
import com.example.aibudgetapp.ui.screens.settings.UploadedReceiptsScreen
import com.example.aibudgetapp.ui.screens.settings.ExportReportScreen
import com.example.aibudgetapp.ui.screens.budget.BudgetViewModel
import com.example.aibudgetapp.ui.screens.budget.BudgetRepository
import com.example.aibudgetapp.ui.screens.settings.FaqScreen


sealed class Screen {
    object HOME : Screen()
    object ADDTRANSACTION : Screen()
    object SETTINGS : Screen()
    object BUDGETOVERVIEW : Screen()
    object BUDGET : Screen()
    data class RECEIPTFLOW(val uri: Uri) : Screen()
    object CHATBOT : Screen()
    object NOTIFICATIONS : Screen()
    object REMINDERS : Screen()
    object UPLOADED_RECEIPTS : Screen()
    object EXPORT_DATA : Screen()
    object FAQ : Screen()





}

@Composable
fun ScreenContainer(
    settingsViewModel: SettingsViewModel,
    onLogout: () -> Unit,
    onNavigatePasscode: () -> Unit
) {
    val screenContainerViewModel: ScreenContainerViewModel = viewModel()



    // Use the Factory to create ViewModel with repo dependency
    val addTransactionViewModel: AddTransactionViewModel = viewModel(
        factory = AddTransactionViewModelFactory(TransactionRepository())
    )

    val budgetRepository = BudgetRepository()
    val budgetViewModel: BudgetViewModel = viewModel(
        factory = BudgetViewModel.Factory(budgetRepository)
    )


    AIBudgetAppTheme {
        Scaffold(
            bottomBar = {
                BottomNavBar(
                    onHomeClick = { screenContainerViewModel.navigateTo(Screen.HOME) },
                    onBudgetClick = { screenContainerViewModel.navigateTo(Screen.BUDGETOVERVIEW) },
                    onSettingsClick = { screenContainerViewModel.navigateTo(Screen.SETTINGS) },
                    onChatbotClick = { screenContainerViewModel.navigateTo(Screen.CHATBOT) }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (val screen = screenContainerViewModel.currentScreen) {

                    Screen.HOME -> HomeScreen(
                        uiState = settingsViewModel.uiState,
                        screenContainerViewModel = screenContainerViewModel,
                        onBellClick = { screenContainerViewModel.navigateTo(Screen.NOTIFICATIONS) }
                    )

                    Screen.ADDTRANSACTION -> AddTransactionScreen(
                        onReceiptPicked = { uri ->
                            screenContainerViewModel.navigateTo(Screen.RECEIPTFLOW(uri))
                        }
                    )

                    Screen.CHATBOT -> ChatbotScreen()

                    Screen.FAQ -> FaqScreen(
                        onBack = { screenContainerViewModel.navigateTo(Screen.SETTINGS) }
                    )

                    Screen.SETTINGS -> {
                        // Repo + VM factory wiring for Settings
                        SettingsScreen(
                            uiState = settingsViewModel.uiState,
                            onEditProfilePhoto = { newPhoto ->
                                settingsViewModel.onEditProfilePhoto(newPhoto)
                            },
                            onNavigateUploads = { screenContainerViewModel.navigateTo(Screen.UPLOADED_RECEIPTS) },
                            onNavigatePasscode = onNavigatePasscode,
                            onNavigateFaq = { screenContainerViewModel.navigateTo(Screen.FAQ) },
                            onNavigateReminders = { screenContainerViewModel.navigateTo(Screen.REMINDERS) },
                            onNavigateExport = { screenContainerViewModel.navigateTo(Screen.EXPORT_DATA) },
                            onLogout = {
                                screenContainerViewModel.navigateTo(Screen.HOME)
                                onLogout()
                            },
                            onConfirmEditName = { newName ->
                                settingsViewModel.onEditProfileDisplayName(newName)
                            },
                            onAddUser = { email, password, firstName, lastName ->
                                RegistrationViewModel().addUser(email, password, firstName, lastName, settingsViewModel.getUid())
                            },
                            onDeleteAccount = {
                                settingsViewModel.deleteAccount(
                                    onSuccess = {
                                        screenContainerViewModel.navigateTo(Screen.HOME)
                                        onLogout()
                                    }, // after deletion, return to login
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

                    Screen.NOTIFICATIONS -> NotificationsScreen(
                        onBack = { screenContainerViewModel.navigateTo(Screen.HOME) }
                    )
                    Screen.REMINDERS -> RemindersScreen(
                        onBack = { screenContainerViewModel.navigateTo(Screen.SETTINGS) },
                        onToggleChanged = { /* hook to ThresholdNotifier in next step */ }
                    )
                    Screen.UPLOADED_RECEIPTS -> UploadedReceiptsScreen(
                        transactions = addTransactionViewModel.transactions
                    )

                    Screen.EXPORT_DATA -> ExportReportScreen(
                        transactions = addTransactionViewModel.transactions,
                        budgets = budgetViewModel.budgets,
                        fetchBudgets = { budgetViewModel.fetchBudgets() }
                    )

                }
            }
        }
    }
}
