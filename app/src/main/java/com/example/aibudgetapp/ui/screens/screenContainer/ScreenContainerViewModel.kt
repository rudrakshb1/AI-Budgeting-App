package com.example.aibudgetapp.ui.screens.screenContainer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ScreenContainerViewModel {
    var currentScreen by mutableStateOf(Screen.HOME)
        private set

    fun navigateTo(screen: Screen) {
        currentScreen = screen
    }
    var addTransactionError by mutableStateOf(false)
        private set

}