package com.example.aibudgetapp.ui.components

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.aibudgetapp.createImageUri

@Composable
fun UploadPhotoButton(
    onImagePicked: (Uri) -> Unit
) {
    val context = LocalContext.current
    var showChooser by remember { mutableStateOf(false) }
    var pendingCameraUri: Uri? by remember { mutableStateOf(null) }

    // Gallery picker (explicit type in lambda)
    val galleryPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let(onImagePicked)
    }

    // Camera capture (explicit Boolean)
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok: Boolean ->
        if (ok) pendingCameraUri?.let(onImagePicked)
    }

    // Camera permission
    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            pendingCameraUri = createImageUri(context).also { cameraLauncher.launch(it) }
        }
    }

    Button(
        onClick = { showChooser = true },
        modifier = Modifier
            .padding(top = 24.dp)
            .fillMaxWidth(0.8f)
    ) {
        Icon(imageVector = Icons.Filled.Camera, contentDescription = "Camera")
        Spacer(modifier = Modifier.width(8.dp))
        Text("Upload photo")
    }

    if (showChooser) {
        AlertDialog(
            onDismissRequest = { showChooser = false },
            title = { Text("Add receipt") },
            text = { Text("Choose a source") },
            confirmButton = {
                TextButton(onClick = {
                    showChooser = false
                    galleryPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) { Text("Gallery") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showChooser = false
                    cameraPermission.launch(Manifest.permission.CAMERA)
                }) { Text("Camera") }
            }
        )
    }
}
