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
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
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
import com.aistudio.omnitools.xkrvp.BuildConfig
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

        val initWatchdog = Runnable {
            if (initializing && !_isInitialized.value) {
                initializing = false
                Log.w(TAG, "MobileAds initialization timed out; allowing retry")
            }
        }
        handler.postDelayed(initWatchdog, 15_000L)

        initScope.launch {
            try {
                MobileAds.initialize(safeContext) { initStatus ->
                    runOnMain {
                        handler.removeCallbacks(initWatchdog)
                        initializing = false
                        _isInitialized.value = true
                        val adapters = initStatus.adapterStatusMap.entries.joinToString {
                            "${it.key}=${it.value.initializationState}"
                        }
                        Log.d(TAG, "AdMob initialized: $adapters")
                        preloadInterstitial(safeContext)
                        preloadRewarded(safeContext)
                    }
                }
            } catch (t: Throwable) {
                runOnMain {
                    handler.removeCallbacks(initWatchdog)
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
        onDismissed: (rewardEarned: Boolean) -> Unit = {}
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
            handler.postDelayed(timeoutRunnable, 15_000L)

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
                                val toastMsg = if (BuildConfig.DEBUG) {
                                    "Rewarded Err ${error.code}: ${error.message.take(40)}"
                                } else {
                                    "Ad unavailable right now. Save with watermark or try again shortly."
                                }
                                Toast.makeText(activity, toastMsg, Toast.LENGTH_SHORT).show()
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
        onDismissed: (rewardEarned: Boolean) -> Unit
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
                onDismissed(rewardEarned)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                isShowingRewarded = false
                Log.w(TAG, "Rewarded show failed: ${error.message}")
                appContext?.let { preloadRewarded(it) }
                onDismissed(false)
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
            onDismissed(false)
        }
    }
}

private class BannerAdHolder(
    context: Context,
    private val onVisibilityChanged: (visible: Boolean) -> Unit
) {

    private val adContext: Context = context.findActivity() ?: context
    val container = FrameLayout(adContext)
    private var adView: AdView? = null
    private val handler = Handler(Looper.getMainLooper())

    var hasLoaded = false
    var isLoading = false
    var isDisposed = false
    private var sdkInitialized = false
    private var retryCount = 0
    private var retriesExhausted = false
    private var pendingRetry: Runnable? = null

    init {
        container.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        container.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                if (isDisposed) return
                adView?.let { view ->
                    try {
                        view.resume()
                    } catch (_: Throwable) {
                    }
                }
                v.post { tryLoadIfReady() }
            }

            override fun onViewDetachedFromWindow(v: View) {
                adView?.let { view ->
                    try {
                        view.pause()
                    } catch (_: Throwable) {
                    }
                }
            }
        })
    }

    fun setSdkInitialized(initialized: Boolean) {
        if (sdkInitialized == initialized) return
        sdkInitialized = initialized
        if (initialized) {
            container.post { tryLoadIfReady() }
        }
    }

    private fun destroyCurrentAdView() {
        adView?.let { oldView ->
            try {
                container.removeView(oldView)
                oldView.pause()
                oldView.destroy()
            } catch (_: Throwable) {
            }
        }
        adView = null
    }

    private fun ensureAdViewCreated(): AdView? {
        adView?.let { return it }
        if (isDisposed || !container.isAttachedToWindow) {
            return null
        }

        val view = AdView(adContext)
        view.adUnitId = AdManager.BANNER_ID
        view.setAdSize(AdSize.BANNER)
        view.adListener = object : AdListener() {
            override fun onAdLoaded() {
                if (isDisposed) return
                hasLoaded = true
                this@BannerAdHolder.isLoading = false
                retryCount = 0
                retriesExhausted = false
                pendingRetry?.let { handler.removeCallbacks(it) }
                pendingRetry = null
                try {
                    view.resume()
                } catch (_: Throwable) {
                }
                container.visibility = View.VISIBLE
                view.visibility = View.VISIBLE
                view.requestLayout()
                container.requestLayout()
                onVisibilityChanged(true)
                Log.d("TopBannerAd", "Standard 320x50 banner loaded")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                this@BannerAdHolder.isLoading = false
                Log.w("TopBannerAd", "Banner load failed (${error.code}): ${error.message}")
                if (!hasLoaded) {
                    container.visibility = View.GONE
                    onVisibilityChanged(false)
                }
                if (isDisposed || hasLoaded || pendingRetry != null) return

                if (retryCount < 4) {
                    val delays = longArrayOf(8_000L, 15_000L, 30_000L, 60_000L)
                    val delay = delays[retryCount++]
                    val retry = Runnable {
                        pendingRetry = null
                        tryLoadIfReady(fromRetry = true)
                    }
                    pendingRetry = retry
                    handler.postDelayed(retry, delay)
                } else {
                    retriesExhausted = true
                }
            }
        }

        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )
        container.removeAllViews()
        container.addView(view, params)
        try {
            view.resume()
        } catch (_: Throwable) {
        }
        adView = view
        return view
    }

    fun tryLoadIfReady(fromRetry: Boolean = false) {
        if (isDisposed || !sdkInitialized || hasLoaded || isLoading) return
        if (!fromRetry && (pendingRetry != null || retriesExhausted)) return
        if (!container.isAttachedToWindow) return

        val view = ensureAdViewCreated() ?: return
        isLoading = true
        container.visibility = View.VISIBLE
        onVisibilityChanged(true)
        view.post {
            if (isDisposed || hasLoaded || adView !== view) {
                isLoading = false
                return@post
            }
            try {
                view.loadAd(AdRequest.Builder().build())
            } catch (t: Throwable) {
                isLoading = false
                if (!hasLoaded) {
                    container.visibility = View.GONE
                    onVisibilityChanged(false)
                }
                Log.e("TopBannerAd", "Banner load exception", t)
            }
        }
    }

    fun resume() {
        if (!isDisposed) {
            try {
                adView?.resume()
            } catch (_: Throwable) {
            }
            if (!hasLoaded && !isLoading && pendingRetry == null && !retriesExhausted) {
                container.post { tryLoadIfReady() }
            }
        }
    }

    fun pause() {
        if (!isDisposed) {
            try {
                adView?.pause()
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
        destroyCurrentAdView()
        try {
            container.removeAllViews()
        } catch (_: Throwable) {
        }
    }
}

@Composable
fun TopBannerAd(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var showBannerSpace by remember { mutableStateOf(true) }
    val holder = remember(context) {
        AdManager.initialize(context)
        BannerAdHolder(
            context = context,
            onVisibilityChanged = { visible -> showBannerSpace = visible }
        )
    }
    val isInitialized by AdManager.isInitializedFlow.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(isInitialized, holder) {
        holder.setSdkInitialized(isInitialized)
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

    // No empty grey box or "Ad" placeholder: collapses cleanly to 0dp when no banner ad is available
    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (showBannerSpace) {
                    Modifier.windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                } else {
                    Modifier
                }
            )
            .animateContentSize()
            .testTag("banner_ad_container"),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (showBannerSpace) 52.dp else 0.dp),
            factory = { holder.container },
            update = {
                holder.setSdkInitialized(isInitialized)
            }
        )
    }
}
