package com.example.aibudgetapp.ui   // keep CsvUtils here with ImageUtils

import android.content.Context
import android.net.Uri
import com.example.aibudgetapp.ui.screens.transaction.Transaction   // ✅ correct import
import java.io.BufferedReader
import java.io.InputStreamReader
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
                    category = if (cols.size > 3) cols[3].trim() else "Uncategorised"
                )
            )
        }
    }
    return transactions
}
