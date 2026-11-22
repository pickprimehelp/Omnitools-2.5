package com.example.ui.tools.design

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

@Composable
fun WatermarkControlBar(
    hasWatermark: Boolean,
    onWatchAdToSaveClean: () -> Unit
) {
    if (!hasWatermark) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Watermark active on export",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            OutlinedButton(
                onClick = onWatchAdToSaveClean,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text("Remove Watermark", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private enum class StatusDesignStyle {
    GRADIENT_GLOW,
    NEON_CYBER,
    MINIMAL_EDITORIAL,
    GLASS_SPOTLIGHT,
    PHOTO_OVERLAY,
    FESTIVE_RIBBON
}

private enum class StatusFontOption(val label: String, val typeface: Typeface) {
    SANS("Modern Sans", Typeface.DEFAULT_BOLD),
    SERIF("Royal Serif", Typeface.create(Typeface.SERIF, Typeface.BOLD)),
    MONO("Editorial Mono", Typeface.create(Typeface.MONOSPACE, Typeface.BOLD))
}

private enum class StatusAlignOption(val label: String, val align: Paint.Align) {
    LEFT("Left", Paint.Align.LEFT),
    CENTER("Center", Paint.Align.CENTER),
    RIGHT("Right", Paint.Align.RIGHT)
}

private data class StatusTemplate(
    val name: String,
    val c1: Color,
    val c2: Color,
    val accent: Color,
    val textColor: Color,
    val style: StatusDesignStyle
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StatusMakerScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val scope = rememberCoroutineScope()

    var quoteText by remember { mutableStateOf("Dream Big. Work Quietly. Let Success Make Noise.") }
    var authorText by remember { mutableStateOf("— Daily Motivation") }
    var tagBadge by remember { mutableStateOf("DAILY THOUGHTS") }
    var templateIndex by remember { mutableIntStateOf(0) }
    var fontOption by remember { mutableStateOf(StatusFontOption.SANS) }
    var alignOption by remember { mutableStateOf(StatusAlignOption.CENTER) }
    var isVerticalStory by remember { mutableStateOf(false) }
    var bgPhoto by remember { mutableStateOf<Bitmap?>(null) }
    var bgDim by remember { mutableFloatStateOf(0.50f) }
    var hasWatermark by remember { mutableStateOf(true) }

    val templates = remember {
        listOf(
            StatusTemplate("Gradient Glow", Color(0xFF4F46E5), Color(0xFFEC4899), Color(0xFFFDE047), Color.White, StatusDesignStyle.GRADIENT_GLOW),
            StatusTemplate("Neon Cyber", Color(0xFF090D16), Color(0xFF1E1B4B), Color(0xFF06B6D4), Color(0xFFF8FAFC), StatusDesignStyle.NEON_CYBER),
            StatusTemplate("Editorial Paper", Color(0xFFFFFBEB), Color(0xFFFEF3C7), Color(0xFFB45309), Color(0xFF292524), StatusDesignStyle.MINIMAL_EDITORIAL),
            StatusTemplate("Glass Spotlight", Color(0xFF0F172A), Color(0xFF334155), Color(0xFF38BDF8), Color.White, StatusDesignStyle.GLASS_SPOTLIGHT),
            StatusTemplate("Photo Overlay", Color(0xFF111827), Color(0xFF312E81), Color(0xFFF43F5E), Color.White, StatusDesignStyle.PHOTO_OVERLAY),
            StatusTemplate("Festive Ribbon", Color(0xFF7F1D1D), Color(0xFFB91C1C), Color(0xFFFBBF24), Color(0xFFFFFBEB), StatusDesignStyle.FESTIVE_RIBBON)
        )
    }
    val active = templates[templateIndex]

    val quotePresets = remember {
        listOf(
            "Motivation" to ("Dream Big. Work Quietly. Let Success Make Noise." to "— Daily Hustle"),
            "Attitude" to ("I am not lucky, I stayed consistent when others quit." to "— Mindset"),
            "Shayari" to ("वक्त से लड़कर जो नसीब बदल दे, इंसान वही जो अपनी तकदीर बदल दे।" to "— हिंदी शायरी"),
            "Love" to ("In every lifetime, my heart will always find its way to you." to "— Forever Yours"),
            "Good Morning" to ("New sunrise, fresh energy, endless possibilities ahead." to "— Good Morning ✨")
        )
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val bmp = try {
                    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
                    var sample = 1
                    while (opts.outWidth / sample > 1200 || opts.outHeight / sample > 1200) sample *= 2
                    context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
                    }
                } catch (_: Exception) {
                    null
                }
                withContext(Dispatchers.Main) { bgPhoto = bmp }
            }
        }
    }

    fun renderStatus(width: Int, height: Int): Bitmap {
        val sx = width / 1080f
        val sy = height / 1080f
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        // Background Gradient
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), active.c1.toArgb(), active.c2.toArgb(), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Optional User Background Photo
        bgPhoto?.let { photo ->
            val scale = maxOf(width.toFloat() / photo.width, height.toFloat() / photo.height)
            val dw = photo.width * scale
            val dh = photo.height * scale
            canvas.drawBitmap(
                photo,
                Rect(0, 0, photo.width, photo.height),
                RectF((width - dw) / 2f, (height - dh) / 2f, (width + dw) / 2f, (height + dh) / 2f),
                Paint(Paint.FILTER_BITMAP_FLAG)
            )
            canvas.drawRect(
                0f, 0f, width.toFloat(), height.toFloat(),
                Paint().apply { color = android.graphics.Color.argb((bgDim * 225).toInt().coerceIn(0, 235), 10, 12, 20) }
            )
        }

        // Design-specific decorations
        when (active.style) {
            StatusDesignStyle.GRADIENT_GLOW -> {
                canvas.drawCircle(width * 0.2f, height * 0.2f, 220f * sx, Paint().apply { color = android.graphics.Color.argb(45, 255, 255, 255) })
                canvas.drawCircle(width * 0.85f, height * 0.82f, 260f * sx, Paint().apply { color = active.accent.copy(alpha = 0.16f).toArgb() })
                val panel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.argb(38, 255, 255, 255)
                }
                canvas.drawRoundRect(RectF(72f * sx, 160f * sy, width - 72f * sx, height - 160f * sy), 42f * sx, 42f * sx, panel)
            }
            StatusDesignStyle.NEON_CYBER -> {
                val neon = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = active.accent.toArgb()
                    style = Paint.Style.STROKE
                    strokeWidth = 7f * sx
                    setShadowLayer(18f * sx, 0f, 0f, active.accent.toArgb())
                }
                canvas.drawRoundRect(RectF(48f * sx, 48f * sy, width - 48f * sx, height - 48f * sy), 28f * sx, 28f * sx, neon)
                val inner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.argb(120, 236, 72, 153)
                    style = Paint.Style.STROKE
                    strokeWidth = 3f * sx
                }
                canvas.drawRoundRect(RectF(72f * sx, 72f * sy, width - 72f * sx, height - 72f * sy), 18f * sx, 18f * sx, inner)
            }
            StatusDesignStyle.MINIMAL_EDITORIAL -> {
                val rulePaint = Paint().apply {
                    color = android.graphics.Color.argb(28, 120, 53, 15)
                    strokeWidth = 2f * sx
                }
                var y = 140f * sy
                while (y < height - 120f * sy) {
                    canvas.drawLine(80f * sx, y, width - 80f * sx, y, rulePaint)
                    y += 70f * sy
                }
                val frame = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = active.accent.toArgb()
                    style = Paint.Style.STROKE
                    strokeWidth = 5f * sx
                }
                canvas.drawRect(54f * sx, 54f * sy, width - 54f * sx, height - 54f * sy, frame)
            }
            StatusDesignStyle.GLASS_SPOTLIGHT -> {
                val card = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.argb(155, 15, 23, 42)
                }
                canvas.drawRoundRect(RectF(80f * sx, height * 0.20f, width - 80f * sx, height * 0.80f), 40f * sx, 40f * sx, card)
                val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = active.accent.copy(alpha = 0.7f).toArgb()
                    style = Paint.Style.STROKE
                    strokeWidth = 4f * sx
                }
                canvas.drawRoundRect(RectF(80f * sx, height * 0.20f, width - 80f * sx, height * 0.80f), 40f * sx, 40f * sx, stroke)
            }
            StatusDesignStyle.PHOTO_OVERLAY -> {
                val bar = Paint().apply { color = active.accent.toArgb() }
                canvas.drawRect(80f * sx, height * 0.26f, 94f * sx, height * 0.74f, bar)
            }
            StatusDesignStyle.FESTIVE_RIBBON -> {
                val ribbon = Paint().apply { color = active.accent.toArgb() }
                canvas.drawRect(0f, 70f * sy, width.toFloat(), 112f * sy, ribbon)
                canvas.drawRect(0f, height - 112f * sy, width.toFloat(), height - 70f * sy, ribbon)
                val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = active.accent.toArgb()
                    style = Paint.Style.STROKE
                    strokeWidth = 8f * sx
                }
                canvas.drawRoundRect(RectF(36f * sx, 36f * sy, width - 36f * sx, height - 36f * sy), 28f * sx, 28f * sx, border)
            }
        }

        // Tag Pill
        val anchorX = when (alignOption) {
            StatusAlignOption.LEFT -> 126f * sx
            StatusAlignOption.CENTER -> width / 2f
            StatusAlignOption.RIGHT -> width - 126f * sx
        }
        if (tagBadge.isNotBlank()) {
            val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = active.accent.toArgb()
                textSize = 28f * sx
                textAlign = alignOption.align
                typeface = Typeface.DEFAULT_BOLD
                letterSpacing = 0.14f
            }
            canvas.drawText(tagBadge.uppercase(), anchorX, height * 0.28f, tagPaint)
        }

        // Decorative quote glyph
        val glyphPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = active.accent.copy(alpha = 0.7f).toArgb()
            textSize = 94f * sx
            textAlign = alignOption.align
            typeface = Typeface.SERIF
        }
        canvas.drawText("“", anchorX, height * 0.38f, glyphPaint)

        // Wrapped Quote Body
        val lines = wrapText(quoteText, maxChars = 22).take(5)
        val quotePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = active.textColor.toArgb()
            textSize = 54f * sx
            textAlign = alignOption.align
            typeface = fontOption.typeface
        }
        val blockStartY = height * 0.46f - ((lines.size - 1) * 32f * sy)
        lines.forEachIndexed { idx, line ->
            canvas.drawText(line, anchorX, blockStartY + idx * 70f * sy, quotePaint)
        }

        // Author Footer
        if (authorText.isNotBlank()) {
            val authPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = active.accent.toArgb()
                textSize = 34f * sx
                textAlign = alignOption.align
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText(authorText, anchorX, height * 0.78f, authPaint)
        }

        if (hasWatermark) {
            val wmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = active.textColor.copy(alpha = 0.6f).toArgb()
                textSize = 24f * sx
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Created with OmniTools Studio", width / 2f, height - 36f * sy, wmPaint)
        }
        return bmp
    }

    val previewW = 720
    val previewH = if (isVerticalStory) 1280 else 720
    val previewBitmap = remember(
        quoteText, authorText, tagBadge, templateIndex, fontOption,
        alignOption, isVerticalStory, bgPhoto, bgDim, hasWatermark
    ) {
        renderStatus(previewW, previewH)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Status Maker Studio", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("6 Card Designs, Photos & Stylish Fonts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        bitmap = previewBitmap.asImageBitmap(),
                        contentDescription = "Live Status Card Preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(if (isVerticalStory) 9f / 14f else 1f)
                    )
                }
            }

            item {
                Text("1. Choose Status Card Design", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(templates) { idx, t ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = t.c1,
                            border = BorderStroke(if (idx == templateIndex) 3.dp else 1.dp, t.accent),
                            modifier = Modifier
                                .clickable { templateIndex = idx }
                                .testTag("status_design_$idx")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (idx == templateIndex) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = t.accent, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(t.name, color = t.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                        Text("2. Layout, Font & Photo Background", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = !isVerticalStory, onClick = { isVerticalStory = false }, label = { Text("1:1 Square") })
                            FilterChip(selected = isVerticalStory, onClick = { isVerticalStory = true }, label = { Text("9:16 Story") })
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (bgPhoto == null) "BG Photo" else "Change", fontSize = 11.sp)
                                }
                                if (bgPhoto != null) {
                                    IconButton(onClick = { bgPhoto = null }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear BG", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                        if (bgPhoto != null) {
                            Text("Photo Scrim: ${(bgDim * 100).toInt()}%", fontSize = 12.sp)
                            Slider(value = bgDim, onValueChange = { bgDim = it }, valueRange = 0f..0.85f)
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusFontOption.entries.forEach { f ->
                                FilterChip(selected = fontOption == f, onClick = { fontOption = f }, label = { Text(f.label, fontSize = 11.sp) })
                            }
                            StatusAlignOption.entries.forEach { a ->
                                FilterChip(selected = alignOption == a, onClick = { alignOption = a }, label = { Text(a.label, fontSize = 11.sp) })
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("3. Quick Quotes & Custom Text", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(quotePresets) { _, item ->
                            FilterChip(
                                selected = quoteText == item.second.first,
                                onClick = {
                                    quoteText = item.second.first
                                    authorText = item.second.second
                                },
                                label = { Text(item.first, fontSize = 11.sp) }
                            )
                        }
                    }
                    OutlinedTextField(value = quoteText, onValueChange = { quoteText = it }, label = { Text("Status Quote / Shayari") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = authorText, onValueChange = { authorText = it }, label = { Text("Author / Signature") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = tagBadge, onValueChange = { tagBadge = it }, label = { Text("Top Badge") }, modifier = Modifier.weight(1f))
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
                val outH = if (isVerticalStory) 1920 else 1080
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { ImageExportUtils.saveBitmapToGallery(context, renderStatus(1080, outH), "OmniTools_Status") },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save HD", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { ImageExportUtils.shareBitmap(context, renderStatus(1080, outH), "Status Card") },
                        modifier = Modifier.weight(1f).height(50.dp),
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

@Composable
fun InvitationCardMakerScreen(onBack: () -> Unit) {
    StudioCardBuilderScreen(
        title = "Invitation Card Maker",
        defaultBadge = "YOU ARE INVITED",
        defaultHeadline = "Join Us For Our Grand Celebration!",
        defaultSubtitle = "Saturday, 7:00 PM • Celebration Lawn",
        presets = listOf(
            "Birthday" to ("Happy Birthday Celebration!" to "Saturday, 6:30 PM • Cake Cutting & Dinner"),
            "Anniversary" to ("Celebrating 25 Years of Love" to "Sunday, 7:30 PM • Courtyard Hall"),
            "Housewarming" to ("Griha Pravesh Ceremony" to "Friday, 10:30 AM • Lunch Follows")
        ),
        exportPrefix = "OmniTools_Invite",
        aspectRatio = 0.8f,
        onBack = onBack
    )
}

@Composable
fun ReelsMakerScreen(onBack: () -> Unit) {
    StudioCardBuilderScreen(
        title = "Reels Cover & Story Maker",
        defaultBadge = "REEL COVER",
        defaultHeadline = "5 Tech & AI Tricks Nobody Tells You 🚀",
        defaultSubtitle = "@omnitools_creator • Save This Reel",
        presets = listOf(
            "Viral Hook" to ("5 Secrets Nobody Tells You 🚀" to "Save & Follow for Part 2"),
            "Before/After" to ("How I Built This in 10 Minutes ⚡" to "Step-by-step breakdown inside"),
            "Tutorial" to ("Stop Doing This Mistake! ❌" to "Watch till the end for the fix")
        ),
        exportPrefix = "OmniTools_Reel",
        aspectRatio = 9f / 16f,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudioCardBuilderScreen(
    title: String,
    defaultBadge: String,
    defaultHeadline: String,
    defaultSubtitle: String,
    presets: List<Pair<String, Pair<String, String>>>,
    exportPrefix: String,
    aspectRatio: Float,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val scope = rememberCoroutineScope()

    var badge by remember { mutableStateOf(defaultBadge) }
    var headline by remember { mutableStateOf(defaultHeadline) }
    var subtitle by remember { mutableStateOf(defaultSubtitle) }
    var paletteIndex by remember { mutableIntStateOf(0) }
    var bgPhoto by remember { mutableStateOf<Bitmap?>(null) }
    var hasWatermark by remember { mutableStateOf(true) }

    val palettes = remember {
        listOf(
            Color(0xFF4F46E5) to Color(0xFFEC4899),
            Color(0xFF0F172A) to Color(0xFF334155),
            Color(0xFFDC2626) to Color(0xFFF59E0B),
            Color(0xFF059669) to Color(0xFF0284C7)
        )
    }
    val (c1, c2) = palettes[paletteIndex]

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val bmp = try {
                    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                } catch (_: Exception) {
                    null
                }
                withContext(Dispatchers.Main) { bgPhoto = bmp }
            }
        }
    }

    fun buildExportBitmap(): Bitmap {
        val w = 1080
        val h = (w / aspectRatio).toInt().coerceIn(720, 1920)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val bgPaint = Paint().apply {
            shader = LinearGradient(0f, 0f, w.toFloat(), h.toFloat(), c1.toArgb(), c2.toArgb(), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        bgPhoto?.let { photo ->
            val scale = maxOf(w.toFloat() / photo.width, h.toFloat() / photo.height)
            val dw = photo.width * scale
            val dh = photo.height * scale
            canvas.drawBitmap(photo, Rect(0, 0, photo.width, photo.height), RectF((w - dw) / 2f, (h - dh) / 2f, (w + dw) / 2f, (h + dh) / 2f), null)
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), Paint().apply { color = android.graphics.Color.argb(135, 10, 12, 24) })
        }

        val frame = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(120, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawRoundRect(RectF(42f, 42f, w - 42f, h - 42f), 32f, 32f, frame)

        if (badge.isNotBlank()) {
            val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(253, 224, 71)
                textSize = 30f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText(badge.uppercase(), w / 2f, h * 0.28f, badgePaint)
        }

        val headPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 62f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        val lines = wrapText(headline, 20).take(4)
        var y = h * 0.45f - ((lines.size - 1) * 32f)
        lines.forEach { line ->
            canvas.drawText(line, w / 2f, y, headPaint)
            y += 74f
        }

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(230, 255, 255, 255)
            textSize = 34f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(subtitle, w / 2f, h * 0.72f, subPaint)

        if (hasWatermark) {
            val wmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.argb(175, 255, 255, 255)
                textSize = 26f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Made with OmniTools Studio", w / 2f, h - 46f, wmPaint)
        }
        return bmp
    }

    val previewBmp = remember(badge, headline, subtitle, paletteIndex, bgPhoto, hasWatermark) { buildExportBitmap() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        bitmap = previewBmp.asImageBitmap(),
                        contentDescription = "$title Preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(aspectRatio.coerceIn(0.65f, 1.8f))
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    palettes.forEachIndexed { idx, pair ->
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(pair.first, pair.second)))
                                .border(
                                    width = if (idx == paletteIndex) 3.dp else 1.dp,
                                    color = if (idx == paletteIndex) MaterialTheme.colorScheme.primary else Color.White,
                                    shape = CircleShape
                                )
                                .clickable { paletteIndex = idx }
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    OutlinedButton(
                        onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (bgPhoto == null) "BG Photo" else "Change", fontSize = 12.sp)
                    }
                    if (bgPhoto != null) {
                        IconButton(onClick = { bgPhoto = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear BG", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(presets) { _, p ->
                        FilterChip(
                            selected = headline == p.second.first,
                            onClick = {
                                headline = p.second.first
                                subtitle = p.second.second
                            },
                            label = { Text(p.first, fontSize = 11.sp) }
                        )
                    }
                }
            }
            item { OutlinedTextField(value = badge, onValueChange = { badge = it }, label = { Text("Top Badge") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(value = headline, onValueChange = { headline = it }, label = { Text("Main Headline") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(value = subtitle, onValueChange = { subtitle = it }, label = { Text("Subtitle / Footer") }, modifier = Modifier.fillMaxWidth()) }
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
                        onClick = { ImageExportUtils.saveBitmapToGallery(context, buildExportBitmap(), exportPrefix) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save HD")
                    }
                    OutlinedButton(
                        onClick = { ImageExportUtils.shareBitmap(context, buildExportBitmap(), title) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share")
                    }
                }
            }
        }
    }
}
