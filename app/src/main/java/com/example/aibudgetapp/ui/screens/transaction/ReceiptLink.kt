package com.example.aibudgetapp.ui.screens.transaction

import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun ReceiptLink(url: String) {
    val context = LocalContext.current

    // Extract a friendly filename from the URL
    val label = run {
        val uri = Uri.parse(url)
        // get last path segment and strip query if any
        val last = (uri.lastPathSegment ?: "Receipt.jpg")
        last.substringAfterLast('/').substringBefore('?')
    }

    TextButton(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    ) {
        Icon(Icons.Outlined.Image, contentDescription = "Receipt")
        Text(text = "  $label")
    }
}
