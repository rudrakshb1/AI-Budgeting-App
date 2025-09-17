package com.example.aibudgetapp.ui   // keep CsvUtils here with ImageUtils

import android.content.Context
import android.net.Uri
import com.example.aibudgetapp.ui.screens.transaction.Transaction   // ✅ correct import
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

fun parseCsv(context: Context, uri: Uri): List<Transaction> {
    val inputStream = context.contentResolver.openInputStream(uri)
    val reader = BufferedReader(InputStreamReader(inputStream))
    val transactions = mutableListOf<Transaction>()

    // Example CSV: Date, Description, Amount, Category
    reader.readLines().drop(1).forEach { line ->
        val cols = line.split(",")

        if (cols.size >= 3) {
            transactions.add(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    description = cols[1].trim(),
                    amount = cols[2].toDoubleOrNull() ?: 0.0,
                    category = if (cols.size > 3) cols[3].trim() else "Uncategorised",
                    //Date time must be below format, if you want something else, please add it on tryParseFlexible() function
                    //2025-09-17
                    //17/09/2025
                    //09-17-2025
                    date = cols[0].tryParseFlexible().toString()
                )
            )
        }
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
