package com.example.aibudgetapp.ai

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

data class ChatMessage(val text: String, val isUser: Boolean)
data class ChatbotUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isSending: Boolean = false,
    val error: String? = null
)

class ChatbotViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChatbotUiState())
    val uiState: StateFlow<ChatbotUiState> = _uiState

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        // 1) Append the user message immediately
        _uiState.update { it.copy(messages = it.messages + ChatMessage(userText, true)) }

        // 2) Call Gemini on a background coroutine
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }
            try {
                // Simple health check: "/ping" should return "OK"
                val prompt = if (userText.trim() == "/ping")
                    "Reply with exactly: OK"
                else
                    """
                    You are a chatbot that only answers questions about budgets and transactions.
                    - If the question is related to budgets, spending, expenses, income, or transactions, answer normally.
                    - If the question is not related, reply with exactly: "I can only help with budgets and transactions."
                    
                    User: $userText
                    """

                val raw = GeminiClient.postText(prompt)
                Log.d("ChatbotVM", "Gemini raw: $raw")

                // Prefer parsed text; fall back to minimal status message
                val parsed = extractText(raw)
                val botText = when {
                    parsed != null -> parsed
                    raw != null      -> "✅ Connected (parse failed). Raw received"
                    else             -> "❌ Gemini call failed (no response)"
                }

                _uiState.update {
                    it.copy(
                        messages = it.messages + ChatMessage(botText, false),
                        isSending = false
                    )
                }
            } catch (e: Exception) {
                Log.e("ChatbotVM", "Gemini error", e)
                _uiState.update {
                    it.copy(
                        isSending = false,
                        error = e.message,
                        messages = it.messages + ChatMessage("❌ Error: ${e.message}", false)
                    )
                }
            }
        }
    }

    /** Extracts first candidate text from Google AI Studio JSON */
    private fun extractText(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return try {
            val root = JSONObject(raw)
            val cand = root.optJSONArray("candidates")?.optJSONObject(0)
            val content = cand?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            parts?.optJSONObject(0)?.optString("text")?.takeIf { it.isNotBlank() }
        } catch (_: Exception) { null }
    }
}