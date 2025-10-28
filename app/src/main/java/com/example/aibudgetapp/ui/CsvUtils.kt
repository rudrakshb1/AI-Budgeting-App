package com.example.aibudgetapp.ui   // keep CsvUtils here with ImageUtils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.aibudgetapp.ai.GeminiClient
import com.example.aibudgetapp.constants.CategoryType
import com.example.aibudgetapp.ui.screens.transaction.Transaction
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun parseCsv(context: Context, uri: Uri): List<Transaction> {
    val inputStream = context.contentResolver.openInputStream(uri)
    val reader = BufferedReader(InputStreamReader(inputStream))
    val transactions = mutableListOf<Transaction>()

    // 1. Read Header
    var headerLine: String? = null
    while (true) {
        val line = reader.readLine() ?: break
        if (line.contains("Date")) {
            headerLine = line
            Log.d("CSVUtil", headerLine)
            break
        }
    }
    if (headerLine == null) return emptyList()
    val headers = headerLine.split(",")

    // 2. Find Dynamic index (use default value if not existing)
    fun findIndex(target: String): Int {
        return headers.indexOfFirst { it.contains(target, ignoreCase = true) }
    }

    val dateCol    = findIndex("date")
    val amountCol  = findIndex("amount")
    val detailsCol = findIndex("details")
    val payeeCol   = findIndex("payee")
    val particularsCol = findIndex("particulars")
    val codeCol    = findIndex("code")
    val memoCol = findIndex("memo")


    if (dateCol == -1 || amountCol == -1) return emptyList()

    // Example CSV: Date, Description, Amount, Category
    reader.forEachLine { line ->
        val cols = line.split(",")
        val date = cols.getOrNull(dateCol)?.trim().orEmpty()
        val amount = cols.getOrNull(amountCol)?.trim()?.toDoubleOrNull() ?: return@forEachLine
        if (amount > 0) return@forEachLine
        val details = if (detailsCol != -1) cols.getOrNull(detailsCol)?.trim().orEmpty() else ""
        val payee = if (payeeCol != -1) cols.getOrNull(payeeCol)?.trim().orEmpty() else ""
        val particulars = if (particularsCol != -1) cols.getOrNull(particularsCol)?.trim().orEmpty() else ""
        val code = if (codeCol != -1) cols.getOrNull(codeCol)?.trim().orEmpty() else ""
        val memo = if (memoCol != -1) cols.getOrNull(memoCol)?.trim().orEmpty() else ""

        Log.d("CSVUtil", date)
        val category = listOf(payee, details, particulars, code, memo)
            .firstNotNullOfOrNull { text ->
                findCategory(text).takeIf { it != "Other" }
            } ?: "Other"

        transactions.add(
            Transaction(
                date = date,
                amount = amount * -1.0,
                category = category
            )
        )
    }
    return transactions
}

fun String.tryParseFlexible(): String {
    val DATE_FORMATS = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE,              // 2025-09-17
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),     // 17/09/2025
        DateTimeFormatter.ofPattern("MM-dd-yyyy")      // 09-17-2025
    )

    for (fmt in DATE_FORMATS) {
        try { return LocalDate.parse(this.trim(), fmt).toString() } catch (_: Exception) {}
    }
    return LocalDate.now().toString();
}

private suspend fun findCategorySuspend(text: String?): String {
    val categories = CategoryType.entries.map { it.value }
    val cats = categories.joinToString(", ")

    val prompt = """
        Pick exactly ONE category from [$cats] for this transaction.
        If unsure, answer "Other".
        Answer ONLY with the category word (no punctuation, no explanation).
        text: ${text?.trim()}
    """.trimIndent()

    val raw = GeminiClient.postText(prompt) ?: return "Other"
    Log.d("findCategoryGemini", "Gemini raw: $raw")

    val regex = Regex("""\b(${categories.joinToString("|") { Regex.escape(it) }})\b""")
    val match = regex.find(raw)?.value
    return match ?: "Other"
}

fun findCategory(text: String?): String = kotlinx.coroutines.runBlocking {
    if (text.isNullOrBlank()) return@runBlocking "Other"
    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        findCategorySuspend(text)
    }
}

