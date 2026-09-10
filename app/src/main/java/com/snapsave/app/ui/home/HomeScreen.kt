package com.snapsave.app.ui.home

import android.view.View
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TextSnippet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snapsave.app.core.HapticKind
import com.snapsave.app.core.S
import com.snapsave.app.core.formatSize
import com.snapsave.app.core.haptic
import com.snapsave.app.ui.components.EmptyState
import com.snapsave.app.ui.components.LanguageIconBox
import com.snapsave.app.ui.components.LoadingBox
import com.snapsave.app.ui.components.SnippetCard
import com.snapsave.app.ui.components.languageTint

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
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    var searchExpanded by rememberSaveable { mutableStateOf(false) }

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
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(s.appName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            if (state.loading) s.loading
                            else s.homeSnippetCount(state.all.size, formatSize(state.all.sumOf { it.sizeBytes })),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { view.haptic(HapticKind.CLICK); onOpenSettings() }) {
                        Icon(Icons.Rounded.Settings, contentDescription = s.settingsDesc)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                scrollBehavior = scrollBehavior
            )
        },
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
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
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
                    .padding(horizontal = if (searchExpanded) 0.dp else 16.dp)
                    .padding(top = 2.dp, bottom = 4.dp)
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

            if (state.languages.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
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

            if (!state.tipDismissed && !state.loading) {
                TipCard(
                    onDismiss = { view.haptic(HapticKind.CLICK); vm.dismissTip() },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            } else {
                Spacer(Modifier.height(6.dp))
            }

            Box(Modifier.weight(1f)) {
                when {
                    state.loading -> LoadingBox(Modifier.fillMaxSize())
                    state.visible.isEmpty() -> {
                        val nothingSaved = state.all.isEmpty()
                        EmptyState(
                            icon = if (nothingSaved) Icons.AutoMirrored.Rounded.TextSnippet else Icons.Rounded.SearchOff,
                            title = if (nothingSaved) s.emptyNoSnippetsTitle else s.emptySearchTitle,
                            message = if (nothingSaved) s.emptyNoSnippetsMsg else s.emptySearchMsg,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 4.dp,
                            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(items = state.visible, key = { it.id }, contentType = { "snippet" }) { snippet ->
                            SnippetCard(
                                snippet = snippet,
                                onClick = {
                                    view.haptic(HapticKind.CLICK)
                                    onOpenDetail(snippet.id)
                                },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
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
