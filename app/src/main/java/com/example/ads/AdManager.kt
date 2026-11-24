package com.example.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

object AdManager {

    private const val TAG = "OmniToolsAds"

    const val APP_ID = "ca-app-pub-7584087725069099~5449667520"

    // Real Production AdMob Ad Unit IDs (active in both Debug APK and Release AAB)
    const val BANNER_ID = "ca-app-pub-7584087725069099/8487278849"
    const val INTERSTITIAL_ID = "ca-app-pub-7584087725069099/7969559042"
    const val REWARDED_ID = "ca-app-pub-7584087725069099/3640752672"

    private val handler = Handler(Looper.getMainLooper())
    private val initScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var appContext: Context? = null
    private val _isInitialized = MutableStateFlow(false)
    val isInitializedFlow: StateFlow<Boolean> = _isInitialized.asStateFlow()

    @Volatile
    private var initializing = false

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private var interstitialLoading = false
    private var rewardedLoading = false
    private var isShowingInterstitial = false
    private var isShowingRewarded = false

    private var interstitialRetries = 0
    private var rewardedRetries = 0

    // Invalid-Activity & Policy Safe Frequency Controls
    private val sessionStartTime = System.currentTimeMillis()
    private var lastInterstitialTime = 0L
    private var lastTriggerDebounceTime = 0L
    private var screenChangeCount = 0
    private var sessionInterstitialCount = 0

    private const val INITIAL_GRACE_PERIOD_MS = 30_000L       // No interstitial in first 30s after launch
    private const val INTERSTITIAL_COOLDOWN_MS = 90_000L      // Minimum 90s gap between interstitials
    private const val FORCED_COOLDOWN_MS = 45_000L            // Minimum 45s gap even for explicit transitions
    private const val TAP_DEBOUNCE_MS = 900L                  // Guard against double-taps triggering accidental clicks
    private const val ACTIONS_BETWEEN_ADS = 3                 // Require 3 distinct tool opens before next interstitial
    private const val MAX_INTERSTITIALS_PER_SESSION = 6       // Hard cap per app session to protect AdMob standing

    private fun runOnMain(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            handler.post(action)
        }
    }

    fun initialize(context: Context) {
        val safeContext = context.applicationContext
        appContext = safeContext

        if (_isInitialized.value || initializing) return
        initializing = true

        initScope.launch {
            try {
                MobileAds.initialize(safeContext) { initStatus ->
                    runOnMain {
                        initializing = false
                        _isInitialized.value = true
                        Log.d(TAG, "AdMob initialized: ${initStatus.adapterStatusMap.keys}")
                        preloadInterstitial(safeContext)
                        preloadRewarded(safeContext)
                    }
                }
            } catch (t: Throwable) {
                runOnMain {
                    initializing = false
                    Log.e(TAG, "MobileAds initialization error", t)
                }
            }
        }
    }

    fun preloadInterstitial(context: Context) {
        val safeContext = context.applicationContext
        appContext = safeContext

        runOnMain {
            if (!_isInitialized.value || interstitialAd != null || interstitialLoading) {
                return@runOnMain
            }
            if (sessionInterstitialCount >= MAX_INTERSTITIALS_PER_SESSION) {
                return@runOnMain
            }
            interstitialLoading = true
            try {
                InterstitialAd.load(
                    safeContext,
                    INTERSTITIAL_ID,
                    AdRequest.Builder().build(),
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(ad: InterstitialAd) {
                            interstitialAd = ad
                            interstitialLoading = false
                            interstitialRetries = 0
                            Log.d(TAG, "Interstitial preloaded")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            interstitialAd = null
                            interstitialLoading = false
                            Log.w(TAG, "Interstitial preload skipped (${error.code}): ${error.message}")
                            retryInterstitial()
                        }
                    }
                )
            } catch (t: Throwable) {
                interstitialLoading = false
                Log.e(TAG, "Interstitial load exception", t)
                retryInterstitial()
            }
        }
    }

    private fun retryInterstitial() {
        if (interstitialRetries >= 3) return
        val delays = longArrayOf(10_000L, 25_000L, 60_000L)
        val delay = delays[interstitialRetries++]
        handler.postDelayed({
            appContext?.let { preloadInterstitial(it) }
        }, delay)
    }

    fun preloadRewarded(context: Context) {
        val safeContext = context.applicationContext
        appContext = safeContext

        runOnMain {
            if (!_isInitialized.value || rewardedAd != null || rewardedLoading) {
                return@runOnMain
            }
            rewardedLoading = true
            try {
                RewardedAd.load(
                    safeContext,
                    REWARDED_ID,
                    AdRequest.Builder().build(),
                    object : RewardedAdLoadCallback() {
                        override fun onAdLoaded(ad: RewardedAd) {
                            rewardedAd = ad
                            rewardedLoading = false
                            rewardedRetries = 0
                            Log.d(TAG, "Rewarded ad preloaded")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            rewardedAd = null
                            rewardedLoading = false
                            Log.w(TAG, "Rewarded preload skipped (${error.code}): ${error.message}")
                            retryRewarded()
                        }
                    }
                )
            } catch (t: Throwable) {
                rewardedLoading = false
                Log.e(TAG, "Rewarded load exception", t)
                retryRewarded()
            }
        }
    }

    private fun retryRewarded() {
        if (rewardedRetries >= 3) return
        val delays = longArrayOf(10_000L, 25_000L, 60_000L)
        val delay = delays[rewardedRetries++]
        handler.postDelayed({
            appContext?.let { preloadRewarded(it) }
        }, delay)
    }

    /**
     * Policy-safe interstitial trigger:
     * - Debounces rapid double-taps to avoid accidental click-throughs
     * - Enforces 30s initial app-launch grace period
     * - Enforces 90s minimum cooldown and 3-action spacing
     * - Caps total interstitials per session to protect AdMob standing
     * - Never shows when another full-screen ad is active or Activity is finishing
     */
    fun showInterstitialIfReady(
        activity: Activity,
        forceShow: Boolean = false,
        onDismissed: () -> Unit
    ) {
        runOnMain {
            val now = System.currentTimeMillis()
            if (now - lastTriggerDebounceTime < TAP_DEBOUNCE_MS) {
                return@runOnMain
            }
            lastTriggerDebounceTime = now

            if (!_isInitialized.value) {
                initialize(activity)
                onDismissed()
                return@runOnMain
            }

            if (isShowingInterstitial || isShowingRewarded || activity.isFinishing || activity.isDestroyed) {
                onDismissed()
                return@runOnMain
            }

            screenChangeCount++
            val sinceLaunch = now - sessionStartTime
            val sinceLastAd = now - lastInterstitialTime

            val graceSatisfied = sinceLaunch >= INITIAL_GRACE_PERIOD_MS
            val capSatisfied = sessionInterstitialCount < MAX_INTERSTITIALS_PER_SESSION
            val spacingSatisfied = if (forceShow) {
                lastInterstitialTime == 0L || sinceLastAd >= FORCED_COOLDOWN_MS
            } else {
                (lastInterstitialTime == 0L || sinceLastAd >= INTERSTITIAL_COOLDOWN_MS) &&
                    screenChangeCount >= ACTIONS_BETWEEN_ADS
            }

            val ad = interstitialAd
            if (ad == null || !graceSatisfied || !capSatisfied || !spacingSatisfied) {
                appContext?.let { preloadInterstitial(it) }
                onDismissed()
                return@runOnMain
            }

            interstitialAd = null
            isShowingInterstitial = true

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    lastInterstitialTime = System.currentTimeMillis()
                    screenChangeCount = 0
                    sessionInterstitialCount++
                }

                override fun onAdDismissedFullScreenContent() {
                    isShowingInterstitial = false
                    lastInterstitialTime = System.currentTimeMillis()
                    appContext?.let { preloadInterstitial(it) }
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    isShowingInterstitial = false
                    Log.w(TAG, "Interstitial show failed: ${error.message}")
                    appContext?.let { preloadInterstitial(it) }
                    onDismissed()
                }
            }

            try {
                ad.show(activity)
            } catch (t: Throwable) {
                isShowingInterstitial = false
                Log.e(TAG, "Interstitial show exception", t)
                appContext?.let { preloadInterstitial(it) }
                onDismissed()
            }
        }
    }

    fun showRewardedAd(
        activity: Activity,
        onLoadingChanged: (Boolean) -> Unit = {},
        onRewardEarned: () -> Unit,
        onAdUnavailable: () -> Unit = {},
        onDismissed: () -> Unit = {}
    ) {
        runOnMain {
            if (isShowingRewarded || isShowingInterstitial || activity.isFinishing || activity.isDestroyed) {
                return@runOnMain
            }

            val cached = rewardedAd
            if (cached != null) {
                rewardedAd = null
                presentRewardedAd(activity, cached, onRewardEarned, onDismissed)
                return@runOnMain
            }

            // On-demand load when preloaded ad isn't ready yet — NEVER unlock watermark without watching ad
            if (!_isInitialized.value) {
                initialize(activity)
            }
            onLoadingChanged(true)
            var handled = false

            val timeoutRunnable = Runnable {
                if (!handled) {
                    handled = true
                    onLoadingChanged(false)
                    Toast.makeText(
                        activity,
                        "Rewarded ad is not ready right now. Please try again or save with watermark.",
                        Toast.LENGTH_SHORT
                    ).show()
                    onAdUnavailable()
                    preloadRewarded(activity)
                }
            }
            handler.postDelayed(timeoutRunnable, 8_000L)

            try {
                RewardedAd.load(
                    activity.applicationContext,
                    REWARDED_ID,
                    AdRequest.Builder().build(),
                    object : RewardedAdLoadCallback() {
                        override fun onAdLoaded(ad: RewardedAd) {
                            runOnMain {
                                if (handled) {
                                    // Timeout already fired; keep ad cached for the next attempt
                                    rewardedAd = ad
                                    return@runOnMain
                                }
                                handled = true
                                handler.removeCallbacks(timeoutRunnable)
                                onLoadingChanged(false)
                                if (activity.isFinishing || activity.isDestroyed) {
                                    rewardedAd = ad
                                    onAdUnavailable()
                                } else {
                                    presentRewardedAd(activity, ad, onRewardEarned, onDismissed)
                                }
                            }
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            runOnMain {
                                if (handled) return@runOnMain
                                handled = true
                                handler.removeCallbacks(timeoutRunnable)
                                onLoadingChanged(false)
                                Log.w(TAG, "On-demand Rewarded load failed (${error.code}): ${error.message}")
                                Toast.makeText(
                                    activity,
                                    "Ad unavailable right now. Save with watermark or try again shortly.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onAdUnavailable()
                                retryRewarded()
                            }
                        }
                    }
                )
            } catch (t: Throwable) {
                if (!handled) {
                    handled = true
                    handler.removeCallbacks(timeoutRunnable)
                    onLoadingChanged(false)
                    Log.e(TAG, "On-demand Rewarded exception", t)
                    onAdUnavailable()
                }
            }
        }
    }

    private fun presentRewardedAd(
        activity: Activity,
        ad: RewardedAd,
        onRewardEarned: () -> Unit,
        onDismissed: () -> Unit
    ) {
        isShowingRewarded = true
        var rewardEarned = false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                isShowingRewarded = false
                appContext?.let { preloadRewarded(it) }
                if (!rewardEarned) {
                    Toast.makeText(
                        activity,
                        "Watch full ad to remove watermark.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                isShowingRewarded = false
                Log.w(TAG, "Rewarded show failed: ${error.message}")
                appContext?.let { preloadRewarded(it) }
                onDismissed()
            }
        }

        try {
            ad.show(activity) {
                if (!rewardEarned) {
                    rewardEarned = true
                    onRewardEarned()
                }
            }
        } catch (t: Throwable) {
            isShowingRewarded = false
            Log.e(TAG, "Rewarded show exception", t)
            appContext?.let { preloadRewarded(it) }
            onDismissed()
        }
    }
}

private class BannerAdHolder(
    context: Context,
    private val onLoadedStateChanged: (Boolean) -> Unit
) {

    private val adContext: Context = context.findActivity() ?: context
    val container = FrameLayout(adContext)
    private val adView = AdView(adContext)
    private val handler = Handler(Looper.getMainLooper())

    val adHeightDp: Float
    var hasLoaded = false
    var isLoading = false
    var isDisposed = false

    private var retryCount = 0
    private var pendingRetry: Runnable? = null

    init {
        val density = adContext.resources.displayMetrics.density.takeIf { it > 0f } ?: 1f
        val widthDp = (adContext.resources.displayMetrics.widthPixels / density).toInt().coerceAtLeast(320)

        val size = try {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(adContext, widthDp)
        } catch (e: Exception) {
            Log.e("TopBannerAd", "Adaptive banner size fallback", e)
            AdSize.BANNER
        }

        val adWidthPx = size.getWidthInPixels(adContext).coerceAtLeast(adContext.resources.displayMetrics.widthPixels)
        val adHeightPx = size.getHeightInPixels(adContext).coerceAtLeast((52f * density).toInt())
        adHeightDp = (adHeightPx / density).coerceAtLeast(52f)

        container.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            adHeightPx
        )
        container.minimumHeight = adHeightPx

        adView.adUnitId = AdManager.BANNER_ID
        adView.setAdSize(size)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                hasLoaded = true
                isLoading = false
                retryCount = 0
                pendingRetry?.let { handler.removeCallbacks(it) }
                pendingRetry = null
                container.visibility = View.VISIBLE
                adView.visibility = View.VISIBLE
                adView.requestLayout()
                container.requestLayout()
                container.invalidate()
                onLoadedStateChanged(true)
                Log.d("TopBannerAd", "Banner loaded (${adWidthPx}x${adHeightPx})")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                isLoading = false
                onLoadedStateChanged(false)
                Log.w("TopBannerAd", "Banner load failed (${error.code}): ${error.message}")
                if (isDisposed || hasLoaded) return

                if (retryCount < 5) {
                    val delays = longArrayOf(3_000L, 6_000L, 12_000L, 25_000L, 45_000L)
                    val delay = delays[retryCount++]
                    val retry = Runnable {
                        pendingRetry = null
                        loadIfNeeded()
                    }
                    pendingRetry = retry
                    handler.postDelayed(retry, delay)
                }
            }
        }

        val params = FrameLayout.LayoutParams(
            adWidthPx,
            adHeightPx,
            Gravity.CENTER
        )
        container.addView(adView, params)
    }

    fun loadIfNeeded() {
        if (isDisposed || hasLoaded || isLoading) return
        isLoading = true
        try {
            adView.loadAd(AdRequest.Builder().build())
        } catch (t: Throwable) {
            isLoading = false
            Log.e("TopBannerAd", "Banner load exception", t)
        }
    }

    fun resume() {
        if (!isDisposed) {
            try {
                adView.resume()
            } catch (_: Throwable) {
            }
        }
    }

    fun pause() {
        if (!isDisposed) {
            try {
                adView.pause()
            } catch (_: Throwable) {
            }
        }
    }

    fun dispose() {
        if (isDisposed) return
        isDisposed = true
        pendingRetry?.let { handler.removeCallbacks(it) }
        pendingRetry = null
        handler.removeCallbacksAndMessages(null)
        try {
            container.removeAllViews()
            adView.pause()
            adView.destroy()
        } catch (_: Throwable) {
        }
    }
}

@Composable
fun TopBannerAd(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var isBannerLoaded by remember { mutableStateOf(false) }
    val holder = remember(context) {
        AdManager.initialize(context)
        BannerAdHolder(context) { loaded ->
            isBannerLoaded = loaded
        }
    }
    val isInitialized by AdManager.isInitializedFlow.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(isInitialized, holder) {
        if (isInitialized) {
            holder.loadIfNeeded()
        }
    }

    DisposableEffect(lifecycleOwner, holder) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> holder.resume()
                Lifecycle.Event.ON_PAUSE -> holder.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            holder.dispose()
        }
    }

    // Docked cleanly in screen footer, always positioned above system navigation/function buttons & IME
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
            .testTag("banner_ad_container"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(holder.adHeightDp.dp)
                    .padding(vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!isBannerLoaded) {
                    Text(
                        text = "Ad",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    )
                }
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(holder.adHeightDp.dp),
                    factory = { holder.container },
                    update = {
                        if (isInitialized && !holder.hasLoaded && !holder.isLoading) {
                            holder.loadIfNeeded()
                        }
                    }
                )
            }
        }
    }
}
