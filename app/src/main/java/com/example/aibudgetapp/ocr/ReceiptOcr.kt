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

        // --- Debug dump for testing ---
        for (block in res.textBlocks) {
            for (line in block.lines) {
                val elems = line.elements.joinToString { it.text }
                Log.d("OCR_DEBUG", "Line='${line.text}' | Elements=[${elems}]")
            }
        }

        // --- Merchant ---
        val merchant = pickMerchantFromBlocks(res)
            .ifBlank {
                raw.lineSequence()
                    .map { it.trim() }
                    .firstOrNull { it.isNotEmpty() && !it.any(Char::isDigit) }
                    ?: "Unknown"
            }

        // --- Total ---
        val amount = findTotalAmount(res) ?: 0.0
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

    // ---------- Total Finder ----------
    private fun findTotalAmount(res: Text): Double? {
        val currencyRegex = Regex("""(?i)(NZD\$|\$|USD\$|AUD\$|EUR\$)\s*\d{1,8}(?:[.,]\d{2})?""")
        val labelRegex = Regex("(?i)(total|amount due|paid|balance|net|grand|sub|sum|to pay)")
        val ignoreRegex = Regex("(?i)(tel|gst|check|invoice|date|time|staff|table|ref|mid|tsp|inv#|auth|card|moto|duplicate|approved|cover|statf|tabla|street|strest|copy|address|merchant)")
        val amountRegex = Regex("""\d{1,8}(?:[.,]\d{2})""") // Only numbers with decimals as valid amounts!

        // 1. Try to pick currency-marked value, on clean lines
        val currCandidates = res.textBlocks.flatMap { block ->
            block.lines.flatMap { line ->
                val text = line.text.trim()
                if (currencyRegex.containsMatchIn(text) && !ignoreRegex.containsMatchIn(text)) {
                    val elements = line.elements.map { it.text.trim() }
                    val joinedCandidates = mutableListOf<Double>()
                    for (i in 0 until elements.size - 1) {
                        val a = elements[i]
                        val b = elements[i + 1]
                        val combined = (a + b).replace(",", "").replace(" ", "")
                        cleanToDouble(combined)?.let { joinedCandidates.add(it) }
                    }
                    val singles = elements.mapNotNull { cleanToDouble(it.replace(",", ".")) }
                    joinedCandidates + singles
                } else {
                    emptyList<Double>()
                }
            }
        }



        if (currCandidates.isNotEmpty()) return currCandidates.maxOrNull()

        // 2. Scan next 1–5 lines after any "Total" label, ignore distractors, take highest value w/ decimal (signifies money, not ID)
        for (block in res.textBlocks) {
            val lines = block.lines
            for ((i, line) in lines.withIndex()) {
                val text = line.text.trim()
                if (labelRegex.containsMatchIn(text)) {
                    val foundCandidates = mutableListOf<Double>()
                    for (j in 1..5) { // scan following 5 lines, skip distractors
                        if (i + j < lines.size) {
                            val nextText = lines[i + j].text.trim()
                            if (!ignoreRegex.containsMatchIn(nextText)) {
                                amountRegex.findAll(nextText).forEach { mt ->
                                    cleanToDouble(mt.value.replace(",", "."))?.let { value ->
                                        foundCandidates.add(value)
                                    }
                                }
                            }
                        }
                    }
                    if (foundCandidates.isNotEmpty()) return foundCandidates.maxOrNull() // pick the highest valid money
                }
            }
        }

        // 3. Fallback: scan all lines (not ignored), only values with decimals (money), pick highest
        val fallback = res.textBlocks.flatMap { block ->
            block.lines.mapNotNull { line ->
                val text = line.text.trim()
                if (!ignoreRegex.containsMatchIn(text)) {
                    amountRegex.find(text)?.value?.replace(",", ".")?.let { cleanToDouble(it) }
                } else null
            }
        }.filter { it > 0.0 }.maxOrNull()

        return fallback
    }

    private fun cleanToDouble(token: String): Double? {
        return token.replace("[^\\d.]".toRegex(), "").toDoubleOrNull()
    }


}

    // ---------- Merchant Finder ----------
    private fun pickMerchantFromBlocks(res: Text): String {
        val pageBottom = res.textBlocks.maxOfOrNull { it.boundingBox?.bottom ?: 0 } ?: return ""
        val topCutoff = (pageBottom * 0.30).toInt()
        val noiseRegex = Regex(
            "(?i)(total|amount|gst|tax|invoice|receipt|tel|phone|date|time|eftpos|visa|mastercard|subtotal|balance|thank|duplicate|merchant)"
        )

        return res.textBlocks.asSequence()
            .filter { (it.boundingBox?.top ?: Int.MAX_VALUE) <= topCutoff }
            .flatMap { it.lines.asSequence() }
            .map { it.text.trim() }
            .filter { it.isNotEmpty() && !noiseRegex.containsMatchIn(it) }
            .maxByOrNull { it.length }
            ?.take(40)
            ?: ""
    }

    // ---------- Helpers ----------
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

        val digitsOnly = repaired.filter { it.isDigit() || it == '.' || it == ',' }
            .replace(",", ".")

        val normalized = run {
            val parts = digitsOnly.split('.')
            when {
                parts.size == 1 -> digitsOnly
                parts.size >= 2 -> {
                    val head = parts.dropLast(1).joinToString("")
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

