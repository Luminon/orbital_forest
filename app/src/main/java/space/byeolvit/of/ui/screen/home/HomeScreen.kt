package space.byeolvit.of.ui.screen.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.res.painterResource
import space.byeolvit.of.R
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import space.byeolvit.of.data.model.ChecklistItem
import space.byeolvit.of.data.model.DocumentBlock
import space.byeolvit.of.ui.components.ChecklistItemRow
import space.byeolvit.of.ui.components.ContextMenuSheet
import space.byeolvit.of.ui.components.DocumentMenuSheet
import space.byeolvit.of.ui.components.DocumentNameChip
import space.byeolvit.of.ui.components.MemoBlock
import space.byeolvit.of.ui.components.bottomFadeBrush
import space.byeolvit.of.ui.components.fadingEdge
import space.byeolvit.of.ui.components.topFadeBrush
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

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = !uiState.isScrolled,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                TopAppBar(
                    title = {
                        uiState.currentDocument?.let {
                            DocumentNameChip(fileName = it.fileName)
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.onDocumentMenuOpen() }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_documents),
                                contentDescription = "문서 정보"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                            top = 16.dp,
                            bottom = 120.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .fadingEdge(topFadeBrush)
                            .fadingEdge(bottomFadeBrush)
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
                                                onLongPress = { viewModel.onContextMenuOpen(it) }
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
                    onTextChanged = { viewModel.onAddFieldTextChanged(it) },
                    onAdd = { viewModel.onAddItem(uiState.addFieldText) },
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
                // 툴바 (스크롤 시 또는 입력 필드 표시 시 숨김)
                AnimatedVisibility(
                    visible = !uiState.isScrolled && !uiState.showAddField,
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

                // FAB (입력 필드 표시 시만 숨김, 스크롤 시에도 유지)
                AnimatedVisibility(
                    visible = !uiState.showAddField,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    LargeAddFab(onClick = { viewModel.onAddFieldToggle() })
                }
            }
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
    onLongPress: (ChecklistItem) -> Unit
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
                    onLongPress = onLongPress
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
// 빈 화면 뷰
// ──────────────────────────────────────────────

@Composable
private fun EmptyDocumentView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "문서가 없습니다",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "아래 + 버튼으로 새 문서를 만들어보세요",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
        Text(
            text = "새로운 할일을 추가해보세요",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
    onTextChanged: (String) -> Unit,
    onAdd: () -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(16.dp)
        ) {
            if (parentName != null) {
                Text(
                    text = "하위 항목 작성중 · $parentName",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    placeholder = { Text("작성해주시길 기다리고 있어요...") },
                    maxLines = 10,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                    keyboardActions = KeyboardActions.Default,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                )
                IconButton(
                    onClick = onAdd,
                    enabled = text.isNotBlank()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_plus),
                        contentDescription = "추가"
                    )
                }
            }
        }
    }
}
