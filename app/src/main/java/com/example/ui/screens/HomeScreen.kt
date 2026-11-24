package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.omnitools.xkrvp.R
import com.example.ads.AdManager
import com.example.ads.TopBannerAd
import com.example.ads.findActivity
import com.example.model.ToolCategory
import com.example.model.ToolItem
import com.example.model.ToolRegistry
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.PlayfairDisplayFamily
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.RoseAccent
import com.example.ui.theme.VioletSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onSelectTool: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(ToolCategory.ALL) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var userRating by remember { mutableIntStateOf(5) }

    val navigateWithAd: (String) -> Unit = { toolId ->
        if (activity != null) {
            AdManager.showInterstitialIfReady(activity, forceShow = false) {
                onSelectTool(toolId)
            }
        } else {
            onSelectTool(toolId)
        }
    }

    val featuredTools = remember { ToolRegistry.allTools.filter { it.isFeatured } }
    val filteredTools = remember(searchQuery, selectedCategory) {
        ToolRegistry.allTools.filter { tool ->
            val matchesCategory = selectedCategory == ToolCategory.ALL || tool.category == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                tool.title.contains(searchQuery, ignoreCase = true) ||
                tool.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchVisible) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search 40+ pro tools...", fontSize = 13.sp) },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    isSearchVisible = false
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close Search", modifier = Modifier.size(18.dp))
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_tools_input")
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.omnitools_logo),
                                contentDescription = "OmniTools App Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "OmniTools",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 19.sp,
                                        fontFamily = PlusJakartaSansFamily
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = IndigoPrimary.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "STUDIO",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                actions = {
                    if (!isSearchVisible) {
                        IconButton(onClick = { isSearchVisible = true }, modifier = Modifier.testTag("search_button")) {
                            Icon(Icons.Default.Search, contentDescription = "Search Tools")
                        }
                    }
                    IconButton(onClick = onToggleTheme, modifier = Modifier.testTag("theme_toggle_button")) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkTheme) "Switch to Light Theme" else "Switch to Dark Theme",
                            tint = if (isDarkTheme) GoldAccent else MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showRatingDialog = true }, modifier = Modifier.testTag("rate_app_button")) {
                        Icon(Icons.Default.Star, contentDescription = "Rate App", tint = GoldAccent)
                    }
                    IconButton(onClick = { showInfoDialog = true }, modifier = Modifier.testTag("about_button")) {
                        Icon(Icons.Default.Info, contentDescription = "About, Share & Privacy")
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
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (searchQuery.isBlank()) {
                item {
                    StudioHeroBanner(onQuickLaunch = navigateWithAd)
                }

                item {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Featured Studios",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = PlusJakartaSansFamily
                            )
                            Text(
                                text = "Swipe →",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(featuredTools, key = { it.id }) { tool ->
                                FeaturedToolCard(tool = tool, onClick = { navigateWithAd(tool.id) })
                            }
                        }
                    }
                }
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ToolCategory.entries) { category ->
                        val count = if (category == ToolCategory.ALL) {
                            ToolRegistry.allTools.size
                        } else {
                            ToolRegistry.allTools.count { it.category == category }
                        }
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    text = "${category.title} ($count)",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = category.badgeColor.copy(alpha = 0.16f),
                                selectedLabelColor = category.badgeColor
                            ),
                            modifier = Modifier.testTag("category_${category.name.lowercase()}")
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "Search Results" else selectedCategory.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${filteredTools.size} Tools",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            items(filteredTools.chunked(2)) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { tool ->
                        ToolCard(
                            tool = tool,
                            onClick = { navigateWithAd(tool.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.2.dp, GoldAccent.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Enjoying OmniTools Studio?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "A 5-star Play Store rating helps us keep all 40+ pro tools updated and free.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { openPlayStoreRating(context) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Rate 5★", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { shareApp(context) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share App", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRatingDialog) {
        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            title = { Text("Rate OmniTools Studio", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("How has your experience been?", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { userRating = star }) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "$star stars",
                                    tint = if (star <= userRating) GoldAccent else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRatingDialog = false
                        openPlayStoreRating(context)
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Review on Play Store", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRatingDialog = false }) { Text("Later") }
            }
        )
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("About OmniTools Studio", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "OmniTools Studio brings 40+ pro design, creator, GST invoicing, image editing, PDF, voice & utility tools offline on your device.",
                        fontSize = 14.sp
                    )
                    Button(
                        onClick = {
                            showInfoDialog = false
                            shareApp(context)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share OmniTools")
                    }
                    OutlinedButton(
                        onClick = {
                            showInfoDialog = false
                            showPrivacyDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Privacy Policy")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) { Text("Close") }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "All card designs, photo edits, PDF conversions, invoices, voice effects, and calculations are processed locally on your device. " +
                        "Microphone permission is only requested inside the Voice Changer tool. Google AdMob displays ads responsibly to keep all tools free.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun StudioHeroBanner(onQuickLaunch: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF312E81), Color(0xFF6D28D9), Color(0xFFBE185D))
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(color = Color.White.copy(alpha = 0.18f), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = "PRO CREATOR & BUSINESS SUITE",
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                    }
                    Surface(color = GoldAccent, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = "40+ TOOLS",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            color = Color(0xFF1E1B4B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Create Royal Cards, Thumbnails & GST Bills",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PlayfairDisplayFamily,
                    lineHeight = 28.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Studio-grade offline tools for creators, weddings, shops & everyday work.",
                    color = Color.White.copy(alpha = 0.86f),
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        HeroQuickChip("Wedding Card", Icons.Default.Favorite) { onQuickLaunch("wedding_card") }
                    }
                    item {
                        HeroQuickChip("Thumbnail", Icons.Default.Videocam) { onQuickLaunch("yt_thumbnail") }
                    }
                    item {
                        HeroQuickChip("Status Card", Icons.Default.Style) { onQuickLaunch("status_maker") }
                    }
                    item {
                        HeroQuickChip("GST Invoice", Icons.Default.Receipt) { onQuickLaunch("invoice_maker") }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroQuickChip(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FeaturedToolCard(tool: ToolItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(214.dp)
            .clickable(onClick = onClick)
            .testTag("featured_tool_${tool.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, tool.accentColor.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Brush.horizontalGradient(listOf(tool.accentColor, tool.accentColor.copy(alpha = 0.45f))))
            )
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(tool.accentColor.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(tool.icon, contentDescription = tool.title, tint = tool.accentColor, modifier = Modifier.size(22.dp))
                    }
                    if (tool.isPopular) {
                        Surface(color = RoseAccent, shape = RoundedCornerShape(6.dp)) {
                            Text(
                                text = "PRO",
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(tool.title, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    tool.description,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        tool.category.title,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = tool.accentColor,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = tool.accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ToolCard(tool: ToolItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("tool_card_${tool.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tool.accentColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(tool.icon, contentDescription = tool.title, tint = tool.accentColor, modifier = Modifier.size(21.dp))
                }
                if (tool.isPopular) {
                    Surface(color = tool.accentColor.copy(alpha = 0.14f), shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = "TOP",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = tool.accentColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(tool.title, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                tool.description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }
}

private fun openPlayStoreRating(context: Context) {
    val packageName = context.packageName
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
    } catch (_: Exception) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        } catch (_: Exception) {
        }
    }
}

private fun shareApp(context: Context) {
    val packageName = context.packageName
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "OmniTools Studio - 40+ All-in-One Tools")
        putExtra(
            Intent.EXTRA_TEXT,
            "Check out OmniTools Studio! 40+ powerful tools for Wedding Cards, Thumbnails, GST Invoices, Photo/PDF editing & more:\nhttps://play.google.com/store/apps/details?id=$packageName"
        )
    }
    try {
        context.startActivity(Intent.createChooser(shareIntent, "Share OmniTools via"))
    } catch (_: Exception) {
    }
}
