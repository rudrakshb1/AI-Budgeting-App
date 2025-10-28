package com.example.aibudgetapp.ui.components

import android.Manifest
import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.aibudgetapp.ui.parseCsv
import com.example.aibudgetapp.ui.screens.transaction.AddTransactionViewModel
import java.util.UUID

@Composable
fun UploadPhotoButton(
    onImagePicked: (Uri) -> Unit,
    addTxViewModel: AddTransactionViewModel
) {
    val context = LocalContext.current
    var showChooser by remember { mutableStateOf(false) }
    // var croppingInProgress by rememberSaveable { mutableStateOf(false) } // cropping disabled
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showCropChoiceDialog by remember { mutableStateOf(false) }

    fun newTempCameraUri(): Uri {
        val resolver = context.contentResolver
        val name = "camera_tmp_${UUID.randomUUID()}.jpg"
        val cv = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Receipts/.tmp")
        }
        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)!!
    }

    // Gallery picker triggers OCR and save directly (no crop)
    val galleryPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            addTxViewModel.runOcr(it, context)
            onImagePicked(it)
        }
    }

    val csvPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val transactions = parseCsv(context, it)
            addTxViewModel.importTransactions(transactions)
            Toast.makeText(context, "CSV imported (${transactions.size})", Toast.LENGTH_SHORT).show()
        }
    }


    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok: Boolean ->
        if (ok) {
            // Instead of crop dialog, trigger OCR + save directly on camera image
            pendingCameraUri?.let {
                addTxViewModel.runOcr(it, context)
                onImagePicked(it)
                pendingCameraUri = null
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            val tmpUri = newTempCameraUri()
            pendingCameraUri = tmpUri
            takePictureLauncher.launch(tmpUri)
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Button(
        onClick = { showChooser = true },
        modifier = Modifier
            .padding(top = 24.dp)
            .fillMaxWidth(0.8f),
        enabled = true
    ) {
        Icon(imageVector = Icons.Filled.Camera, contentDescription = "Camera")
        Spacer(modifier = Modifier.width(8.dp))
        Text("Upload document")
    }

    if (showChooser) {
        AlertDialog(
            onDismissRequest = { showChooser = false },
            title = { Text("Upload document") },
            text = {
                Column {
                    TextButton(onClick = {
                        showChooser = false
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }) { Text("Camera") }

                    TextButton(onClick = {
                        showChooser = false
                        galleryPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) { Text("Gallery") }

                    TextButton(onClick = {
                        showChooser = false
                        csvPicker.launch("text/*")

                    }) { Text("Upload Bank Statement (CSV)") }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}
