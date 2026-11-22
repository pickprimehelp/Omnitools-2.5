package com.example.ui.tools.design

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdManager
import com.example.ads.TopBannerAd
import com.example.ads.findActivity
import com.example.util.ImageExportUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class ThumbnailTemplateStyle {
    VIRAL_SPLIT,
    TECH_DARK_GLOW,
    CINEMATIC_VLOG,
    BREAKING_ALERT,
    TUTORIAL_GRID
}

private enum class LogoCorner(val label: String) {
    TOP_LEFT("Top Left"),
    TOP_RIGHT("Top Right"),
    BOTTOM_LEFT("Bottom Left")
}

private data class ThumbnailDesign(
    val name: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val headlineColor: Color,
    val style: ThumbnailTemplateStyle
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun YouTubeThumbnailMakerScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val scope = rememberCoroutineScope()

    var headline by remember { mutableStateOf("GROW 10X FASTER IN 2026!") }
    var subheadline by remember { mutableStateOf("PRO CREATOR SECRETS") }
    var hookBadge by remember { mutableStateOf("NEW 🔥") }
    var channelName by remember { mutableStateOf("@OmniStudio") }

    var designIndex by remember { mutableIntStateOf(0) }
    var bgPhoto by remember { mutableStateOf<Bitmap?>(null) }
    var bgDim by remember { mutableFloatStateOf(0.45f) }

    var logoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var logoCorner by remember { mutableStateOf(LogoCorner.TOP_LEFT) }
    var isCircleLogo by remember { mutableStateOf(true) }
    var showSafeZoneGuide by remember { mutableStateOf(true) }
    var hasWatermark by remember { mutableStateOf(true) }

    val designs = remember {
        listOf(
            ThumbnailDesign("Viral Split", Color(0xFF4F46E5), Color(0xFFDB2777), Color(0xFFFACC15), Color.White, ThumbnailTemplateStyle.VIRAL_SPLIT),
            ThumbnailDesign("Tech Studio", Color(0xFF090D16), Color(0xFF1E293B), Color(0xFF06B6D4), Color.White, ThumbnailTemplateStyle.TECH_DARK_GLOW),
            ThumbnailDesign("Cinematic Vlog", Color(0xFF451A03), Color(0xFF1C1917), Color(0xFFF59E0B), Color(0xFFFFFBEB), ThumbnailTemplateStyle.CINEMATIC_VLOG),
            ThumbnailDesign("Breaking Alert", Color(0xFF991B1B), Color(0xFF111827), Color(0xFFFDE047), Color.White, ThumbnailTemplateStyle.BREAKING_ALERT),
            ThumbnailDesign("Tutorial Grid", Color(0xFF065F46), Color(0xFF0F172A), Color(0xFF34D399), Color.White, ThumbnailTemplateStyle.TUTORIAL_GRID)
        )
    }
    val activeDesign = designs[designIndex]

    fun decodeImage(uri: android.net.Uri, maxDim: Int, onLoaded: (Bitmap?) -> Unit) {
        scope.launch(Dispatchers.IO) {
            val bmp = try {
                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
                var sample = 1
                while (opts.outWidth / sample > maxDim || opts.outHeight / sample > maxDim) sample *= 2
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
                }
            } catch (_: Exception) {
                null
            }
            withContext(Dispatchers.Main) { onLoaded(bmp) }
        }
    }

    val bgPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) decodeImage(uri, 1400) { bgPhoto = it }
    }
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) decodeImage(uri, 600) { logoBitmap = it }
    }

    fun renderThumbnail(width: Int, height: Int, includeSafeZoneGuide: Boolean): Bitmap {
        val s = width / 1280f
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        // 1. Background base or user photo
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                activeDesign.primaryColor.toArgb(),
                activeDesign.secondaryColor.toArgb(),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        bgPhoto?.let { photo ->
            val scale = maxOf(width.toFloat() / photo.width, height.toFloat() / photo.height)
            val dw = photo.width * scale
            val dh = photo.height * scale
            val dx = (width - dw) / 2f
            val dy = (height - dh) / 2f
            canvas.drawBitmap(photo, Rect(0, 0, photo.width, photo.height), RectF(dx, dy, dx + dw, dy + dh), Paint(Paint.FILTER_BITMAP_FLAG))

            val scrimPaint = Paint().apply {
                color = android.graphics.Color.argb((bgDim * 220).toInt().coerceIn(0, 230), 8, 12, 24)
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), scrimPaint)
        }

        // 2. Design Template Geometry
        when (activeDesign.style) {
            ThumbnailTemplateStyle.VIRAL_SPLIT -> {
                val splitPath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(width * 0.66f, 0f)
                    lineTo(width * 0.52f, height.toFloat())
                    lineTo(0f, height.toFloat())
                    close()
                }
                val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = activeDesign.primaryColor.copy(alpha = if (bgPhoto != null) 0.72f else 0.92f).toArgb()
                }
                canvas.drawPath(splitPath, p)
                val lineP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = activeDesign.accentColor.toArgb()
                    strokeWidth = 10f * s
                }
                canvas.drawLine(width * 0.66f, 0f, width * 0.52f, height.toFloat(), lineP)
            }
            ThumbnailTemplateStyle.TECH_DARK_GLOW -> {
                val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = activeDesign.accentColor.copy(alpha = 0.22f).toArgb()
                }
                canvas.drawCircle(width * 0.82f, height * 0.48f, 240f * s, glow)
                val frame = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = activeDesign.accentColor.toArgb()
                    style = Paint.Style.STROKE
                    strokeWidth = 8f * s
                }
                canvas.drawRoundRect(RectF(22f * s, 22f * s, width - 22f * s, height - 22f * s), 26f * s, 26f * s, frame)
            }
            ThumbnailTemplateStyle.CINEMATIC_VLOG -> {
                val barPaint = Paint().apply { color = android.graphics.Color.argb(215, 0, 0, 0) }
                canvas.drawRect(0f, 0f, width.toFloat(), 54f * s, barPaint)
                canvas.drawRect(0f, height - 54f * s, width.toFloat(), height.toFloat(), barPaint)
                val goldLine = Paint().apply {
                    color = activeDesign.accentColor.toArgb()
                    strokeWidth = 4f * s
                }
                canvas.drawLine(0f, 54f * s, width.toFloat(), 54f * s, goldLine)
                canvas.drawLine(0f, height - 54f * s, width.toFloat(), height - 54f * s, goldLine)
            }
            ThumbnailTemplateStyle.BREAKING_ALERT -> {
                val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = activeDesign.accentColor.toArgb()
                    style = Paint.Style.STROKE
                    strokeWidth = 16f * s
                }
                canvas.drawRect(8f * s, 8f * s, width - 8f * s, height - 8f * s, border)
                val strip = Paint().apply { color = android.graphics.Color.rgb(220, 38, 38) }
                canvas.drawRect(0f, height - 110f * s, width * 0.72f, height.toFloat(), strip)
            }
            ThumbnailTemplateStyle.TUTORIAL_GRID -> {
                val gridPaint = Paint().apply {
                    color = android.graphics.Color.argb(40, 255, 255, 255)
                    strokeWidth = 2f * s
                }
                var gx = 0f
                while (gx < width) {
                    canvas.drawLine(gx, 0f, gx, height.toFloat(), gridPaint)
                    gx += 64f * s
                }
                var gy = 0f
                while (gy < height) {
                    canvas.drawLine(0f, gy, width.toFloat(), gy, gridPaint)
                    gy += 64f * s
                }
            }
        }

        // 3. Hook Sticker Badge (Top-Right or Top-Left opposite logo)
        if (hookBadge.isNotBlank()) {
            val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = activeDesign.accentColor.toArgb()
            }
            val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(15, 23, 42)
                textSize = 36f * s
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }
            val tw = badgeTextPaint.measureText(hookBadge) + 56f * s
            val bx = if (logoCorner == LogoCorner.TOP_RIGHT && logoBitmap != null) 48f * s else width - tw - 48f * s
            val by = if (activeDesign.style == ThumbnailTemplateStyle.CINEMATIC_VLOG) 76f * s else 44f * s
            val rect = RectF(bx, by, bx + tw, by + 64f * s)
            canvas.drawRoundRect(rect, 18f * s, 18f * s, badgePaint)
            canvas.drawText(hookBadge, rect.centerX(), rect.centerY() + 12f * s, badgeTextPaint)
        }

        // 4. Channel Logo + Brand Name Pill
        val logoSize = 118f * s
        val margin = 44f * s
        val topPad = if (activeDesign.style == ThumbnailTemplateStyle.CINEMATIC_VLOG) 72f * s else margin
        val logoRect = when (logoCorner) {
            LogoCorner.TOP_LEFT -> RectF(margin, topPad, margin + logoSize, topPad + logoSize)
            LogoCorner.TOP_RIGHT -> RectF(width - margin - logoSize, topPad, width - margin, topPad + logoSize)
            LogoCorner.BOTTOM_LEFT -> RectF(margin, height - margin - logoSize, margin + logoSize, height - margin)
        }

        if (logoBitmap != null) {
            WeddingClipartRenderer.drawCenterCroppedPhoto(
                canvas = canvas,
                photo = logoBitmap!!,
                dst = logoRect,
                isCircle = isCircleLogo,
                cornerRadius = 22f * s,
                borderColor = activeDesign.accentColor.toArgb(),
                borderWidth = 6f * s
            )
        }

        if (channelName.isNotBlank()) {
            val pillTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                textSize = 28f * s
                typeface = Typeface.DEFAULT_BOLD
            }
            val pillW = pillTextPaint.measureText(channelName) + 38f * s
            val pillBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.argb(185, 15, 23, 42)
            }
            val px = when {
                logoBitmap != null && logoCorner == LogoCorner.TOP_RIGHT -> logoRect.left - pillW - 16f * s
                logoBitmap != null -> logoRect.right + 16f * s
                else -> margin
            }
            val py = when (logoCorner) {
                LogoCorner.BOTTOM_LEFT -> logoRect.centerY() - 25f * s
                else -> topPad + 34f * s
            }
            val pillRect = RectF(px, py, px + pillW, py + 50f * s)
            canvas.drawRoundRect(pillRect, 25f * s, 25f * s, pillBg)
            canvas.drawText(channelName, px + 19f * s, py + 34f * s, pillTextPaint)
        }

        // 5. Bold Multi-line Headline with heavy contrast shadow
        val lines = wrapText(headline.uppercase(), maxChars = 16).take(3)
        val headPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = activeDesign.headlineColor.toArgb()
            textSize = 92f * s
            typeface = Typeface.DEFAULT_BOLD
            setShadowLayer(12f * s, 5f * s, 6f * s, android.graphics.Color.argb(220, 0, 0, 0))
        }
        var textY = height * 0.42f
        lines.forEach { line ->
            canvas.drawText(line, 56f * s, textY, headPaint)
            textY += 102f * s
        }

        if (subheadline.isNotBlank()) {
            val subBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = activeDesign.accentColor.toArgb()
            }
            val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(15, 23, 42)
                textSize = 38f * s
                typeface = Typeface.DEFAULT_BOLD
            }
            val sw = (subTextPaint.measureText(subheadline) + 46f * s).coerceAtMost(width * 0.85f)
            val subTop = (textY - 24f * s).coerceAtMost(height - 130f * s)
            canvas.drawRoundRect(RectF(56f * s, subTop, 56f * s + sw, subTop + 62f * s), 14f * s, 14f * s, subBgPaint)
            canvas.drawText(subheadline, 78f * s, subTop + 44f * s, subTextPaint)
        }

        if (hasWatermark) {
            val wmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.argb(180, 255, 255, 255)
                textSize = 24f * s
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Made with OmniTools", width / 2f, height - 22f * s, wmPaint)
        }

        // Preview-only YouTube duration safe-zone pill
        if (includeSafeZoneGuide) {
            val szBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.argb(215, 0, 0, 0) }
            val szRect = RectF(width - 150f * s, height - 68f * s, width - 24f * s, height - 22f * s)
            canvas.drawRoundRect(szRect, 10f * s, 10f * s, szBg)
            val szText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                textSize = 26f * s
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("12:45", szRect.centerX(), szRect.centerY() + 9f * s, szText)
        }
        return bmp
    }

    val previewBitmap = remember(
        headline, subheadline, hookBadge, channelName, designIndex,
        bgPhoto, bgDim, logoBitmap, logoCorner, isCircleLogo, showSafeZoneGuide, hasWatermark
    ) {
        renderThumbnail(960, 540, includeSafeZoneGuide = showSafeZoneGuide)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("YouTube Thumbnail Studio", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Channel Logo, Photo BG & High-CTR Cards", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = { TopBannerAd() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        bitmap = previewBitmap.asImageBitmap(),
                        contentDescription = "YouTube Thumbnail 16:9 Live Preview",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                    )
                }
            }

            item {
                Text("1. Thumbnail Design Card", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(designs) { idx, d ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = d.primaryColor,
                            border = BorderStroke(if (idx == designIndex) 3.dp else 1.dp, d.accentColor),
                            modifier = Modifier
                                .clickable { designIndex = idx }
                                .testTag("thumb_design_$idx")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (idx == designIndex) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = d.accentColor, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(d.name, color = d.headlineColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("2. Channel Logo & Background Photo", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedButton(
                                    onClick = { logoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).height(48.dp).testTag("pick_channel_logo")
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (logoBitmap == null) "Add Logo" else "Change Logo", fontSize = 12.sp)
                                }
                                if (logoBitmap != null) {
                                    IconButton(onClick = { logoBitmap = null }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear Logo", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedButton(
                                    onClick = { bgPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).height(48.dp).testTag("pick_thumb_bg")
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (bgPhoto == null) "BG Photo" else "Change BG", fontSize = 12.sp)
                                }
                                if (bgPhoto != null) {
                                    IconButton(onClick = { bgPhoto = null }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear BG", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                        if (logoBitmap != null) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                LogoCorner.entries.forEach { corner ->
                                    FilterChip(
                                        selected = logoCorner == corner,
                                        onClick = { logoCorner = corner },
                                        label = { Text(corner.label, fontSize = 11.sp) }
                                    )
                                }
                                FilterChip(
                                    selected = isCircleLogo,
                                    onClick = { isCircleLogo = !isCircleLogo },
                                    label = { Text(if (isCircleLogo) "Circle Logo" else "Rounded Square", fontSize = 11.sp) }
                                )
                            }
                        }
                        if (bgPhoto != null) {
                            Text("Background Dark Scrim: ${(bgDim * 100).toInt()}%", fontSize = 12.sp)
                            Slider(value = bgDim, onValueChange = { bgDim = it }, valueRange = 0f..0.85f)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show YouTube Time Safe-Zone (12:45)", fontSize = 12.sp)
                            Switch(checked = showSafeZoneGuide, onCheckedChange = { showSafeZoneGuide = it })
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("3. Headlines & Hook Stickers", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val stickers = listOf("NEW 🔥", "SECRET ⚡", "2026 GUIDE", "100% FREE", "EXPOSED 🚨", "TOP 5 ⭐", "")
                        itemsIndexed(stickers) { _, sticker ->
                            FilterChip(
                                selected = hookBadge == sticker,
                                onClick = { hookBadge = sticker },
                                label = { Text(sticker.ifBlank { "No Badge" }, fontSize = 11.sp) }
                            )
                        }
                    }
                    OutlinedTextField(value = headline, onValueChange = { headline = it }, label = { Text("Main Thumbnail Headline") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = subheadline, onValueChange = { subheadline = it }, label = { Text("Highlight Banner") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = channelName, onValueChange = { channelName = it }, label = { Text("Channel Name") }, modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                WatermarkControlBar(hasWatermark = hasWatermark) {
                    if (activity != null) {
                        AdManager.showRewardedAd(activity, onRewardEarned = { hasWatermark = false }, onDismissed = {})
                    } else {
                        hasWatermark = false
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            ImageExportUtils.saveBitmapToGallery(
                                context,
                                renderThumbnail(1280, 720, includeSafeZoneGuide = false),
                                "OmniTools_Thumbnail"
                            )
                        },
                        modifier = Modifier.weight(1f).height(50.dp).testTag("save_thumbnail"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save 1280×720 HD", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            ImageExportUtils.shareBitmap(
                                context,
                                renderThumbnail(1280, 720, includeSafeZoneGuide = false),
                                "YouTube Thumbnail"
                            )
                        },
                        modifier = Modifier.weight(1f).height(50.dp).testTag("share_thumbnail"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

internal fun wrapText(text: String, maxChars: Int): List<String> {
    if (text.isBlank()) return emptyList()
    val words = text.trim().split(Regex("\\s+"))
    val lines = mutableListOf<String>()
    var current = StringBuilder()
    for (word in words) {
        if (current.isEmpty()) {
            current.append(word)
        } else if (current.length + 1 + word.length <= maxChars) {
            current.append(" ").append(word)
        } else {
            lines.add(current.toString())
            current = StringBuilder(word)
        }
    }
    if (current.isNotEmpty()) lines.add(current.toString())
    return lines
}
