package com.example.aibudgetapp

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import kotlin.math.pow
import kotlin.math.sqrt
import android.graphics.*

object ImageUtils {

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
    fun enhanceContrast(bitmap: Bitmap, contrast: Float = 1.5f, brightness: Float = 20f): Bitmap {
        val cm = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, brightness,
                0f, contrast, 0f, 0f, brightness,
                0f, 0f, contrast, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            )
        )
        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(cm) }
        val newBitmap = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height,
            bitmap.config ?: Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(newBitmap)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return newBitmap
    }

    fun sharpen(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(
            width,
            height,
            bitmap.config ?: Bitmap.Config.ARGB_8888
        )

        val kernel = arrayOf(
            floatArrayOf(0f, -1f, 0f),
            floatArrayOf(-1f, 5f, -1f),
            floatArrayOf(0f, -1f, 0f)
        )

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val newPixels = pixels.clone()

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                var r = 0f
                var g = 0f
                var b = 0f

                for (ky in -1..1) {
                    for (kx in -1..1) {
                        val pixel = pixels[(y + ky) * width + (x + kx)]
                        val weight = kernel[ky + 1][kx + 1]
                        r += ((pixel shr 16) and 0xFF) * weight
                        g += ((pixel shr 8) and 0xFF) * weight
                        b += (pixel and 0xFF) * weight
                    }
                }

                val nr = r.coerceIn(0f, 255f).toInt()
                val ng = g.coerceIn(0f, 255f).toInt()
                val nb = b.coerceIn(0f, 255f).toInt()
                newPixels[y * width + x] = (0xFF shl 24) or (nr shl 16) or (ng shl 8) or nb
            }
        }

        result.setPixels(newPixels, 0, width, 0, 0, width, height)
        return result
    }

    fun isImageBlurry(bitmap: Bitmap, threshold: Double = 50.0): Boolean {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        var sum = 0.0
        var sumSq = 0.0
        var count = 0

        for (y in 0 until height - 1) {
            for (x in 0 until width - 1) {
                val p1 = pixels[y * width + x] and 0xFF
                val p2 = pixels[y * width + x + 1] and 0xFF
                val diff = (p1 - p2).toDouble()
                sum += diff
                sumSq += diff.pow(2.0)
                count++
            }
        }

        val mean = sum / count
        val variance = (sumSq / count) - mean.pow(2.0)
        return sqrt(variance) < threshold
    }
}
