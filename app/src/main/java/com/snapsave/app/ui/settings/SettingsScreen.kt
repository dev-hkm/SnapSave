package com.snapsave.app.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.ViewAgenda
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snapsave.app.BuildConfig
import com.snapsave.app.core.HapticKind
import com.snapsave.app.core.QuickSaveTarget
import com.snapsave.app.core.S
import com.snapsave.app.core.formatSize
import com.snapsave.app.core.haptic

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
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

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
            // 1. Language Settings (English default, Switch component matching theme switch)
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

            // 3. Appearance Settings
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

            // 4. Data & Storage
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

            // 5. About
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
