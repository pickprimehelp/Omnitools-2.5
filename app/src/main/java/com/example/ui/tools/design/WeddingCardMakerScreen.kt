package com.example.ui.tools.design

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

private enum class WeddingPhotoMode(val label: String) {
    SEPARATE("Dulha & Dulhan"),
    COUPLE("Couple Photo"),
    CLIPART_ONLY("Art Clipart Only")
}

private enum class WeddingPhotoShape(val label: String) {
    CIRCLE("Circle"),
    ARCH("Rounded"),
    SQUARE("Square")
}

private enum class WeddingDecorStyle {
    MANDAP_ARCH,
    FLORAL_CORNERS,
    ROYAL_JALI,
    IVORY_MINIMAL,
    TEMPLE_GOLD
}

private data class WeddingTemplate(
    val name: String,
    val bgTop: Color,
    val bgBottom: Color,
    val textColor: Color,
    val accentColor: Color,
    val borderColor: Color,
    val style: WeddingDecorStyle
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WeddingCardMakerScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val scope = rememberCoroutineScope()

    var eventHeader by remember { mutableStateOf("SHUBH VIVAH") }
    var topMantra by remember { mutableStateOf("॥ श्री गणेशाय नमः ॥") }
    var parentsLine by remember { mutableStateOf("With the blessings of Smt. & Shri Sharma Family") }
    var groomName by remember { mutableStateOf("Rohan") }
    var brideName by remember { mutableStateOf("Ananya") }
    var dateText by remember { mutableStateOf("14 Dec 2026") }
    var timeText by remember { mutableStateOf("7:30 PM Onwards") }
    var venueText by remember { mutableStateOf("Royal Heritage Palace, Jaipur") }
    var rsvpText by remember { mutableStateOf("Sharma & Verma Family • +91 98765 43210") }

    var templateIndex by remember { mutableIntStateOf(0) }
    var photoMode by remember { mutableStateOf(WeddingPhotoMode.SEPARATE) }
    var photoShape by remember { mutableStateOf(WeddingPhotoShape.CIRCLE) }
    var groomPhoto by remember { mutableStateOf<Bitmap?>(null) }
    var bridePhoto by remember { mutableStateOf<Bitmap?>(null) }
    var couplePhoto by remember { mutableStateOf<Bitmap?>(null) }

    var selectedCliparts by remember {
        mutableStateOf(
            setOf(
                WeddingClipartType.GANPATI_AUSPICIOUS,
                WeddingClipartType.DULHA_DULHAN_ROYAL,
                WeddingClipartType.SHEHNAI_KALASH
            )
        )
    }
    var hasWatermark by remember { mutableStateOf(true) }

    val templates = remember {
        listOf(
            WeddingTemplate("Royal Rajwadi", Color(0xFF7F1D1D), Color(0xFF3F0713), Color(0xFFFEF3C7), Color(0xFFFBBF24), Color(0xFFD97706), WeddingDecorStyle.MANDAP_ARCH),
            WeddingTemplate("Rose Floral", Color(0xFF881337), Color(0xFF4C0519), Color(0xFFFFF1F2), Color(0xFFFDA4AF), Color(0xFFFB7185), WeddingDecorStyle.FLORAL_CORNERS),
            WeddingTemplate("Emerald Royal", Color(0xFF064E3B), Color(0xFF022C22), Color(0xFFECFDF5), Color(0xFFFCD34D), Color(0xFF10B981), WeddingDecorStyle.ROYAL_JALI),
            WeddingTemplate("Ivory Classic", Color(0xFFFFFBEB), Color(0xFFFEF3C7), Color(0xFF78350F), Color(0xFFB45309), Color(0xFFD97706), WeddingDecorStyle.IVORY_MINIMAL),
            WeddingTemplate("Temple Gold", Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFFF8FAFC), Color(0xFFFBBF24), Color(0xFF818CF8), WeddingDecorStyle.TEMPLE_GOLD)
        )
    }
    val activeTemplate = templates[templateIndex]

    fun decodeUri(uri: android.net.Uri, onReady: (Bitmap?) -> Unit) {
        scope.launch(Dispatchers.IO) {
            val bmp = try {
                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
                var sample = 1
                while (opts.outWidth / sample > 900 || opts.outHeight / sample > 900) sample *= 2
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
                }
            } catch (_: Exception) {
                null
            }
            withContext(Dispatchers.Main) { onReady(bmp) }
        }
    }

    val groomPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) decodeUri(uri) { groomPhoto = it }
    }
    val bridePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) decodeUri(uri) { bridePhoto = it }
    }
    val couplePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) decodeUri(uri) { couplePhoto = it }
    }

    fun renderCardBitmap(width: Int, height: Int): Bitmap {
        val sx = width / 1080f
        val sy = height / 1440f
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        // 1. Background gradient
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                activeTemplate.bgTop.toArgb(),
                activeTemplate.bgBottom.toArgb(),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Subtle ornamental frame + template style artwork
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = activeTemplate.borderColor.toArgb()
            style = Paint.Style.STROKE
            strokeWidth = 10f * sx
        }
        canvas.drawRoundRect(RectF(32f * sx, 32f * sy, width - 32f * sx, height - 32f * sy), 34f * sx, 34f * sx, borderPaint)

        val innerBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = activeTemplate.accentColor.copy(alpha = 0.65f).toArgb()
            style = Paint.Style.STROKE
            strokeWidth = 3f * sx
            pathEffect = DashPathEffect(floatArrayOf(16f * sx, 10f * sx), 0f)
        }
        canvas.drawRoundRect(RectF(52f * sx, 52f * sy, width - 52f * sx, height - 52f * sy), 24f * sx, 24f * sx, innerBorder)

        val cornerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = activeTemplate.accentColor.toArgb()
            style = Paint.Style.STROKE
            strokeWidth = 5f * sx
        }
        val cs = 72f * sx
        canvas.drawArc(RectF(32f * sx, 32f * sy, 32f * sx + cs, 32f * sy + cs), 0f, 90f, false, cornerPaint)
        canvas.drawArc(RectF(width - 32f * sx - cs, 32f * sy, width - 32f * sx, 32f * sy + cs), 90f, 90f, false, cornerPaint)
        canvas.drawArc(RectF(32f * sx, height - 32f * sy - cs, 32f * sx + cs, height - 32f * sy), 270f, 90f, false, cornerPaint)
        canvas.drawArc(RectF(width - 32f * sx - cs, height - 32f * sy - cs, width - 32f * sx, height - 32f * sy), 180f, 90f, false, cornerPaint)

        if (activeTemplate.style == WeddingDecorStyle.MANDAP_ARCH || activeTemplate.style == WeddingDecorStyle.TEMPLE_GOLD) {
            val archPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = activeTemplate.accentColor.copy(alpha = 0.25f).toArgb()
                style = Paint.Style.STROKE
                strokeWidth = 6f * sx
            }
            val arch = Path().apply {
                moveTo(95f * sx, 420f * sy)
                quadTo(width / 2f, 95f * sy, width - 95f * sx, 420f * sy)
            }
            canvas.drawPath(arch, archPaint)
        }

        // 3. Optional Floral Toran at top
        if (WeddingClipartType.FLORAL_TORAN in selectedCliparts) {
            val toran = WeddingClipartRenderer.createFloralToranBitmap((900 * sx).toInt().coerceAtLeast(100), (100 * sy).toInt().coerceAtLeast(30))
            canvas.drawBitmap(toran, (width - toran.width) / 2f, 46f * sy, null)
        }

        // 4. Ganpati Bappa or Mantra
        var cursorY = if (WeddingClipartType.FLORAL_TORAN in selectedCliparts) 145f * sy else 92f * sy
        if (WeddingClipartType.GANPATI_AUSPICIOUS in selectedCliparts) {
            val ganpati = WeddingClipartRenderer.createGanpatiBitmap(activeTemplate.accentColor.toArgb())
            val gw = 230f * sx
            val gh = 172f * sy
            canvas.drawBitmap(ganpati, null, RectF(width / 2f - gw / 2f, cursorY, width / 2f + gw / 2f, cursorY + gh), null)
            cursorY += gh + 18f * sy
        }

        val mantraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = activeTemplate.accentColor.toArgb()
            textSize = 36f * sx
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(topMantra, width / 2f, cursorY + 28f * sy, mantraPaint)
        cursorY += 58f * sy

        val eventPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = activeTemplate.textColor.toArgb()
            textSize = 26f * sx
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.14f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(eventHeader.uppercase(), width / 2f, cursorY + 24f * sy, eventPaint)
        cursorY += 54f * sy

        // 5. Dulha & Dulhan Photos / Couple Photo / Clipart
        val borderArgb = activeTemplate.accentColor.toArgb()
        val isCircle = photoShape == WeddingPhotoShape.CIRCLE
        val cornerR = if (photoShape == WeddingPhotoShape.ARCH) 44f * sx else 14f * sx

        when (photoMode) {
            WeddingPhotoMode.SEPARATE -> {
                if (groomPhoto != null || bridePhoto != null) {
                    val photoSize = 230f * sx
                    val leftX = width / 2f - photoSize - 38f * sx
                    val rightX = width / 2f + 38f * sx
                    val topY = cursorY
                    groomPhoto?.let {
                        WeddingClipartRenderer.drawCenterCroppedPhoto(
                            canvas, it, RectF(leftX, topY, leftX + photoSize, topY + photoSize),
                            isCircle, cornerR, borderArgb, 7f * sx
                        )
                    } ?: drawPhotoPlaceholder(canvas, RectF(leftX, topY, leftX + photoSize, topY + photoSize), "Dulha", borderArgb, isCircle, cornerR, sx)

                    bridePhoto?.let {
                        WeddingClipartRenderer.drawCenterCroppedPhoto(
                            canvas, it, RectF(rightX, topY, rightX + photoSize, topY + photoSize),
                            isCircle, cornerR, borderArgb, 7f * sx
                        )
                    } ?: drawPhotoPlaceholder(canvas, RectF(rightX, topY, rightX + photoSize, topY + photoSize), "Dulhan", borderArgb, isCircle, cornerR, sx)

                    val heartPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = activeTemplate.accentColor.toArgb()
                        textSize = 48f * sx
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText("❤", width / 2f, topY + photoSize / 2f + 16f * sy, heartPaint)
                    cursorY += photoSize + 24f * sy
                } else if (WeddingClipartType.DULHA_DULHAN_ROYAL in selectedCliparts) {
                    val dd = WeddingClipartRenderer.createDulhaDulhanBitmap(activeTemplate.accentColor.toArgb())
                    val dw = 380f * sx
                    val dh = 228f * sy
                    canvas.drawBitmap(dd, null, RectF(width / 2f - dw / 2f, cursorY, width / 2f + dw / 2f, cursorY + dh), null)
                    cursorY += dh + 14f * sy
                }
            }
            WeddingPhotoMode.COUPLE -> {
                if (couplePhoto != null) {
                    val cw = if (isCircle) 270f * sx else 360f * sx
                    val ch = 260f * sy
                    WeddingClipartRenderer.drawCenterCroppedPhoto(
                        canvas, couplePhoto!!, RectF(width / 2f - cw / 2f, cursorY, width / 2f + cw / 2f, cursorY + ch),
                        isCircle, cornerR, borderArgb, 7f * sx
                    )
                    cursorY += ch + 22f * sy
                } else if (WeddingClipartType.DULHA_DULHAN_ROYAL in selectedCliparts) {
                    val dd = WeddingClipartRenderer.createDulhaDulhanBitmap(activeTemplate.accentColor.toArgb())
                    val dw = 380f * sx
                    val dh = 228f * sy
                    canvas.drawBitmap(dd, null, RectF(width / 2f - dw / 2f, cursorY, width / 2f + dw / 2f, cursorY + dh), null)
                    cursorY += dh + 14f * sy
                }
            }
            WeddingPhotoMode.CLIPART_ONLY -> {
                if (WeddingClipartType.DULHA_DULHAN_ROYAL in selectedCliparts) {
                    val dd = WeddingClipartRenderer.createDulhaDulhanBitmap(activeTemplate.accentColor.toArgb())
                    val dw = 380f * sx
                    val dh = 228f * sy
                    canvas.drawBitmap(dd, null, RectF(width / 2f - dw / 2f, cursorY, width / 2f + dw / 2f, cursorY + dh), null)
                    cursorY += dh + 14f * sy
                }
            }
        }

        // If user also picked Rings or Dulha-Dulhan clipart alongside photos, draw small rings badge
        if (WeddingClipartType.ROYAL_RINGS in selectedCliparts) {
            val rings = WeddingClipartRenderer.createRingsMotifBitmap(activeTemplate.accentColor.toArgb())
            val rw = 160f * sx
            val rh = 86f * sy
            canvas.drawBitmap(rings, null, RectF(width / 2f - rw / 2f, cursorY - 6f * sy, width / 2f + rw / 2f, cursorY + rh - 6f * sy), null)
            cursorY += rh + 8f * sy
        }

        // 6. Blessings + Couple Names
        val parentsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = activeTemplate.textColor.copy(alpha = 0.85f).toArgb()
            textSize = 27f * sx
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(parentsLine.take(54), width / 2f, cursorY + 26f * sy, parentsPaint)
        cursorY += 74f * sy

        val namesPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = activeTemplate.textColor.toArgb()
            textSize = 72f * sx
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        canvas.drawText("$groomName  &  $brideName", width / 2f, cursorY + 24f * sy, namesPaint)
        cursorY += 76f * sy

        // Divider line
        val divPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = activeTemplate.accentColor.toArgb()
            strokeWidth = 3f * sx
        }
        canvas.drawLine(width * 0.25f, cursorY, width * 0.75f, cursorY, divPaint)
        cursorY += 54f * sy

        // 7. Date, Time, Venue, RSVP
        val detailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = activeTemplate.textColor.toArgb()
            textSize = 33f * sx
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("$dateText  •  $timeText", width / 2f, cursorY, detailPaint)
        cursorY += 52f * sy

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = activeTemplate.textColor.copy(alpha = 0.92f).toArgb()
            textSize = 30f * sx
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(venueText.take(50), width / 2f, cursorY, subPaint)
        cursorY += 48f * sy

        val rsvpPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = activeTemplate.accentColor.toArgb()
            textSize = 26f * sx
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("RSVP: ${rsvpText.take(50)}", width / 2f, cursorY, rsvpPaint)

        // 8. Shehnai & Kalash bottom clipart
        if (WeddingClipartType.SHEHNAI_KALASH in selectedCliparts) {
            val sk = WeddingClipartRenderer.createShehnaiKalashBitmap(activeTemplate.accentColor.toArgb())
            val sw = 320f * sx
            val sh = 138f * sy
            val skTop = (height - sh - 76f * sy).coerceAtLeast(cursorY + 18f * sy)
            if (skTop + sh < height - 44f * sy) {
                canvas.drawBitmap(sk, null, RectF(width / 2f - sw / 2f, skTop, width / 2f + sw / 2f, skTop + sh), null)
            }
        }

        if (hasWatermark) {
            val wmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = activeTemplate.textColor.copy(alpha = 0.65f).toArgb()
                textSize = 24f * sx
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Created with OmniTools Studio", width / 2f, height - 48f * sy, wmPaint)
        }
        return bmp
    }

    val previewBitmap = remember(
        eventHeader, topMantra, parentsLine, groomName, brideName, dateText, timeText,
        venueText, rsvpText, templateIndex, photoMode, photoShape,
        groomPhoto, bridePhoto, couplePhoto, selectedCliparts, hasWatermark
    ) {
        renderCardBitmap(720, 960)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Wedding Card Studio", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Dulha-Dulhan Photos, Cliparts & Royal Designs", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        bitmap = previewBitmap.asImageBitmap(),
                        contentDescription = "Live Wedding Card Preview",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 4f)
                    )
                }
            }

            item {
                Text("1. Choose Card Design", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(templates) { idx, t ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = t.bgTop,
                            border = BorderStroke(if (idx == templateIndex) 3.dp else 1.dp, t.borderColor),
                            modifier = Modifier
                                .clickable { templateIndex = idx }
                                .testTag("wedding_theme_$idx")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (idx == templateIndex) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = t.accentColor, modifier = Modifier.size(16.dp))
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
                        Text("2. Dulha & Dulhan Photos", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemsIndexed(WeddingPhotoMode.entries) { _, mode ->
                                FilterChip(
                                    selected = photoMode == mode,
                                    onClick = { photoMode = mode },
                                    label = { Text(mode.label, fontSize = 12.sp) }
                                )
                            }
                        }
                        if (photoMode != WeddingPhotoMode.CLIPART_ONLY) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("Frame:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                WeddingPhotoShape.entries.forEach { s ->
                                    FilterChip(
                                        selected = photoShape == s,
                                        onClick = { photoShape = s },
                                        label = { Text(s.label, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                        when (photoMode) {
                            WeddingPhotoMode.SEPARATE -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    PhotoPickButton(
                                        label = if (groomPhoto == null) "Add Dulha Photo" else "Change Dulha",
                                        hasPhoto = groomPhoto != null,
                                        onPick = { groomPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                        onClear = { groomPhoto = null },
                                        modifier = Modifier.weight(1f).testTag("pick_groom_photo")
                                    )
                                    PhotoPickButton(
                                        label = if (bridePhoto == null) "Add Dulhan Photo" else "Change Dulhan",
                                        hasPhoto = bridePhoto != null,
                                        onPick = { bridePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                        onClear = { bridePhoto = null },
                                        modifier = Modifier.weight(1f).testTag("pick_bride_photo")
                                    )
                                }
                            }
                            WeddingPhotoMode.COUPLE -> {
                                PhotoPickButton(
                                    label = if (couplePhoto == null) "Upload Couple Photo" else "Change Couple Photo",
                                    hasPhoto = couplePhoto != null,
                                    onPick = { couplePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                    onClear = { couplePhoto = null },
                                    modifier = Modifier.fillMaxWidth().testTag("pick_couple_photo")
                                )
                            }
                            WeddingPhotoMode.CLIPART_ONLY -> {
                                Text(
                                    "Using traditional Dulha-Dulhan illustration clipart.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("3. Wedding Cliparts (Tap to Toggle)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            WeddingClipartType.entries.forEach { clip ->
                                val active = clip in selectedCliparts
                                FilterChip(
                                    selected = active,
                                    onClick = {
                                        selectedCliparts = if (active) selectedCliparts - clip else selectedCliparts + clip
                                    },
                                    leadingIcon = if (active) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    label = { Text(clip.title, fontSize = 12.sp) },
                                    modifier = Modifier.testTag("clipart_${clip.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("4. Event & Couple Details", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val events = listOf("SHUBH VIVAH", "ENGAGEMENT", "RECEPTION", "HALDI & MEHENDI", "NIKAH INVITATION")
                        itemsIndexed(events) { _, ev ->
                            FilterChip(
                                selected = eventHeader == ev,
                                onClick = { eventHeader = ev },
                                label = { Text(ev, fontSize = 11.sp) }
                            )
                        }
                    }
                    OutlinedTextField(value = topMantra, onValueChange = { topMantra = it }, label = { Text("Top Blessing / Mantra") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = parentsLine, onValueChange = { parentsLine = it }, label = { Text("Parents / Family Blessings") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = groomName, onValueChange = { groomName = it }, label = { Text("Dulha (Groom)") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = brideName, onValueChange = { brideName = it }, label = { Text("Dulhan (Bride)") }, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = dateText, onValueChange = { dateText = it }, label = { Text("Wedding Date") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = timeText, onValueChange = { timeText = it }, label = { Text("Time") }, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(value = venueText, onValueChange = { venueText = it }, label = { Text("Venue Address") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = rsvpText, onValueChange = { rsvpText = it }, label = { Text("RSVP / Contact") }, modifier = Modifier.fillMaxWidth())
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
                        onClick = { ImageExportUtils.saveBitmapToGallery(context, renderCardBitmap(1080, 1440), "WeddingCard") },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("save_wedding_card"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save HD Card", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { ImageExportUtils.shareBitmap(context, renderCardBitmap(1080, 1440), "Wedding Invitation") },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("share_wedding_card"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Card", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoPickButton(
    label: String,
    hasPhoto: Boolean,
    onPick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(
            onClick = onPick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
        ) {
            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, fontSize = 11.5.sp, maxLines = 1)
        }
        if (hasPhoto) {
            IconButton(onClick = onClear) {
                Icon(Icons.Default.Close, contentDescription = "Clear Photo", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun drawPhotoPlaceholder(
    canvas: Canvas,
    dst: RectF,
    label: String,
    borderColor: Int,
    isCircle: Boolean,
    cornerRadius: Float,
    sx: Float
) {
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(40, 255, 255, 255)
        style = Paint.Style.FILL
    }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = borderColor
        style = Paint.Style.STROKE
        strokeWidth = 5f * sx
        pathEffect = DashPathEffect(floatArrayOf(12f * sx, 8f * sx), 0f)
    }
    if (isCircle) {
        val r = minOf(dst.width(), dst.height()) / 2f
        canvas.drawCircle(dst.centerX(), dst.centerY(), r, fillPaint)
        canvas.drawCircle(dst.centerX(), dst.centerY(), r, strokePaint)
    } else {
        canvas.drawRoundRect(dst, cornerRadius, cornerRadius, fillPaint)
        canvas.drawRoundRect(dst, cornerRadius, cornerRadius, strokePaint)
    }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = borderColor
        textSize = 26f * sx
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    canvas.drawText(label, dst.centerX(), dst.centerY() + 8f * sx, textPaint)
}
