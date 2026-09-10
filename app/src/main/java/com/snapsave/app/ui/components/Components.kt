package com.snapsave.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.TextSnippet
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.Css
import androidx.compose.material.icons.rounded.DataArray
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.Html
import androidx.compose.material.icons.rounded.IntegrationInstructions
import androidx.compose.material.icons.rounded.Javascript
import androidx.compose.material.icons.rounded.Php
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapsave.app.R
import com.snapsave.app.core.CodeLanguage
import com.snapsave.app.core.HapticKind
import com.snapsave.app.core.S
import com.snapsave.app.core.formatSize
import com.snapsave.app.core.haptic
import com.snapsave.app.core.relativeTime
import com.snapsave.app.data.SnippetEntity

/** Hiệu ứng nén nhẹ bằng spring khi chạm giữ — không nuốt sự kiện nên ripple vẫn chạy. */
fun Modifier.pressScale(scaleTo: Float = 0.97f): Modifier = composed {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) scaleTo else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessHigh,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "pressScale"
    )
    this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                pressed = true
                waitForUpOrCancellation()
                pressed = false
            }
        }
}

@DrawableRes
fun getFileTypeIconRes(language: String, extension: String = ""): Int {
    val ext = (if (extension.isNotBlank()) extension else language).lowercase().trim().removePrefix(".")
    val lang = language.lowercase().trim()

    return when {
        ext in listOf("kt", "kts") || lang == "kotlin" -> R.drawable.ic_file_kotlin
        ext in listOf("py", "pyw") || lang == "python" -> R.drawable.ic_file_python
        ext in listOf("js", "mjs", "cjs") || lang == "javascript" -> R.drawable.ic_file_javascript
        ext in listOf("ts", "mts", "cts") || lang == "typescript" -> R.drawable.ic_file_typescript
        ext in listOf("html", "htm") || lang == "html" -> R.drawable.ic_file_html
        ext in listOf("css", "less") || lang == "css" -> R.drawable.ic_file_css
        ext in listOf("scss", "sass") || lang == "sass" -> R.drawable.ic_file_sass
        ext in listOf("java", "class", "jar") || lang == "java" -> R.drawable.ic_file_java
        ext in listOf("c", "h") || lang == "c" -> R.drawable.ic_file_c
        ext in listOf("cpp", "cc", "cxx", "hpp") || lang in listOf("c++", "cpp") -> R.drawable.ic_file_cpp
        ext in listOf("cs", "csx") || lang in listOf("c#", "csharp") -> R.drawable.ic_file_csharp
        ext in listOf("sql", "sqlite", "psql") || lang == "sql" -> R.drawable.ic_file_sql
        ext in listOf("sh", "bash", "zsh", "fish") || lang == "shell" -> R.drawable.ic_file_shell
        ext in listOf("md", "markdown") || lang == "markdown" -> R.drawable.ic_file_markdown
        ext == "json" || lang == "json" -> R.drawable.ic_file_json
        ext in listOf("xml", "svg", "plist") || lang == "xml" -> R.drawable.ic_file_xml
        ext in listOf("yml", "yaml") || lang == "yaml" -> R.drawable.ic_file_yaml
        ext == "php" || lang == "php" -> R.drawable.ic_file_php
        ext in listOf("rs", "rust") || lang == "rust" -> R.drawable.ic_file_rust
        ext in listOf("go", "golang") || lang == "go" -> R.drawable.ic_file_go
        ext in listOf("swift") || lang == "swift" -> R.drawable.ic_file_swift
        ext in listOf("dart") || lang == "dart" -> R.drawable.ic_file_dart
        ext in listOf("vue") || lang == "vue" -> R.drawable.ic_file_vue
        ext in listOf("jsx", "tsx") || lang in listOf("react", "react native") -> R.drawable.ic_file_react
        ext in listOf("rb", "ruby") || lang == "ruby" -> R.drawable.ic_file_ruby
        ext in listOf("docker", "dockerfile") || lang == "docker" -> R.drawable.ic_file_docker
        ext in listOf("gradle") || lang == "gradle" -> R.drawable.ic_file_gradle
        ext in listOf("env", "ini", "toml", "conf", "config", "properties") || lang in listOf("settings", "config") -> R.drawable.ic_file_settings
        else -> R.drawable.ic_file_document
    }
}

/** Màu nhận diện quen thuộc cho từng loại code (dùng cho thanh tìm kiếm, nhãn hoặc hiệu ứng). */
fun languageTint(language: String): Color = when (language.lowercase().trim()) {
    "html" -> Color(0xFFE44D26)
    "css", "scss", "sass" -> Color(0xFF264DE4)
    "javascript", "js" -> Color(0xFFF7DF1E)
    "typescript", "ts" -> Color(0xFF3178C6)
    "kotlin", "kt" -> Color(0xFF7F52FF)
    "java" -> Color(0xFFEA580C)
    "python", "py" -> Color(0xFF3776AB)
    "json" -> Color(0xFFF59E0B)
    "xml" -> Color(0xFF0284C7)
    "sql" -> Color(0xFF0D9488)
    "shell", "sh", "bash" -> Color(0xFF10B981)
    "markdown", "md" -> Color(0xFF42A5F5)
    "c++", "cpp", "c" -> Color(0xFF00599C)
    "c#", "cs" -> Color(0xFF7C3AED)
    "yaml", "yml" -> Color(0xFFEF4444)
    "php" -> Color(0xFF777BB3)
    "rust", "rs" -> Color(0xFFD97706)
    "go" -> Color(0xFF06B6D4)
    "swift" -> Color(0xFFF97316)
    "dart" -> Color(0xFF0284C7)
    "vue" -> Color(0xFF10B981)
    "react" -> Color(0xFF00D8FF)
    "ruby", "rb" -> Color(0xFFE11D48)
    else -> Color(0xFF64748B)
}

/** Icon đặc trưng đại diện cho từng ngôn ngữ/định dạng tệp */
fun getLanguageIcon(language: String): ImageVector = when (language.lowercase().trim()) {
    "html" -> Icons.Rounded.Html
    "css" -> Icons.Rounded.Css
    "javascript", "js" -> Icons.Rounded.Javascript
    "typescript", "ts" -> Icons.Rounded.DataObject
    "kotlin", "kt" -> Icons.Rounded.Code
    "java" -> Icons.Rounded.Coffee
    "python", "py" -> Icons.Rounded.Terminal
    "sql" -> Icons.Rounded.Storage
    "shell", "sh", "bash" -> Icons.Rounded.Terminal
    "markdown", "md" -> Icons.AutoMirrored.Rounded.Article
    "json" -> Icons.Rounded.DataObject
    "yaml", "yml" -> Icons.Rounded.DataArray
    "xml" -> Icons.Rounded.IntegrationInstructions
    "php" -> Icons.Rounded.Php
    "c++", "cpp", "c" -> Icons.Rounded.Code
    "c#", "cs" -> Icons.Rounded.Tag
    else -> Icons.AutoMirrored.Rounded.TextSnippet
}

/**
 * Hộp biểu trưng tệp/ngôn ngữ chuẩn bộ Material Icon Theme (VS Code):
 * Sử dụng icon chính hãng, sắc nét, đặt trong khung bo tròn tinh xảo.
 */
@Composable
fun LanguageIconBox(
    language: String,
    extension: String = "",
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    shapeRadius: Dp = 12.dp
) {
    val iconRes = remember(language, extension) { getFileTypeIconRes(language, extension) }
    Surface(
        modifier = modifier.size(size),
        shape = RoundedCornerShape(shapeRadius),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = language.ifBlank { extension },
                modifier = Modifier.size(size * 0.72f)
            )
        }
    }
}

@Composable
fun LanguageBadge(
    extension: String,
    language: String = "",
    tint: Color = languageTint(language.ifBlank { extension }),
    modifier: Modifier = Modifier
) {
    val iconRes = remember(language, extension) { getFileTypeIconRes(language, extension) }
    val ext = extension.uppercase().trim().removePrefix(".").take(5).ifBlank { "TXT" }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = ext,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SnippetCard(
    snippet: SnippetEntity,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    isPinned: Boolean = false,
    onQuickShare: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
            .fillMaxWidth()
            .pressScale()
            .clip(MaterialTheme.shapes.large)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LanguageIconBox(
                    language = snippet.language,
                    extension = snippet.extension,
                    size = 44.dp,
                    shapeRadius = 12.dp
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isPinned) {
                            Icon(
                                Icons.Rounded.PushPin,
                                contentDescription = S.pinnedHeader,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .size(16.dp)
                            )
                        }
                        Text(
                            snippet.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        "${snippet.language} · ${formatSize(snippet.sizeBytes)} · ${S.lines(snippet.lineCount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        relativeTime(snippet.updatedAt, S),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (onQuickShare != null) {
                        IconButton(
                            onClick = onQuickShare,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Share,
                                contentDescription = S.share,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            if (snippet.preview.isNotBlank()) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                ) {
                    Text(
                        snippet.preview,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SnippetGridCard(
    snippet: SnippetEntity,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    isPinned: Boolean = false,
    onQuickShare: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
            .fillMaxWidth()
            .pressScale()
            .clip(MaterialTheme.shapes.large)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LanguageIconBox(
                    language = snippet.language,
                    extension = snippet.extension,
                    size = 36.dp,
                    shapeRadius = 10.dp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPinned) {
                        Icon(
                            Icons.Rounded.PushPin,
                            contentDescription = S.pinnedHeader,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(14.dp)
                        )
                    }
                    if (onQuickShare != null) {
                        IconButton(
                            onClick = onQuickShare,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Share,
                                contentDescription = S.share,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                snippet.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${snippet.language} · ${formatSize(snippet.sizeBytes)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    relativeTime(snippet.updatedAt, S),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (snippet.preview.isNotBlank()) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        snippet.preview,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(54.dp)
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/** Hàng chip chọn ngôn ngữ. selected = null nghĩa là đang ở chế độ Tự động (nếu customExtension rỗng). */
@Composable
fun LanguagePickerRow(
    selected: CodeLanguage?,
    detected: CodeLanguage?,
    customExtension: String? = null,
    onSelect: (CodeLanguage?) -> Unit,
    onCustomExtension: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val s = S
    var showDialog by remember { mutableStateOf(false) }
    var inputExt by remember { mutableStateOf("") }
    val isCustomActive = !customExtension.isNullOrBlank()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = selected == null && !isCustomActive,
            onClick = { view.haptic(HapticKind.TICK); onSelect(null) },
            label = {
                Text(if (detected != null) S.autoWithLang(detected.label) else S.auto)
            }
        )
        CodeLanguage.common.forEach { lang ->
            FilterChip(
                selected = selected == lang && !isCustomActive,
                onClick = { view.haptic(HapticKind.TICK); onSelect(lang) },
                label = { Text(lang.label) }
            )
        }
        FilterChip(
            selected = isCustomActive,
            onClick = {
                view.haptic(HapticKind.CLICK)
                inputExt = customExtension ?: ""
                showDialog = true
            },
            label = {
                Text(if (isCustomActive) ".$customExtension" else s.customChip)
            }
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(s.customExtensionTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        s.customExtensionPrompt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = inputExt,
                        onValueChange = { inputExt = it.removePrefix(".").trim() },
                        singleLine = true,
                        prefix = { Text(".") },
                        placeholder = { Text("vue, go, rs, env, log…") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val clean = inputExt.trim().removePrefix(".").lowercase()
                        if (clean.isNotBlank()) {
                            view.haptic(HapticKind.CONFIRM)
                            onCustomExtension(clean)
                        }
                        showDialog = false
                    }
                ) {
                    Text(s.apply)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(s.cancel)
                }
            }
        )
    }
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
