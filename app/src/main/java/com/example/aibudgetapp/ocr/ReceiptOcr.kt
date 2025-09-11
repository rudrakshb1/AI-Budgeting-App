package com.example.aibudgetapp.ocr


import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ParsedReceipt(
    val merchant: String,
    val total: Double,
    val dateEpochMs: Long,
    val rawText: String
)

object ReceiptOcr {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extract(uri: Uri, context: Context): ParsedReceipt {
        val img = InputImage.fromFilePath(context, uri)
        val res = recognizer.process(img).await()
        val raw = res.text

        Log.d("OCR_RAW", raw)

        // --- Merchant ---
        val merchant = pickMerchantFromBlocks(res)
            .ifBlank {
                raw.lineSequence()
                    .map { it.trim() }
                    .firstOrNull { it.isNotEmpty() && !it.any(Char::isDigit) }
                    ?: "Unknown"
            }

        // --- Amount (robust) ---
        val lines = raw.lines()

        // 1) Prefer labeled totals (supports spaced words: "sub total", "grand total", "amount due")
        val labeledTotalRegex = Regex(
            // label then up to ~25 non-digit chars then a money number (with optional $/thousand sep)
            """(?i)(grand\s*total|amount\s*due|sub\s*total|gst\s*subtotal|total)\D{0,25}([$]?\s*\d{1,3}(?:[.,\s]\d{3})*(?:[.,]\d{2}))"""
        )
        val labeledMatches = labeledTotalRegex.findAll(raw).toList()
        val labeledPick = labeledMatches.lastOrNull()?.groupValues?.getOrNull(2)
            ?.let { cleanToDouble(it) }

        if (labeledPick != null) {
            Log.d("OCR_AMOUNT", "Picked labeled total = $labeledPick")
        }

        // 2) Fallback: scan bottom 1/3, repair OCR digits and parse numbers
        val unitPattern = Regex("""(?i)\b(@|ea|each|qty|x|kg|g|ml|pack|pcs?)\b""")
        val startIdx = (lines.size * 2 / 3).coerceAtMost(lines.size - 1)

        var bestBottom: Double? = null
        for (i in lines.size - 1 downTo startIdx) {
            val line = lines[i]
            if (unitPattern.containsMatchIn(line)) continue

            // collect candidate tokens that look like a monetary number
            val tokenRegex = Regex("""[$]?\s*[0-9OolIiSsBbZzGg.,]{3,}""")
            val tokens = tokenRegex.findAll(line).map { it.value }.toList()

            val candidates = tokens.mapNotNull { token ->
                cleanToDouble(token)
            }.filter { v ->
                // ignore silly values that are clearly not totals
                v in 0.05..100000.0
            }

            if (candidates.isNotEmpty()) {
                val localMax = candidates.maxOrNull()
                if (localMax != null) {
                    bestBottom = if (bestBottom == null) localMax else maxOf(bestBottom!!, localMax)
                }
            }
        }

        if (bestBottom != null) {
            Log.d("OCR_AMOUNT", "Picked bottom fallback = $bestBottom")
        }

        val amount = labeledPick ?: bestBottom ?: 0.0
        Log.d("OCR_RESULT", "Merchant=$merchant, Total=$amount")

        // --- Date ---
        val dateRegex = Regex("""(\d{1,2}[/-]\d{1,2}[/-]\d{2,4}|\d{4}[.]\d{2}[.]\d{2}|\d{1,2}\s+[A-Za-z]{3,9}\s+\d{2,4})""")
        val dateStr = dateRegex.find(raw)?.value
        val dateEpochMs = parseDate(dateStr)

        return ParsedReceipt(
            merchant = merchant,
            total = amount,
            dateEpochMs = dateEpochMs,
            rawText = raw
        )
    }

    private fun pickMerchantFromBlocks(res: Text): String {
        val pageBottom = res.textBlocks.maxOfOrNull { it.boundingBox?.bottom ?: 0 } ?: return ""
        val topCutoff = (pageBottom * 0.30).toInt()
        val noiseRegex = Regex("(?i)(total|amount|gst|tax|invoice|receipt|tel|phone|date|time|eftpos|visa|mastercard|subtotal|balance|thank)")

        return res.textBlocks.asSequence()
            .filter { (it.boundingBox?.top ?: Int.MAX_VALUE) <= topCutoff }
            .flatMap { it.lines.asSequence() }
            .map { it.text.trim() }
            .filter { it.isNotEmpty() && !noiseRegex.containsMatchIn(it) }
            .maxByOrNull { it.length }
            ?.take(40)
            ?: ""
    }

    // -- helpers --

    /**
     * Repair common OCR digit mistakes and parse to Double.
     * Examples: O->0, S->5, B->8, I/l->1, Z->2, G->6
     */
    private fun cleanToDouble(token: String): Double? {
        val repaired = token
            .replace("$", "")
            .replace(" ", "")
            .map { ch ->
                when (ch) {
                    'O', 'o' -> '0'
                    'I', 'l' -> '1'
                    'S', 's' -> '5'
                    'B' -> '8'
                    'Z', 'z' -> '2'
                    'G', 'g' -> '6'
                    else -> ch
                }
            }
            .joinToString("")

        // keep only digits and separators, normalize comma to dot
        val digitsOnly = repaired.filter { it.isDigit() || it == '.' || it == ',' }
            .replace(",", ".")

        // ensure we have at least one dot with 2 decimals; if multiple dots, keep the last one
        val normalized = run {
            val parts = digitsOnly.split('.')
            when {
                parts.size == 1 -> digitsOnly // no dot; might be an integer – let toDoubleOrNull decide
                parts.size >= 2 -> {
                    val head = parts.dropLast(1).joinToString("") // remove inner dots
                    val tail = parts.last()
                    "$head.$tail"
                }
                else -> digitsOnly
            }
        }

        return normalized.toDoubleOrNull()
    }

    private fun parseDate(dateStr: String?): Long {
        if (dateStr == null) return System.currentTimeMillis()
        val cleaned = dateStr.replace("-", "/").replace(".", "/").trim()
        val formats = listOf("d/M/yy", "d/M/yyyy", "M/d/yy", "M/d/yyyy", "yyyy/MM/dd", "dd MMM yyyy", "d MMM yyyy")

        for (fmt in formats) {
            try {
                val formatter = DateTimeFormatter.ofPattern(fmt, Locale.ENGLISH)
                val localDate = LocalDate.parse(cleaned, formatter)
                return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (_: Exception) { }
        }
        return System.currentTimeMillis()
    }
}
