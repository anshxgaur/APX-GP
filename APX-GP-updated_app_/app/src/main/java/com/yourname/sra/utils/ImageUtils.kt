package com.yourname.sra.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

/**
 * Utility for compressing images before uploading to Supabase Storage.
 * Prevents uploading raw 3-10MB camera photos.
 */
object ImageUtils {

    private const val MAX_WIDTH = 1024
    private const val MAX_HEIGHT = 1024
    private const val QUALITY = 75

    /**
     * Compress an image from a content URI to JPEG bytes.
     * @param context Application context
     * @param uri Content URI of the image
     * @param maxWidth Maximum width in pixels (default 1024)
     * @param maxHeight Maximum height in pixels (default 1024)
     * @param quality JPEG quality 0-100 (default 75)
     * @return Compressed image bytes, or null if reading failed
     */
    fun compressImage(
        context: Context,
        uri: Uri,
        maxWidth: Int = MAX_WIDTH,
        maxHeight: Int = MAX_HEIGHT,
        quality: Int = QUALITY
    ): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null

            // First pass: decode bounds only
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // Calculate sample size
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
            options.inJustDecodeBounds = false

            // Second pass: decode scaled bitmap
            val scaledStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(scaledStream, null, options)
            scaledStream.close()

            bitmap ?: return null

            // Further scale if still too large
            val finalBitmap = scaleBitmap(bitmap, maxWidth, maxHeight)

            // Compress to JPEG
            val outputStream = ByteArrayOutputStream()
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

            // Recycle bitmaps
            if (finalBitmap != bitmap) finalBitmap.recycle()
            bitmap.recycle()

            outputStream.toByteArray()
        } catch (e: Exception) {
            android.util.Log.e("ImageUtils", "Failed to compress image", e)
            null
        }
    }

    /**
     * Read raw bytes from a content URI (fallback if compression fails).
     */
    fun readBytes(context: Context, uri: Uri): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bytes = inputStream.readBytes()
            inputStream.close()
            bytes
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) return bitmap

        val ratio = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Compress a bitmap to JPEG with progressive quality reduction until file size is under maxSizeBytes.
     * Implements progressive quality reduction algorithm (start at 100, reduce by 10).
     * 
     * @param bitmap The bitmap to compress
     * @param maxSizeBytes Maximum file size in bytes (default 2MB = 2,097,152 bytes)
     * @return Compressed image bytes
     * @throws IllegalStateException if image cannot be compressed below maxSizeBytes
     * 
     * Requirements: 5.1, 18.1, 29.1, 29.2, 29.3, 29.4
     */
    fun compressImage(bitmap: Bitmap, maxSizeBytes: Long = 2_097_152): ByteArray {
        var quality = 100
        var compressedBytes: ByteArray
        
        do {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            compressedBytes = outputStream.toByteArray()
            outputStream.close()
            
            // If size is acceptable, return the bytes
            if (compressedBytes.size <= maxSizeBytes) {
                return compressedBytes
            }
            
            // Reduce quality by 10
            quality -= 10
            
        } while (quality >= 10)
        
        // If we've exhausted all quality levels and still above maxSizeBytes, throw exception
        throw IllegalStateException("Image file too large")
    }
}
