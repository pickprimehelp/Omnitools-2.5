package com.example.ui.tools.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.TopBannerAd
import com.example.util.ImageExportUtils
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextToPdfScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    val context = LocalContext.current

    var docTitle by remember { mutableStateOf("My Document Notes") }
    var bodyText by remember {
        mutableStateOf(
            "Write or paste any notes, essay, resume draft, or letter here.\n\n" +
                "This tool automatically wraps lines across crisp A4 PDF pages ready to open or share."
        )
    }
    var fontSize by remember { mutableFloatStateOf(13f) }
    var isCenterAligned by remember { mutableStateOf(false) }
    var generatedPdf by remember { mutableStateOf<File?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Text → PDF Maker", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Real A4 Multi-Page PDF Document Export", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = docTitle,
                    onValueChange = { docTitle = it },
                    label = { Text("Document Title / Header") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(11f, 13f, 16f, 18f).forEach { sz ->
                        item {
                            FilterChip(
                                selected = fontSize == sz,
                                onClick = { fontSize = sz },
                                label = { Text("${sz.toInt()}pt") }
                            )
                        }
                    }
                    item {
                        FilterChip(
                            selected = !isCenterAligned,
                            onClick = { isCenterAligned = false },
                            label = { Text("Left Align") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = isCenterAligned,
                            onClick = { isCenterAligned = true },
                            label = { Text("Centered") }
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = bodyText,
                    onValueChange = { bodyText = it },
                    label = { Text("Document Content") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )
            }
            item {
                Button(
                    onClick = {
                        if (bodyText.isBlank() && docTitle.isBlank()) {
                            Toast.makeText(context, "Enter some text first", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val out = buildTextPdf(context, docTitle, bodyText, fontSize, isCenterAligned)
                        if (out != null) {
                            generatedPdf = out
                            Toast.makeText(context, "PDF Created!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate A4 PDF", fontWeight = FontWeight.Bold)
                }
            }
            generatedPdf?.let { pdf ->
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${pdf.name} Ready (${pdf.length() / 1024} KB)", fontWeight = FontWeight.Bold)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { ImageExportUtils.sharePdfFile(context, pdf, "Share PDF") },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share PDF")
                                }
                                OutlinedButton(
                                    onClick = { ImageExportUtils.openPdfFile(context, pdf) },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Open PDF")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun buildTextPdf(
    context: Context,
    title: String,
    body: String,
    fontSize: Float,
    centerAligned: Boolean
): File? {
    return try {
        val doc = PdfDocument()
        val pageW = 595
        val pageH = 842
        val margin = 42f
        val maxLineW = pageW - margin * 2

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(30, 41, 59)
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = if (centerAligned) Paint.Align.CENTER else Paint.Align.LEFT
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(30, 41, 59)
            textSize = fontSize
            textAlign = if (centerAligned) Paint.Align.CENTER else Paint.Align.LEFT
        }

        val wrappedLines = mutableListOf<String>()
        body.split("\n").forEach { para ->
            if (para.isBlank()) {
                wrappedLines.add("")
            } else {
                val words = para.split(Regex("\\s+"))
                var current = StringBuilder()
                for (w in words) {
                    val candidate = if (current.isEmpty()) w else "$current $w"
                    if (bodyPaint.measureText(candidate) <= maxLineW) {
                        current = StringBuilder(candidate)
                    } else {
                        if (current.isNotEmpty()) wrappedLines.add(current.toString())
                        current = StringBuilder(w)
                    }
                }
                if (current.isNotEmpty()) wrappedLines.add(current.toString())
            }
        }

        var pageNum = 1
        var lineIdx = 0
        while (lineIdx < wrappedLines.size || pageNum == 1) {
            val page = doc.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, pageNum).create())
            val c: Canvas = page.canvas
            var y = margin + 22f
            val anchorX = if (centerAligned) pageW / 2f else margin

            if (pageNum == 1 && title.isNotBlank()) {
                c.drawText(title, anchorX, y, titlePaint)
                y += 14f
                c.drawLine(margin, y, pageW - margin, y, Paint().apply {
                    color = android.graphics.Color.rgb(203, 213, 225)
                    strokeWidth = 1.5f
                })
                y += 24f
            }

            val lineHeight = fontSize * 1.48f
            while (lineIdx < wrappedLines.size && y < pageH - margin) {
                c.drawText(wrappedLines[lineIdx], anchorX, y, bodyPaint)
                y += lineHeight
                lineIdx++
            }
            doc.finishPage(page)
            if (lineIdx >= wrappedLines.size) break
            pageNum++
        }

        val outDir = File(context.getExternalFilesDir(null), "PDF_Documents").apply { mkdirs() }
        val safeTitle = title.ifBlank { "Document" }.replace(Regex("[^A-Za-z0-9_-]"), "_").take(24)
        val file = File(outDir, "${safeTitle}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        file
    } catch (e: Exception) {
        Toast.makeText(context, "Error creating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordAndCharacterCounterScreen(
    isCharacterFocus: Boolean = false,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var textInput by remember { mutableStateOf("") }

    val charCount = textInput.length
    val charNoSpaceCount = textInput.replace(" ", "").replace("\n", "").length
    val words = textInput.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    val wordCount = if (textInput.isBlank()) 0 else words.size
    val sentenceCount = if (textInput.isBlank()) 0 else textInput.split(Regex("[.!?]+\\s*")).filter { it.isNotBlank() }.size
    val paragraphCount = if (textInput.isBlank()) 0 else textInput.split(Regex("\n+")).filter { it.isNotBlank() }.size
    val readingTimeMinutes = if (wordCount == 0) 0 else maxOf(1, (wordCount / 200.0).toInt())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isCharacterFocus) "Character & Limit Counter" else "Word & Character Counter",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
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
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CountStatCard("Words", "$wordCount", Modifier.weight(1f))
                    CountStatCard("Characters", "$charCount", Modifier.weight(1f))
                    CountStatCard("No Spaces", "$charNoSpaceCount", Modifier.weight(1f))
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CountStatCard("Sentences", "$sentenceCount", Modifier.weight(1f))
                    CountStatCard("Paragraphs", "$paragraphCount", Modifier.weight(1f))
                    CountStatCard("Read Time", "${readingTimeMinutes}m", Modifier.weight(1f))
                }
            }
            item {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    label = { Text("Type or paste your text here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Platform Character Limits", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        LimitBar("SEO Meta Title", charCount, 60)
                        LimitBar("YouTube Video Title", charCount, 100)
                        LimitBar("SMS / Meta Description", charCount, 160)
                        LimitBar("X (Twitter) Post", charCount, 280)
                    }
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { ImageExportUtils.copyToClipboard(context, textInput, "Text Stats") },
                        enabled = textInput.isNotBlank(),
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Text")
                    }
                    OutlinedButton(
                        onClick = { textInput = "" },
                        enabled = textInput.isNotBlank(),
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear")
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun LimitBar(label: String, current: Int, max: Int) {
    val ratio = (current.toFloat() / max.toFloat()).coerceIn(0f, 1f)
    val over = current > max
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 12.sp)
            Text(
                "$current / $max",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
        LinearProgressIndicator(
            progress = { ratio },
            color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
        )
    }
}

@Composable
private fun CountStatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseConverterScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }

    fun toTitleCase(str: String) = str.split(" ").joinToString(" ") { w ->
        w.lowercase(Locale.getDefault()).replaceFirstChar { it.titlecase(Locale.getDefault()) }
    }
    fun toSentenceCase(str: String) = str.lowercase(Locale.getDefault()).replaceFirstChar { it.titlecase(Locale.getDefault()) }
    fun toCamelCase(str: String): String {
        val words = str.trim().split(Regex("[\\s_\\-]+"))
        return words.mapIndexed { index, w ->
            if (index == 0) w.lowercase(Locale.getDefault())
            else w.lowercase(Locale.getDefault()).replaceFirstChar { it.titlecase(Locale.getDefault()) }
        }.joinToString("")
    }
    fun toSnakeCase(str: String) = str.trim().split(Regex("[\\s_\\-]+")).joinToString("_") { it.lowercase(Locale.getDefault()) }
    fun toKebabCase(str: String) = str.trim().split(Regex("[\\s_\\-]+")).joinToString("-") { it.lowercase(Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Case Converter Pro", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Enter your text to convert...") },
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    shape = RoundedCornerShape(16.dp)
                )
            }
            if (inputText.isNotBlank()) {
                val conversions = listOf(
                    "UPPERCASE" to inputText.uppercase(Locale.getDefault()),
                    "lowercase" to inputText.lowercase(Locale.getDefault()),
                    "Sentence case" to toSentenceCase(inputText),
                    "Title Case" to toTitleCase(inputText),
                    "camelCase" to toCamelCase(inputText),
                    "snake_case" to toSnakeCase(inputText),
                    "kebab-case" to toKebabCase(inputText)
                )
                items(conversions.size) { i ->
                    val (label, converted) = conversions[i]
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = converted, fontSize = 15.sp, maxLines = 3)
                            }
                            Row {
                                IconButton(onClick = { ImageExportUtils.copyToClipboard(context, converted, label) }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { ImageExportUtils.shareText(context, converted, "Converted Text - $label") }) {
                                    Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
