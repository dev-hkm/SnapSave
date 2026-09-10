package com.snapsave.app.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.TextSnippet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Launch
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snapsave.app.core.HapticKind
import com.snapsave.app.core.S
import com.snapsave.app.core.formatSize
import com.snapsave.app.core.haptic
import com.snapsave.app.data.SnippetEntity
import com.snapsave.app.ui.components.EmptyState
import com.snapsave.app.ui.components.LanguageIconBox
import com.snapsave.app.ui.components.LoadingBox
import com.snapsave.app.ui.components.SnippetCard
import com.snapsave.app.ui.components.SnippetGridCard
import com.snapsave.app.ui.components.pressScale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenDetail: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCreate: () -> Unit,
    vm: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val s = S
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbarState = remember { SnackbarHostState() }
    val view = LocalView.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var searchExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedSnippetForModal by remember { mutableStateOf<SnippetEntity?>(null) }

    val snackbarMsg = s.snackbarDeleted
    val undoLabel = s.undo
    LaunchedEffect(Unit) {
        vm.events.collect {
            val result = snackbarState.showSnackbar(
                message = snackbarMsg,
                actionLabel = undoLabel,
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) vm.undoDelete()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { view.haptic(HapticKind.CLICK); onOpenCreate() },
                expanded = true,
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text(s.addSnippet) },
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp, end = 4.dp)
            )
        },
        snackbarHost = {
            SnackbarHost(
                snackbarState,
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { _ ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (state.isGridView) 2 else 1),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 0.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Unified Scrolling Header (Title + Count + Grid/List Switch + Modern Tune Settings Button)
            item(span = { GridItemSpan(maxLineSpan) }, key = "header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 16.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = s.appName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (state.loading) s.loading
                            else s.homeSnippetCount(state.all.size, formatSize(state.all.sumOf { it.sizeBytes })),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick Grid / List Toggle Button
                        Surface(
                            onClick = {
                                view.haptic(HapticKind.CLICK)
                                vm.toggleViewMode()
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .size(42.dp)
                                .pressScale()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (state.isGridView) Icons.Rounded.ViewAgenda else Icons.Rounded.GridView,
                                    contentDescription = s.switchViewModeDesc,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Modernized Settings Button
                        Surface(
                            onClick = {
                                view.haptic(HapticKind.CLICK)
                                onOpenSettings()
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .size(42.dp)
                                .pressScale()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Tune,
                                    contentDescription = s.settingsDesc,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Search Bar (Toggleable from Settings)
            if (state.showSearchBar) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "search") {
                    SearchBar(
                        inputField = {
                            SearchBarDefaults.InputField(
                                query = state.query,
                                onQueryChange = vm::setQuery,
                                onSearch = { searchExpanded = false },
                                expanded = searchExpanded,
                                onExpandedChange = { searchExpanded = it },
                                placeholder = { Text(s.searchPlaceholder) },
                                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (state.query.isNotBlank()) {
                                        IconButton(onClick = { vm.setQuery("") }) {
                                            Icon(Icons.Rounded.Close, contentDescription = s.clearSearchDesc)
                                        }
                                    }
                                }
                            )
                        },
                        expanded = searchExpanded,
                        onExpandedChange = { searchExpanded = it },
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp, bottom = 2.dp)
                    ) {
                        if (state.languages.isEmpty()) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(s.searchEmptyHint, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    s.savedLanguagesHeader,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                )
                                state.languages.take(8).forEach { lang ->
                                    ListItem(
                                        headlineContent = { Text(lang, fontWeight = FontWeight.SemiBold) },
                                        leadingContent = {
                                            LanguageIconBox(
                                                language = lang,
                                                extension = lang,
                                                size = 28.dp,
                                                shapeRadius = 8.dp
                                            )
                                        },
                                        modifier = Modifier.clickable {
                                            vm.setQuery(lang)
                                            searchExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Category Filter Chips (Toggleable from Settings)
            if (state.showCategoryBar && state.languages.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "categories") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 4.dp)
                    ) {
                        item(key = "all") {
                            FilterChip(
                                selected = state.activeLanguage == null,
                                onClick = { view.haptic(HapticKind.TICK); vm.setLanguage(null) },
                                label = { Text(s.all) }
                            )
                        }
                        items(state.languages, key = { it }) { lang ->
                            FilterChip(
                                selected = state.activeLanguage == lang,
                                onClick = {
                                    view.haptic(HapticKind.TICK)
                                    vm.setLanguage(if (state.activeLanguage == lang) null else lang)
                                },
                                label = { Text(lang) }
                            )
                        }
                    }
                }
            }

            // 4. Tip Card (if not dismissed)
            if (!state.tipDismissed && !state.loading) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "tip") {
                    TipCard(
                        onDismiss = { view.haptic(HapticKind.CLICK); vm.dismissTip() },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
            }

            // 5. Snippet Cards or Empty State
            if (state.loading) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "loading") {
                    LoadingBox(
                        Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    )
                }
            } else if (state.visible.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "empty") {
                    val nothingSaved = state.all.isEmpty()
                    EmptyState(
                        icon = if (nothingSaved) Icons.AutoMirrored.Rounded.TextSnippet else Icons.Rounded.SearchOff,
                        title = if (nothingSaved) s.emptyNoSnippetsTitle else s.emptySearchTitle,
                        message = if (nothingSaved) s.emptyNoSnippetsMsg else s.emptySearchMsg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                    )
                }
            } else {
                items(
                    items = state.visible,
                    key = { it.id },
                    contentType = { "snippet" }
                ) { snippet ->
                    if (state.isGridView) {
                        SnippetGridCard(
                            snippet = snippet,
                            onClick = {
                                view.haptic(HapticKind.CLICK)
                                onOpenDetail(snippet.id)
                            },
                            onLongClick = {
                                view.haptic(HapticKind.CONFIRM)
                                selectedSnippetForModal = snippet
                            },
                            modifier = Modifier.animateItem()
                        )
                    } else {
                        SnippetCard(
                            snippet = snippet,
                            onClick = {
                                view.haptic(HapticKind.CLICK)
                                onOpenDetail(snippet.id)
                            },
                            onLongClick = {
                                view.haptic(HapticKind.CONFIRM)
                                selectedSnippetForModal = snippet
                            },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }

    // 6. Long-Press Action Bottom Sheet
    selectedSnippetForModal?.let { snippet ->
        SnippetActionBottomSheet(
            snippet = snippet,
            onDismiss = { selectedSnippetForModal = null },
            onOpenDetail = {
                selectedSnippetForModal = null
                view.haptic(HapticKind.CLICK)
                onOpenDetail(snippet.id)
            },
            onOpenWith = {
                selectedSnippetForModal = null
                view.haptic(HapticKind.CLICK)
                val (uri, mime) = vm.shareData(snippet)
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
            },
            onShare = {
                selectedSnippetForModal = null
                view.haptic(HapticKind.CLICK)
                val (uri, mime) = vm.shareData(snippet)
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = mime
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(send, s.shareTitle))
            },
            onCopy = {
                selectedSnippetForModal = null
                view.haptic(HapticKind.CLICK)
                scope.launch {
                    val content = vm.getContent(snippet)
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText(snippet.title, content))
                    view.haptic(HapticKind.CONFIRM)
                    snackbarState.showSnackbar(s.copiedToClipboard)
                }
            },
            onDelete = {
                selectedSnippetForModal = null
                view.haptic(HapticKind.CONFIRM)
                vm.delete(snippet)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SnippetActionBottomSheet(
    snippet: SnippetEntity,
    onDismiss: () -> Unit,
    onOpenDetail: () -> Unit,
    onOpenWith: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    val s = S
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header: snippet info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                LanguageIconBox(
                    language = snippet.language,
                    extension = snippet.extension,
                    size = 42.dp,
                    shapeRadius = 12.dp
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = snippet.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${snippet.language} · ${formatSize(snippet.sizeBytes)} · ${s.lines(snippet.lineCount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ActionListItem(
                icon = Icons.AutoMirrored.Rounded.OpenInNew,
                title = s.actionViewDetails,
                onClick = onOpenDetail
            )
            ActionListItem(
                icon = Icons.Rounded.Launch,
                title = s.actionOpenWith,
                onClick = onOpenWith
            )
            ActionListItem(
                icon = Icons.Rounded.Share,
                title = s.actionShareFile,
                onClick = onShare
            )
            ActionListItem(
                icon = Icons.Rounded.ContentCopy,
                title = s.actionCopyCode,
                onClick = onCopy
            )
            ActionListItem(
                icon = Icons.Rounded.Delete,
                title = s.actionDeleteFile,
                tint = MaterialTheme.colorScheme.error,
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun ActionListItem(
    icon: ImageVector,
    title: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .pressScale()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = tint
            )
        }
    }
}

@Composable
private fun TipCard(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val s = S
    ElevatedCard(shape = MaterialTheme.shapes.large, modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    s.tipCardTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = s.tipCardDismiss)
                }
            }
            Text(
                s.tipCardBody,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
