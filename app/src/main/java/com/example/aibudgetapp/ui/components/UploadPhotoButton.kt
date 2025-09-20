package com.example.aibudgetapp.ui.components

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
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
import com.example.aibudgetapp.ocr.ReceiptOcr
import com.example.aibudgetapp.ui.parseCsv
import com.example.aibudgetapp.ui.screens.transaction.AddTransactionViewModel
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import java.util.UUID
import com.example.aibudgetapp.ocr.AutoCategorizer


@Composable
fun UploadPhotoButton(
    onImagePicked: (Uri) -> Unit,
    onCategoryDetected: (String, String, Double, String, Uri) -> Unit,
    addTxViewModel: AddTransactionViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showChooser by remember { mutableStateOf(false) }

    // -------- helpers to create MediaStore Uris in Gallery/Pictures/Receipts --------
    fun newGalleryDestUri(): Uri {
        val resolver = context.contentResolver
        val name = "receipt_${System.currentTimeMillis()}.jpg"
        val cv = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Receipts")
        }
        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)!!
    }

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

    // -------------------- uCrop launcher --------------------
    val uCropLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val resultUri = UCrop.getOutput(result.data!!)
            if (resultUri != null) {
                scope.launch {
                    try {
                        val parsed = ReceiptOcr.extract(resultUri, context)
                        val detectedCategory = AutoCategorizer.guess(parsed.rawText)

                        // only forward data, no saving here
                        onCategoryDetected(
                            detectedCategory,
                            parsed.merchant,
                            parsed.total,
                            parsed.rawText,
                            resultUri
                        )
                        Toast.makeText(context, "Receipt cropped & processed", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "OCR failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
                onImagePicked(resultUri)
            }
        }
    }

    fun launchCrop(source: Uri) {
        val dest = newGalleryDestUri()
        val intent = UCrop.of(source, dest)
            .withAspectRatio(0f, 0f)
            .withMaxResultSize(3000, 3000)
            .withOptions(UCrop.Options().apply {
                setCompressionFormat(Bitmap.CompressFormat.JPEG)
                setCompressionQuality(90)
                setFreeStyleCropEnabled(true)
                setHideBottomControls(false)
            })
            .getIntent(context)
        uCropLauncher.launch(intent)
    }

    // Gallery (pick & crop)
    val galleryPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { launchCrop(it) }
    }

    //  Camera (save & use)
    var pendingCameraSaveUri by remember { mutableStateOf<Uri?>(null) }

    val takePictureSaveOnly = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok: Boolean ->
        if (ok) {
            pendingCameraSaveUri?.let { saved ->
                scope.launch {
                    try {
                        val parsed = ReceiptOcr.extract(saved, context)
                        val detectedCategory = AutoCategorizer.guess(parsed.rawText)

                        // only forward data, no saving here
                        onCategoryDetected(
                            detectedCategory,
                            parsed.merchant,
                            parsed.total,
                            parsed.rawText,
                            saved
                        )
                        Toast.makeText(context, "Photo processed", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "OCR failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
                onImagePicked(saved)
            }
        }
        pendingCameraSaveUri = null
    }

    val cameraSavePermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            val dest = newGalleryDestUri()
            pendingCameraSaveUri = dest
            takePictureSaveOnly.launch(dest)
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    //  Camera (crop & use)
    var pendingCameraCropUri by remember { mutableStateOf<Uri?>(null) }

    val takePictureForCrop = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok: Boolean ->
        if (ok) {
            pendingCameraCropUri?.let { src ->
                launchCrop(src)
            }
        }
        pendingCameraCropUri = null
    }

    val cameraCropPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            val tmp = newTempCameraUri()
            pendingCameraCropUri = tmp
            takePictureForCrop.launch(tmp)
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    //  CSV picker
    val csvPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val transactions = parseCsv(context, it)
            transactions.forEach { tx ->
                android.util.Log.d("CSV_IMPORT", "Parsed transaction: $tx")
            }
            Toast.makeText(context, "CSV imported (${transactions.size})", Toast.LENGTH_SHORT).show()
        }
    }

    //  UI
    Button(
        onClick = { showChooser = true },
        modifier = Modifier
            .padding(top = 24.dp)
            .fillMaxWidth(0.8f)
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
                        cameraSavePermission.launch(Manifest.permission.CAMERA)
                    }) { Text("Camera (save)") }

                    TextButton(onClick = {
                        showChooser = false
                        cameraCropPermission.launch(Manifest.permission.CAMERA)
                    }) { Text("Camera (crop)") }

                    TextButton(onClick = {
                        showChooser = false
                        galleryPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) { Text("Gallery") }

                    TextButton(onClick = {
                        showChooser = false
                        csvPicker.launch("text/csv")
                    }) { Text("Upload Bank Statement (CSV)") }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}
