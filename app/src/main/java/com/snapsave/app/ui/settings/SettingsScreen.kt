package com.snapsave.app.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snapsave.app.BuildConfig
import com.snapsave.app.core.HapticKind
import com.snapsave.app.core.QuickSaveTarget
import com.snapsave.app.core.S
import com.snapsave.app.core.formatSize
import com.snapsave.app.core.haptic
import com.snapsave.app.service.OverlayAfterCopyAction
import com.snapsave.app.service.OverlayAppearancePreset
import com.snapsave.app.service.OverlayStartFilterMode
import com.snapsave.app.service.SnippetOverlayPreferences
import com.snapsave.app.service.SnippetOverlayService
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val s = S
    val theme by vm.theme.collectAsStateWithLifecycle()
    val storage by vm.storage.collectAsStateWithLifecycle()
    val stats by vm.stats.collectAsStateWithLifecycle()
    val view = LocalView.current
    val context = LocalContext.current
    var confirmClear by remember { mutableStateOf(false) }

    // Floating overlay states
    var isOverlayRunning by remember { mutableStateOf(SnippetOverlayService.isRunning) }
    var bubbleSizeDp by remember { mutableFloatStateOf(SnippetOverlayPreferences.bubbleSizeDp(context)) }
    var bubbleOpacity by remember { mutableFloatStateOf(SnippetOverlayPreferences.bubbleOpacity(context)) }
    var masterOpacity by remember { mutableFloatStateOf(SnippetOverlayPreferences.popupMasterOpacity(context)) }
    var surfaceOpacity by remember { mutableFloatStateOf(SnippetOverlayPreferences.popupSurfaceOpacity(context)) }
    var snippetsOpacity by remember { mutableFloatStateOf(SnippetOverlayPreferences.popupSnippetsOpacity(context)) }
    var chromeOpacity by remember { mutableFloatStateOf(SnippetOverlayPreferences.popupChromeOpacity(context)) }
    var closeOpacity by remember { mutableFloatStateOf(SnippetOverlayPreferences.popupCloseOpacity(context)) }
    var resizeOpacity by remember { mutableFloatStateOf(SnippetOverlayPreferences.popupResizeOpacity(context)) }
    var shadowStrength by remember { mutableFloatStateOf(SnippetOverlayPreferences.snippetShadowStrength(context)) }

    var showQuickTitle by remember { mutableStateOf(SnippetOverlayPreferences.showTitle(context)) }
    var showQuickSearch by remember { mutableStateOf(SnippetOverlayPreferences.showSearch(context)) }
    var showQuickCategories by remember { mutableStateOf(SnippetOverlayPreferences.showCategories(context)) }
    var isGridView by remember { mutableStateOf(SnippetOverlayPreferences.isGridView(context)) }

    var startFilterMode by remember { mutableStateOf(SnippetOverlayPreferences.startFilterMode(context)) }
    var afterCopyAction by remember { mutableStateOf(SnippetOverlayPreferences.afterCopyAction(context)) }

    var showResetDialog by remember { mutableStateOf(false) }
    var showStartFilterDialog by remember { mutableStateOf(false) }
    var showAfterCopyDialog by remember { mutableStateOf(false) }

    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val hasOverlayPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)

    // Throttled live preview dispatcher
    val previewLimiter = remember { PreviewRateLimiter() }
    fun sendAppearancePreview(layer: String, value: Float) {
        if (!SnippetOverlayService.isRunning) return
        if (!previewLimiter.shouldDispatch(layer, SystemClock.uptimeMillis())) return
        context.startService(
            Intent(context, SnippetOverlayService::class.java)
                .setAction(SnippetOverlayService.ACTION_PREVIEW_APPEARANCE)
                .putExtra(SnippetOverlayService.EXTRA_APPEARANCE_LAYER, layer)
                .putExtra(SnippetOverlayService.EXTRA_APPEARANCE_VALUE, value)
        )
    }

    fun sendLightweightOverlayUpdate() {
        if (SnippetOverlayService.isRunning) {
            context.startService(
                Intent(context, SnippetOverlayService::class.java)
                    .setAction(SnippetOverlayService.ACTION_UPDATE_APPEARANCE)
            )
        }
    }

    fun sendShadowOverlayUpdate() {
        if (SnippetOverlayService.isRunning) {
            context.startService(
                Intent(context, SnippetOverlayService::class.java)
                    .setAction(SnippetOverlayService.ACTION_UPDATE_SHADOW)
            )
        }
    }

    fun sendRefreshConfiguration() {
        if (SnippetOverlayService.isRunning) {
            context.startService(
                Intent(context, SnippetOverlayService::class.java)
                    .setAction(SnippetOverlayService.ACTION_REFRESH_CONFIGURATION)
            )
        }
    }

    fun revealOverlayControls() {
        if (SnippetOverlayService.isRunning) {
            context.startService(
                Intent(context, SnippetOverlayService::class.java)
                    .setAction(SnippetOverlayService.ACTION_REVEAL_CONTROLS)
            )
        }
    }

    fun applyPreset(preset: OverlayAppearancePreset) {
        SnippetOverlayPreferences.applyAppearancePreset(context, preset)
        bubbleOpacity = SnippetOverlayPreferences.bubbleOpacity(context)
        masterOpacity = SnippetOverlayPreferences.popupMasterOpacity(context)
        surfaceOpacity = SnippetOverlayPreferences.popupSurfaceOpacity(context)
        snippetsOpacity = SnippetOverlayPreferences.popupSnippetsOpacity(context)
        chromeOpacity = SnippetOverlayPreferences.popupChromeOpacity(context)
        closeOpacity = SnippetOverlayPreferences.popupCloseOpacity(context)
        resizeOpacity = SnippetOverlayPreferences.popupResizeOpacity(context)
        shadowStrength = SnippetOverlayPreferences.snippetShadowStrength(context)
        sendLightweightOverlayUpdate()
        sendShadowOverlayUpdate()
    }

    fun resetAppearance() {
        SnippetOverlayPreferences.resetAppearance(context)
        bubbleSizeDp = SnippetOverlayPreferences.bubbleSizeDp(context)
        bubbleOpacity = SnippetOverlayPreferences.bubbleOpacity(context)
        masterOpacity = SnippetOverlayPreferences.popupMasterOpacity(context)
        surfaceOpacity = SnippetOverlayPreferences.popupSurfaceOpacity(context)
        snippetsOpacity = SnippetOverlayPreferences.popupSnippetsOpacity(context)
        chromeOpacity = SnippetOverlayPreferences.popupChromeOpacity(context)
        closeOpacity = SnippetOverlayPreferences.popupCloseOpacity(context)
        resizeOpacity = SnippetOverlayPreferences.popupResizeOpacity(context)
        shadowStrength = SnippetOverlayPreferences.snippetShadowStrength(context)
        sendLightweightOverlayUpdate()
        sendShadowOverlayUpdate()
        sendRefreshConfiguration()
    }

    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, flags)
            }
            val docFile = DocumentFile.fromTreeUri(context, uri)
            val name = docFile?.name ?: uri.lastPathSegment ?: s.folderStorageTitle
            vm.setCustomFolder(uri.toString(), name)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.settingsTitle, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { view.haptic(HapticKind.CLICK); onBack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = s.back)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Language Settings
            SettingsGroup(title = s.languageGroupTitle) {
                Text(
                    s.languageSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    val languages = listOf("English" to "en", "Tiếng Việt" to "vi")
                    languages.forEachIndexed { index, (label, code) ->
                        SegmentedButton(
                            selected = (vm.languageOverride ?: theme.language) == code,
                            onClick = {
                                view.haptic(HapticKind.TICK)
                                vm.setLanguage(code)
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 2)
                        ) {
                            Text(label)
                        }
                    }
                }
            }

            // 2. Storage & Quick Save Settings
            SettingsGroup(title = s.defaultSaveDestinationTitle) {
                Text(
                    s.defaultSaveDestinationSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    val options = listOf(
                        QuickSaveTarget.APP_ONLY to s.targetAppOnly,
                        QuickSaveTarget.DEVICE_FOLDER to s.targetDevice,
                        QuickSaveTarget.BOTH to s.targetBoth
                    )
                    options.forEachIndexed { index, (target, label) ->
                        SegmentedButton(
                            selected = storage.quickSaveTarget == target,
                            onClick = {
                                view.haptic(HapticKind.TICK)
                                vm.setQuickSaveTarget(target)
                                if (target != QuickSaveTarget.APP_ONLY && storage.customFolderUri == null) {
                                    folderPicker.launch(null)
                                }
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                        ) {
                            Text(label, maxLines = 1)
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (storage.customFolderUri != null) Icons.Rounded.Folder else Icons.Rounded.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            s.folderStorageTitle,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            storage.customFolderName ?: s.folderNotSelected,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (storage.customFolderUri != null) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (storage.customFolderUri != null) {
                        TextButton(
                            onClick = {
                                view.haptic(HapticKind.CLICK)
                                vm.setCustomFolder(null, null)
                            }
                        ) {
                            Text(s.deselect)
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    OutlinedButton(
                        onClick = {
                            view.haptic(HapticKind.CLICK)
                            folderPicker.launch(null)
                        }
                    ) {
                        Icon(Icons.Rounded.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (storage.customFolderUri == null) s.selectFolder else s.changeFolder)
                    }
                }
            }

            // 3. Main App Appearance
            SettingsGroup(title = s.appearanceGroupTitle) {
                Text(
                    s.themeTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    listOf(s.themeSystem, s.themeLight, s.themeDark).forEachIndexed { index, label ->
                        SegmentedButton(
                            selected = (vm.themeModeOverride ?: theme.themeMode) == index,
                            onClick = { view.haptic(HapticKind.TICK); vm.setThemeMode(index) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                        ) { Text(label) }
                    }
                }
                SettingSwitchRow(
                    icon = Icons.Rounded.Palette,
                    title = s.dynamicColorTitle,
                    subtitle = if (supportsDynamic) s.dynamicColorSubtitle else s.dynamicColorRequires,
                    checked = theme.dynamicColor && supportsDynamic,
                    enabled = supportsDynamic,
                    onChange = {
                        view.haptic(HapticKind.CLICK)
                        vm.setDynamicColor(it)
                    }
                )
                SettingSwitchRow(
                    icon = Icons.Rounded.Vibration,
                    title = s.hapticsTitle,
                    subtitle = s.hapticsSubtitle,
                    checked = theme.hapticsEnabled,
                    onChange = {
                        view.haptic(HapticKind.CLICK)
                        vm.setHaptics(it)
                    }
                )

                SettingSwitchRow(
                    icon = Icons.Rounded.Search,
                    title = s.showSearchBarTitle,
                    subtitle = s.showSearchBarSubtitle,
                    checked = theme.showSearchBar,
                    onChange = {
                        view.haptic(HapticKind.CLICK)
                        vm.setShowSearchBar(it)
                    }
                )

                SettingSwitchRow(
                    icon = Icons.Rounded.FilterList,
                    title = s.showCategoryBarTitle,
                    subtitle = s.showCategoryBarSubtitle,
                    checked = theme.showCategoryBar,
                    onChange = {
                        view.haptic(HapticKind.CLICK)
                        vm.setShowCategoryBar(it)
                    }
                )

                Spacer(Modifier.height(4.dp))
                Text(
                    s.viewLayoutTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = !theme.isGridView,
                        onClick = { view.haptic(HapticKind.TICK); vm.setIsGridView(false) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        icon = { Icon(Icons.Rounded.ViewAgenda, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    ) { Text(s.viewLayoutList) }

                    SegmentedButton(
                        selected = theme.isGridView,
                        onClick = { view.haptic(HapticKind.TICK); vm.setIsGridView(true) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        icon = { Icon(Icons.Rounded.GridView, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    ) { Text(s.viewLayoutGrid) }
                }
            }

            // 4. Floating Quick Snippets (100% StickHub Parity)
            SettingsGroup(title = s.floatingOverlayTitle) {
                SettingSwitchRow(
                    icon = Icons.Rounded.Bolt,
                    title = s.floatingOverlayTitle,
                    subtitle = if (!hasOverlayPermission) s.floatingOverlayPermissionRequired else s.floatingOverlaySubtitle,
                    checked = isOverlayRunning && hasOverlayPermission,
                    onChange = { enabled ->
                        view.haptic(HapticKind.CLICK)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        } else {
                            val serviceIntent = Intent(context, SnippetOverlayService::class.java)
                            if (enabled) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    context.startForegroundService(serviceIntent)
                                } else {
                                    context.startService(serviceIntent)
                                }
                                isOverlayRunning = true
                            } else {
                                context.stopService(serviceIntent)
                                isOverlayRunning = false
                            }
                        }
                    }
                )

                if (!hasOverlayPermission) {
                    FilledTonalButton(
                        onClick = {
                            view.haptic(HapticKind.CLICK)
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(s.overlayGrantPermission, style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 4A. Presets Section
                    Text(
                        text = s.overlayPresets,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OverlayAppearancePreset.entries.forEach { preset ->
                            val active = presetAppliesTo(
                                preset = preset,
                                bubble = bubbleOpacity,
                                master = masterOpacity,
                                surface = surfaceOpacity,
                                snippets = snippetsOpacity,
                                chrome = chromeOpacity,
                                close = closeOpacity,
                                resize = resizeOpacity,
                                shadow = shadowStrength
                            )
                            PresetRow(
                                title = when (preset) {
                                    OverlayAppearancePreset.BALANCED -> s.overlayPresetBalanced
                                    OverlayAppearancePreset.FLOATING -> s.overlayPresetFloating
                                    OverlayAppearancePreset.DISCREET -> s.overlayPresetDiscreet
                                },
                                description = when (preset) {
                                    OverlayAppearancePreset.BALANCED -> s.overlayPresetBalancedDesc
                                    OverlayAppearancePreset.FLOATING -> s.overlayPresetFloatingDesc
                                    OverlayAppearancePreset.DISCREET -> s.overlayPresetDiscreetDesc
                                },
                                active = active,
                                onApply = {
                                    view.haptic(HapticKind.TICK)
                                    applyPreset(preset)
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // 4B. Bubble Group
                    Text(
                        text = s.overlayBubbleGroup,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    SettingsSliderItem(
                        title = s.overlayBubbleSize,
                        value = bubbleSizeDp,
                        valueRange = 32f..72f,
                        valueText = "${bubbleSizeDp.toInt()} dp",
                        onValueChange = { bubbleSizeDp = it },
                        onValueChangeFinished = {
                            if (abs(it - SnippetOverlayPreferences.bubbleSizeDp(context)) >= 0.5f) {
                                view.haptic(HapticKind.TICK)
                                SnippetOverlayPreferences.setBubbleSizeDp(context, it)
                                sendRefreshConfiguration()
                            }
                        }
                    )

                    SettingsSliderItem(
                        title = s.overlayBubbleOpacity,
                        value = bubbleOpacity,
                        valueRange = 0.1f..1.0f,
                        valueText = s.overlayVisiblePercent((bubbleOpacity * 100).toInt()),
                        onValueChange = {
                            bubbleOpacity = it
                            sendAppearancePreview("bubble", it)
                        },
                        onValueChangeFinished = {
                            view.haptic(HapticKind.TICK)
                            SnippetOverlayPreferences.setBubbleOpacity(context, it)
                            sendLightweightOverlayUpdate()
                        }
                    )

                    Spacer(Modifier.height(10.dp))

                    // 4C. Popup Composition Group
                    Text(
                        text = s.overlayPopupComposition,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = s.overlayCompositionTip,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp),
                            lineHeight = 17.sp
                        )
                    }

                    SettingsSliderItem(
                        title = s.overlayMasterOpacity,
                        subtitle = s.overlayMasterOpacitySubtitle,
                        value = masterOpacity,
                        valueRange = 0.1f..1.0f,
                        valueText = s.overlayVisiblePercent((masterOpacity * 100).toInt()),
                        onValueChange = {
                            masterOpacity = it
                            sendAppearancePreview("master", it)
                        },
                        onValueChangeFinished = {
                            view.haptic(HapticKind.TICK)
                            SnippetOverlayPreferences.setPopupMasterOpacity(context, it)
                            sendLightweightOverlayUpdate()
                        }
                    )

                    SettingsSliderItem(
                        title = s.overlaySurfaceOpacity,
                        subtitle = s.overlaySurfaceOpacitySubtitle,
                        value = surfaceOpacity,
                        valueRange = 0.0f..1.0f,
                        valueText = s.overlayVisiblePercent((surfaceOpacity * 100).toInt()),
                        onValueChange = {
                            surfaceOpacity = it
                            sendAppearancePreview("surface", it)
                        },
                        onValueChangeFinished = {
                            view.haptic(HapticKind.TICK)
                            SnippetOverlayPreferences.setPopupSurfaceOpacity(context, it)
                            sendLightweightOverlayUpdate()
                        }
                    )

                    SettingsSliderItem(
                        title = s.overlaySnippetsOpacity,
                        subtitle = s.overlaySnippetsOpacitySubtitle,
                        value = snippetsOpacity,
                        valueRange = 0.1f..1.0f,
                        valueText = s.overlayVisiblePercent((snippetsOpacity * 100).toInt()),
                        onValueChange = {
                            snippetsOpacity = it
                            sendAppearancePreview("snippets", it)
                        },
                        onValueChangeFinished = {
                            view.haptic(HapticKind.TICK)
                            SnippetOverlayPreferences.setPopupSnippetsOpacity(context, it)
                            sendLightweightOverlayUpdate()
                        }
                    )

                    SettingsSliderItem(
                        title = s.overlayChromeOpacity,
                        subtitle = s.overlayChromeOpacitySubtitle,
                        value = chromeOpacity,
                        valueRange = 0.0f..1.0f,
                        enabled = showQuickTitle || showQuickSearch || showQuickCategories,
                        valueText = if (showQuickTitle || showQuickSearch || showQuickCategories)
                            s.overlayVisiblePercent((chromeOpacity * 100).toInt())
                        else "Hidden",
                        onValueChange = {
                            chromeOpacity = it
                            sendAppearancePreview("chrome", it)
                        },
                        onValueChangeFinished = {
                            view.haptic(HapticKind.TICK)
                            SnippetOverlayPreferences.setPopupChromeOpacity(context, it)
                            sendLightweightOverlayUpdate()
                        }
                    )

                    SettingsSliderItem(
                        title = s.overlayCloseOpacity,
                        subtitle = s.overlayCloseOpacitySubtitle,
                        value = closeOpacity,
                        valueRange = 0.1f..1.0f,
                        valueText = s.overlayVisiblePercent((closeOpacity * 100).toInt()),
                        onValueChange = {
                            closeOpacity = it
                            sendAppearancePreview("close", it)
                        },
                        onValueChangeFinished = {
                            view.haptic(HapticKind.TICK)
                            SnippetOverlayPreferences.setPopupCloseOpacity(context, it)
                            sendLightweightOverlayUpdate()
                        }
                    )

                    SettingsSliderItem(
                        title = s.overlayResizeOpacity,
                        subtitle = s.overlayResizeOpacitySubtitle,
                        value = resizeOpacity,
                        valueRange = 0.1f..1.0f,
                        valueText = s.overlayVisiblePercent((resizeOpacity * 100).toInt()),
                        onValueChange = {
                            resizeOpacity = it
                            sendAppearancePreview("resize", it)
                        },
                        onValueChangeFinished = {
                            view.haptic(HapticKind.TICK)
                            SnippetOverlayPreferences.setPopupResizeOpacity(context, it)
                            sendLightweightOverlayUpdate()
                        }
                    )

                    Spacer(Modifier.height(10.dp))

                    // 4D. Snippet Clarity & Shadow
                    Text(
                        text = s.overlaySnippetClarity,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    SettingsSliderItem(
                        title = s.overlayShadowStrength,
                        subtitle = s.overlayShadowStrengthSubtitle,
                        value = shadowStrength,
                        valueRange = 0.0f..1.0f,
                        valueText = "${(shadowStrength * 100).toInt()}%",
                        onValueChange = { shadowStrength = it },
                        onValueChangeFinished = {
                            view.haptic(HapticKind.TICK)
                            SnippetOverlayPreferences.setSnippetShadowStrength(context, it)
                            sendShadowOverlayUpdate()
                        }
                    )

                    Spacer(Modifier.height(10.dp))

                    // 4E. Actions
                    SettingsClickableRow(
                        title = s.overlayRevealControls,
                        subtitle = s.overlayRevealControlsSubtitle,
                        icon = Icons.Rounded.Visibility,
                        onClick = {
                            view.haptic(HapticKind.CLICK)
                            revealOverlayControls()
                        }
                    )

                    SettingsClickableRow(
                        title = s.overlayResetAppearance,
                        subtitle = s.overlayResetAppearanceSubtitle,
                        icon = Icons.Rounded.RestartAlt,
                        onClick = {
                            view.haptic(HapticKind.CLICK)
                            showResetDialog = true
                        }
                    )

                    Spacer(Modifier.height(10.dp))

                    // 4F. Opening Behavior
                    Text(
                        text = s.overlayOpeningBehavior,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val startFilterSummary = when (startFilterMode) {
                        OverlayStartFilterMode.ALL -> s.overlayFilterAll
                        OverlayStartFilterMode.PINNED -> s.overlayFilterPinned
                        OverlayStartFilterMode.FREQUENT -> s.overlayFilterFrequent
                        OverlayStartFilterMode.LAST_USED -> s.overlayFilterLastUsed
                        OverlayStartFilterMode.CUSTOM_CATEGORY -> s.overlayFilterCustom
                    }
                    SettingsClickableRow(
                        title = s.overlayOpenPopupWith,
                        subtitle = startFilterSummary,
                        icon = Icons.Rounded.FilterList,
                        onClick = {
                            view.haptic(HapticKind.CLICK)
                            showStartFilterDialog = true
                        }
                    )

                    val afterCopySummary = when (afterCopyAction) {
                        OverlayAfterCopyAction.CLOSE_POPUP -> s.overlayAfterCopyClose
                        OverlayAfterCopyAction.KEEP_OPEN -> s.overlayAfterCopyKeep
                    }
                    SettingsClickableRow(
                        title = s.overlayAfterCopyTitle,
                        subtitle = afterCopySummary,
                        icon = Icons.Rounded.ChevronRight,
                        onClick = {
                            view.haptic(HapticKind.CLICK)
                            showAfterCopyDialog = true
                        }
                    )

                    Spacer(Modifier.height(10.dp))

                    // 4G. Popup Content Toggles
                    Text(
                        text = s.overlayPopupContent,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    SettingSwitchRow(
                        icon = Icons.Rounded.Bolt,
                        title = s.overlayShowTitle,
                        subtitle = s.overlayShowTitleSubtitle,
                        checked = showQuickTitle,
                        onChange = {
                            view.haptic(HapticKind.CLICK)
                            showQuickTitle = it
                            SnippetOverlayPreferences.setShowTitle(context, it)
                            sendRefreshConfiguration()
                        }
                    )

                    SettingSwitchRow(
                        icon = Icons.Rounded.Search,
                        title = s.overlayShowSearch,
                        subtitle = s.overlayShowSearchSubtitle,
                        checked = showQuickSearch,
                        onChange = {
                            view.haptic(HapticKind.CLICK)
                            showQuickSearch = it
                            SnippetOverlayPreferences.setShowSearch(context, it)
                            sendRefreshConfiguration()
                        }
                    )

                    SettingSwitchRow(
                        icon = Icons.Rounded.FilterList,
                        title = s.overlayShowCategories,
                        subtitle = s.overlayShowCategoriesSubtitle,
                        checked = showQuickCategories,
                        onChange = {
                            view.haptic(HapticKind.CLICK)
                            showQuickCategories = it
                            SnippetOverlayPreferences.setShowCategories(context, it)
                            sendRefreshConfiguration()
                        }
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = s.overlayViewLayout,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = isGridView,
                            onClick = {
                                view.haptic(HapticKind.TICK)
                                isGridView = true
                                SnippetOverlayPreferences.setIsGridView(context, true)
                                sendRefreshConfiguration()
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.GridView,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        ) {
                            Text(s.overlayViewLayoutGrid)
                        }
                        SegmentedButton(
                            selected = !isGridView,
                            onClick = {
                                view.haptic(HapticKind.TICK)
                                isGridView = false
                                SnippetOverlayPreferences.setIsGridView(context, false)
                                sendRefreshConfiguration()
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.ViewAgenda,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        ) {
                            Text(s.overlayViewLayoutList)
                        }
                    }
                }

            // 5. Data & Storage
            SettingsGroup(title = s.dataGroupTitle) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(s.dataGroupTitle, style = MaterialTheme.typography.titleSmall)
                        Text(
                            s.statsFormat(stats.first, formatSize(stats.second)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                FilledTonalButton(
                    onClick = { view.haptic(HapticKind.CLICK); confirmClear = true },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Rounded.DeleteForever, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(s.deleteAllTitle)
                }
            }

            // 6. About
            SettingsGroup(title = s.aboutGroupTitle) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(s.appName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            "v${BuildConfig.VERSION_NAME} · Material Design 3 + Dynamic Color",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            s.aboutSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            icon = { Icon(Icons.Rounded.DeleteForever, contentDescription = null) },
            title = { Text(s.clearDialogTitle) },
            text = { Text(s.clearDialogText) },
            confirmButton = {
                TextButton(onClick = {
                    confirmClear = false
                    vm.clearAll { }
                }) {
                    Text(s.delete, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text(s.cancel) }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = { Icon(Icons.Rounded.RestartAlt, contentDescription = null) },
            title = { Text(s.overlayResetConfirmTitle) },
            text = { Text(s.overlayResetConfirmMessage) },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    resetAppearance()
                }) {
                    Text(s.overlayResetConfirmButton)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text(s.cancel) }
            }
        )
    }

    if (showStartFilterDialog) {
        AlertDialog(
            onDismissRequest = { showStartFilterDialog = false },
            title = { Text(s.overlayOpenPopupWith) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OverlayStartFilterMode.entries.forEach { mode ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .selectable(
                                    selected = startFilterMode == mode,
                                    onClick = {
                                        startFilterMode = mode
                                        SnippetOverlayPreferences.setStartFilterMode(context, mode)
                                        showStartFilterDialog = false
                                    }
                                )
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            RadioButton(
                                selected = startFilterMode == mode,
                                onClick = null
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = when (mode) {
                                    OverlayStartFilterMode.ALL -> s.overlayFilterAll
                                    OverlayStartFilterMode.PINNED -> s.overlayFilterPinned
                                    OverlayStartFilterMode.FREQUENT -> s.overlayFilterFrequent
                                    OverlayStartFilterMode.LAST_USED -> s.overlayFilterLastUsed
                                    OverlayStartFilterMode.CUSTOM_CATEGORY -> s.overlayFilterCustom
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStartFilterDialog = false }) { Text(s.cancel) }
            }
        )
    }

    if (showAfterCopyDialog) {
        AlertDialog(
            onDismissRequest = { showAfterCopyDialog = false },
            title = { Text(s.overlayAfterCopyTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        OverlayAfterCopyAction.CLOSE_POPUP to s.overlayAfterCopyClose,
                        OverlayAfterCopyAction.KEEP_OPEN to s.overlayAfterCopyKeep
                    ).forEach { (action, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .selectable(
                                    selected = afterCopyAction == action,
                                    onClick = {
                                        afterCopyAction = action
                                        SnippetOverlayPreferences.setAfterCopyAction(context, action)
                                        showAfterCopyDialog = false
                                    }
                                )
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            RadioButton(
                                selected = afterCopyAction == action,
                                onClick = null
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAfterCopyDialog = false }) { Text(s.cancel) }
            }
        )
    }
}

@Composable
private fun PresetRow(
    title: String,
    description: String,
    active: Boolean,
    onApply: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(
            width = if (active) 2.dp else 1.dp,
            color = if (active) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(role = Role.RadioButton, onClick = onApply)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (active) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Active preset",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

private fun presetAppliesTo(
    preset: OverlayAppearancePreset,
    bubble: Float,
    master: Float,
    surface: Float,
    snippets: Float,
    chrome: Float,
    close: Float,
    resize: Float,
    shadow: Float
): Boolean {
    fun closeTo(a: Float, b: Float) = abs(a - b) < 0.01f
    return closeTo(preset.bubble, bubble) &&
        closeTo(preset.master, master) &&
        closeTo(preset.surface, surface) &&
        closeTo(preset.snippets, snippets) &&
        closeTo(preset.chrome, chrome) &&
        closeTo(preset.close, close) &&
        closeTo(preset.resize, resize) &&
        closeTo(preset.shadow, shadow)
}

@Composable
private fun SettingsSliderItem(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueText: String,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (Float) -> Unit,
    subtitle: String? = null,
    enabled: Boolean = true
) {
    val interaction = remember(valueRange) { SliderInteractionState(value, valueRange) }
    var uiValue by remember(valueRange) { mutableFloatStateOf(value) }
    LaunchedEffect(value) {
        interaction.synchronize(value)
        uiValue = interaction.value
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (enabled) 0.35f else 0.15f))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .wrapContentWidth(Alignment.End)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Slider(
            value = uiValue,
            onValueChange = {
                uiValue = interaction.change(it)
                onValueChange(uiValue)
            },
            onValueChangeFinished = { onValueChangeFinished(interaction.finish()) },
            valueRange = valueRange,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SettingsClickableRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        ElevatedCard(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(
                Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = content
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onChange: (Boolean) -> Unit
) {
    val alpha = if (enabled) 1f else 0.45f
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onChange(!checked) }
    ) {
        Box(
            Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
            )
        }
        Switch(checked = checked, onCheckedChange = onChange, enabled = enabled)
    }
}
