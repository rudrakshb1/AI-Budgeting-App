package com.example.aibudgetapp.ocr

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

data class ParsedReceipt(
    val merchant: String,
    val total: Double,
    val rawText: String
)

object ReceiptOcr {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extract(uri: Uri, context: Context): ParsedReceipt {
        val img = InputImage.fromFilePath(context, uri)

        val res = recognizer.process(img).await()
        val raw = res.text

        val merchant = raw.lineSequence().firstOrNull()?.trim().orEmpty().ifBlank { "Unknown" }

        val totalRegex = Regex("""(?i)(total|amount due|grand total)\D*([$€£]?\s*\d+[.,]\d{2})""")
        val priceRegex = Regex("""([$€£]?\s*\d+[.,]\d{2})""")
        val totalStr = totalRegex.find(raw)?.groupValues?.get(2)
            ?: priceRegex.findAll(raw).map { it.value }.lastOrNull()
            ?: "0.00"
        val amount = totalStr.replace(Regex("[^0-9.,]"), "").replace(",", ".").toDoubleOrNull() ?: 0.0

        return ParsedReceipt(merchant = merchant.take(40), total = amount, rawText = raw)
    }
}