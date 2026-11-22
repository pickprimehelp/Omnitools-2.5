package com.example.ui.tools.creator

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.TopBannerAd
import com.example.util.ImageExportUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouTubeTitleGeneratorScreen(
    initialMode: Int = 0, // 0=YT Title, 1=Shorts Title, 2=SEO Description, 3=IG Caption
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var activeMode by remember { mutableIntStateOf(initialMode) }
    var topic by remember { mutableStateOf("") }
    var niche by remember { mutableStateOf("Tech & Tutorials") }
    var channelHandle by remember { mutableStateOf("@YourChannel") }
    val niches = listOf("Tech & Tutorials", "Vlogs", "Gaming", "Finance", "Motivation")

    val cleanTopic = topic.trim().ifBlank { "Android Productivity" }
    val slug = cleanTopic.lowercase(Locale.getDefault()).replace(Regex("[^a-z0-9]"), "").ifBlank { "trending" }

    val outputs = remember(cleanTopic, niche, channelHandle, activeMode) {
        when (activeMode) {
            1 -> listOf(
                "$cleanTopic Hack You Missed! 🔥",
                "Stop Doing $cleanTopic Wrong! ❌",
                "60s $cleanTopic Secret ⚡",
                "Best $cleanTopic Trick in 2026 🤯",
                "$cleanTopic: Watch Before Deleted! 👀",
                "How Pros Do $cleanTopic 🚀"
            )
            2 -> listOf(
                buildString {
                    appendLine("Learn everything about $cleanTopic in this complete step-by-step $niche guide!")
                    appendLine()
                    appendLine("⏱️ TIMESTAMPS:")
                    appendLine("00:00 - Intro & Why $cleanTopic Matters")
                    appendLine("01:30 - Key Setup & Pro Settings")
                    appendLine("04:15 - Best Tips & Mistakes to Avoid")
                    appendLine("07:40 - Final Results & Summary")
                    appendLine()
                    appendLine("📌 Connect with $channelHandle")
                    appendLine("#$slug #${slug}guide #trending #tutorial")
                }
            )
            3 -> listOf(
                "Mastering $cleanTopic one step at a time ✨\nWhich tip helped you most? Comment below! 👇\n\n#$slug #reelsindia #creator",
                "POV: You finally cracked the $cleanTopic workflow 🚀\nSave this post before you forget! 🔖\n\nFollow $channelHandle for daily tips.",
                "Real progress happens quietly. Keep building with $cleanTopic 💡🔥\n\n#$slug #motivation #aesthetic"
            )
            else -> listOf(
                "How to Master $cleanTopic in 2026 (Step-by-Step Guide)",
                "7 $cleanTopic Mistakes Beginners Make & How to Fix Them",
                "I Tested $cleanTopic for 30 Days — Honest Results!",
                "The Ultimate $cleanTopic Blueprint for $niche",
                "Why Everyone Is Switching to $cleanTopic Right Now",
                "Complete $cleanTopic Tutorial: Beginner to Pro"
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (activeMode) {
                            1 -> "Shorts Title Generator"
                            2 -> "YouTube SEO Description"
                            3 -> "Instagram Caption Pro"
                            else -> "YouTube Title Generator"
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(selected = activeMode == 0, onClick = { activeMode = 0 }, label = { Text("YT Titles") }) }
                    item { FilterChip(selected = activeMode == 1, onClick = { activeMode = 1 }, label = { Text("Shorts") }) }
                    item { FilterChip(selected = activeMode == 2, onClick = { activeMode = 2 }, label = { Text("SEO Description") }) }
                    item { FilterChip(selected = activeMode == 3, onClick = { activeMode = 3 }, label = { Text("IG Captions") }) }
                }
            }
            item {
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Enter Video / Post Topic") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (activeMode == 2 || activeMode == 3) {
                item {
                    OutlinedTextField(
                        value = channelHandle,
                        onValueChange = { channelHandle = it },
                        label = { Text("Channel / IG Handle") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(niches) { item ->
                        FilterChip(
                            selected = niche == item,
                            onClick = { niche = item },
                            label = { Text(item, fontSize = 12.sp) }
                        )
                    }
                }
            }
            items(outputs) { output ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(output, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${output.length} chars", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { ImageExportUtils.copyToClipboard(context, output, "Creator Copy") }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HashtagAndTagGeneratorScreen(
    isTagMode: Boolean = false,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var keyword by remember { mutableStateOf("creator") }
    var tagMode by remember { mutableStateOf(isTagMode) }
    val slug = keyword.trim().lowercase(Locale.getDefault()).replace(Regex("[^a-z0-9]"), "").ifBlank { "trending" }

    val tags = remember(slug, tagMode) {
        val base = listOf(
            slug, "${slug}tips", "${slug}2026", "${slug}tutorial", "${slug}guide",
            "viral$slug", "${slug}hacks", "best$slug", "${slug}india", "${slug}forbeginners",
            "trending", "reels", "youtubeshorts", "explore", "creator", "viralshorts"
        )
        if (tagMode) base.joinToString(", ") else base.joinToString(" ") { "#$it" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (tagMode) "SEO Video Tag Generator" else "Viral Hashtag Generator",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !tagMode, onClick = { tagMode = false }, label = { Text("#Hashtags") })
                    FilterChip(selected = tagMode, onClick = { tagMode = true }, label = { Text("YouTube SEO Tags") })
                }
            }
            item {
                OutlinedTextField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    label = { Text("Enter Niche or Keyword") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (tagMode) "YouTube Tag Budget (${tags.length}/500 chars)" else "Ready-to-Paste Hashtags",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (tagMode) {
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (tags.length / 500f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(tags, fontSize = 14.sp, lineHeight = 22.sp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { ImageExportUtils.copyToClipboard(context, tags, "Tags") },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy All")
                            }
                            Button(
                                onClick = { ImageExportUtils.shareText(context, tags, "Share Tags") },
                                modifier = Modifier.weight(1f).height(48.dp),
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouTubeRevenueCalculatorScreen(
    initialMode: Int = 0, // 0 = Revenue Estimator, 1 = Reverse CPM/RPM Calc
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    var activeTab by remember { mutableIntStateOf(initialMode) }
    var dailyViews by remember { mutableStateOf("15000") }
    var rpmSlider by remember { mutableFloatStateOf(1.8f) }

    var totalViewsInput by remember { mutableStateOf("250000") }
    var totalEarningsInput by remember { mutableStateOf("420") }

    val views = dailyViews.toDoubleOrNull() ?: 0.0
    val dailyRevUsd = (views / 1000.0) * rpmSlider
    val monthlyRevUsd = dailyRevUsd * 30.0
    val yearlyRevUsd = dailyRevUsd * 365.0
    val inrRate = 84.5

    val tViews = (totalViewsInput.toDoubleOrNull() ?: 0.0).coerceAtLeast(1.0)
    val tEarn = totalEarningsInput.toDoubleOrNull() ?: 0.0
    val calcRpm = (tEarn / tViews) * 1000.0
    val estCpm = calcRpm / 0.55 // YouTube shares ~55% AdSense revenue with creator

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (activeTab == 0) "YouTube Revenue Estimator" else "CPM / RPM Calculator",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = activeTab == 0, onClick = { activeTab = 0 }, label = { Text("Revenue Estimator") })
                    FilterChip(selected = activeTab == 1, onClick = { activeTab = 1 }, label = { Text("CPM / RPM Calc") })
                }
            }
            if (activeTab == 0) {
                item {
                    OutlinedTextField(
                        value = dailyViews,
                        onValueChange = { dailyViews = it.filter { c -> c.isDigit() } },
                        label = { Text("Daily Views") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Column {
                        Text(
                            text = "Estimated RPM: $${String.format(Locale.US, "%.2f", rpmSlider)} (₹${String.format(Locale.US, "%.0f", rpmSlider * inrRate)}) / 1K views",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Slider(value = rpmSlider, onValueChange = { rpmSlider = it }, valueRange = 0.2f..15f)
                    }
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Estimated Creator Payout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Daily: $${String.format(Locale.US, "%.2f", dailyRevUsd)} (₹${String.format(Locale.US, "%.0f", dailyRevUsd * inrRate)})")
                            Text(
                                "Monthly: $${String.format(Locale.US, "%.2f", monthlyRevUsd)} (₹${String.format(Locale.US, "%.0f", monthlyRevUsd * inrRate)})",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("Yearly: $${String.format(Locale.US, "%.2f", yearlyRevUsd)} (₹${String.format(Locale.US, "%.0f", yearlyRevUsd * inrRate)})")
                        }
                    }
                }
            } else {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = totalViewsInput,
                            onValueChange = { totalViewsInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Total Views") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = totalEarningsInput,
                            onValueChange = { totalEarningsInput = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Earnings ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Your RPM: $${String.format(Locale.US, "%.2f", calcRpm)} / 1K views",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("Estimated Playback CPM: $${String.format(Locale.US, "%.2f", estCpm)} (at 55% creator share)")
                            Text("INR Value: ₹${String.format(Locale.US, "%.2f", calcRpm * inrRate)} per 1K views")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoFileSizeCalculatorScreen(
    initialMode: Int = 0, // 0 = Video File Size, 1 = Thumbnail Spec & Safe Zone
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    var activeTab by remember { mutableIntStateOf(initialMode) }
    var durationMinutes by remember { mutableStateOf("10") }
    var bitrateMbps by remember { mutableStateOf("12") }
    var resolution by remember { mutableStateOf("1080p Full HD (1920x1080)") }
    var fps60 by remember { mutableStateOf(false) }

    val resolutions = listOf(
        "720p HD (1280x720)",
        "1080p Full HD (1920x1080)",
        "4K UHD (3840x2160)"
    )

    val mins = durationMinutes.toDoubleOrNull() ?: 0.0
    val mbps = (bitrateMbps.toDoubleOrNull() ?: 0.0) * (if (fps60) 1.35 else 1.0)
    val sizeMb = (mins * 60.0 * mbps) / 8.0
    val sizeGb = sizeMb / 1024.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (activeTab == 0) "Video File Size Estimator" else "Thumbnail Size & Safe Zone",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = activeTab == 0, onClick = { activeTab = 0 }, label = { Text("Video Bitrate Calc") })
                    FilterChip(selected = activeTab == 1, onClick = { activeTab = 1 }, label = { Text("Thumbnail Safe Zone") })
                }
            }
            if (activeTab == 0) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        resolutions.forEach { res ->
                            FilterChip(
                                selected = resolution == res,
                                onClick = {
                                    resolution = res
                                    bitrateMbps = when {
                                        res.startsWith("720p") -> "6"
                                        res.startsWith("4K") -> "35"
                                        else -> "12"
                                    }
                                },
                                label = { Text(res) }
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = !fps60, onClick = { fps60 = false }, label = { Text("30 FPS") })
                            FilterChip(selected = fps60, onClick = { fps60 = true }, label = { Text("60 FPS (+35%)") })
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = durationMinutes,
                            onValueChange = { durationMinutes = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Duration (mins)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = bitrateMbps,
                            onValueChange = { bitrateMbps = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Bitrate (Mbps)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Estimated Output File Size", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                text = "${String.format(Locale.US, "%.1f", sizeMb)} MB (${String.format(Locale.US, "%.2f", sizeGb)} GB)",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1E293B))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "1280 × 720 px (16:9 HD)",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                        Surface(
                            color = Color.Black.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.BottomEnd)
                        ) {
                            Text(
                                text = "SAFE ZONE (12:45)",
                                color = Color(0xFFFACC15),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Official YouTube Thumbnail Specs", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("• Ideal Resolution: 1280 × 720 px (16:9 Aspect Ratio)")
                            Text("• Minimum Width: 640 px • Max File Size: 2 MB")
                            Text("• Supported Formats: JPG, PNG, GIF")
                            Text("• Keep Bottom-Right corner clear for the timestamp pill.")
                        }
                    }
                }
            }
        }
    }
}
