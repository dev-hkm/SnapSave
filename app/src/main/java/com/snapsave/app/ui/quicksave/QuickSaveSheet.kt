package com.snapsave.app.ui.quicksave

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snapsave.app.core.CodeLanguage
import com.snapsave.app.core.HapticKind
import com.snapsave.app.core.LanguageDetector
import com.snapsave.app.core.QuickSaveTarget
import com.snapsave.app.core.S
import com.snapsave.app.core.formatSize
import com.snapsave.app.core.haptic
import com.snapsave.app.ui.components.LanguageBadge
import com.snapsave.app.ui.components.LanguageIconBox
import com.snapsave.app.ui.components.LanguagePickerRow
import com.snapsave.app.ui.components.languageTint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Toàn bộ UI của "Lưu nhanh": scrim mờ + sheet trượt lên, hiển thị đẹp và chuẩn chỉ. */
@Composable
fun QuickSaveSheet(
    initialText: String,
    onFinished: () -> Unit,
    vm: QuickSaveViewModel = viewModel(factory = QuickSaveViewModel.Factory)
) {
    val s = S
    val view = LocalView.current
    val context = LocalContext.current
    val config = LocalConfiguration.current
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    BackHandler(onBack = onFinished)

    val detected by produceState<CodeLanguage?>(initialValue = null, initialText) {
        value = withContext(Dispatchers.Default) { LanguageDetector.detect(initialText) }
    }
    var manual by remember { mutableStateOf<CodeLanguage?>(null) }
    var customExtension by rememberSaveable { mutableStateOf<String?>(null) }
    var title by rememberSaveable { mutableStateOf("") }

    val storageSettings by vm.storageSettings.collectAsStateWithLifecycle()
    val savedMessage = vm.savedMessage

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

    LaunchedEffect(detected) {
        val d = detected
        if (d != null && title.isBlank()) {
            title = LanguageDetector.suggestTitle(initialText, d)
        }
    }

    val lang = manual ?: detected ?: CodeLanguage.PLAIN

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onFinished
            )
    ) {
        // Scrim làm tối nền phía sau
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(150))
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
            )
        }

        // Bottom Sheet nổi từ đáy lên
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioNoBouncy
                )
            ) { it } + fadeIn(tween(200)),
            exit = fadeOut(tween(150)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = (config.screenHeightDp * 0.88f).dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // chặn click xuyên qua scrim
                    ),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Thanh gạt (Drag Handle)
                    Box(
                        Modifier
                            .padding(top = 2.dp, bottom = 12.dp)
                            .size(width = 36.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
                    )

                    AnimatedContent(
                        targetState = savedMessage != null,
                        transitionSpec = {
                            (fadeIn(tween(200)) + scaleIn(
                                initialScale = 0.92f,
                                animationSpec = spring(
                                    stiffness = Spring.StiffnessMediumLow,
                                    dampingRatio = Spring.DampingRatioNoBouncy
                                )
                            )) togetherWith fadeOut(tween(150))
                        },
                        label = "quickSaveState"
                    ) { isDone ->
                        if (isDone) {
                            SuccessPane(
                                message = savedMessage.orEmpty(),
                                hasFileToOpen = vm.savedFileUri != null,
                                onOpenFile = {
                                    val uri = vm.savedFileUri
                                    if (uri != null) {
                                        openFileWithChooser(context, uri, vm.savedMimeType, s.openWithTitle)
                                    }
                                    onFinished()
                                },
                                onDone = onFinished
                            )
                        } else {
                            QuickSaveForm(
                                rawText = initialText,
                                title = title,
                                onTitleChange = { newTitle ->
                                    if (newTitle.contains('.') && !newTitle.startsWith('.')) {
                                        val lastDot = newTitle.lastIndexOf('.')
                                        val extPart = newTitle.substring(lastDot + 1).trim()
                                        val basePart = newTitle.substring(0, lastDot).trim()
                                        if (extPart.length in 1..8 && extPart.all { it.isLetterOrDigit() }) {
                                            customExtension = extPart.lowercase()
                                            manual = null
                                            title = basePart
                                        } else {
                                            title = newTitle
                                        }
                                    } else {
                                        title = newTitle
                                    }
                                },
                                lang = lang,
                                detected = detected,
                                manual = manual,
                                customExtension = customExtension,
                                onPickLanguage = {
                                    manual = it
                                    customExtension = null
                                },
                                onCustomExtension = {
                                    customExtension = it
                                    manual = null
                                },
                                currentTarget = vm.targetOverride ?: storageSettings.quickSaveTarget,
                                onSelectTarget = { vm.updateTarget(it) },
                                customFolderName = storageSettings.customFolderName,
                                onPickFolder = { folderPicker.launch(null) },
                                saving = vm.saving,
                                error = vm.error,
                                onSave = {
                                    view.haptic(HapticKind.CLICK)
                                    val target = vm.targetOverride ?: storageSettings.quickSaveTarget
                                    if (target != QuickSaveTarget.APP_ONLY && storageSettings.customFolderUri == null) {
                                        folderPicker.launch(null)
                                    } else {
                                        vm.save(title, initialText, lang, customExtension)
                                    }
                                },
                                onCancel = onFinished
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(savedMessage) {
        if (savedMessage != null) {
            view.haptic(HapticKind.CONFIRM)
            delay(3500)
            onFinished()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickSaveForm(
    rawText: String,
    title: String,
    onTitleChange: (String) -> Unit,
    lang: CodeLanguage,
    detected: CodeLanguage?,
    manual: CodeLanguage?,
    customExtension: String?,
    onPickLanguage: (CodeLanguage?) -> Unit,
    onCustomExtension: (String) -> Unit,
    currentTarget: QuickSaveTarget,
    onSelectTarget: (QuickSaveTarget) -> Unit,
    customFolderName: String?,
    onPickFolder: () -> Unit,
    saving: Boolean,
    error: String?,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val s = S
    val stats = remember(rawText) {
        Triple(rawText.count { it == '\n' } + 1, rawText.length, rawText.toByteArray(Charsets.UTF_8).size)
    }
    val preview = remember(rawText) {
        rawText.lineSequence().take(5).joinToString("\n").trimEnd()
    }
    val effectiveExt = customExtension ?: lang.extension
    val effectiveLabel = if (!customExtension.isNullOrBlank()) customExtension.uppercase() else lang.label
    val tint = languageTint(effectiveExt)
    val view = LocalView.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Tiêu đề + Huy hiệu ngôn ngữ
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            LanguageIconBox(
                language = effectiveLabel,
                extension = effectiveExt,
                size = 48.dp,
                shapeRadius = 14.dp
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    s.quickSaveTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    s.detectedFormat(effectiveLabel),
                    style = MaterialTheme.typography.bodySmall,
                    color = tint,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 2. Tùy chọn nơi lưu (Vào App / Vào Máy / Cả hai)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                            onSelectTarget(target)
                            if (target != QuickSaveTarget.APP_ONLY && customFolderName == null) {
                                onPickFolder()
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
                            .clickable { onPickFolder() }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (customFolderName != null) Icons.Rounded.Folder else Icons.Rounded.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (customFolderName != null) s.folderLabel(customFolderName)
                            else s.tapToSelectFolder,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (customFolderName != null) MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (customFolderName != null) s.changeAction else s.selectAction,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 3. Khung xem trước văn bản
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    preview,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "${s.lines(stats.first)} · ${s.chars(stats.second)} · ${formatSize(stats.third.toLong())}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }

        // 4. Ô nhập tên tệp
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            singleLine = true,
            label = { Text(s.fileName) },
            suffix = {
                Text(
                    ".$effectiveExt",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )

        // 5. Chọn ngôn ngữ
        LanguagePickerRow(
            selected = if (customExtension.isNullOrBlank()) manual else null,
            detected = detected,
            customExtension = customExtension,
            onSelect = onPickLanguage,
            onCustomExtension = onCustomExtension,
            modifier = Modifier.fillMaxWidth()
        )

        // Báo lỗi nếu có
        if (error != null) {
            Text(
                error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }

        // 6. Nút Hủy và Lưu file
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) { Text(s.cancel) }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onSave,
                enabled = !saving,
                shape = MaterialTheme.shapes.medium
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(if (saving) s.savingState else s.saveFile)
            }
        }
    }
}

@Composable
private fun SuccessPane(
    message: String,
    hasFileToOpen: Boolean,
    onOpenFile: () -> Unit,
    onDone: () -> Unit
) {
    val s = S
    val view = LocalView.current
    var popped by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { popped = true }
    val scale by animateFloatAsState(
        targetValue = if (popped) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkPop"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .size(76.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(38.dp)
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            s.saveSuccessTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(20.dp))

        // Action Buttons: [Open with…] & [Done]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasFileToOpen) {
                Button(
                    onClick = {
                        view.haptic(HapticKind.CLICK)
                        onOpenFile()
                    },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(s.openWithAction, maxLines = 1)
                }
            }
            TextButton(
                onClick = {
                    view.haptic(HapticKind.CLICK)
                    onDone()
                },
                modifier = if (hasFileToOpen) Modifier else Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(s.doneAction)
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            s.autoClosing,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

private fun openFileWithChooser(context: Context, uri: Uri, mime: String, title: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mime)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val chooser = Intent.createChooser(intent, title).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching {
        context.startActivity(chooser)
    }.onFailure {
        runCatching {
            val fallback = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/plain")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(fallback, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }
}
