package com.example.aibudgetapp.ui.screens.settings

import android.net.Uri
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
    val avatarInitials: String = "",
    val photoUri: Uri? = null
)

class SettingsViewModel(
    private val accountRepo: AccountRepository
) : ViewModel() {

    var uiState by mutableStateOf(SettingsUiState())
        private set

    init {
        val currentDisplayName = accountRepo.currentDisplayName()
        val currentPhoto = accountRepo.currentPhotoUri()
        if (currentDisplayName.isNotBlank()) setDisplayName(currentDisplayName)
        if (currentPhoto != null) setPhoto(currentPhoto)
    }

    fun getUid(): String {
        return accountRepo.currentUid()
    }

    private fun setDisplayName(name: String) {
        val initials = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: ""
        uiState = uiState.copy(displayName = name, avatarInitials = initials)
    }

    private fun setPhoto(photo: Uri?) {
        uiState = uiState.copy(photoUri = photo)
    }

    fun onEditProfileDisplayName(
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

    fun onEditProfilePhoto(
        newPhoto: Uri?,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                accountRepo.updateProfilePhoto(newPhoto)
                setPhoto(newPhoto)
                onSuccess?.invoke()
            } catch (e: Exception) {
                onError?.invoke(e.localizedMessage ?: "Failed to update photo")
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
}

class SettingsViewModelFactory(
    private val repo: AccountRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(repo) as T
    }
}
