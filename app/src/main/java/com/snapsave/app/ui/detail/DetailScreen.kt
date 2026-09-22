package com.snapsave.app.ui.detail

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Launch
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snapsave.app.core.HapticKind
import com.snapsave.app.core.S
import com.snapsave.app.core.formatDateTime
import com.snapsave.app.core.formatSize
import com.snapsave.app.core.haptic
import com.snapsave.app.ui.components.LanguageBadge
import com.snapsave.app.ui.components.LoadingBox
import com.snapsave.app.ui.components.adaptiveLanguageTint
import com.snapsave.app.ui.components.pressScale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    id: Long,
    onBack: () -> Unit,
    vm: DetailViewModel = viewModel(factory = DetailViewModel.Factory)
) {
    val s = S
    val entity by vm.entity.collectAsStateWithLifecycle()
    val content by vm.content.collectAsStateWithLifecycle()
    val isPinned by vm.isPinned.collectAsStateWithLifecycle()

    var editing by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarState = remember { SnackbarHostState() }
    val view = LocalView.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(id) { vm.bind(id) }
    LaunchedEffect(Unit) {
        vm.messages.collect { msg ->
            view.haptic(HapticKind.CONFIRM)
            snackbarState.showSnackbar(msg)
        }
    }

    if (editing) BackHandler { editing = false }

    val e = entity
    if (e == null) {
        Scaffold { padding -> LoadingBox(Modifier.fillMaxSize().padding(padding)) }
        return
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) vm.export()
        else scope.launch { snackbarState.showSnackbar(s.permissionRequired) }
    }

    fun exportWithPermission() {
        val needsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) exportLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        else vm.export()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (editing) s.detailEditingTitle else e.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { view.haptic(HapticKind.CLICK); onBack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = s.back)
                    }
                },
                actions = {
                    val len = content?.length ?: 0
                    if (len in 0..150_000) {
                        IconButton(onClick = { view.haptic(HapticKind.CLICK); editing = !editing }) {
                            Icon(
                                if (editing) Icons.Rounded.Close else Icons.Rounded.Edit,
                                contentDescription = if (editing) s.exitEditDesc else s.editDesc
                            )
                        }
                    }
                    IconButton(onClick = { view.haptic(HapticKind.CLICK); showDeleteDialog = true }) {
                        Icon(Icons.Rounded.Delete, contentDescription = s.deleteDesc)
                    }
                }
            )
        },
        bottomBar = {
            if (!editing) {
                BottomAppBar {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ActionTile(icon = Icons.Rounded.Share, label = s.share) {
                            view.haptic(HapticKind.CLICK)
                            val (uri, mime) = vm.shareData(e)
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = mime
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(send, s.shareTitle))
                        }
                        ActionTile(icon = Icons.AutoMirrored.Rounded.Launch, label = s.openWithAction) {
                            view.haptic(HapticKind.CLICK)
                            val (uri, mime) = vm.shareData(e)
                            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, mime)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            runCatching {
                                context.startActivity(Intent.createChooser(viewIntent, s.openWithTitle).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                })
                            }.onFailure {
                                runCatching {
                                    val fallback = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "text/plain")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(Intent.createChooser(fallback, s.openWithTitle).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    })
                                }
                            }
                        }
                        ActionTile(icon = Icons.Rounded.FileDownload, label = s.saveToDevice) {
                            view.haptic(HapticKind.CLICK)
                            exportWithPermission()
                        }
                        ActionTile(
                            icon = Icons.Rounded.PushPin,
                            label = if (isPinned) s.actionUnpinSnippet else s.actionPinSnippet
                        ) {
                            view.haptic(HapticKind.CLICK)
                            vm.togglePin()
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarState) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Chips metadata
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LanguageBadge(extension = e.extension, language = e.language, tint = adaptiveLanguageTint(e.language))
                MetaChip(icon = Icons.Rounded.CalendarMonth, label = formatDateTime(e.createdAt))
                MetaChip(icon = Icons.Rounded.FormatListNumbered, label = s.lines(e.lineCount))
                MetaChip(icon = Icons.Rounded.Storage, label = formatSize(e.sizeBytes))
            }

            if (editing) {
                EditPanel(
                    initialTitle = e.title,
                    initialContent = content.orEmpty(),
                    onCancel = { editing = false },
                    onSave = { newTitle, newText ->
                        view.haptic(HapticKind.CONFIRM)
                        vm.saveEdits(newTitle, newText) { editing = false }
                    }
                )
            } else {
                CodePanel(content = content, totalLines = e.lineCount)
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
            title = { Text(s.deleteDialogTitle) },
            text = { Text(s.deleteDialogText(e.title)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    vm.delete(onDeleted = onBack)
                }) {
                    Text(s.delete, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(s.cancel) }
            }
        )
    }
}

@Composable
private fun ActionTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .pressScale(0.94f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun MetaChip(
    label: String,
    icon: ImageVector? = null,
    tint: androidx.compose.ui.graphics.Color? = null
) {
    AssistChip(
        onClick = {},
        label = { Text(label, color = tint ?: MaterialTheme.colorScheme.onSurface) },
        leadingIcon = if (icon != null) {
            {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.padding(0.dp),
                    tint = tint ?: MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else null
    )
}

@Composable
private fun CodePanel(content: String?, totalLines: Int) {
    val codeStyle = MaterialTheme.typography.bodySmall.copy(
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
        lineHeight = 19.sp
    )
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (content == null) {
            LoadingBox(Modifier.fillMaxWidth().height(160.dp))
        } else {
            val numbers = remember(content) { (1..totalLines).joinToString("\n") }
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 14.dp)
            ) {
                Text(
                    text = numbers,
                    style = codeStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.End,
                    softWrap = false,
                    modifier = Modifier.padding(start = 16.dp, end = 12.dp, top = 1.dp)
                )
                SelectionContainer(Modifier.padding(end = 16.dp)) {
                    Text(
                        text = content,
                        style = codeStyle,
                        color = MaterialTheme.colorScheme.onSurface,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
private fun EditPanel(
    initialTitle: String,
    initialContent: String,
    onCancel: () -> Unit,
    onSave: (title: String, content: String) -> Unit
) {
    val s = S
    var newTitle by remember(initialTitle) { mutableStateOf(initialTitle) }
    var newText by remember(initialContent) { mutableStateOf(initialContent) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = newTitle,
            onValueChange = { newTitle = it },
            singleLine = true,
            label = { Text(s.editFileName) },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = newText,
            onValueChange = { newText = it },
            label = { Text(s.editContent) },
            minLines = 14,
            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onCancel) { Text(s.cancel) }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { onSave(newTitle, newText) }) {
                Icon(Icons.Rounded.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(s.saveEdits)
            }
        }
    }
}
