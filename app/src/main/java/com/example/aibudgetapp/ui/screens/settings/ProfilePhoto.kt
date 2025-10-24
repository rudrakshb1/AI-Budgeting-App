@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
package com.example.aibudgetapp.ui.screens.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider

@Composable
fun ProfilePhoto(
    displayName: String,
    avatarUri: Uri?,
    onPhotoPicked: (Uri?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showOptions by remember { mutableStateOf(false) } // directly show photo options
    var showUrlDialog by remember { mutableStateOf(false) }
    var urlText by remember { mutableStateOf("") }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { onPhotoPicked(it) } }

    var cameraTempUri by remember { mutableStateOf<Uri?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && cameraTempUri != null) onPhotoPicked(cameraTempUri!!)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createImageTempUri(context)
            cameraTempUri = uri
            takePictureLauncher.launch(uri)
        }
    }

    Box(
        modifier = Modifier
            .size(90.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { showOptions = true },
        contentAlignment = Alignment.Center
    ) {
        if (avatarUri != null) {
            AsyncImage(model = avatarUri, contentDescription = "Profile photo", modifier = Modifier.fillMaxSize())
        } else {
            Text(
                text = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
    }

    if (showOptions) {
        AlertDialog(
            onDismissRequest = { showOptions = false },
            title = { Text("Choose an option") },
            text = {
                Column {
                    TextButton(onClick = {
                        showOptions = false
                        if (hasCameraPermission(context)) {
                            val uri = createImageTempUri(context)
                            cameraTempUri = uri
                            takePictureLauncher.launch(uri)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }) {
                        Text("📸 Take a photo")
                    }

                    TextButton(onClick = {
                        showOptions = false
                        galleryLauncher.launch("image/*")
                    }) {
                        Text("🖼️ Choose from gallery")
                    }

                    TextButton(onClick = {
                        showOptions = false
                        showUrlDialog = true
                    }) {
                        Text("🌐 Paste image URL")
                    }

                    TextButton(onClick = {
                        onPhotoPicked(null)
                        showOptions = false
                    }) {
                        Text("❌ Delete")
                    }

                    TextButton(onClick = { showOptions = false }) {
                        Text("❌ Cancel")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    if (showUrlDialog) {
        AlertDialog(
            onDismissRequest = { showUrlDialog = false },
            title = { Text("Paste image URL") },
            text = {
                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    singleLine = true,
                    placeholder = { Text("https://example.com/photo.jpg") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showUrlDialog = false
                    val url = urlText.trim()
                    if (url.isNotBlank()) {
                        scope.launch {
                            val downloadedUri = downloadImageToCache(context, url)
                            if (downloadedUri != null) onPhotoPicked(downloadedUri)
                        }
                    }
                    urlText = ""
                }) { Text("Download") }
            },
            dismissButton = {
                TextButton(onClick = { showUrlDialog = false }) { Text("Cancel") }
            }
        )
    }
}

private fun hasCameraPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}

private fun createImageTempUri(context: Context): Uri {
    val cacheDir = File(context.cacheDir, "images").apply { if (!exists()) mkdirs() }
    val file = File.createTempFile("profile_", ".jpg", cacheDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

private suspend fun downloadImageToCache(context: Context, urlString: String): Uri? {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout = 10_000
                doInput = true
                requestMethod = "GET"
            }
            conn.connect()
            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                conn.disconnect()
                return@withContext null
            }
            val input: InputStream = conn.inputStream
            val cacheDir = File(context.cacheDir, "images").apply { if (!exists()) mkdirs() }
            val outFile = File.createTempFile("download_", ".jpg", cacheDir)
            outFile.outputStream().use { out -> input.copyTo(out) }
            input.close()
            conn.disconnect()
            return@withContext FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}
