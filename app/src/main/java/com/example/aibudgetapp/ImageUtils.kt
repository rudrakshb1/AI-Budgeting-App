package com.example.aibudgetapp

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun createImageUri(context: Context): Uri {
    // Creates a temp file inside your app cache
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
    val imageFile = File.createTempFile("camera_", ".jpg", imagesDir)

    // Return content:// URI to give camera app a safe place to write
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}


