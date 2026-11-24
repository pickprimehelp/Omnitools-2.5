package com.example.ui.tools.daily

import android.graphics.Bitmap
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.TopBannerAd
import com.example.ui.components.WatermarkExportBar
import com.example.util.BarcodeQrUtils
import com.example.util.ImageExportUtils
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrCodeGeneratorScreen(
    initialIsBarcode: Boolean = false,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("https://play.google.com") }
    var isBarcode by remember { mutableStateOf(initialIsBarcode) }
    var qrColorIndex by remember { mutableIntStateOf(0) }
    var hasWatermark by remember { mutableStateOf(true) }

    val qrColors = listOf(Color.Black, Color(0xFF4338CA), Color(0xFF047857), Color(0xFFB91C1C))
    val qrTemplates = listOf(
        "URL" to "https://example.com",
        "UPI Pay" to "upi://pay?pa=name@upi&pn=Merchant&cu=INR",
        "WiFi" to "WIFI:T:WPA;S:MyNetwork;P:password123;;",
        "Phone" to "tel:+919876543210"
    )

    val bitmap: Bitmap? = remember(inputText, isBarcode, qrColorIndex) {
        if (isBarcode) {
            BarcodeQrUtils.generateBarcodeBitmap(inputText)
        } else {
            BarcodeQrUtils.generateQrBitmap(inputText, foregroundColor = qrColors[qrColorIndex].toArgb())
        }
    }
    val displayBitmap: Bitmap? = remember(bitmap, hasWatermark) {
        bitmap?.let { if (hasWatermark) ImageExportUtils.applyFooterWatermark(it) else it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR & Barcode Studio", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !isBarcode, onClick = { isBarcode = false }, label = { Text("QR Code") })
                    FilterChip(selected = isBarcode, onClick = { isBarcode = true }, label = { Text("Barcode (Code 128)") })
                }
            }
            if (!isBarcode) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(qrTemplates) { (label, preset) ->
                            FilterChip(selected = inputText == preset, onClick = { inputText = preset }, label = { Text(label, fontSize = 11.sp) })
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("QR Color:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        qrColors.forEachIndexed { idx, c ->
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(
                                        if (idx == qrColorIndex) 3.dp else 1.dp,
                                        if (idx == qrColorIndex) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        CircleShape
                                    )
                                    .clickable { qrColorIndex = idx }
                            )
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Enter URL, UPI, WiFi, Phone or Text") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (displayBitmap != null) {
                        Image(
                            bitmap = displayBitmap.asImageBitmap(),
                            contentDescription = if (isBarcode) "Generated Barcode" else "Generated QR Code",
                            modifier = Modifier.size(if (isBarcode) 260.dp else 220.dp)
                        )
                    } else {
                        Text("Enter text to generate code", color = Color.Gray)
                    }
                }
            }
            if (bitmap != null) {
                item {
                    WatermarkExportBar(
                        hasWatermark = hasWatermark,
                        onWatermarkUnlocked = { hasWatermark = false },
                        onExportSave = { includeWm ->
                            val outBmp = if (includeWm) ImageExportUtils.applyFooterWatermark(bitmap) else bitmap
                            ImageExportUtils.saveBitmapToGallery(context, outBmp, "OmniTools_Code")
                        },
                        onExportShare = { includeWm ->
                            val outBmp = if (includeWm) ImageExportUtils.applyFooterWatermark(bitmap) else bitmap
                            ImageExportUtils.shareBitmap(context, outBmp, "OmniTools QR Code")
                        },
                        saveLabel = "Save",
                        shareLabel = "Share",
                        saveTestTag = "save_qr_code",
                        shareTestTag = "share_qr_code"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgeCalculatorScreen(
    initialMode: Int = 0, // 0 = Age & Next Birthday, 1 = Date Difference
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    var activeTab by remember { mutableIntStateOf(initialMode) }

    var birthYear by remember { mutableStateOf("2000") }
    var birthMonth by remember { mutableStateOf("1") }
    var birthDay by remember { mutableStateOf("15") }

    var endYear by remember { mutableStateOf("2026") }
    var endMonth by remember { mutableStateOf("12") }
    var endDay by remember { mutableStateOf("31") }

    val now = Calendar.getInstance()
    val y = (birthYear.toIntOrNull() ?: 2000).coerceIn(1900, 2100)
    val m = (birthMonth.toIntOrNull() ?: 1).coerceIn(1, 12)
    val d = (birthDay.toIntOrNull() ?: 1).coerceIn(1, 31)
    val dob = Calendar.getInstance().apply { set(y, m - 1, d, 0, 0, 0) }

    var years = now.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
    var months = now.get(Calendar.MONTH) - dob.get(Calendar.MONTH)
    var days = now.get(Calendar.DAY_OF_MONTH) - dob.get(Calendar.DAY_OF_MONTH)
    if (days < 0) { months--; days += 30 }
    if (months < 0) { years--; months += 12 }
    val totalDays = ((now.timeInMillis - dob.timeInMillis) / (1000L * 60 * 60 * 24)).coerceAtLeast(0L)

    val nextBday = Calendar.getInstance().apply {
        set(now.get(Calendar.YEAR), m - 1, d, 0, 0, 0)
        if (before(now)) add(Calendar.YEAR, 1)
    }
    val daysUntilBirthday = ((nextBday.timeInMillis - now.timeInMillis) / (1000L * 60 * 60 * 24)).coerceAtLeast(0L)

    val ey = (endYear.toIntOrNull() ?: 2026).coerceIn(1900, 2100)
    val em = (endMonth.toIntOrNull() ?: 12).coerceIn(1, 12)
    val ed = (endDay.toIntOrNull() ?: 31).coerceIn(1, 31)
    val endCal = Calendar.getInstance().apply { set(ey, em - 1, ed, 0, 0, 0) }
    val diffDays = abs((endCal.timeInMillis - dob.timeInMillis) / (1000L * 60 * 60 * 24))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (activeTab == 0) "Age & Birthday Countdown" else "Date Difference Calculator",
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
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = activeTab == 0, onClick = { activeTab = 0 }, label = { Text("Age Calculator") })
                    FilterChip(selected = activeTab == 1, onClick = { activeTab = 1 }, label = { Text("Date Difference") })
                }
            }
            item {
                Text(if (activeTab == 0) "Date of Birth" else "Start Date", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = birthDay,
                        onValueChange = { birthDay = it.filter { c -> c.isDigit() }.take(2) },
                        label = { Text("Day") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = birthMonth,
                        onValueChange = { birthMonth = it.filter { c -> c.isDigit() }.take(2) },
                        label = { Text("Month") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = birthYear,
                        onValueChange = { birthYear = it.filter { c -> c.isDigit() }.take(4) },
                        label = { Text("Year") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1.3f)
                    )
                }
            }
            if (activeTab == 1) {
                item {
                    Text("End Date", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = endDay,
                            onValueChange = { endDay = it.filter { c -> c.isDigit() }.take(2) },
                            label = { Text("Day") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endMonth,
                            onValueChange = { endMonth = it.filter { c -> c.isDigit() }.take(2) },
                            label = { Text("Month") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endYear,
                            onValueChange = { endYear = it.filter { c -> c.isDigit() }.take(4) },
                            label = { Text("Year") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1.3f)
                        )
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (activeTab == 0) {
                            Text(
                                text = "${years.coerceAtLeast(0)} Years, $months Months, $days Days",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("Next Birthday in: $daysUntilBirthday days 🎂", fontWeight = FontWeight.SemiBold)
                            Text("Total Days Lived: $totalDays days (${totalDays * 24} hrs)")
                        } else {
                            Text(
                                text = "$diffDays Total Days",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("Weeks: ${diffDays / 7} weeks & ${diffDays % 7} days")
                            Text("Approx Months: ${String.format(Locale.US, "%.1f", diffDays / 30.4375)} months")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BmiCalculatorScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    var weightKg by remember { mutableStateOf("68") }
    var heightCm by remember { mutableStateOf("172") }

    val w = weightKg.toDoubleOrNull() ?: 0.0
    val hMeters = (heightCm.toDoubleOrNull() ?: 0.0) / 100.0
    val bmi = if (hMeters > 0.0) w / (hMeters * hMeters) else 0.0
    val idealMin = 18.5 * hMeters * hMeters
    val idealMax = 24.9 * hMeters * hMeters
    val (category, statusColor) = when {
        bmi <= 0.0 -> "Enter valid height & weight" to Color.Gray
        bmi < 18.5 -> "Underweight" to Color(0xFF0284C7)
        bmi < 25.0 -> "Normal & Healthy" to Color(0xFF10B981)
        bmi < 30.0 -> "Overweight" to Color(0xFFF59E0B)
        else -> "Obese" to Color(0xFFEF4444)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BMI Health Calculator", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = weightKg,
                        onValueChange = { weightKg = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = heightCm,
                        onValueChange = { heightCm = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Height (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("BMI: ${String.format(Locale.US, "%.1f", bmi)}", fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = statusColor)
                        Text("Status: $category", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = statusColor)
                        LinearProgressIndicator(
                            progress = { ((bmi.toFloat() - 12f) / 28f).coerceIn(0f, 1f) },
                            color = statusColor,
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp))
                        )
                        if (hMeters > 0.0) {
                            Text(
                                "Healthy Weight Range: ${String.format(Locale.US, "%.1f", idealMin)} – ${String.format(Locale.US, "%.1f", idealMax)} kg",
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchAndTimerScreen(
    initialIsTimer: Boolean = false,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    var isTimerMode by remember { mutableStateOf(initialIsTimer) }

    // Stopwatch
    var elapsedMs by remember { mutableLongStateOf(0L) }
    var swRunning by remember { mutableStateOf(false) }
    val laps = remember { mutableStateListOf<String>() }

    // Countdown Timer
    var timerTotalSec by remember { mutableLongStateOf(300L) }
    var timerLeftSec by remember { mutableLongStateOf(300L) }
    var timerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(swRunning) {
        var lastTick = System.currentTimeMillis()
        while (swRunning) {
            delay(50L)
            val now = System.currentTimeMillis()
            elapsedMs += (now - lastTick)
            lastTick = now
        }
    }

    LaunchedEffect(timerRunning) {
        while (timerRunning && timerLeftSec > 0L) {
            delay(1000L)
            timerLeftSec--
            if (timerLeftSec == 0L) {
                timerRunning = false
                try {
                    ToneGenerator(AudioManager.STREAM_ALARM, 85).startTone(ToneGenerator.TONE_PROP_BEEP2, 500)
                } catch (_: Throwable) {
                }
            }
        }
    }

    fun formatMs(ms: Long): String {
        val mins = ms / 60000L
        val secs = (ms % 60000L) / 1000L
        val hundredths = (ms % 1000L) / 10L
        return String.format(Locale.US, "%02d:%02d.%02d", mins, secs, hundredths)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isTimerMode) "Countdown Timer" else "Precision Stopwatch", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !isTimerMode, onClick = { isTimerMode = false }, label = { Text("Stopwatch") })
                    FilterChip(selected = isTimerMode, onClick = { isTimerMode = true }, label = { Text("Countdown Timer") })
                }
            }
            if (!isTimerMode) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = formatMs(elapsedMs), fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(onClick = { swRunning = !swRunning }) { Text(if (swRunning) "Pause" else "Start") }
                                OutlinedButton(
                                    onClick = { if (elapsedMs > 0L) laps.add(0, "Lap ${laps.size + 1}: ${formatMs(elapsedMs)}") },
                                    enabled = elapsedMs > 0L
                                ) { Text("Lap") }
                                OutlinedButton(onClick = { swRunning = false; elapsedMs = 0L; laps.clear() }) { Text("Reset") }
                            }
                        }
                    }
                }
                items(laps) { lap ->
                    Card(modifier = Modifier.fillMaxWidth()) { Text(lap, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Medium) }
                }
            } else {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("1m" to 60L, "5m" to 300L, "10m" to 600L, "25m" to 1500L).forEach { (label, secs) ->
                            item {
                                FilterChip(
                                    selected = timerTotalSec == secs,
                                    onClick = { timerRunning = false; timerTotalSec = secs; timerLeftSec = secs },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            val mm = timerLeftSec / 60
                            val ss = timerLeftSec % 60
                            Text(
                                text = String.format(Locale.US, "%02d:%02d", mm, ss),
                                fontSize = 46.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { if (timerTotalSec > 0) timerLeftSec.toFloat() / timerTotalSec else 0f },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(onClick = { timerRunning = !timerRunning }, enabled = timerLeftSec > 0) {
                                    Text(if (timerRunning) "Pause" else "Start Timer")
                                }
                                OutlinedButton(onClick = { timerRunning = false; timerLeftSec = timerTotalSec }) { Text("Reset") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    initialMode: Int = 0, // 0 = Units, 1 = Currency, 2 = World Time Zones
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    var activeTab by remember { mutableIntStateOf(initialMode) }
    var valueText by remember { mutableStateOf("10") }
    var selectedUnitIdx by remember { mutableIntStateOf(0) }
    val unitTypes = listOf("Length (m → ft)", "Weight (kg → lb)", "Temp (°C → °F)", "Speed (km/h → mph)")

    val v = valueText.toDoubleOrNull() ?: 0.0
    val unitResult = when (selectedUnitIdx) {
        0 -> "${String.format(Locale.US, "%.2f", v * 3.28084)} ft"
        1 -> "${String.format(Locale.US, "%.2f", v * 2.20462)} lb"
        2 -> "${String.format(Locale.US, "%.2f", (v * 9.0 / 5.0) + 32.0)} °F"
        else -> "${String.format(Locale.US, "%.2f", v * 0.621371)} mph"
    }

    val usdAmount = valueText.toDoubleOrNull() ?: 1.0
    val currencies = listOf(
        "INR (₹)" to usdAmount * 84.5,
        "EUR (€)" to usdAmount * 0.92,
        "GBP (£)" to usdAmount * 0.79,
        "AED (د.إ)" to usdAmount * 3.67,
        "JPY (¥)" to usdAmount * 154.0,
        "CAD ($)" to usdAmount * 1.40
    )

    val zones = listOf(
        "IST (India)" to "Asia/Kolkata",
        "UTC (Universal)" to "UTC",
        "EST (New York)" to "America/New_York",
        "PST (Los Angeles)" to "America/Los_Angeles",
        "GMT (London)" to "Europe/London",
        "GST (Dubai)" to "Asia/Dubai",
        "JST (Tokyo)" to "Asia/Tokyo"
    )
    val nowDate = remember { Date() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (activeTab) {
                            1 -> "Multi-Currency Converter"
                            2 -> "World Time Zone Clock"
                            else -> "Universal Unit Converter"
                        },
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
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = activeTab == 0, onClick = { activeTab = 0 }, label = { Text("Units") })
                    FilterChip(selected = activeTab == 1, onClick = { activeTab = 1 }, label = { Text("Currency") })
                    FilterChip(selected = activeTab == 2, onClick = { activeTab = 2 }, label = { Text("Time Zones") })
                }
            }
            when (activeTab) {
                0 -> {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            unitTypes.forEachIndexed { idx, label ->
                                FilterChip(selected = selectedUnitIdx == idx, onClick = { selectedUnitIdx = idx }, label = { Text(label) })
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = valueText,
                            onValueChange = { valueText = it.filter { c -> c.isDigit() || c == '.' || c == '-' } },
                            label = { Text("Input Value") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text("Converted Output", fontSize = 13.sp)
                                Text(text = unitResult, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                1 -> {
                    item {
                        OutlinedTextField(
                            value = valueText,
                            onValueChange = { valueText = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Amount in USD ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    items(currencies) { (code, converted) ->
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(code, fontWeight = FontWeight.Bold)
                                Text(String.format(Locale.US, "%.2f", converted), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                else -> {
                    items(zones) { (label, tzId) ->
                        val fmt = SimpleDateFormat("hh:mm a • dd MMM", Locale.US).apply { timeZone = TimeZone.getTimeZone(tzId) }
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(label, fontWeight = FontWeight.Bold)
                                Text(fmt.format(nowDate), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
