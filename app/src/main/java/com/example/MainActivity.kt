package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import com.example.ads.AdManager
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.tools.business.EmiCalculatorScreen
import com.example.ui.tools.business.GstCalculatorScreen
import com.example.ui.tools.business.InvoiceBillMakerScreen
import com.example.ui.tools.business.ProfitDiscountCalculatorScreen
import com.example.ui.tools.creator.HashtagAndTagGeneratorScreen
import com.example.ui.tools.creator.VideoFileSizeCalculatorScreen
import com.example.ui.tools.creator.YouTubeRevenueCalculatorScreen
import com.example.ui.tools.creator.YouTubeTitleGeneratorScreen
import com.example.ui.tools.daily.AgeCalculatorScreen
import com.example.ui.tools.daily.BmiCalculatorScreen
import com.example.ui.tools.daily.QrCodeGeneratorScreen
import com.example.ui.tools.daily.StopwatchAndTimerScreen
import com.example.ui.tools.daily.UnitConverterScreen
import com.example.ui.tools.design.InvitationCardMakerScreen
import com.example.ui.tools.design.ReelsMakerScreen
import com.example.ui.tools.design.StatusMakerScreen
import com.example.ui.tools.design.WeddingCardMakerScreen
import com.example.ui.tools.design.YouTubeThumbnailMakerScreen
import com.example.ui.tools.fonts.FontsMakerScreen
import com.example.ui.tools.image.ImageCompressorScreen
import com.example.ui.tools.image.ImageQualityCheckerScreen
import com.example.ui.tools.image.PassportPhotoMakerScreen
import com.example.ui.tools.pdf.CaseConverterScreen
import com.example.ui.tools.pdf.PhotoToPdfScreen
import com.example.ui.tools.pdf.TextToPdfScreen
import com.example.ui.tools.pdf.WordAndCharacterCounterScreen
import com.example.ui.tools.voice.VoiceChangerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AdManager.initialize(applicationContext)
        setContent {
            MyApplicationTheme {
                OmniToolsApp()
            }
        }
    }
}

@Composable
fun OmniToolsApp() {
    var activeToolId by remember { mutableStateOf<String?>(null) }
    val goBack: () -> Unit = { activeToolId = null }

    when (activeToolId) {
        null -> HomeScreen(onSelectTool = { toolId -> activeToolId = toolId })

        // Design & Cards
        "wedding_card" -> WeddingCardMakerScreen(onBack = goBack)
        "invitation_card" -> InvitationCardMakerScreen(onBack = goBack)
        "status_maker" -> StatusMakerScreen(onBack = goBack)
        "reels_maker" -> ReelsMakerScreen(onBack = goBack)
        "yt_thumbnail" -> YouTubeThumbnailMakerScreen(onBack = goBack)
        "fonts_maker" -> FontsMakerScreen(onBack = goBack)

        // Creator Tools
        "yt_title_gen" -> YouTubeTitleGeneratorScreen(initialMode = 0, onBack = goBack)
        "shorts_title_gen" -> YouTubeTitleGeneratorScreen(initialMode = 1, onBack = goBack)
        "yt_desc_gen" -> YouTubeTitleGeneratorScreen(initialMode = 2, onBack = goBack)
        "ig_caption_gen" -> YouTubeTitleGeneratorScreen(initialMode = 3, onBack = goBack)
        "hashtag_gen" -> HashtagAndTagGeneratorScreen(isTagMode = false, onBack = goBack)
        "tag_gen" -> HashtagAndTagGeneratorScreen(isTagMode = true, onBack = goBack)
        "yt_revenue_calc" -> YouTubeRevenueCalculatorScreen(initialMode = 0, onBack = goBack)
        "cpm_rpm_calc" -> YouTubeRevenueCalculatorScreen(initialMode = 1, onBack = goBack)
        "video_file_size" -> VideoFileSizeCalculatorScreen(initialMode = 0, onBack = goBack)
        "thumbnail_size" -> VideoFileSizeCalculatorScreen(initialMode = 1, onBack = goBack)

        // Business & Finance
        "invoice_maker" -> InvoiceBillMakerScreen(onBack = goBack)
        "gst_calc" -> GstCalculatorScreen(initialMode = 0, onBack = goBack)
        "tax_calc" -> GstCalculatorScreen(initialMode = 1, onBack = goBack)
        "emi_calc" -> EmiCalculatorScreen(initialMode = 0, onBack = goBack)
        "loan_calc" -> EmiCalculatorScreen(initialMode = 1, onBack = goBack)
        "salary_calc" -> EmiCalculatorScreen(initialMode = 2, onBack = goBack)
        "profit_margin" -> ProfitDiscountCalculatorScreen(initialMode = 0, onBack = goBack)
        "discount_calc" -> ProfitDiscountCalculatorScreen(initialMode = 1, onBack = goBack)
        "percentage_calc" -> ProfitDiscountCalculatorScreen(initialMode = 2, onBack = goBack)
        "tip_calc" -> ProfitDiscountCalculatorScreen(initialMode = 3, onBack = goBack)

        // Image Tools
        "image_compressor" -> ImageCompressorScreen(initialMode = 0, onBack = goBack)
        "image_resizer" -> ImageCompressorScreen(initialMode = 1, onBack = goBack)
        "format_converter" -> ImageCompressorScreen(initialMode = 2, onBack = goBack)
        "passport_photo" -> PassportPhotoMakerScreen(initialMode = 0, onBack = goBack)
        "photo_size_calc" -> PassportPhotoMakerScreen(initialMode = 1, onBack = goBack)
        "image_quality", "image_cropper" -> ImageQualityCheckerScreen(initialMode = 0, onBack = goBack)
        "blur_image" -> ImageQualityCheckerScreen(initialMode = 1, onBack = goBack)

        // PDF & Document
        "image_to_pdf_quick", "pdf_merger" -> PhotoToPdfScreen(onBack = goBack)
        "text_to_pdf" -> TextToPdfScreen(onBack = goBack)
        "word_counter" -> WordAndCharacterCounterScreen(isCharacterFocus = false, onBack = goBack)
        "character_counter" -> WordAndCharacterCounterScreen(isCharacterFocus = true, onBack = goBack)
        "case_converter" -> CaseConverterScreen(onBack = goBack)

        // Voice & Audio
        "voice_changer" -> VoiceChangerScreen(onBack = goBack)

        // Daily Utilities
        "qr_generator" -> QrCodeGeneratorScreen(initialIsBarcode = false, onBack = goBack)
        "barcode_generator" -> QrCodeGeneratorScreen(initialIsBarcode = true, onBack = goBack)
        "age_calculator" -> AgeCalculatorScreen(initialMode = 0, onBack = goBack)
        "date_diff_calc" -> AgeCalculatorScreen(initialMode = 1, onBack = goBack)
        "bmi_calculator" -> BmiCalculatorScreen(onBack = goBack)
        NotificationCompat.CATEGORY_STOPWATCH -> StopwatchAndTimerScreen(initialIsTimer = false, onBack = goBack)
        "countdown_timer" -> StopwatchAndTimerScreen(initialIsTimer = true, onBack = goBack)
        "unit_converter" -> UnitConverterScreen(initialMode = 0, onBack = goBack)
        "currency_converter" -> UnitConverterScreen(initialMode = 1, onBack = goBack)
        "timezone_converter" -> UnitConverterScreen(initialMode = 2, onBack = goBack)

        else -> HomeScreen(onSelectTool = { toolId -> activeToolId = toolId })
    }
}
