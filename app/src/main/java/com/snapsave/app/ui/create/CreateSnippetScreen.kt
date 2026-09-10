package com.snapsave.app.ui.create

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snapsave.app.core.HapticKind
import com.snapsave.app.core.QuickSaveTarget
import com.snapsave.app.core.S
import com.snapsave.app.core.formatSize
import com.snapsave.app.core.haptic
import com.snapsave.app.ui.components.LanguageBadge
import com.snapsave.app.ui.components.LanguageIconBox
import com.snapsave.app.ui.components.LanguagePickerRow
import com.snapsave.app.ui.components.languageTint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSnippetScreen(
    onBack: () -> Unit,
    onCreated: (Long) -> Unit,
    vm: CreateSnippetViewModel = viewModel(factory = CreateSnippetViewModel.Factory)
) {
    val s = S
    val view = LocalView.current
    val context = LocalContext.current
    val storageSettings by vm.storageSettings.collectAsStateWithLifecycle()
    val currentTarget = vm.targetOverride ?: storageSettings.quickSaveTarget

    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            runCatching { context.contentResolver.takePersistableUriPermission(uri, flags) }
            val docFile = DocumentFile.fromTreeUri(context, uri)
            val name = docFile?.name ?: uri.lastPathSegment ?: s.folderStorageTitle
            vm.setCustomFolder(uri.toString(), name)
        }
    }

    val stats = remember(vm.content) {
        Triple(
            if (vm.content.isEmpty()) 0 else vm.content.count { it == '\n' } + 1,
            vm.content.length,
            vm.content.toByteArray(Charsets.UTF_8).size.toLong()
        )
    }

    val lang = vm.effectiveLanguage
    val effectiveExt = vm.customExtension ?: lang.extension
    val effectiveLabel = if (!vm.customExtension.isNullOrBlank()) vm.customExtension!!.uppercase() else lang.label
    val tint = languageTint(effectiveExt)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(s.createTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { view.haptic(HapticKind.CLICK); onBack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = s.back)
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            view.haptic(HapticKind.CONFIRM)
                            vm.saveSnippet(onCreated)
                        },
                        enabled = !vm.saving && vm.content.isNotBlank(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        if (vm.saving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(s.saveButton)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 32.dp
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tên tệp & Language Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = vm.title,
                    onValueChange = { newTitle ->
                        if (newTitle.contains('.') && !newTitle.startsWith('.')) {
                            val lastDot = newTitle.lastIndexOf('.')
                            val extPart = newTitle.substring(lastDot + 1).trim()
                            val basePart = newTitle.substring(0, lastDot).trim()
                            if (extPart.length in 1..8 && extPart.all { it.isLetterOrDigit() }) {
                                vm.customExtension = extPart.lowercase()
                                vm.manualLanguage = null
                                vm.title = basePart
                            } else {
                                vm.title = newTitle
                            }
                        } else {
                            vm.title = newTitle
                        }
                    },
                    singleLine = true,
                    label = { Text(s.fileNameLabel) },
                    suffix = {
                        Text(
                            ".$effectiveExt",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                LanguageIconBox(
                    language = effectiveLabel,
                    extension = effectiveExt,
                    size = 52.dp,
                    shapeRadius = 12.dp
                )
            }

            // Chọn nơi lưu (Vào App / Vào Máy / Cả hai)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    s.createCardTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    val targets = listOf(
                        QuickSaveTarget.APP_ONLY to s.targetAppOnly,
                        QuickSaveTarget.DEVICE_FOLDER to s.targetDevice,
                        QuickSaveTarget.BOTH to s.targetBoth
                    )
                    targets.forEachIndexed { index, (target, label) ->
                        SegmentedButton(
                            selected = currentTarget == target,
                            onClick = {
                                view.haptic(HapticKind.TICK)
                                vm.updateTarget(target)
                                if (target != QuickSaveTarget.APP_ONLY && storageSettings.customFolderUri == null) {
                                    folderPicker.launch(null)
                                }
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                        ) {
                            Text(label, maxLines = 1)
                        }
                    }
                }

                if (currentTarget != QuickSaveTarget.APP_ONLY) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { folderPicker.launch(null) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (storageSettings.customFolderName != null) Icons.Rounded.Folder else Icons.Rounded.FolderOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (storageSettings.customFolderName != null) s.folderLabel(storageSettings.customFolderName.orEmpty())
                                else s.tapToSelectFolder,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (storageSettings.customFolderName != null) MaterialTheme.colorScheme.onSecondaryContainer
                                else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (storageSettings.customFolderName != null) s.changeAction else s.selectAction,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Chọn ngôn ngữ
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    s.fileType,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LanguagePickerRow(
                    selected = if (vm.customExtension.isNullOrBlank()) vm.manualLanguage else null,
                    detected = vm.detectedLanguage,
                    customExtension = vm.customExtension,
                    onSelect = {
                        vm.manualLanguage = it
                        vm.customExtension = null
                    },
                    onCustomExtension = {
                        vm.customExtension = it
                        vm.manualLanguage = null
                    }
                )
            }

            // Header nội dung + nút Paste
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    s.editContent,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedButton(
                    onClick = {
                        view.haptic(HapticKind.CLICK)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                        if (!clipText.isNullOrBlank()) {
                            vm.content = clipText
                        }
                    }
                ) {
                    Icon(Icons.Rounded.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(s.pasteClipboard)
                }
            }

            // Ô nhập nội dung lớn
            OutlinedTextField(
                value = vm.content,
                onValueChange = { vm.content = it },
                label = { Text(s.contentLabel) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp),
                shape = MaterialTheme.shapes.medium,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
            )

            // Thống kê
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${s.lines(stats.first)} · ${s.chars(stats.second)} · ${formatSize(stats.third)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (vm.content.isNotBlank()) {
                    Text(
                        s.detectedFormat(lang.label),
                        style = MaterialTheme.typography.bodySmall,
                        color = tint,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (vm.error != null) {
                Text(
                    vm.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
