package com.snapsave.app.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.snapsave.app.MainActivity
import com.snapsave.app.R
import com.snapsave.app.SnapSaveApp
import com.snapsave.app.core.HapticKind
import com.snapsave.app.core.LanguageDetector
import com.snapsave.app.core.haptic
import com.snapsave.app.data.SnippetEntity
import com.snapsave.app.data.SnippetRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max

class SnippetOverlayService : Service() {

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main + CoroutineExceptionHandler { _, error ->
            android.util.Log.e("SnapSaveOverlay", "Overlay operation failed", error)
        }
    )

    private lateinit var windowManager: WindowManager
    private lateinit var repository: SnippetRepository

    enum class PanelLifecycleState {
        CLOSED,
        OPENING,
        OPEN,
        CLOSING
    }

    private var panelState = PanelLifecycleState.CLOSED
    private var isPanelOpen = false

    private var bubbleView: View? = null
    private var bubbleBg: GradientDrawable? = null
    private var bubbleIcon: ImageView? = null

    private var panelRoot: FrameLayout? = null
    private var panelSurfaceView: View? = null
    private var panelBg: GradientDrawable? = null

    private var panelContent: LinearLayout? = null
    private var chromeContainer: LinearLayout? = null
    private var panelHeaderView: LinearLayout? = null
    private var panelTitleView: TextView? = null
    private var searchBoxContainer: LinearLayout? = null
    private var searchEditText: EditText? = null
    private var searchClearBtn: ImageView? = null
    private var searchBg: GradientDrawable? = null
    private var categoryScrollView: HorizontalScrollView? = null
    private var chipsContainer: LinearLayout? = null
    private var isGridMode = true
    private var gridToggleBtnView: ImageView? = null

    private var snippetRecyclerView: androidx.recyclerview.widget.RecyclerView? = null
    private var snippetAdapter: OverlaySnippetAdapter? = null
    private var emptyStateTextView: TextView? = null

    private var closeBtnView: ImageView? = null
    private var closeBtnBg: GradientDrawable? = null
    private var closeOverlayAttached = false
    private var closeOverlaySizePx = 0
    private lateinit var closeOverlayParams: WindowManager.LayoutParams

    private var resizeBtnView: ImageView? = null
    private var resizeHandleBg: GradientDrawable? = null

    private lateinit var bubbleParams: WindowManager.LayoutParams
    private lateinit var panelParams: WindowManager.LayoutParams

    private var selectedCategory = "All"
    private var searchQuery = ""
    private var searchDebounceJob: Job? = null
    private var allSnippets = emptyList<SnippetEntity>()
    private var panelGeneration = 0L

    /** Transient slider previews (never persisted) + the 5s reveal deadline. */
    private val appearanceState = OverlayAppearanceState()
    private var revealJob: Job? = null

    companion object {
        const val CHANNEL_ID = "snapsave_overlay_channel"
        const val NOTIF_ID = 2002
        const val ACTION_REFRESH_CONFIGURATION = "com.snapsave.app.action.REFRESH_OVERLAY_CONFIGURATION"
        const val ACTION_UPDATE_APPEARANCE = "com.snapsave.app.action.UPDATE_APPEARANCE"
        const val ACTION_UPDATE_SHADOW = "com.snapsave.app.action.UPDATE_SHADOW"
        const val ACTION_REVEAL_CONTROLS = "com.snapsave.app.action.REVEAL_CONTROLS"
        const val ACTION_PREVIEW_APPEARANCE = "com.snapsave.app.PREVIEW_APPEARANCE"
        const val EXTRA_APPEARANCE_LAYER = "appearance_layer"
        const val EXTRA_APPEARANCE_VALUE = "appearance_value"
        var isRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun hasOverlayPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            android.provider.Settings.canDrawOverlays(this)
    }

    private fun nowMs(): Long = try {
        SystemClock.uptimeMillis()
    } catch (_: Exception) {
        0L
    }

    private fun currentPalette(): SnippetOverlayPalette {
        return SnippetOverlayPaletteResolver.resolve(this)
    }

    private fun withAlpha(color: Int, alpha: Int): Int = Color.argb(
        alpha.coerceIn(0, 255),
        Color.red(color),
        Color.green(color),
        Color.blue(color)
    )

    private fun effectiveBubbleOpacity(): Float = appearanceState.opacity(
        "bubble",
        OverlayOpacityPolicy.clamp(SnippetOverlayPreferences.bubbleOpacity(this)),
        nowMs()
    )

    private fun effectiveMasterOpacity(): Float = appearanceState.opacity(
        "master",
        OverlayOpacityPolicy.clamp(SnippetOverlayPreferences.popupMasterOpacity(this)),
        nowMs()
    )

    private fun effectiveSurfaceOpacity(): Float {
        val surface = SnippetOverlayPreferences.popupSurfaceOpacity(this)
        return appearanceState.opacity(
            "surface",
            effectiveMasterOpacity() * OverlayOpacityPolicy.clamp(surface),
            nowMs()
        )
    }

    private fun effectiveSnippetsOpacity(): Float {
        val snippets = SnippetOverlayPreferences.popupSnippetsOpacity(this)
        return appearanceState.opacity(
            "snippets",
            effectiveMasterOpacity() * OverlayOpacityPolicy.clamp(snippets),
            nowMs()
        )
    }

    private fun effectiveChromeOpacity(): Float {
        val chrome = SnippetOverlayPreferences.popupChromeOpacity(this)
        return appearanceState.opacity(
            "chrome",
            effectiveMasterOpacity() * OverlayOpacityPolicy.clamp(chrome),
            nowMs()
        )
    }

    private fun effectiveCloseOpacity(): Float {
        val close = SnippetOverlayPreferences.popupCloseOpacity(this)
        return appearanceState.opacity(
            "close",
            effectiveMasterOpacity() * OverlayOpacityPolicy.clamp(close),
            nowMs()
        )
    }

    private fun effectiveResizeOpacity(): Float {
        val resize = SnippetOverlayPreferences.popupResizeOpacity(this)
        return appearanceState.opacity(
            "resize",
            effectiveMasterOpacity() * OverlayOpacityPolicy.clamp(resize),
            nowMs()
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!hasOverlayPermission()) {
            isRunning = false
            stopSelf()
            return START_NOT_STICKY
        }
        if (::windowManager.isInitialized) {
            when (intent?.action) {
                ACTION_REFRESH_CONFIGURATION -> {
                    reflowOverlayViews()
                    refreshOverlayConfiguration()
                }
                ACTION_UPDATE_APPEARANCE -> {
                    appearanceState.clearPreviews()
                    updateOverlayAppearance()
                }
                ACTION_UPDATE_SHADOW -> {
                    updateSnippetShadows()
                }
                ACTION_REVEAL_CONTROLS -> {
                    revealOverlayControls()
                }
                ACTION_PREVIEW_APPEARANCE -> {
                    val layer = intent.getStringExtra(EXTRA_APPEARANCE_LAYER)
                    val value = intent.getFloatExtra(EXTRA_APPEARANCE_VALUE, Float.NaN)
                    if (appearanceState.preview(layer, value)) {
                        updateOverlayAppearance()
                    }
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(this)) {
            isRunning = false
            stopSelf()
            return
        }
        isRunning = true
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        repository = (application as SnapSaveApp).container.repository

        startForegroundServiceNotification()
        try {
            setupBubbleView()
            setupPanelView()
        } catch (e: Exception) {
            android.util.Log.e("SnapSaveOverlay", "Failed to setup views", e)
            isRunning = false
            stopSelf()
            return
        }

        serviceScope.launch {
            repository.snippets.collect { list ->
                allSnippets = list
                updateCategoryChips()
                filterAndSubmitSnippets()
            }
        }
    }

    private fun startForegroundServiceNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SnapSave Floating Snippets",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Quick floating access to saved snippets & long text"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SnapSave Quick Snippets")
            .setContentText("Tap floating button to copy/paste snippets")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIF_ID, notification)
    }

    private fun currentOverlayBounds(): Pair<Int, Int> {
        val metrics = resources.displayMetrics
        return Pair(metrics.widthPixels, metrics.heightPixels)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        reflowOverlayViews()
    }

    private fun updateOverlayAppearance() {
        bubbleView?.alpha = effectiveBubbleOpacity()

        if (isPanelOpen) {
            panelSurfaceView?.alpha = effectiveSurfaceOpacity()
            chromeContainer?.alpha = effectiveChromeOpacity()
            snippetRecyclerView?.alpha = effectiveSnippetsOpacity()
            closeBtnView?.alpha = effectiveCloseOpacity()
            resizeBtnView?.alpha = effectiveResizeOpacity()
        }

        val palette = currentPalette()
        val density = resources.displayMetrics.density

        bubbleBg?.apply {
            setColor(withAlpha(palette.surfaceColor, if (palette.isDark) 240 else 250))
            setStroke((2 * density).toInt(), palette.primaryColor)
        }
        bubbleIcon?.setColorFilter(palette.primaryColor)

        panelBg?.apply {
            cornerRadius = 24 * density
            setColor(withAlpha(palette.surfaceColor, if (palette.isDark) 244 else 252))
            setStroke((1.2f * density).toInt(), withAlpha(palette.outlineColor, if (palette.isDark) 70 else 80))
        }
        closeBtnView?.setColorFilter(palette.textColor)
        closeBtnBg?.apply {
            setColor(withAlpha(palette.surfaceColor, if (palette.isDark) 200 else 230))
            setStroke((1 * density).toInt(), withAlpha(palette.outlineColor, if (palette.isDark) 70 else 80))
        }
        resizeBtnView?.setColorFilter(palette.accentColor)
        resizeHandleBg?.setColor(withAlpha(palette.surfaceVariantColor, if (palette.isDark) 190 else 220))
        emptyStateTextView?.setTextColor(palette.mutedTextColor)
        searchBg?.setColor(withAlpha(palette.surfaceVariantColor, if (palette.isDark) 150 else 210))
    }

    private fun updateSnippetShadows() {
        filterAndSubmitSnippets(force = true)
    }

    private fun revealOverlayControls() {
        revealJob?.cancel()
        appearanceState.reveal(nowMs())
        updateOverlayAppearance()
        revealJob = serviceScope.launch {
            delay(OverlayAppearanceState.REVEAL_DURATION_MS)
            updateOverlayAppearance()
        }
    }

    private fun syncSearchRow(showSearch: Boolean) {
        if (showSearch) return
        searchDebounceJob?.cancel()
        searchDebounceJob = null
        searchQuery = ""
        val box = searchEditText
        if (box != null && box.text.isNotEmpty()) {
            box.setText("")
        }
        searchClearBtn?.visibility = View.GONE
    }

    private fun updateLayoutManager() {
        val recycler = snippetRecyclerView ?: return
        val density = resources.displayMetrics.density
        val panelW = if (::panelParams.isInitialized) panelParams.width else (SnippetOverlayLayoutPolicy.DEFAULT_PANEL_WIDTH_DP * density).toInt()
        isGridMode = SnippetOverlayPreferences.isGridView(this)
        if (isGridMode) {
            val cols = if (panelW >= 400 * density) 3 else 2
            val currentLm = recycler.layoutManager
            if (currentLm is androidx.recyclerview.widget.GridLayoutManager) {
                currentLm.spanCount = cols
            } else {
                recycler.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, cols)
            }
        } else {
            val currentLm = recycler.layoutManager
            if (currentLm !is androidx.recyclerview.widget.LinearLayoutManager || currentLm is androidx.recyclerview.widget.GridLayoutManager) {
                recycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
            }
        }
        snippetAdapter?.isGrid = isGridMode
        snippetAdapter?.notifyDataSetChanged()
        gridToggleBtnView?.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                if (isGridMode) R.drawable.ic_overlay_list else R.drawable.ic_overlay_grid
            )
        )
    }

    private fun refreshOverlayConfiguration() {
        val density = resources.displayMetrics.density
        val palette = currentPalette()

        val bubbleSizeDp = SnippetOverlayPreferences.bubbleSizeDp(this)
        val bubbleSize = (bubbleSizeDp * density).toInt()
        bubbleView?.let { bubble ->
            if (::bubbleParams.isInitialized) {
                bubbleParams.width = bubbleSize
                bubbleParams.height = bubbleSize
                bubbleBg?.apply {
                    setColor(withAlpha(palette.surfaceColor, if (palette.isDark) 240 else 250))
                    setStroke((2 * density).toInt(), palette.primaryColor)
                }
                bubbleIcon?.setColorFilter(palette.primaryColor)
                if (bubble.isAttachedToWindow) {
                    windowManager.updateViewLayout(bubble, bubbleParams)
                }
            }
        }

        val showTitle = SnippetOverlayPreferences.showTitle(this)
        val showSearch = SnippetOverlayPreferences.showSearch(this)
        val showCategories = SnippetOverlayPreferences.showCategories(this)

        panelHeaderView?.visibility = if (showTitle) View.VISIBLE else View.GONE
        panelTitleView?.visibility = if (showTitle) View.VISIBLE else View.GONE
        searchBoxContainer?.visibility = if (showSearch) View.VISIBLE else View.GONE
        categoryScrollView?.visibility = if (showCategories) View.VISIBLE else View.GONE

        syncSearchRow(showSearch)

        panelContent?.let { content ->
            val isGridOnly = !showTitle && !showSearch && !showCategories
            val outerPad = if (isGridOnly) (4 * density).toInt() else (6 * density).toInt()
            val topPad = if (!showTitle && !showSearch && !showCategories) {
                (20 * density).toInt()
            } else if (!showTitle && !showSearch) {
                (14 * density).toInt()
            } else if (!showTitle) {
                (10 * density).toInt()
            } else {
                (4 * density).toInt()
            }
            content.setPadding(outerPad, topPad, outerPad, (10 * density).toInt())
        }

        updateLayoutManager()
        updateOverlayAppearance()

        panelRoot?.let { panel ->
            if (panel.isAttachedToWindow && ::panelParams.isInitialized) {
                windowManager.updateViewLayout(panel, panelParams)
            }
        }
        updateCloseOverlayLayout()
    }

    private fun reflowOverlayViews() {
        val (screenW, screenH) = currentOverlayBounds()

        // 1. Reflow bubble position using saved normalized fractions
        bubbleView?.let { bubble ->
            if (bubble.isAttachedToWindow && ::bubbleParams.isInitialized) {
                val fracX = SnippetOverlayPreferences.bubblePositionFractionX(this)
                val fracY = SnippetOverlayPreferences.bubblePositionFractionY(this)
                val bounds = SnippetOverlayLayoutPolicy.clampBubbleBounds(
                    x = bubbleParams.x,
                    y = bubbleParams.y,
                    bubbleSize = bubbleParams.width,
                    screenWidth = screenW,
                    screenHeight = screenH
                )
                val (newX, newY) = SnippetOverlayLayoutPolicy.denormalizePosition(
                    SnippetOverlayLayoutPolicy.NormalizedPosition(fracX, fracY),
                    bounds.maxX,
                    bounds.maxY
                )
                bubbleParams.x = newX
                bubbleParams.y = newY
                windowManager.updateViewLayout(bubble, bubbleParams)
            }
        }

        // 2. Reflow panel geometry
        if (::panelParams.isInitialized) {
            val density = resources.displayMetrics.density
            val showTitle = SnippetOverlayPreferences.showTitle(this)
            val showSearch = SnippetOverlayPreferences.showSearch(this)
            val showCategories = SnippetOverlayPreferences.showCategories(this)

            val geometry = SnippetOverlayLayoutPolicy.computePopupGeometry(
                requestedX = panelParams.x,
                requestedY = panelParams.y,
                requestedWidth = panelParams.width,
                requestedHeight = panelParams.height,
                screenWidth = screenW,
                screenHeight = screenH,
                density = density,
                showTitle = showTitle,
                showSearch = showSearch,
                showCategories = showCategories,
                closeButtonSizePx = closeOverlaySizePx,
                closeOffsetPx = (4 * density).toInt().coerceAtLeast(4)
            )
            panelParams.x = geometry.panelBounds.x
            panelParams.y = geometry.panelBounds.y
            panelParams.width = geometry.panelBounds.width
            panelParams.height = geometry.panelBounds.height

            SnippetOverlayPreferences.setPanelPosition(this, geometry.panelBounds.x, geometry.panelBounds.y)
            SnippetOverlayPreferences.setPanelWidthPx(this, geometry.panelBounds.width)
            SnippetOverlayPreferences.setPanelHeightPx(this, geometry.panelBounds.height)

            panelRoot?.let { panel ->
                if (panel.isAttachedToWindow) {
                    windowManager.updateViewLayout(panel, panelParams)
                }
            }
            updateCloseOverlayLayout()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupBubbleView() {
        val density = resources.displayMetrics.density
        val bubbleSizeDp = SnippetOverlayPreferences.bubbleSizeDp(this)
        val bubbleSize = (bubbleSizeDp * density).toInt()
        val (screenW, screenH) = currentOverlayBounds()

        val fracX = SnippetOverlayPreferences.bubblePositionFractionX(this)
        val fracY = SnippetOverlayPreferences.bubblePositionFractionY(this)
        val bounds = SnippetOverlayLayoutPolicy.clampBubbleBounds(
            x = 0,
            y = 0,
            bubbleSize = bubbleSize,
            screenWidth = screenW,
            screenHeight = screenH
        )
        val (initialX, initialY) = SnippetOverlayLayoutPolicy.denormalizePosition(
            SnippetOverlayLayoutPolicy.NormalizedPosition(fracX, fracY),
            bounds.maxX,
            bounds.maxY
        )

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        bubbleParams = WindowManager.LayoutParams(
            bubbleSize,
            bubbleSize,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = initialX
            y = initialY
        }

        val palette = currentPalette()
        val bubble = FrameLayout(this).apply {
            alpha = effectiveBubbleOpacity()
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(withAlpha(palette.surfaceColor, if (palette.isDark) 240 else 250))
                setStroke((2 * density).toInt(), palette.primaryColor)
            }
            bubbleBg = bg
            background = bg
            elevation = 16f

            val icon = ImageView(this@SnippetOverlayService).apply {
                setImageDrawable(ContextCompat.getDrawable(this@SnippetOverlayService, R.drawable.ic_overlay_copy))
                setColorFilter(palette.primaryColor)
                val pad = (bubbleSize * 0.24f).toInt()
                setPadding(pad, pad, pad, pad)
            }
            bubbleIcon = icon
            addView(icon)
        }

        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop
        var dragStartX = 0
        var dragStartY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isClick = false

        bubble.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    dragStartX = bubbleParams.x
                    dragStartY = bubbleParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (hypot(dx.toDouble(), dy.toDouble()) > touchSlop) {
                        isClick = false
                    }
                    val (currentW, currentH) = currentOverlayBounds()
                    val clamped = SnippetOverlayLayoutPolicy.clampBubbleBounds(
                        x = dragStartX + dx,
                        y = dragStartY + dy,
                        bubbleSize = bubbleParams.width,
                        screenWidth = currentW,
                        screenHeight = currentH
                    )
                    bubbleParams.x = clamped.x
                    bubbleParams.y = clamped.y
                    windowManager.updateViewLayout(bubble, bubbleParams)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isClick && event.actionMasked == MotionEvent.ACTION_UP) {
                        v.performClick()
                    } else {
                        val (currentW, currentH) = currentOverlayBounds()
                        val clamped = SnippetOverlayLayoutPolicy.clampBubbleBounds(
                            x = bubbleParams.x,
                            y = bubbleParams.y,
                            bubbleSize = bubbleParams.width,
                            screenWidth = currentW,
                            screenHeight = currentH
                        )
                        val normalized = SnippetOverlayLayoutPolicy.normalizePosition(
                            x = clamped.x,
                            y = clamped.y,
                            maxX = clamped.maxX,
                            maxY = clamped.maxY
                        )
                        SnippetOverlayPreferences.setBubblePositionFraction(
                            this@SnippetOverlayService,
                            normalized.fractionX,
                            normalized.fractionY
                        )
                    }
                    true
                }
                else -> false
            }
        }

        bubble.setOnClickListener {
            it.haptic(HapticKind.CLICK)
            togglePanel()
        }

        bubbleView = bubble
        windowManager.addView(bubble, bubbleParams)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupPanelView() {
        val density = resources.displayMetrics.density
        val (screenW, screenH) = currentOverlayBounds()

        val showTitle = SnippetOverlayPreferences.showTitle(this)
        val showSearch = SnippetOverlayPreferences.showSearch(this)
        val showCategories = SnippetOverlayPreferences.showCategories(this)

        val minPanelWidth = SnippetOverlayLayoutPolicy.minPanelWidthPx(density)
        val minPanelHeight = SnippetOverlayLayoutPolicy.minPanelHeightPx(
            density = density,
            showTitle = showTitle,
            showSearch = showSearch,
            showCategories = showCategories
        )
        val maxPanelWidth = (screenW * 0.94f).toInt()
        val maxPanelHeight = (screenH * 0.85f).toInt()

        val savedWidth = SnippetOverlayPreferences.panelWidthPx(this)
        val savedHeight = SnippetOverlayPreferences.panelHeightPx(this)
        val savedX = SnippetOverlayPreferences.panelPositionX(this)
        val savedY = SnippetOverlayPreferences.panelPositionY(this)

        val initialW = if (savedWidth > 0) savedWidth else (SnippetOverlayLayoutPolicy.DEFAULT_PANEL_WIDTH_DP * density).toInt()
        val initialH = if (savedHeight > 0) savedHeight else (SnippetOverlayLayoutPolicy.DEFAULT_PANEL_HEIGHT_DP * density).toInt()
        val initialX = if (savedX >= 0) savedX else (screenW - initialW) / 2
        val initialY = if (savedY >= 0) savedY else (screenH - initialH) / 3

        val clampedBounds = SnippetOverlayLayoutPolicy.clampPanelBounds(
            x = initialX,
            y = initialY,
            width = initialW,
            height = initialH,
            screenWidth = screenW,
            screenHeight = screenH,
            minWidth = minPanelWidth,
            minHeight = minPanelHeight,
            maxWidth = maxPanelWidth,
            maxHeight = maxPanelHeight
        )

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        panelParams = WindowManager.LayoutParams(
            clampedBounds.width,
            clampedBounds.height,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = clampedBounds.x
            y = clampedBounds.y
        }

        val palette = currentPalette()

        // 1. Root popup FrameLayout: translucent container
        val root = FrameLayout(this).apply {
            clipChildren = false
            clipToPadding = false
        }

        // 2. Layer 1: Background Surface View (Bo tròn 24dp mềm mại chuẩn Material 3)
        val surfaceLayer = View(this).apply {
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 24 * density
                setColor(withAlpha(palette.surfaceColor, if (palette.isDark) 244 else 252))
                setStroke((1.2f * density).toInt(), withAlpha(palette.outlineColor, if (palette.isDark) 70 else 80))
            }
            panelBg = bg
            background = bg
            elevation = 12f
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            alpha = effectiveSurfaceOpacity()
        }
        panelSurfaceView = surfaceLayer
        root.addView(surfaceLayer)

        // 3. Layer 2: Panel Content LinearLayout
        val isGridOnly = !showTitle && !showSearch && !showCategories
        val outerPad = if (isGridOnly) (4 * density).toInt() else (6 * density).toInt()
        val topPad = if (!showTitle && !showSearch && !showCategories) {
            (20 * density).toInt()
        } else if (!showTitle && !showSearch) {
            (14 * density).toInt()
        } else if (!showTitle) {
            (10 * density).toInt()
        } else {
            (4 * density).toInt()
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            elevation = 12f
            setPadding(outerPad, topPad, outerPad, (10 * density).toInt())
        }
        panelContent = content

        // Chrome Container (Header, Search, Categories)
        val chrome = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.TRANSPARENT)
            elevation = 0f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            alpha = effectiveChromeOpacity()
        }
        chromeContainer = chrome

        var initialPanelX = 0
        var initialPanelY = 0
        var initialTouchPanelX = 0f
        var initialTouchPanelY = 0f
        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop
        var draggingPanel = false

        val panelDragListener = View.OnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    initialPanelX = panelParams.x
                    initialPanelY = panelParams.y
                    initialTouchPanelX = event.rawX
                    initialTouchPanelY = event.rawY
                    draggingPanel = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchPanelX).toInt()
                    val dy = (event.rawY - initialTouchPanelY).toInt()
                    if (!draggingPanel && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                        draggingPanel = true
                    }
                    if (draggingPanel) {
                        val (currentW, currentH) = currentOverlayBounds()
                        panelParams.x = (initialPanelX + dx).coerceIn(0, max(0, currentW - panelParams.width))
                        panelParams.y = (initialPanelY + dy).coerceIn(0, max(0, currentH - panelParams.height))
                        windowManager.updateViewLayout(root, panelParams)
                        updateCloseOverlayLayout()
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (draggingPanel) {
                        SnippetOverlayPreferences.setPanelPosition(this@SnippetOverlayService, panelParams.x, panelParams.y)
                    }
                    true
                }
                else -> false
            }
        }

        // A. Header Bar
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding((10 * density).toInt(), (4 * density).toInt(), (10 * density).toInt(), (4 * density).toInt())
            visibility = if (showTitle) View.VISIBLE else View.GONE
            setOnTouchListener(panelDragListener)
        }
        panelHeaderView = header

        val title = TextView(this).apply {
            text = "SnapSave"
            setTextColor(palette.textColor)
            textSize = 14.5f
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        panelTitleView = title
        header.addView(title)

        // Grid / List View Toggle Button
        isGridMode = SnippetOverlayPreferences.isGridView(this)
        val gridToggleBtn = ImageView(this).apply {
            val s = (28 * density).toInt()
            val pad = (5 * density).toInt()
            setPadding(pad, pad, pad, pad)
            val btnBg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(withAlpha(palette.surfaceVariantColor, if (palette.isDark) 140 else 200))
            }
            background = btnBg
            setImageDrawable(ContextCompat.getDrawable(this@SnippetOverlayService, if (isGridMode) R.drawable.ic_overlay_list else R.drawable.ic_overlay_grid))
            setColorFilter(palette.textColor)
            layoutParams = LinearLayout.LayoutParams(s, s).apply {
                setMargins(0, 0, (8 * density).toInt(), 0)
            }
            contentDescription = "Chuyển chế độ xem lưới / danh sách"
            setOnClickListener {
                it.haptic(HapticKind.CLICK)
                isGridMode = !isGridMode
                SnippetOverlayPreferences.setIsGridView(this@SnippetOverlayService, isGridMode)
                setImageDrawable(ContextCompat.getDrawable(this@SnippetOverlayService, if (isGridMode) R.drawable.ic_overlay_list else R.drawable.ic_overlay_grid))
                updateLayoutManager()
            }
        }
        gridToggleBtnView = gridToggleBtn
        header.addView(gridToggleBtn)

        // Save Clipboard Button in Header
        val saveClipboardBtn = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val padH = (9 * density).toInt()
            val padV = (4 * density).toInt()
            setPadding(padH, padV, padH, padV)
            val btnBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 10 * density
                setColor(palette.primaryContainerColor)
            }
            background = btnBg

            val plusIcon = ImageView(this@SnippetOverlayService).apply {
                setImageDrawable(ContextCompat.getDrawable(this@SnippetOverlayService, R.drawable.ic_overlay_plus))
                setColorFilter(palette.onPrimaryContainerColor)
                val s = (13 * density).toInt()
                layoutParams = LinearLayout.LayoutParams(s, s).apply {
                    setMargins(0, 0, (4 * density).toInt(), 0)
                }
            }
            val btnText = TextView(this@SnippetOverlayService).apply {
                text = "Lưu Clipboard"
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(palette.onPrimaryContainerColor)
            }
            addView(plusIcon)
            addView(btnText)

            setOnClickListener {
                it.haptic(HapticKind.CLICK)
                quickSaveCurrentClipboard()
            }
        }
        header.addView(saveClipboardBtn)
        chrome.addView(header)

        // B. Search Box Container (~38dp, bo tròn 14dp, search icon + input + clear button)
        val searchContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val sBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 14 * density
                setColor(withAlpha(palette.surfaceVariantColor, if (palette.isDark) 150 else 210))
            }
            searchBg = sBg
            background = sBg
            val pH = (10 * density).toInt()
            val pV = (4 * density).toInt()
            setPadding(pH, pV, pH, pV)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (38 * density).toInt()
            ).apply {
                setMargins((4 * density).toInt(), (2 * density).toInt(), (4 * density).toInt(), (6 * density).toInt())
            }
            visibility = if (showSearch) View.VISIBLE else View.GONE
        }
        searchBoxContainer = searchContainer

        val searchIcon = ImageView(this).apply {
            setImageDrawable(ContextCompat.getDrawable(this@SnippetOverlayService, R.drawable.ic_overlay_search))
            setColorFilter(withAlpha(palette.mutedTextColor, 180))
            val s = (16 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(s, s).apply {
                setMargins(0, 0, (8 * density).toInt(), 0)
            }
        }
        searchContainer.addView(searchIcon)

        val search = EditText(this).apply {
            hint = "Tìm snippet, nội dung, ngôn ngữ…"
            setHintTextColor(withAlpha(palette.mutedTextColor, 160))
            setTextColor(palette.textColor)
            textSize = 12.5f
            setSingleLine(true)
            background = null
            setPadding(0, 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val q = s?.toString()?.trim()?.lowercase().orEmpty()
                    searchClearBtn?.visibility = if (q.isNotEmpty()) View.VISIBLE else View.GONE
                    searchDebounceJob?.cancel()
                    searchDebounceJob = serviceScope.launch {
                        delay(200)
                        searchQuery = q
                        filterAndSubmitSnippets()
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
        searchEditText = search
        searchContainer.addView(search)

        val clearBtn = ImageView(this).apply {
            setImageDrawable(ContextCompat.getDrawable(this@SnippetOverlayService, R.drawable.ic_overlay_close))
            setColorFilter(palette.mutedTextColor)
            val s = (18 * density).toInt()
            val pad = (2 * density).toInt()
            setPadding(pad, pad, pad, pad)
            layoutParams = LinearLayout.LayoutParams(s, s).apply {
                setMargins((4 * density).toInt(), 0, 0, 0)
            }
            visibility = View.GONE
            setOnClickListener {
                it.haptic(HapticKind.CLICK)
                search.setText("")
                searchQuery = ""
                filterAndSubmitSnippets()
            }
        }
        searchClearBtn = clearBtn
        searchContainer.addView(clearBtn)
        chrome.addView(searchContainer)

        // C. Category Chips (Thanh cuộn ngang, bo góc dạng pill 18dp)
        val chipsScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            setBackgroundColor(Color.TRANSPARENT)
            elevation = 0f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins((4 * density).toInt(), 0, (4 * density).toInt(), (6 * density).toInt())
            }
            visibility = if (showCategories) View.VISIBLE else View.GONE
        }
        categoryScrollView = chipsScroll
        val chipsGroup = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        chipsContainer = chipsGroup
        chipsScroll.addView(chipsGroup)
        chrome.addView(chipsScroll)
        content.addView(chrome)

        // D. Snippets RecyclerView
        val recycler = androidx.recyclerview.widget.RecyclerView(this).apply {
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            clipToPadding = false
            alpha = effectiveSnippetsOpacity()
        }
        snippetRecyclerView = recycler
        snippetAdapter = OverlaySnippetAdapter(
            scope = serviceScope,
            onSelected = { view, snippet -> onSnippetSelected(view, snippet) },
            onLongClick = { view, snippet -> onSnippetLongClick(view, snippet) }
        ).also { recycler.adapter = it }
        content.addView(recycler)
        updateLayoutManager()

        root.addView(content)

        // Allow dragging from the top rim of panel if title is hidden
        var rimDragActive = false
        root.setOnTouchListener { _, event ->
            val showTitleNow = SnippetOverlayPreferences.showTitle(this@SnippetOverlayService)
            if (!showTitleNow) {
                val topRimHeight = (26 * density).toInt()
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        rimDragActive = event.y <= topRimHeight
                        if (rimDragActive) {
                            panelDragListener.onTouch(root, event)
                        } else false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (rimDragActive) {
                            panelDragListener.onTouch(root, event)
                        } else false
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        if (rimDragActive) {
                            rimDragActive = false
                            panelDragListener.onTouch(root, event)
                        } else false
                    }
                    else -> false
                }
            } else {
                rimDragActive = false
                false
            }
        }

        // Empty state view
        val emptyView = TextView(this).apply {
            text = "Không tìm thấy snippet nào"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(palette.mutedTextColor)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            visibility = View.GONE
        }
        emptyStateTextView = emptyView
        root.addView(emptyView)

        // 4. Layer 3: Floating Controls Overlaying the Popup Edges

        // Top-End Close Button (Tap closes, Hold and drag moves popup)
        val closeBtn = ImageView(this).apply {
            setImageDrawable(ContextCompat.getDrawable(this@SnippetOverlayService, R.drawable.ic_overlay_close))
            setColorFilter(palette.textColor)
            val btnSize = (SnippetOverlayLayoutPolicy.CLOSE_CONTROL_SIZE_DP * density).toInt()
            val pad = (7 * density).toInt()
            setPadding(pad, pad, pad, pad)
            val btnBg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(withAlpha(palette.surfaceColor, if (palette.isDark) 200 else 220))
                setStroke((1 * density).toInt(), withAlpha(palette.outlineColor, if (palette.isDark) 60 else 70))
            }
            closeBtnBg = btnBg
            background = btnBg
            contentDescription = "Đóng popup. Nhấn giữ để di chuyển"
            elevation = 14f
            closeOverlaySizePx = btnSize
        }
        closeOverlayParams = WindowManager.LayoutParams(
            closeOverlaySizePx, closeOverlaySizePx, layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START }

        var closeInitialPanelX = 0
        var closeInitialPanelY = 0
        var closeInitialTouchX = 0f
        var closeInitialTouchY = 0f
        var movingFromClose = false
        var closeGestureCancelled = false
        var closePressActive = false
        val closeLongPress = Runnable {
            if (closePressActive && !closeGestureCancelled) {
                movingFromClose = true
                closeBtn.haptic(HapticKind.TICK)
                closeBtn.animate().scaleX(0.9f).scaleY(0.9f).alpha(0.8f).setDuration(120).start()
            }
        }
        closeBtn.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    closeInitialPanelX = panelParams.x
                    closeInitialPanelY = panelParams.y
                    closeInitialTouchX = event.rawX
                    closeInitialTouchY = event.rawY
                    movingFromClose = false
                    closeGestureCancelled = false
                    closePressActive = true
                    view.postDelayed(closeLongPress, ViewConfiguration.getLongPressTimeout().toLong())
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - closeInitialTouchX).toInt()
                    val dy = (event.rawY - closeInitialTouchY).toInt()
                    if (!movingFromClose && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                        closeGestureCancelled = true
                        view.removeCallbacks(closeLongPress)
                    }
                    if (movingFromClose) {
                        val (currentW, currentH) = currentOverlayBounds()
                        panelParams.x = (closeInitialPanelX + dx).coerceIn(0, max(0, currentW - panelParams.width))
                        panelParams.y = (closeInitialPanelY + dy).coerceIn(0, max(0, currentH - panelParams.height))
                        windowManager.updateViewLayout(root, panelParams)
                        updateCloseOverlayLayout()
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.removeCallbacks(closeLongPress)
                    closePressActive = false
                    closeBtn.animate().scaleX(1f).scaleY(1f).alpha(effectiveCloseOpacity()).setDuration(160).start()
                    if (movingFromClose) {
                        SnippetOverlayPreferences.setPanelPosition(this@SnippetOverlayService, panelParams.x, panelParams.y)
                    } else if (!closeGestureCancelled && event.actionMasked == MotionEvent.ACTION_UP) {
                        view.performClick()
                    }
                    movingFromClose = false
                    true
                }
                else -> true
            }
        }
        closeBtn.setOnClickListener { view ->
            view.haptic(HapticKind.CLICK)
            togglePanel()
        }
        closeBtn.alpha = effectiveCloseOpacity()
        closeBtnView = closeBtn

        // Resize Handle (Bottom-End)
        val resizeBtn = ImageView(this).apply {
            setImageDrawable(ContextCompat.getDrawable(this@SnippetOverlayService, R.drawable.ic_overlay_resize))
            setColorFilter(palette.accentColor)
            val btnSize = (34 * density).toInt()
            val pad = (9 * density).toInt()
            setPadding(pad, pad, pad, pad)
            val handleBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadii = floatArrayOf(
                    8 * density, 8 * density,
                    4 * density, 4 * density,
                    20 * density, 20 * density,
                    4 * density, 4 * density
                )
                setColor(withAlpha(palette.surfaceVariantColor, if (palette.isDark) 190 else 220))
            }
            resizeHandleBg = handleBg
            background = handleBg
            contentDescription = "Resize quick snippets"
            elevation = 14f
            layoutParams = FrameLayout.LayoutParams(btnSize, btnSize).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                setMargins(0, 0, (2 * density).toInt(), (2 * density).toInt())
            }
        }

        var initialPanelWidth = 0
        var initialPanelHeight = 0
        var initialResizeTouchX = 0f
        var initialResizeTouchY = 0f

        resizeBtn.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    initialPanelWidth = panelParams.width
                    initialPanelHeight = panelParams.height
                    initialResizeTouchX = event.rawX
                    initialResizeTouchY = event.rawY
                    view.haptic(HapticKind.TICK)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val targetW = initialPanelWidth + (event.rawX - initialResizeTouchX).toInt()
                    val targetH = initialPanelHeight + (event.rawY - initialResizeTouchY).toInt()

                    val (currentW, currentH) = currentOverlayBounds()
                    val currentMaxW = (currentW * 0.94f).toInt()
                    val currentMaxH = (currentH * 0.85f).toInt()
                    val liveMinW = SnippetOverlayLayoutPolicy.minPanelWidthPx(density)
                    val liveMinH = SnippetOverlayLayoutPolicy.minPanelHeightPx(
                        density = density,
                        showTitle = SnippetOverlayPreferences.showTitle(this@SnippetOverlayService),
                        showSearch = SnippetOverlayPreferences.showSearch(this@SnippetOverlayService),
                        showCategories = SnippetOverlayPreferences.showCategories(this@SnippetOverlayService)
                    )

                    val clamped = SnippetOverlayLayoutPolicy.clampPanelBounds(
                        x = panelParams.x,
                        y = panelParams.y,
                        width = targetW,
                        height = targetH,
                        screenWidth = currentW,
                        screenHeight = currentH,
                        minWidth = liveMinW,
                        minHeight = liveMinH,
                        maxWidth = currentMaxW,
                        maxHeight = currentMaxH
                    )

                    panelParams.width = clamped.width
                    panelParams.height = clamped.height
                    panelParams.x = clamped.x
                    panelParams.y = clamped.y
                    if (isGridMode) {
                        val liveCols = if (clamped.width >= 400 * density) 3 else 2
                        (snippetRecyclerView?.layoutManager as? androidx.recyclerview.widget.GridLayoutManager)?.spanCount = liveCols
                    }
                    windowManager.updateViewLayout(root, panelParams)
                    updateCloseOverlayLayout()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    SnippetOverlayPreferences.setPanelWidthPx(this@SnippetOverlayService, panelParams.width)
                    SnippetOverlayPreferences.setPanelHeightPx(this@SnippetOverlayService, panelParams.height)
                    SnippetOverlayPreferences.setPanelPosition(this@SnippetOverlayService, panelParams.x, panelParams.y)
                    true
                }
                else -> true
            }
        }
        resizeBtnView = resizeBtn
        root.addView(resizeBtn)

        panelRoot = root
    }

    private fun togglePanel() {
        val panel = panelRoot ?: return
        val currentToken = ++panelGeneration

        val closing = (panelState == PanelLifecycleState.OPEN || panelState == PanelLifecycleState.OPENING || isPanelOpen)
        if (closing) {
            panelState = PanelLifecycleState.CLOSING
            isPanelOpen = false
            if (searchEditText?.hasFocus() == true) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
                imm?.hideSoftInputFromWindow(searchEditText?.windowToken, 0)
                searchEditText?.clearFocus()
            }
            panel.animate().cancel()
            panelSurfaceView?.animate()?.alpha(0f)?.setDuration(180)?.start()
            chromeContainer?.animate()?.alpha(0f)?.setDuration(180)?.start()
            snippetRecyclerView?.animate()?.alpha(0f)?.setDuration(180)?.start()
            closeBtnView?.animate()?.alpha(0f)?.setDuration(180)?.start()
            resizeBtnView?.animate()?.alpha(0f)?.setDuration(180)?.start()

            panel.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .translationY(10f)
                .setDuration(220)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    if (panelGeneration == currentToken) {
                        removeCloseOverlay()
                        if (panel.isAttachedToWindow) {
                            windowManager.removeView(panel)
                        }
                        panelState = PanelLifecycleState.CLOSED
                    }
                }
                .start()
        } else {
            panelState = PanelLifecycleState.OPENING
            isPanelOpen = true
            panel.animate().cancel()

            // Single source of truth: re-sync chrome visibility from preferences on EVERY open
            val openShowTitle = SnippetOverlayPreferences.showTitle(this)
            val openShowSearch = SnippetOverlayPreferences.showSearch(this)
            val openShowCategories = SnippetOverlayPreferences.showCategories(this)
            panelHeaderView?.visibility = if (openShowTitle) View.VISIBLE else View.GONE
            panelTitleView?.visibility = if (openShowTitle) View.VISIBLE else View.GONE
            searchBoxContainer?.visibility = if (openShowSearch) View.VISIBLE else View.GONE
            categoryScrollView?.visibility = if (openShowCategories) View.VISIBLE else View.GONE
            syncSearchRow(openShowSearch)

            val density = resources.displayMetrics.density
            panelContent?.let { content ->
                val isGridOnly = !openShowTitle && !openShowSearch && !openShowCategories
                val outerPad = if (isGridOnly) (4 * density).toInt() else (6 * density).toInt()
                val topPad = if (!openShowTitle && !openShowSearch && !openShowCategories) {
                    (20 * density).toInt()
                } else if (!openShowTitle && !openShowSearch) {
                    (14 * density).toInt()
                } else if (!openShowTitle) {
                    (10 * density).toInt()
                } else {
                    (4 * density).toInt()
                }
                content.setPadding(outerPad, topPad, outerPad, (10 * density).toInt())
            }

            isGridMode = SnippetOverlayPreferences.isGridView(this)
            updateLayoutManager()

            // Resolve start filter mode on open
            resolveStartFilterOnOpen()
            updateCategoryChips()
            filterAndSubmitSnippets()

            panelSurfaceView?.alpha = 0f
            chromeContainer?.alpha = 0f
            snippetRecyclerView?.alpha = 0f
            closeBtnView?.alpha = 0f
            resizeBtnView?.alpha = 0f

            panel.scaleX = 0.96f
            panel.scaleY = 0.96f
            panel.translationY = 10f

            if (!panel.isAttachedToWindow) {
                windowManager.addView(panel, panelParams)
            }
            attachCloseOverlay()

            val surfOp = effectiveSurfaceOpacity()
            val chrOp = effectiveChromeOpacity()
            val snpOp = effectiveSnippetsOpacity()
            val clsOp = effectiveCloseOpacity()
            val resOp = effectiveResizeOpacity()

            panelSurfaceView?.animate()?.alpha(surfOp)?.setDuration(260)?.start()
            chromeContainer?.animate()?.alpha(chrOp)?.setDuration(260)?.start()
            snippetRecyclerView?.animate()?.alpha(snpOp)?.setDuration(260)?.start()
            closeBtnView?.animate()?.alpha(clsOp)?.setDuration(260)?.start()
            resizeBtnView?.animate()?.alpha(resOp)?.setDuration(260)?.start()

            panel.animate()
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setDuration(260)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    if (panelGeneration == currentToken) {
                        panelState = PanelLifecycleState.OPEN
                    }
                }
                .start()
        }
    }

    private fun resolveStartFilterOnOpen() {
        val startMode = SnippetOverlayPreferences.startFilterMode(this)
        when (startMode) {
            OverlayStartFilterMode.ALL -> selectedCategory = "All"
            OverlayStartFilterMode.PINNED -> selectedCategory = "Pinned"
            OverlayStartFilterMode.FREQUENT -> selectedCategory = "Frequent"
            OverlayStartFilterMode.LAST_USED -> {
                val last = SnippetOverlayPreferences.lastUsedFilter(this)
                selectedCategory = if (!last.isNullOrBlank()) last else "All"
            }
            OverlayStartFilterMode.CUSTOM_CATEGORY -> {
                val custom = SnippetOverlayPreferences.startCustomCategory(this)
                selectedCategory = if (custom.isNotBlank()) custom else "All"
            }
        }
    }

    private fun updateCloseOverlayLayout() {
        val close = closeBtnView ?: return
        if (!::panelParams.isInitialized || !::closeOverlayParams.isInitialized) return
        val (screenW, screenH) = currentOverlayBounds()
        val position = SnippetOverlayLayoutPolicy.closeOverlayPosition(
            panelX = panelParams.x,
            panelY = panelParams.y,
            panelWidth = panelParams.width,
            closeSize = closeOverlaySizePx,
            screenWidth = screenW,
            screenHeight = screenH,
            offsetPx = (SnippetOverlayLayoutPolicy.CLOSE_DOCK_OFFSET_DP * resources.displayMetrics.density)
                .toInt()
                .coerceAtLeast(1)
        )
        closeOverlayParams.x = position.x
        closeOverlayParams.y = position.y
        if (closeOverlayAttached && close.isAttachedToWindow) {
            try { windowManager.updateViewLayout(close, closeOverlayParams) } catch (_: Exception) { closeOverlayAttached = false }
        }
    }

    private fun attachCloseOverlay() {
        val close = closeBtnView ?: return
        if (!::closeOverlayParams.isInitialized) return
        updateCloseOverlayLayout()
        if (!close.isAttachedToWindow) {
            try {
                windowManager.addView(close, closeOverlayParams)
                closeOverlayAttached = true
                close.isClickable = true
            } catch (_: Exception) {
                closeOverlayAttached = false
                close.isClickable = false
            }
        }
    }

    private fun removeCloseOverlay() {
        val close = closeBtnView ?: return
        close.isClickable = false
        if (close.isAttachedToWindow) {
            try { windowManager.removeViewImmediate(close) } catch (_: Exception) { }
        }
        closeOverlayAttached = false
    }

    private fun updateCategoryChips() {
        val group = chipsContainer ?: return
        group.removeAllViews()

        val density = resources.displayMetrics.density
        val palette = currentPalette()

        val uniqueLangs = allSnippets.map { it.extension.uppercase().ifBlank { it.language.uppercase() } }
            .distinct()
            .sorted()
        val categories = listOf("All") + uniqueLangs

        for (cat in categories) {
            val isSelected = (selectedCategory == cat)
            val chip = TextView(this).apply {
                text = cat
                textSize = 11.5f
                typeface = if (isSelected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                setTextColor(if (isSelected) palette.selectedChipContentColor else palette.mutedTextColor)
                val bg = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 18 * density
                    if (isSelected) {
                        setColor(palette.selectedChipContainerColor)
                    } else {
                        setColor(withAlpha(palette.surfaceVariantColor, if (palette.isDark) 160 else 220))
                    }
                }
                background = bg
                val padH = (12 * density).toInt()
                val padV = (5 * density).toInt()
                setPadding(padH, padV, padH, padV)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, (6 * density).toInt(), 0)
                }

                setOnClickListener { view ->
                    view.haptic(HapticKind.TICK)
                    selectedCategory = cat
                    SnippetOverlayPreferences.setLastUsedFilter(this@SnippetOverlayService, cat)
                    updateCategoryChips()
                    filterAndSubmitSnippets()
                }
            }
            group.addView(chip)
        }
    }

    private fun filterAndSubmitSnippets(force: Boolean = false) {
        var filtered = allSnippets
        if (selectedCategory == "Pinned") {
            val pinnedIds = SnippetOverlayPreferences.pinnedSnippetIds(this)
            filtered = filtered.filter { pinnedIds.contains(it.id.toString()) }
        } else if (selectedCategory == "Frequent") {
            filtered = filtered.sortedByDescending { it.updatedAt }
        } else if (selectedCategory != "All") {
            filtered = filtered.filter {
                it.extension.equals(selectedCategory, ignoreCase = true) ||
                it.language.equals(selectedCategory, ignoreCase = true)
            }
        }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.lowercase()
            filtered = filtered.filter {
                it.title.lowercase().contains(q) ||
                it.preview.lowercase().contains(q) ||
                it.extension.lowercase().contains(q) ||
                it.language.lowercase().contains(q)
            }
        }
        snippetAdapter?.submit(filtered, force = force)
        emptyStateTextView?.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun onSnippetSelected(view: View, snippet: SnippetEntity) {
        serviceScope.launch {
            val content = try {
                repository.content(snippet)
            } catch (_: Exception) {
                snippet.preview
            }
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText(snippet.title, content))

            view.haptic(HapticKind.CONFIRM)

            val afterCopy = SnippetOverlayPreferences.afterCopyAction(this@SnippetOverlayService)
            if (afterCopy == OverlayAfterCopyAction.KEEP_OPEN) {
                showCopiedFeedbackOnItem(view as? ViewGroup)
            } else {
                Toast.makeText(this@SnippetOverlayService, "Đã sao chép: ${snippet.title}", Toast.LENGTH_SHORT).show()
                togglePanel()
            }
        }
    }

    private fun onSnippetLongClick(view: View, snippet: SnippetEntity) {
        view.haptic(HapticKind.LONG)
        serviceScope.launch {
            val content = try {
                repository.content(snippet)
            } catch (_: Exception) {
                snippet.preview
            }
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, snippet.title)
                putExtra(Intent.EXTRA_TEXT, content)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ ${snippet.title}").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    private fun quickSaveCurrentClipboard() {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = cm.primaryClip
        if (clip == null || clip.itemCount == 0) {
            Toast.makeText(this, "Bộ nhớ tạm hiện đang trống!", Toast.LENGTH_SHORT).show()
            return
        }
        val text = clip.getItemAt(0)?.coerceToText(this)?.toString().orEmpty()
        if (text.isBlank()) {
            Toast.makeText(this, "Bộ nhớ tạm hiện đang trống!", Toast.LENGTH_SHORT).show()
            return
        }

        serviceScope.launch {
            val detected = LanguageDetector.detect(text)
            val firstLine = text.lineSequence().firstOrNull { it.isNotBlank() }?.trim()?.take(40).orEmpty()
            val title = firstLine.ifBlank { "Snippet ${detected.label}" }

            val created = repository.create(
                title = title,
                content = text,
                language = detected
            )
            Toast.makeText(this@SnippetOverlayService, "Đã lưu nhanh: ${created.title} (${detected.label})", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCopiedFeedbackOnItem(frame: ViewGroup?) {
        frame ?: return
        val existing = frame.findViewWithTag<View>("copied_feedback_badge")
        if (existing != null) {
            frame.removeView(existing)
        }
        val density = resources.displayMetrics.density
        val palette = currentPalette()
        val badge = TextView(this).apply {
            tag = "copied_feedback_badge"
            text = "Copied!"
            textSize = 10f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 8 * density
                setColor(palette.primaryColor)
            }
            val pH = (8 * density).toInt()
            val pV = (3 * density).toInt()
            setPadding(pH, pV, pH, pV)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
        }
        frame.addView(badge)
        badge.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(120)
            .withEndAction {
                badge.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setStartDelay(500)
                    .withEndAction {
                        frame.removeView(badge)
                    }
                    .start()
            }
            .start()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        panelState = PanelLifecycleState.CLOSED
        isPanelOpen = false
        searchDebounceJob?.cancel()
        revealJob?.cancel()
        serviceScope.cancel()
        removeCloseOverlay()

        bubbleView?.let { bubble ->
            try { bubble.animate().cancel() } catch (_: Exception) {}
            if (bubble.isAttachedToWindow) {
                try { windowManager.removeViewImmediate(bubble) } catch (_: Exception) {}
            }
        }
        bubbleView = null

        panelRoot?.let { panel ->
            try { panel.animate().cancel() } catch (_: Exception) {}
            if (panel.isAttachedToWindow) {
                try { windowManager.removeViewImmediate(panel) } catch (_: Exception) {}
            }
        }
        panelRoot = null
    }
}
