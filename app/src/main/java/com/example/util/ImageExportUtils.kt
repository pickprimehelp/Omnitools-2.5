package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ImageExportUtils {

    fun applyCornerWatermark(source: Bitmap, label: String = "Made with OmniTools"): Bitmap {
        val out = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(out)
        val minDim = minOf(out.width, out.height).coerceAtLeast(320)
        val textSizePx = (minDim * 0.036f).coerceIn(18f, 44f)
        val padH = textSizePx * 0.75f
        val padV = textSizePx * 0.42f
        val margin = (minDim * 0.032f).coerceAtLeast(14f)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = textSizePx
            typeface = Typeface.DEFAULT_BOLD
        }
        val textWidth = textPaint.measureText(label)
        val boxW = textWidth + padH * 2f
        val boxH = textSizePx + padV * 2f
        val left = (out.width - boxW - margin).coerceAtLeast(8f)
        val top = (out.height - boxH - margin).coerceAtLeast(8f)
        val rect = RectF(left, top, left + boxW, top + boxH)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(170, 15, 23, 42)
        }
        canvas.drawRoundRect(rect, boxH / 2f, boxH / 2f, bgPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(120, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = (textSizePx * 0.08f).coerceAtLeast(1.5f)
        }
        canvas.drawRoundRect(rect, boxH / 2f, boxH / 2f, borderPaint)

        val fm = textPaint.fontMetrics
        val baselineY = rect.centerY() - (fm.ascent + fm.descent) / 2f
        canvas.drawText(label, left + padH, baselineY, textPaint)
        return out
    }

    fun applyFooterWatermark(source: Bitmap, label: String = "Made with OmniTools"): Bitmap {
        val stripHeight = (source.height * 0.11f).toInt().coerceIn(44, 96)
        val out = Bitmap.createBitmap(source.width, source.height + stripHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(source, 0f, 0f, null)

        val linePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 2f
        }
        canvas.drawLine(0f, source.height.toFloat(), out.width.toFloat(), source.height.toFloat(), linePaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(71, 85, 105)
            textSize = (stripHeight * 0.42f).coerceIn(18f, 32f)
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        val fm = textPaint.fontMetrics
        val centerY = source.height + stripHeight / 2f - (fm.ascent + fm.descent) / 2f
        canvas.drawText(label, out.width / 2f, centerY, textPaint)
        return out
    }

    fun copyToClipboard(context: Context, text: String, label: String = "OmniTools") {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareText(context: Context, text: String, title: String = "Share via") {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareBitmap(context: Context, bitmap: Bitmap, title: String = "OmniTools Creation") {
        try {
            val cachePath = File(context.cacheDir, "images").apply { mkdirs() }
            val file = File(cachePath, "omnitools_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share via"))
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        filenamePrefix: String = "OmniTools",
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 100
    ): Boolean {
        val ext = when {
            format == Bitmap.CompressFormat.JPEG -> "jpg"
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                (format == Bitmap.CompressFormat.WEBP_LOSSY || format == Bitmap.CompressFormat.WEBP_LOSSLESS) -> "webp"
            @Suppress("DEPRECATION")
            format == Bitmap.CompressFormat.WEBP -> "webp"
            else -> "png"
        }
        val mime = when {
            format == Bitmap.CompressFormat.JPEG -> "image/jpeg"
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                (format == Bitmap.CompressFormat.WEBP_LOSSY || format == Bitmap.CompressFormat.WEBP_LOSSLESS) -> "image/webp"
            @Suppress("DEPRECATION")
            format == Bitmap.CompressFormat.WEBP -> "image/webp"
            else -> "image/png"
        }
        return try {
            val filename = "${filenamePrefix}_${System.currentTimeMillis()}.$ext"
            var fos: OutputStream? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver: ContentResolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, mime)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/OmniTools")
                }
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    fos = resolver.openOutputStream(imageUri)
                }
            } else {
                @Suppress("DEPRECATION")
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/OmniTools"
                val file = File(imagesDir).apply { if (!exists()) mkdirs() }
                val image = File(file, filename)
                fos = FileOutputStream(image)
            }

            if (fos == null) return false

            fos.use { out ->
                bitmap.compress(format, quality, out)
            }
            Toast.makeText(context, "Saved to Gallery (Pictures/OmniTools)", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun sharePdfFile(context: Context, pdfFile: File, title: String = "Share PDF Document") {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, title))
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openPdfFile(context: Context, pdfFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(viewIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "No PDF viewer app found", Toast.LENGTH_SHORT).show()
        }
    }
}
