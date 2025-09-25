package com.example.aibudgetapp.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class SettingsUiState(
    val displayName: String = "",
    val avatarInitials: String = ""
)

class SettingsViewModel : ViewModel() {

    var uiState by mutableStateOf(SettingsUiState())
        private set

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    init {
        val current = auth.currentUser?.displayName.orEmpty()
        if (current.isNotBlank()) setDisplayName(current)
    }

    fun setDisplayName(name: String) {
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
                val user = auth.currentUser ?: error("Not logged in")
                val req = UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build()
                user.updateProfile(req).await()
                setDisplayName(newName)
                onSuccess?.invoke()
            } catch (e: Exception) {
                onError?.invoke(e.localizedMessage ?: "Failed to update name")
            }
        }
    }

    fun onEditProfileClick() { }
    fun onAddUserClick() { /* TODO */ }
}
