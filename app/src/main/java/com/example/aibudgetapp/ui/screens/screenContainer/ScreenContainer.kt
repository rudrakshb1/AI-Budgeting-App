package com.example.aibudgetapp.ui.screens.screenContainer


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.aibudgetapp.ui.components.BottomNavBar
import com.example.aibudgetapp.ui.screens.budget.BudgetOverviewScreen
import com.example.aibudgetapp.ui.screens.budget.BudgetScreen
import com.example.aibudgetapp.ui.screens.home.HomeScreen
import com.example.aibudgetapp.ui.screens.settings.SettingsScreen
import com.example.aibudgetapp.ui.screens.budget.BudgetScreen
import com.example.aibudgetapp.ui.screens.settings.SettingsUiState
import com.example.aibudgetapp.ui.screens.transaction.AddTransactionScreen
import com.example.aibudgetapp.ui.screens.budget.BudgetOverviewScreen
import com.example.aibudgetapp.ui.theme.AIBudgetAppTheme

enum class Screen { HOME, ADDTRANSACTION, SETTINGS, BUDGETOVERVIEW, BUDGET }

@Composable
fun ScreenContainer(
    userName: String,
    onLogout: () -> Unit,
) {
    val screenContainerViewModel = remember { ScreenContainerViewModel() }

    val initials = userName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    SettingsScreen(
        uiState = SettingsUiState(displayName = userName, avatarInitials = initials),
        onLogout = onLogout
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
                when (screenContainerViewModel.currentScreen) {
                    Screen.HOME -> HomeScreen(
                        userName = userName,
                        screenContainerViewModel = screenContainerViewModel,
                    )
                    Screen.ADDTRANSACTION -> AddTransactionScreen()
                    Screen.SETTINGS -> SettingsScreen(
                        uiState = SettingsUiState(displayName = userName, avatarInitials = initials),
                        bottomBar = { },
                        onLogout = onLogout
                    )
                    Screen.BUDGETOVERVIEW -> BudgetOverviewScreen(
                        onAddBudgetClick = { screenContainerViewModel.navigateTo(Screen.BUDGET) }
                    )
                    Screen.BUDGET -> BudgetScreen(
                        onBackClick = { screenContainerViewModel.navigateTo(Screen.BUDGETOVERVIEW) }
                    )
                }
            }
        }
    }
}
