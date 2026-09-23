package com.example.data.chat

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

data class ProcessedImageResult(
    val localFilePath: String,
    val dataUrl: String
)

object ChatImageHelper {
    private const val TAG = "ChatImageHelper"
    private const val MAX_DIMENSION = 720
    private const val JPEG_QUALITY = 70

    /**
     * Compresses image from content Uri, saves to local cache,
     * and returns data URL (data:image/jpeg;base64,...) for global Firestore sync.
     */
    fun processAndCompressImage(context: Context, imageUri: Uri): ProcessedImageResult? {
        return try {
            val contentResolver = context.contentResolver

            // 1. Decode bounds first to avoid OOM
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(imageUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }

            var inSampleSize = 1
            while (options.outWidth / inSampleSize > MAX_DIMENSION * 1.5 ||
                options.outHeight / inSampleSize > MAX_DIMENSION * 1.5) {
                inSampleSize *= 2
            }

            // 2. Decode sampled bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            val originalBitmap = contentResolver.openInputStream(imageUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            } ?: return null

            // 3. Scale down proportionally to MAX_DIMENSION
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scale = (MAX_DIMENSION.toFloat() / maxOf(width, height)).coerceAtMost(1f)
            val finalWidth = (width * scale).toInt().coerceAtLeast(1)
            val finalHeight = (height * scale).toInt().coerceAtLeast(1)

            val scaledBitmap = if (scale < 1f) {
                Bitmap.createScaledBitmap(originalBitmap, finalWidth, finalHeight, true)
            } else {
                originalBitmap
            }

            // 4. Compress to JPEG
            val baos = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos)
            val imageBytes = baos.toByteArray()

            // 5. Save locally in cache dir
            val localFileName = "chat_local_${System.currentTimeMillis()}.jpg"
            val localFile = File(context.cacheDir, localFileName)
            FileOutputStream(localFile).use { fos ->
                fos.write(imageBytes)
            }

            // 6. Encode to Base64 data URL
            val base64String = "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.NO_WRAP)

            if (scaledBitmap != originalBitmap) {
                originalBitmap.recycle()
            }
            scaledBitmap.recycle()

            Log.i(TAG, "Image successfully compressed: ${imageBytes.size / 1024} KB")
            ProcessedImageResult(
                localFilePath = localFile.absolutePath,
                dataUrl = base64String
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to compress chat image: ${e.message}", e)
            null
        }
    }

    /**
     * When receiving a base64 image data URL from Firestore,
     * writes it to local disk cache and returns the local file path.
     */
    fun saveBase64ToCache(context: Context, docId: String, dataUrl: String): String {
        return try {
            val cacheFile = File(context.cacheDir, "chat_cloud_${docId}.jpg")
            if (cacheFile.exists() && cacheFile.length() > 0) {
                return cacheFile.absolutePath
            }

            val base64Payload = if (dataUrl.contains(",")) {
                dataUrl.substringAfter(",")
            } else {
                dataUrl
            }

            val decodedBytes = Base64.decode(base64Payload, Base64.DEFAULT)
            FileOutputStream(cacheFile).use { fos ->
                fos.write(decodedBytes)
            }
            cacheFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode cloud image: ${e.message}", e)
            dataUrl
        }
    }
}
