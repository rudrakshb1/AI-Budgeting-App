package com.example.aibudgetapp.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId

data class ParsedReceipt(
    val merchant: String,
    val total: Double,
    val dateEpochMs: Long,
    val rawText: String
)

object ReceiptOcr {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extract(uri: Uri, context: Context): ParsedReceipt {
        // ✅ Works on minSdk 24 (no ImageDecoder)
        val img = InputImage.fromFilePath(context, uri)
        val res = recognizer.process(img).await()
        val raw = res.text

        // Merchant: first non-empty line, trimmed
        val merchant = raw.lineSequence()
            .firstOrNull { it.isNotBlank() }
            ?.trim()
            .orEmpty()
            .ifBlank { "Unknown" }
            .take(40)

        // Total/Amount: try explicit "total" lines first, fallback to last price-looking number
        val totalRegex = Regex("""(?i)(total|amount due|grand total)\D*([$€£]?\s*\d+[.,]\d{2})""")
        val priceRegex = Regex("""([$€£]?\s*\d+[.,]\d{2})""")
        val totalStr = totalRegex.find(raw)?.groupValues?.get(2)
            ?: priceRegex.findAll(raw).map { it.value }.lastOrNull()
            ?: "0.00"
        val amount = totalStr
            .replace(Regex("[^0-9.,]"), "")
            .replace(",", ".")
            .toDoubleOrNull() ?: 0.0

        // Date: dd/mm/yyyy or mm/dd/yyyy or with '-' separators; fallback to now
        val dateRegex = Regex("""(\d{1,2}[/-]\d{1,2}[/-]\d{2,4})""")
        val dateStr = dateRegex.find(raw)?.value
        val dateEpochMs = parseToEpochMsOrNow(dateStr)

        return ParsedReceipt(
            merchant = merchant,
            total = amount,
            dateEpochMs = dateEpochMs,
            rawText = raw
        )
    }

    private fun parseToEpochMsOrNow(dateStr: String?): Long {
        if (dateStr == null) return System.currentTimeMillis()
        val parts = dateStr.replace("-", "/").split("/")
        if (parts.size != 3) return System.currentTimeMillis()

        // Heuristic: try both dd/MM and MM/dd; pick the one that doesn't crash and yields a plausible date
        val y = parts[2].let { if (it.length == 2) "20$it" else it }.toIntOrNull() ?: LocalDate.now().year
        val a = parts[0].toIntOrNull() ?: return System.currentTimeMillis()
        val b = parts[1].toIntOrNull() ?: return System.currentTimeMillis()

        val candidates = listOf(
            runCatching { LocalDate.of(y, b, a) }.getOrNull(), // dd/MM/yyyy
            runCatching { LocalDate.of(y, a, b) }.getOrNull()  // MM/dd/yyyy
        ).filterNotNull()

        val picked = candidates.firstOrNull() ?: LocalDate.now()
        return picked.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}