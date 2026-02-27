package space.byeolvit.of.ui.screen.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.documentfile.provider.DocumentFile
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.res.painterResource
import space.byeolvit.of.R
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import space.byeolvit.of.data.model.ChecklistItem
import space.byeolvit.of.data.model.DocumentBlock
import space.byeolvit.of.ui.components.ChecklistItemRow
import space.byeolvit.of.ui.components.ContextMenuSheet
import space.byeolvit.of.ui.components.DocumentMenuSheet
import space.byeolvit.of.ui.components.DocumentNameChip
import space.byeolvit.of.ui.components.MemoBlock
import space.byeolvit.of.ui.components.fadingEdges
import space.byeolvit.of.ui.screen.home.newdoc.NewDocumentDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSelect: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }
    val canScrollUp by remember { derivedStateOf { listState.canScrollBackward } }
    val canScrollDown by remember { derivedStateOf { listState.canScrollForward } }

    LaunchedEffect(isScrolled) {
        viewModel.onScrollChanged(isScrolled)
    }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        val action = uiState.snackbarAction
        val result = snackbarHostState.showSnackbar(
            message = msg,
            actionLabel = action?.label,
            duration = SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) {
            action?.action?.invoke()
        }
        viewModel.onSnackbarDismissed()
    }

    BackHandler(enabled = uiState.showAddField) {
        viewModel.onAddFieldDismiss()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { _ ->
        val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val appBarHeight = statusBarTop + 60.dp // 10dp(top) + 40dp(content) + 10dp(bottom)

        val pullRefreshState = rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.onRefresh() },
            state = pullRefreshState,
            modifier = Modifier.fillMaxSize(),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullRefreshState,
                    isRefreshing = uiState.isRefreshing,
                    color = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = appBarHeight)
                )
            }
        ) {
            // 배경 글로우 효과 (Figma: Background Effect — bottom center radial glow)
            val glowColor = MaterialTheme.colorScheme.primary
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        center = Offset(size.width / 2f, size.height + 22.dp.toPx()),
                        radius = size.width * 0.74f
                    )
                )
            }

            val doc = uiState.currentDocument

            if (doc == null) {
                EmptyDocumentView(modifier = Modifier.fillMaxSize())
            } else {
                val visibleBlocks = doc.blocks.filter { block ->
                    when (block) {
                        is DocumentBlock.MemoBlock -> !uiState.hideNonChecklist
                        is DocumentBlock.ChecklistBlock -> true
                    }
                }

                val hasVisibleContent = visibleBlocks.any { block ->
                    when (block) {
                        is DocumentBlock.ChecklistBlock -> block.items.isNotEmpty()
                        is DocumentBlock.MemoBlock -> true
                    }
                }

                if (!hasVisibleContent) {
                    EmptyChecklistView(modifier = Modifier.fillMaxSize())
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(
                            start = 24.dp,
                            end = 24.dp,
                            top = appBarHeight + 16.dp,
                            bottom = 120.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .fadingEdges(showTop = canScrollUp, showBottom = canScrollDown)
                    ) {
                        visibleBlocks.forEachIndexed { blockIdx, block ->
                            when (block) {
                                is DocumentBlock.ChecklistBlock -> {
                                    val displayItems = if (uiState.hideCompleted) {
                                        block.items.filter { !it.isChecked }
                                    } else block.items

                                    if (displayItems.isNotEmpty()) {
                                        item(key = "checklist_$blockIdx") {
                                            ChecklistGroupCard(
                                                items = displayItems,
                                                onChecked = { i, checked -> viewModel.onCheckItem(i, checked) },
                                                onLongPress = { viewModel.onContextMenuOpen(it) },
                                                onOpenFile = {
                                                    val folderUri = uiState.appFolderUri
                                                    val fileName = uiState.currentDocument?.fileName
                                                    if (folderUri != null && fileName != null) {
                                                        val file = DocumentFile.fromTreeUri(context, folderUri)?.findFile(fileName)
                                                        if (file != null) {
                                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                                setDataAndType(file.uri, "text/plain")
                                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                            }
                                                            try { context.startActivity(intent) } catch (_: Exception) { }
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                                is DocumentBlock.MemoBlock -> {
                                    item(key = "memo_$blockIdx") {
                                        MemoBlock(rawText = block.rawText)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 상단 앱바 오버레이
            AnimatedVisibility(
                visible = !uiState.isScrolled && doc != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        uiState.currentDocument?.let {
                            DocumentNameChip(fileName = it.fileName)
                        }
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Surface(
                        onClick = { viewModel.onDocumentMenuOpen() },
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(width = 72.dp, height = 40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.ic_logo_of),
                                contentDescription = "문서 정보",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // 하단 입력 필드 (HME_10 / HME_09)
            AnimatedVisibility(
                visible = uiState.showAddField,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                AddItemField(
                    text = uiState.addFieldText,
                    parentName = uiState.pendingChildParent?.rawText,
                    editingItemText = uiState.pendingEditItem?.rawText,
                    onTextChanged = { viewModel.onAddFieldTextChanged(it) },
                    onAdd = {
                        if (uiState.pendingEditItem != null) {
                            viewModel.onEditItem(uiState.addFieldText)
                        } else {
                            viewModel.onAddItem(uiState.addFieldText)
                        }
                    },
                    onDismiss = { viewModel.onAddFieldDismiss() }
                )
            }

            // 하단 컨트롤: 플로팅 툴바 + FAB
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // 툴바 (스크롤 시, 입력 필드 표시 시, 문서 없을 시 숨김)
                AnimatedVisibility(
                    visible = !uiState.isScrolled && !uiState.showAddField && doc != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    ToolbarPill(
                        onNewDoc = { viewModel.onShowNewDocDialog() },
                        onSelectDoc = onNavigateToSelect,
                        onSettings = onNavigateToSettings
                    )
                }

                // FAB (입력 필드 표시 시 숨김, 문서 없을 시 숨김)
                AnimatedVisibility(
                    visible = !uiState.showAddField && doc != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    LargeAddFab(onClick = { viewModel.onAddFieldToggle() })
                }

                // Extended FAB (문서 없을 때만 표시)
                AnimatedVisibility(
                    visible = doc == null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    LargeExtendedAddFab(onClick = { viewModel.onShowNewDocDialog() })
                }
            }

            // Snackbar — 하단 컨트롤(FAB 80dp + 상하 패딩 16dp+16dp) 위에 표시
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 34.dp, end = 34.dp, bottom = 112.dp)
            )
        }
    }

    // Context Menu (HME_06)
    if (uiState.showContextMenu && uiState.contextMenuTarget != null) {
        ContextMenuSheet(
            item = uiState.contextMenuTarget!!,
            onDismiss = { viewModel.onContextMenuDismiss() },
            onDelete = { viewModel.onDeleteItem(uiState.contextMenuTarget!!) },
            onCopy = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("checklist_item", uiState.contextMenuTarget!!.rawText)
                clipboard.setPrimaryClip(clip)
                viewModel.onCopyItem(uiState.contextMenuTarget!!)
            },
            onEdit = { viewModel.onStartEditItem(uiState.contextMenuTarget!!) },
            onAddChild = { viewModel.onStartAddChildItem(uiState.contextMenuTarget!!) }
        )
    }

    // Document Menu Sheet (HME_13)
    if (uiState.showDocumentMenu && uiState.currentDocument != null) {
        DocumentMenuSheet(
            document = uiState.currentDocument!!,
            onDismiss = { viewModel.onDocumentMenuDismiss() },
            onDelete = { viewModel.onDeleteDocument {} },
            onRename = { viewModel.onRenameDocument(it) }
        )
    }

    // New Document Dialog (MDO_01)
    if (uiState.isNewDocDialogVisible) {
        NewDocumentDialog(
            settingsRepository = null,
            documentRepository = null,
            onCreated = { fileName ->
                viewModel.onNewDocumentCreated(fileName)
            },
            onDismiss = { viewModel.onDismissNewDocDialog() }
        )
    }
}

// ──────────────────────────────────────────────
// 체크리스트 그룹 카드 (Figma: List — rounded-[16dp] 컨테이너, 2dp 간격)
// ──────────────────────────────────────────────

@Composable
private fun ChecklistGroupCard(
    items: List<ChecklistItem>,
    onChecked: (ChecklistItem, Boolean) -> Unit,
    onLongPress: (ChecklistItem) -> Unit,
    onOpenFile: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            items.forEachIndexed { index, item ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                }
                ChecklistItemRow(
                    item = item,
                    depth = 0,
                    onChecked = onChecked,
                    onLongPress = onLongPress,
                    onOpenFile = onOpenFile
                )
            }
        }
    }
}

// ──────────────────────────────────────────────
// 플로팅 툴바 필 (Figma: Toolbar — rounded-[32dp] pill)
// ──────────────────────────────────────────────

@Composable
private fun ToolbarPill(
    onNewDoc: () -> Unit,
    onSelectDoc: () -> Unit,
    onSettings: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 4.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNewDoc,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = "문서 생성",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(
                onClick = onSelectDoc,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_doc_lists),
                    contentDescription = "문서 선택",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(
                onClick = onSettings,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings),
                    contentDescription = "설정",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

// ──────────────────────────────────────────────
// 대형 FAB (Figma: FAB — 80dp, rounded-[20dp], primary color)
// ──────────────────────────────────────────────

@Composable
private fun LargeAddFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.size(80.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_plus),
            contentDescription = "체크리스트 추가",
            modifier = Modifier.size(28.dp)
        )
    }
}

// ──────────────────────────────────────────────
// Extended FAB — 문서 없을 때 (HME_15)
// ──────────────────────────────────────────────

@Composable
private fun LargeExtendedAddFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.height(80.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 26.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_file_add),
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "새 문서 추가",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

// ──────────────────────────────────────────────
// 빈 화면 뷰
// ──────────────────────────────────────────────

@Composable
private fun EmptyDocumentView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_empty),
            contentDescription = null,
            tint = Color(0xFF252533),
            modifier = Modifier.size(156.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "새로운 문서를 추가해야 할일을 만들 수 있어요.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF65658A),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyChecklistView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_empty),
            contentDescription = null,
            tint = Color(0xFF252533),
            modifier = Modifier.size(156.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "새로운 할일을 추가해보세요.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF65658A),
            textAlign = TextAlign.Center
        )
    }
}

// ──────────────────────────────────────────────
// 항목 작성 필드 (HME_10 / HME_09)
// ──────────────────────────────────────────────

@Composable
private fun AddItemField(
    text: String,
    parentName: String?,
    editingItemText: String?,
    onTextChanged: (String) -> Unit,
    onAdd: () -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 입력 Row
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onPrimary
                    ),
                    maxLines = 10,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                    keyboardActions = KeyboardActions.Default,
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                        .focusRequester(focusRequester),
                    decorationBox = { innerTextField ->
                        Box {
                            if (text.isEmpty()) {
                                Text(
                                    text = "작성해주시길 기다리고 있어요...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Surface(
                    onClick = onAdd,
                    enabled = text.isNotBlank(),
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Icon(
                        painter = painterResource(
                            if (editingItemText != null) R.drawable.ic_check else R.drawable.ic_plus
                        ),
                        contentDescription = if (editingItemText != null) "수정 완료" else "추가",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp)
                    )
                }
            }

            // 상태 바: 하위 항목(HME_09) 또는 수정 모드(HME_09A)
            val statusLabel = when {
                editingItemText != null -> "수정중"
                parentName != null -> "하위 항목 작성중"
                else -> null
            }
            val statusValue = editingItemText ?: parentName
            if (statusLabel != null && statusValue != null) {
                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = statusValue,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
