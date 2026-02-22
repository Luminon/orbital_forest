package space.byeolvit.of.ui.screen.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
                            Icon(Icons.Default.Info, contentDescription = "문서 정보")
                        }
                    }
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = !uiState.isScrolled,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Surface(shadowElevation = 4.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = { viewModel.onShowNewDocDialog() }) {
                            Icon(Icons.Default.Add, contentDescription = "문서 생성")
                        }
                        IconButton(onClick = onNavigateToSelect) {
                            Icon(Icons.Default.List, contentDescription = "문서 선택")
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "설정")
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            AnimatedContent(
                targetState = uiState.showAddField,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "FabTransition"
            ) { showField ->
                if (!showField) {
                    FloatingActionButton(
                        onClick = { viewModel.onAddFieldToggle() }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "체크리스트 추가")
                    }
                } else {
                    // FAB 상태일 때는 하단 입력 필드를 사용하므로 FAB 숨김
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val doc = uiState.currentDocument

            if (doc == null) {
                // HME_15: md 파일이 없는 경우
                EmptyDocumentView(modifier = Modifier.fillMaxSize())
            } else {
                val visibleBlocks = doc.blocks.filter { block ->
                    when (block) {
                        is DocumentBlock.MemoBlock -> !uiState.hideNonChecklist
                        is DocumentBlock.ChecklistBlock -> true
                    }
                }

                val hasItems = visibleBlocks.any { block ->
                    block is DocumentBlock.ChecklistBlock && block.items.isNotEmpty()
                }

                if (!hasItems && visibleBlocks.none { it is DocumentBlock.MemoBlock }) {
                    // HME_01: Empty View
                    EmptyChecklistView(modifier = Modifier.fillMaxSize())
                } else {
                    // HME_02: 체크리스트 목록
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .fadingEdge(topFadeBrush)
                                .fadingEdge(bottomFadeBrush)
                        ) {
                            visibleBlocks.forEach { block ->
                                when (block) {
                                    is DocumentBlock.ChecklistBlock -> {
                                        val displayItems = if (uiState.hideCompleted) {
                                            block.items.filter { !it.isChecked }
                                        } else block.items

                                        items(displayItems, key = { it.id }) { item ->
                                            ChecklistItemRow(
                                                item = item,
                                                depth = 0,
                                                onChecked = { i, checked -> viewModel.onCheckItem(i, checked) },
                                                onLongPress = { viewModel.onContextMenuOpen(it) }
                                            )
                                        }
                                    }
                                    is DocumentBlock.MemoBlock -> {
                                        item {
                                            MemoBlock(rawText = block.rawText)
                                        }
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
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
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                    Icon(Icons.Default.Add, contentDescription = "추가")
                }
            }
        }
    }
}
