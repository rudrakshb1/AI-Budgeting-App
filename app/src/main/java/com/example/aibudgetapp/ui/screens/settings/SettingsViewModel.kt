package com.example.aibudgetapp.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class SettingsUiState(
    val displayName: String = "",
    val avatarInitials: String = ""
)


class SettingsViewModel : ViewModel() {
    var uiState by mutableStateOf(SettingsUiState())
        private set

    fun setDisplayName(name: String) {
        val initials = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: ""
        uiState = uiState.copy(displayName = name, avatarInitials = initials)
    }

    fun onEditProfileClick() { /* TODO */ }
    fun onAddUserClick() { /* TODO */ }
}
