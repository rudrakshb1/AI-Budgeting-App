package com.example.aibudgetapp.ui.screens.screenContainer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.aibudgetapp.ui.components.*
import com.example.aibudgetapp.ui.screens.home.HomeScreen
import com.example.aibudgetapp.ui.screens.settings.SettingsScreen
import com.example.aibudgetapp.ui.screens.transaction.AddTransactionScreen
import com.example.aibudgetapp.ui.theme.*

enum class Screen { HOME, ADDTRANSACTION, SETTINGS }

@Composable
fun ScreenContainer(userName: String) {
    val screenContainerViewModel = remember { ScreenContainerViewModel() }

    AIBudgetAppTheme {
        Scaffold(
            bottomBar = {
                BottomNavBar(
                    onHomeClick = { screenContainerViewModel.navigateTo(Screen.HOME) },
                    onAddTransactionButtonClick = { screenContainerViewModel.navigateTo(Screen.ADDTRANSACTION) },
                    onSettingsClick = { screenContainerViewModel.navigateTo(Screen.SETTINGS) }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (screenContainerViewModel.currentScreen) {
                    Screen.HOME -> HomeScreen(userName = userName)
                    Screen.ADDTRANSACTION -> AddTransactionScreen()
                    Screen.SETTINGS -> SettingsScreen()
                }
            }
        }
    }
}
