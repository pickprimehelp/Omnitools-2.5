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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.omnitools.xkrvp.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
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

    // Real Production AdMob Ad Unit IDs (used automatically in Release APK/AAB when !BuildConfig.DEBUG)
    private const val PROD_BANNER_ID = "ca-app-pub-7584087725069099/8487278849"
    private const val PROD_INTERSTITIAL_ID = "ca-app-pub-7584087725069099/7969559042"
    private const val PROD_REWARDED_ID = "ca-app-pub-7584087725069099/3640752672"

    // Google Official Test Ad Unit IDs (used automatically in Debug APK to keep AdMob account 100% safe)
    private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    val BANNER_ID: String
        get() = if (BuildConfig.DEBUG) TEST_BANNER_ID else PROD_BANNER_ID

    val INTERSTITIAL_ID: String
        get() = if (BuildConfig.DEBUG) TEST_INTERSTITIAL_ID else PROD_INTERSTITIAL_ID

    val REWARDED_ID: String
        get() = if (BuildConfig.DEBUG) TEST_REWARDED_ID else PROD_REWARDED_ID

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
                if (BuildConfig.DEBUG) {
                    val config = RequestConfiguration.Builder()
                        .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                        .build()
                    MobileAds.setRequestConfiguration(config)
                }
                MobileAds.initialize(safeContext) { initStatus ->
                    runOnMain {
                        initializing = false
                        _isInitialized.value = true
                        Log.d(TAG, "AdMob initialized (debug=${BuildConfig.DEBUG}): ${initStatus.adapterStatusMap.keys}")
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
                            Log.d(TAG, "Interstitial preloaded (debug=${BuildConfig.DEBUG})")
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
        onRewardEarned: () -> Unit,
        onDismissed: () -> Unit
    ) {
        runOnMain {
            if (isShowingRewarded || isShowingInterstitial || activity.isFinishing || activity.isDestroyed) {
                return@runOnMain
            }

            val ad = rewardedAd
            if (ad == null) {
                appContext?.let { preloadRewarded(it) }
                // Graceful fallback when ad inventory isn't cached so offline/low-fill users aren't stuck
                onRewardEarned()
                onDismissed()
                return@runOnMain
            }

            rewardedAd = null
            isShowingRewarded = true
            var rewardEarned = false

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    isShowingRewarded = false
                    appContext?.let { preloadRewarded(it) }
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
}

private class BannerAdHolder(context: Context) {

    val container = FrameLayout(context)
    private val adView = AdView(context)
    private val handler = Handler(Looper.getMainLooper())

    var hasLoaded = false
    var isLoading = false
    var isDisposed = false

    private var retryCount = 0
    private var pendingRetry: Runnable? = null

    init {
        val density = context.resources.displayMetrics.density.takeIf { it > 0f } ?: 1f
        val widthDp = (context.resources.displayMetrics.widthPixels / density).toInt().coerceAtLeast(320)

        val size = try {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
        } catch (e: Exception) {
            Log.e("TopBannerAd", "Adaptive banner size fallback", e)
            AdSize.BANNER
        }

        val minHeightPx = size.getHeightInPixels(context).coerceAtLeast((50f * density).toInt())
        container.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        container.minimumHeight = minHeightPx

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
                Log.d("TopBannerAd", "Banner loaded (debug=${BuildConfig.DEBUG})")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                isLoading = false
                Log.w("TopBannerAd", "Banner load failed (${error.code}): ${error.message}")
                if (isDisposed || hasLoaded) return

                if (retryCount < 3) {
                    val delays = longArrayOf(6_000L, 15_000L, 35_000L)
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
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
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
    val holderState = remember { mutableStateOf<BannerAdHolder?>(null) }
    val isInitialized by AdManager.isInitializedFlow.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(isInitialized) {
        if (isInitialized) {
            holderState.value?.loadIfNeeded()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> holderState.value?.resume()
                Lifecycle.Event.ON_PAUSE -> holderState.value?.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            holderState.value?.dispose()
            holderState.value = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
            .testTag("banner_ad_container")
    ) {
        // Clear boundary divider prevents accidental clicks between interactive UI and Banner Ad
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp),
                factory = { context ->
                    AdManager.initialize(context)
                    val banner = BannerAdHolder(context)
                    holderState.value = banner
                    if (isInitialized) {
                        banner.loadIfNeeded()
                    }
                    banner.container
                },
                update = {
                    holderState.value?.let { banner ->
                        if (isInitialized && !banner.hasLoaded && !banner.isLoading) {
                            banner.loadIfNeeded()
                        }
                    }
                }
            )
        }
    }
}
