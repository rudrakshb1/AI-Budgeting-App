package com.example.aibudgetapp.ai

import android.util.Log
import com.example.aibudgetapp.BuildConfig
import com.example.aibudgetapp.constants.CategoryType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.net.URLEncoder

/** Minimal REST client for Google AI Studio (Gemini) generateContent API. */
object GeminiClient {
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private const val MODEL = "gemini-2.5-flash"
    private const val ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    /** Returns raw response string; null on HTTP error. */
    suspend fun postText(prompt: String): String? = withContext(Dispatchers.IO) {
        val bodyJson = """
          {"contents":[{"parts":[{"text": ${jsonStr(prompt)} }]}]}
        """.trimIndent()

        val url = "$ENDPOINT?key=${URLEncoder.encode(BuildConfig.GEMINI_API_KEY, "UTF-8")}"
        val req = Request.Builder()
            .url(url)
            .post(RequestBody.create("application/json".toMediaTypeOrNull(), bodyJson))
            .build()

        Log.d("Gemini", "Request body: $bodyJson")
        Log.d("Gemini", "URL: $url")

        client.newCall(req).execute().use { res ->
            val code = res.code
            val bodyStr = res.body?.string() ?: ""

            Log.d("Gemini", "Response code: $code")
            Log.d("Gemini", "Response body: $bodyStr")

            if (!res.isSuccessful) return@withContext null
            return@withContext extractText(bodyStr)
        }
    }

    /** Escape JSON string. */
    private fun jsonStr(s: String) =
        "\"" + s.replace("\"", "\\\"").replace("\n", "\\n") + "\""

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