package com.example.ui.tools.design

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader

enum class WeddingClipartType(val title: String, val subtitle: String) {
    GANPATI_AUSPICIOUS("Ganpati Bappa", "Shree Ganesha Blessing & Sacred Halo"),
    DULHA_DULHAN_ROYAL("Dulha-Dulhan Art", "Traditional Couple Clipart with Varmala"),
    SHEHNAI_KALASH("Shehnai & Kalash", "Mangal Kalash, Swastik & Twin Shehnais"),
    FLORAL_TORAN("Floral Garland", "Auspicious Marigold & Rose Toran"),
    ROYAL_RINGS("Wedding Rings", "Interlocking Gold Rings & Hearts")
}

object WeddingClipartRenderer {

    fun createGanpatiBitmap(accentColor: Int = AndroidColor.rgb(255, 215, 0)): Bitmap {
        val width = 400
        val height = 300
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val cx = width / 2f
        val cy = height / 2f - 18f

        val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx, cy, 135f,
                intArrayOf(
                    AndroidColor.argb(190, 255, 223, 0),
                    AndroidColor.argb(75, 255, 140, 0),
                    AndroidColor.TRANSPARENT
                ),
                floatArrayOf(0f, 0.7f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(cx, cy, 125f, haloPaint)

        val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(245, 185, 30)
            style = Paint.Style.FILL
        }
        val redPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(200, 20, 20)
            style = Paint.Style.FILL
        }

        val crownPath = Path().apply {
            moveTo(cx, cy - 105f)
            lineTo(cx - 48f, cy - 38f)
            lineTo(cx + 48f, cy - 38f)
            close()
        }
        canvas.drawPath(crownPath, goldPaint)
        canvas.drawCircle(cx, cy - 62f, 11f, redPaint)
        canvas.drawCircle(cx, cy - 105f, 6f, redPaint)

        val earPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(255, 200, 70)
            style = Paint.Style.FILL
        }
        canvas.drawOval(RectF(cx - 92f, cy - 38f, cx - 24f, cy + 28f), earPaint)
        canvas.drawOval(RectF(cx + 24f, cy - 38f, cx + 92f, cy + 28f), earPaint)

        val innerEarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(230, 80, 50)
            style = Paint.Style.FILL
            alpha = 180
        }
        canvas.drawOval(RectF(cx - 82f, cy - 28f, cx - 34f, cy + 18f), innerEarPaint)
        canvas.drawOval(RectF(cx + 34f, cy - 28f, cx + 82f, cy + 18f), innerEarPaint)

        val facePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(255, 215, 90)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy - 10f, 44f, facePaint)

        val tilakPath = Path().apply {
            moveTo(cx - 15f, cy - 28f)
            quadTo(cx, cy - 14f, cx + 15f, cy - 28f)
            lineTo(cx + 11f, cy - 40f)
            quadTo(cx, cy - 24f, cx - 11f, cy - 40f)
            close()
        }
        canvas.drawPath(tilakPath, redPaint)
        canvas.drawCircle(cx, cy - 24f, 5f, redPaint)

        val trunkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(255, 195, 60)
            style = Paint.Style.STROKE
            strokeWidth = 22f
            strokeCap = Paint.Cap.ROUND
        }
        val trunkPath = Path().apply {
            moveTo(cx, cy - 5f)
            quadTo(cx - 14f, cy + 44f, cx + 20f, cy + 54f)
            quadTo(cx + 42f, cy + 54f, cx + 36f, cy + 38f)
        }
        canvas.drawPath(trunkPath, trunkPaint)

        val modakPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(255, 230, 100)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx + 43f, cy + 36f, 12f, modakPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 26f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("॥ वक्रतुण्ड महाकाय ॥", cx, height - 16f, textPaint)
        return bmp
    }

    fun createDulhaDulhanBitmap(accentColor: Int = AndroidColor.rgb(255, 215, 0)): Bitmap {
        val width = 500
        val height = 300
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val groomCx = 180f
        val brideCx = 320f
        val headCy = 104f

        val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                250f, 145f, 190f,
                intArrayOf(AndroidColor.argb(150, 255, 220, 120), AndroidColor.TRANSPARENT),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(250f, 145f, 180f, auraPaint)

        val pagdiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(190, 25, 45)
            style = Paint.Style.FILL
        }
        val goldTrim = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(255, 215, 0)
            style = Paint.Style.FILL
        }
        canvas.drawOval(RectF(groomCx - 45f, headCy - 66f, groomCx + 45f, headCy - 18f), pagdiPaint)
        val kalgiPath = Path().apply {
            moveTo(groomCx, headCy - 84f)
            lineTo(groomCx - 12f, headCy - 58f)
            lineTo(groomCx + 12f, headCy - 58f)
            close()
        }
        canvas.drawPath(kalgiPath, goldTrim)
        canvas.drawCircle(groomCx, headCy - 56f, 7f, goldTrim)

        val skinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(240, 195, 160)
            style = Paint.Style.FILL
        }
        canvas.drawOval(RectF(groomCx - 34f, headCy - 28f, groomCx + 34f, headCy + 40f), skinPaint)

        val tilakPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(200, 20, 20)
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(groomCx, headCy - 14f, groomCx, headCy - 2f, tilakPaint)

        val sherwaniPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(248, 240, 220)
            style = Paint.Style.FILL
        }
        val groomBody = Path().apply {
            moveTo(groomCx - 48f, headCy + 40f)
            lineTo(groomCx + 44f, headCy + 40f)
            lineTo(groomCx + 68f, headCy + 146f)
            lineTo(groomCx - 72f, headCy + 146f)
            close()
        }
        canvas.drawPath(groomBody, sherwaniPaint)

        val stolePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(180, 20, 40)
            style = Paint.Style.FILL
        }
        val stolePath = Path().apply {
            moveTo(groomCx - 38f, headCy + 40f)
            lineTo(groomCx - 14f, headCy + 40f)
            lineTo(groomCx + 24f, headCy + 146f)
            lineTo(groomCx - 10f, headCy + 146f)
            close()
        }
        canvas.drawPath(stolePath, stolePaint)

        val lehengaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(215, 20, 45)
            style = Paint.Style.FILL
        }
        val chunariPath = Path().apply {
            moveTo(brideCx, headCy - 54f)
            quadTo(brideCx - 58f, headCy, brideCx - 52f, headCy + 146f)
            lineTo(brideCx + 72f, headCy + 146f)
            quadTo(brideCx + 62f, headCy, brideCx, headCy - 54f)
            close()
        }
        canvas.drawPath(chunariPath, lehengaPaint)

        val brideSkinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(248, 205, 175)
            style = Paint.Style.FILL
        }
        canvas.drawOval(RectF(brideCx - 32f, headCy - 26f, brideCx + 32f, headCy + 36f), brideSkinPaint)
        canvas.drawCircle(brideCx, headCy - 20f, 6f, goldTrim)
        canvas.drawLine(brideCx, headCy - 36f, brideCx, headCy - 20f, tilakPaint)
        canvas.drawCircle(brideCx, headCy - 10f, 4f, tilakPaint)

        val nathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(255, 215, 0)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawCircle(brideCx - 8f, headCy + 8f, 7f, nathPaint)

        val brideBody = Path().apply {
            moveTo(brideCx - 40f, headCy + 36f)
            lineTo(brideCx + 46f, headCy + 36f)
            lineTo(brideCx + 62f, headCy + 146f)
            lineTo(brideCx - 52f, headCy + 146f)
            close()
        }
        canvas.drawPath(brideBody, lehengaPaint)
        canvas.drawOval(RectF(brideCx - 20f, headCy + 36f, brideCx + 20f, headCy + 62f), nathPaint)

        val centerHeartPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(230, 30, 60)
            textSize = 40f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("❤", 250f, headCy + 64f, centerHeartPaint)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 25f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("शुभ विवाह • Forever Together", 250f, height - 14f, labelPaint)
        return bmp
    }

    fun createShehnaiKalashBitmap(accentColor: Int = AndroidColor.rgb(255, 215, 0)): Bitmap {
        val width = 460
        val height = 200
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val cx = width / 2f
        val kalashCy = 104f

        val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(240, 190, 40)
            style = Paint.Style.FILL
        }
        val brassPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(215, 155, 25)
            style = Paint.Style.FILL
        }
        val redPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(210, 30, 40)
            style = Paint.Style.FILL
        }
        val greenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(40, 160, 60)
            style = Paint.Style.FILL
        }

        val potPath = Path().apply {
            moveTo(cx - 28f, kalashCy - 10f)
            quadTo(cx - 56f, kalashCy + 28f, cx - 38f, kalashCy + 60f)
            lineTo(cx + 38f, kalashCy + 60f)
            quadTo(cx + 56f, kalashCy + 28f, cx + 28f, kalashCy - 10f)
            close()
        }
        canvas.drawPath(potPath, goldPaint)
        canvas.drawRect(cx - 32f, kalashCy - 18f, cx + 32f, kalashCy - 10f, brassPaint)

        val swastikPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(210, 30, 40)
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(cx, kalashCy + 14f, cx, kalashCy + 42f, swastikPaint)
        canvas.drawLine(cx - 14f, kalashCy + 28f, cx + 14f, kalashCy + 28f, swastikPaint)
        canvas.drawLine(cx - 14f, kalashCy + 28f, cx - 14f, kalashCy + 18f, swastikPaint)
        canvas.drawLine(cx + 14f, kalashCy + 28f, cx + 14f, kalashCy + 38f, swastikPaint)
        canvas.drawLine(cx, kalashCy + 14f, cx + 10f, kalashCy + 14f, swastikPaint)
        canvas.drawLine(cx, kalashCy + 42f, cx - 10f, kalashCy + 42f, swastikPaint)

        val leaf1 = Path().apply {
            moveTo(cx, kalashCy - 18f)
            quadTo(cx - 42f, kalashCy - 42f, cx - 48f, kalashCy - 18f)
            close()
        }
        val leaf2 = Path().apply {
            moveTo(cx, kalashCy - 18f)
            quadTo(cx + 42f, kalashCy - 42f, cx + 48f, kalashCy - 18f)
            close()
        }
        canvas.drawPath(leaf1, greenPaint)
        canvas.drawPath(leaf2, greenPaint)

        val coconutPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(139, 69, 19)
            style = Paint.Style.FILL
        }
        canvas.drawOval(RectF(cx - 22f, kalashCy - 52f, cx + 22f, kalashCy - 16f), coconutPaint)
        canvas.drawCircle(cx, kalashCy - 34f, 6f, redPaint)

        val shehnaiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(255, 210, 50)
            style = Paint.Style.STROKE
            strokeWidth = 9f
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(44f, kalashCy + 36f, cx - 72f, kalashCy - 28f, shehnaiPaint)
        canvas.drawLine(width - 44f, kalashCy + 36f, cx + 72f, kalashCy - 28f, shehnaiPaint)

        val musicPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 22f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("♬ मंगल वाद्य व शहनाई ♬", cx, height - 10f, musicPaint)
        return bmp
    }

    fun createFloralToranBitmap(width: Int = 900, height: Int = 110): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val leafPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(34, 139, 34)
            style = Paint.Style.FILL
        }
        val marigold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(255, 165, 0)
            style = Paint.Style.FILL
        }
        val rose = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(225, 29, 72)
            style = Paint.Style.FILL
        }
        val stringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(251, 191, 36)
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        val swagCount = 5
        val segW = width.toFloat() / swagCount
        for (i in 0 until swagCount) {
            val sx = i * segW
            val ex = sx + segW
            val mx = (sx + ex) / 2f
            val path = Path().apply {
                moveTo(sx, 12f)
                quadTo(mx, height * 0.78f, ex, 12f)
            }
            canvas.drawPath(path, stringPaint)
            for (step in 1..4) {
                val t = step / 5f
                val px = sx + segW * t
                val py = 12f + 4f * t * (1f - t) * (height * 0.65f)
                canvas.drawCircle(px, py, 14f, if (step % 2 == 0) rose else marigold)
                canvas.drawOval(RectF(px - 7f, py + 10f, px + 7f, py + 26f), leafPaint)
            }
        }
        return bmp
    }

    fun createRingsMotifBitmap(accentColor: Int = AndroidColor.rgb(255, 215, 0)): Bitmap {
        val w = 280
        val h = 150
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.STROKE
            strokeWidth = 10f
        }
        canvas.drawCircle(w / 2f - 28f, h / 2f + 6f, 38f, ringPaint)
        canvas.drawCircle(w / 2f + 28f, h / 2f + 6f, 38f, ringPaint)
        val diamondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(224, 242, 254)
            style = Paint.Style.FILL
        }
        val dPath = Path().apply {
            moveTo(w / 2f - 28f, h / 2f - 46f)
            lineTo(w / 2f - 14f, h / 2f - 32f)
            lineTo(w / 2f - 28f, h / 2f - 22f)
            lineTo(w / 2f - 42f, h / 2f - 32f)
            close()
        }
        canvas.drawPath(dPath, diamondPaint)
        return bmp
    }

    fun drawCenterCroppedPhoto(
        canvas: Canvas,
        photo: Bitmap,
        dst: RectF,
        isCircle: Boolean,
        cornerRadius: Float,
        borderColor: Int,
        borderWidth: Float = 8f
    ) {
        val targetW = dst.width().toInt().coerceAtLeast(1)
        val targetH = dst.height().toInt().coerceAtLeast(1)

        // Center-crop photo into temporary bitmap
        val scale = maxOf(targetW.toFloat() / photo.width, targetH.toFloat() / photo.height)
        val scaledW = (photo.width * scale).toInt().coerceAtLeast(1)
        val scaledH = (photo.height * scale).toInt().coerceAtLeast(1)
        val leftOffset = (targetW - scaledW) / 2f
        val topOffset = (targetH - scaledH) / 2f

        val masked = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        val mCanvas = Canvas(masked)
        val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        if (isCircle) {
            val r = minOf(targetW, targetH) / 2f
            mCanvas.drawCircle(targetW / 2f, targetH / 2f, r, maskPaint)
        } else {
            mCanvas.drawRoundRect(RectF(0f, 0f, targetW.toFloat(), targetH.toFloat()), cornerRadius, cornerRadius, maskPaint)
        }
        maskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val srcRect = Rect(0, 0, photo.width, photo.height)
        val drawDst = RectF(leftOffset, topOffset, leftOffset + scaledW, topOffset + scaledH)
        mCanvas.drawBitmap(photo, srcRect, drawDst, maskPaint)

        canvas.drawBitmap(masked, dst.left, dst.top, null)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
        }
        if (isCircle) {
            val r = minOf(dst.width(), dst.height()) / 2f
            canvas.drawCircle(dst.centerX(), dst.centerY(), r, borderPaint)
        } else {
            canvas.drawRoundRect(dst, cornerRadius, cornerRadius, borderPaint)
        }
    }
}
