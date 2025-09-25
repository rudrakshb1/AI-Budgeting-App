package com.example.aibudgetapp.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aibudgetapp.data.AccountRepository
import kotlinx.coroutines.launch

data class SettingsUiState(
    val displayName: String = "",
    val avatarInitials: String = ""
)

class SettingsViewModel(
    private val accountRepo: AccountRepository
) : ViewModel() {

    var uiState by mutableStateOf(SettingsUiState())
        private set

    init {
        val current = accountRepo.currentDisplayName()
        if (current.isNotBlank()) setDisplayName(current)
    }

    private fun setDisplayName(name: String) {
        val initials = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: ""
        uiState = uiState.copy(displayName = name, avatarInitials = initials)
    }

    fun onEditProfileConfirm(
        newName: String,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                accountRepo.updateDisplayName(newName)
                setDisplayName(newName)
                onSuccess?.invoke()
            } catch (e: Exception) {
                onError?.invoke(e.localizedMessage ?: "Failed to update name")
            }
        }
    }

    fun deleteAccount(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = try { accountRepo.deleteAccount() } catch (e: Exception) { Result.failure(e) }
            result.onSuccess { onSuccess() }
                .onFailure { e -> onError(e.localizedMessage ?: "Couldn't delete account") }
        }
    }

    fun onAddUserClick() { /* TODO if we to do multi-user */ }
    fun onEditProfileClick() { }
}

class SettingsViewModelFactory(
    private val repo: AccountRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(repo) as T
    }
}
