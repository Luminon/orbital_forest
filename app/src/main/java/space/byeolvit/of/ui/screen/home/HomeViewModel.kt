package space.byeolvit.of.ui.screen.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import space.byeolvit.of.data.model.ChecklistItem
import space.byeolvit.of.data.model.DocumentBlock
import space.byeolvit.of.data.model.ParsedDocument
import space.byeolvit.of.data.repository.DocumentRepository
import space.byeolvit.of.data.repository.SettingsRepository

data class SnackbarAction(val label: String, val action: () -> Unit)

data class HomeUiState(
    val currentDocument: ParsedDocument? = null,
    val appFolderUri: Uri? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hideCompleted: Boolean = false,
    val hideNonChecklist: Boolean = false,
    val childInteraction: Boolean = true,
    val isScrolled: Boolean = false,
    val showContextMenu: Boolean = false,
    val contextMenuTarget: ChecklistItem? = null,
    val showDocumentMenu: Boolean = false,
    val showAddField: Boolean = false,
    val addFieldText: String = "",
    val pendingChildParent: ChecklistItem? = null,
    val pendingEditItem: ChecklistItem? = null,
    val snackbarMessage: String? = null,
    val snackbarAction: SnackbarAction? = null,
    val isNewDocDialogVisible: Boolean = false
)

class HomeViewModel(
    private val settingsRepository: SettingsRepository,
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var loadDocumentJob: Job? = null

    init {
        // 표시 설정(필터링 옵션) 감지
        viewModelScope.launch {
            combine(
                settingsRepository.hideCompleted,
                settingsRepository.hideNonChecklist,
                settingsRepository.childInteraction
            ) { hideCompleted, hideNonChecklist, childInteraction ->
                Triple(hideCompleted, hideNonChecklist, childInteraction)
            }.collect { (hideCompleted, hideNonChecklist, childInteraction) ->
                _uiState.update {
                    it.copy(
                        hideCompleted = hideCompleted,
                        hideNonChecklist = hideNonChecklist,
                        childInteraction = childInteraction
                    )
                }
            }
        }

        // 현재 문서 이름 변경 감지 — combine과 분리해 stale 중간값 문제 방지
        viewModelScope.launch {
            settingsRepository.currentDocumentName.collect { name ->
                if (name.isNotEmpty()) {
                    loadDocument(name)
                }
            }
        }
    }

    fun loadDocument(fileName: String) {
        loadDocumentJob?.cancel()
        loadDocumentJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val appFolderUri = settingsRepository.safAppFolderUri.filterNotNull().first()
                val doc = documentRepository.readDocument(appFolderUri, fileName)
                _uiState.update { it.copy(currentDocument = doc, appFolderUri = appFolderUri, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onCheckItem(item: ChecklistItem, isChecked: Boolean) {
        val doc = _uiState.value.currentDocument ?: return
        val childInteraction = _uiState.value.childInteraction

        val updatedBlocks = doc.blocks.map { block ->
            when (block) {
                is DocumentBlock.ChecklistBlock -> {
                    DocumentBlock.ChecklistBlock(
                        updateItemInTree(block.items, item.id, isChecked, childInteraction)
                    )
                }
                else -> block
            }
        }
        val updatedDoc = doc.copy(blocks = updatedBlocks)
        _uiState.update { it.copy(currentDocument = updatedDoc) }
        saveCurrentDocument(updatedDoc)
    }

    fun onAddItem(text: String) {
        if (text.isBlank()) return
        val doc = _uiState.value.currentDocument ?: return
        val parent = _uiState.value.pendingChildParent

        val indentLevel = if (parent != null) parent.indentLevel + 1 else 0
        val newItems = text.split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { ChecklistItem(rawText = it, isChecked = false, indentLevel = indentLevel) }
        if (newItems.isEmpty()) return

        val updatedBlocks = if (parent != null) {
            doc.blocks.map { block ->
                when (block) {
                    is DocumentBlock.ChecklistBlock -> {
                        DocumentBlock.ChecklistBlock(
                            addChildrenToItem(block.items, parent.id, newItems)
                        )
                    }
                    else -> block
                }
            }
        } else {
            val lastChecklistIndex = doc.blocks.indexOfLast { it is DocumentBlock.ChecklistBlock }
            if (lastChecklistIndex == -1) {
                doc.blocks + DocumentBlock.ChecklistBlock(newItems)
            } else {
                doc.blocks.mapIndexed { index, block ->
                    if (index == lastChecklistIndex && block is DocumentBlock.ChecklistBlock) {
                        block.copy(items = block.items + newItems)
                    } else block
                }
            }
        }

        val updatedDoc = doc.copy(blocks = updatedBlocks)
        _uiState.update {
            it.copy(
                currentDocument = updatedDoc,
                showAddField = false,
                addFieldText = "",
                pendingChildParent = null,
                pendingEditItem = null
            )
        }
        saveCurrentDocument(updatedDoc)
    }

    fun onDeleteItem(item: ChecklistItem) {
        val doc = _uiState.value.currentDocument ?: return
        val updatedBlocks = doc.blocks.map { block ->
            when (block) {
                is DocumentBlock.ChecklistBlock -> {
                    val newItems = removeItemFromTree(block.items, item.id)
                    DocumentBlock.ChecklistBlock(newItems)
                }
                else -> block
            }
        }.filter { it !is DocumentBlock.ChecklistBlock || (it as DocumentBlock.ChecklistBlock).items.isNotEmpty() }

        val updatedDoc = doc.copy(blocks = updatedBlocks)
        _uiState.update {
            it.copy(
                currentDocument = updatedDoc,
                showContextMenu = false,
                contextMenuTarget = null,
                snackbarMessage = "항목을 삭제했습니다.",
                snackbarAction = SnackbarAction("실행 취소") {
                    _uiState.update { s -> s.copy(currentDocument = doc, snackbarMessage = null, snackbarAction = null) }
                    saveCurrentDocument(doc)
                }
            )
        }
        saveCurrentDocument(updatedDoc)
    }

    fun onCopyItem(item: ChecklistItem) {
        _uiState.update {
            it.copy(
                showContextMenu = false,
                contextMenuTarget = null,
                snackbarMessage = "클립보드에 복사했습니다.",
                snackbarAction = null
            )
        }
    }

    fun onStartAddChildItem(parent: ChecklistItem) {
        _uiState.update {
            it.copy(
                showContextMenu = false,
                contextMenuTarget = null,
                showAddField = true,
                pendingChildParent = parent
            )
        }
    }

    fun onStartEditItem(item: ChecklistItem) {
        _uiState.update {
            it.copy(
                showContextMenu = false,
                contextMenuTarget = null,
                showAddField = true,
                addFieldText = item.rawText,
                pendingEditItem = item
            )
        }
    }

    fun onEditItem(text: String) {
        if (text.isBlank()) return
        val doc = _uiState.value.currentDocument ?: return
        val editItem = _uiState.value.pendingEditItem ?: return

        val updatedBlocks = doc.blocks.map { block ->
            when (block) {
                is DocumentBlock.ChecklistBlock -> DocumentBlock.ChecklistBlock(
                    updateItemTextInTree(block.items, editItem.id, text.trim())
                )
                else -> block
            }
        }
        val updatedDoc = doc.copy(blocks = updatedBlocks)
        _uiState.update {
            it.copy(
                currentDocument = updatedDoc,
                showAddField = false,
                addFieldText = "",
                pendingEditItem = null,
                snackbarMessage = "수정되었습니다.",
                snackbarAction = null
            )
        }
        saveCurrentDocument(updatedDoc)
    }

    fun onAddFieldTextChanged(text: String) {
        _uiState.update { it.copy(addFieldText = text) }
    }

    fun onAddFieldToggle() {
        _uiState.update {
            it.copy(
                showAddField = !it.showAddField,
                addFieldText = "",
                pendingChildParent = null,
                pendingEditItem = null
            )
        }
    }

    fun onAddFieldDismiss() {
        _uiState.update {
            it.copy(showAddField = false, addFieldText = "", pendingChildParent = null, pendingEditItem = null)
        }
    }

    fun onRefresh() {
        val currentDocName = _uiState.value.currentDocument?.fileName ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            try {
                val appFolderUri = settingsRepository.safAppFolderUri.filterNotNull().first()
                val doc = documentRepository.readDocument(appFolderUri, currentDocName)
                _uiState.update { it.copy(currentDocument = doc, appFolderUri = appFolderUri, isRefreshing = false) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun onScrollChanged(isScrolled: Boolean) {
        _uiState.update { it.copy(isScrolled = isScrolled) }
    }

    fun onContextMenuOpen(item: ChecklistItem) {
        _uiState.update { it.copy(showContextMenu = true, contextMenuTarget = item) }
    }

    fun onContextMenuDismiss() {
        _uiState.update { it.copy(showContextMenu = false, contextMenuTarget = null) }
    }

    fun onDocumentMenuOpen() {
        _uiState.update { it.copy(showDocumentMenu = true) }
    }

    fun onDocumentMenuDismiss() {
        _uiState.update { it.copy(showDocumentMenu = false) }
    }

    fun onRenameDocument(newName: String) {
        val doc = _uiState.value.currentDocument ?: return
        viewModelScope.launch {
            try {
                val appFolderUri = settingsRepository.safAppFolderUri.filterNotNull().first()
                val nameWithExt = if (newName.endsWith(".md")) newName else "$newName.md"
                val actualName = documentRepository.renameDocument(appFolderUri, doc.fileName, nameWithExt)
                settingsRepository.setCurrentDocumentName(actualName)
                _uiState.update {
                    it.copy(
                        currentDocument = doc.copy(fileName = actualName),
                        snackbarMessage = "이름이 변경되었습니다.",
                        snackbarAction = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "이름 변경에 실패했습니다.") }
            }
        }
    }

    fun onDeleteDocument(onNoDocuments: () -> Unit) {
        val doc = _uiState.value.currentDocument ?: return
        viewModelScope.launch {
            try {
                val appFolderUri = settingsRepository.safAppFolderUri.filterNotNull().first()
                documentRepository.deleteDocument(appFolderUri, doc.fileName)
                val remaining = documentRepository.getAllDocuments(appFolderUri).first()
                _uiState.update { it.copy(showDocumentMenu = false) }
                if (remaining.isEmpty()) {
                    settingsRepository.setCurrentDocumentName("")
                    _uiState.update { it.copy(currentDocument = null, snackbarMessage = "문서를 삭제했습니다.") }
                    onNoDocuments()
                } else {
                    _uiState.update { it.copy(snackbarMessage = "문서를 삭제했습니다.") }
                    settingsRepository.setCurrentDocumentName(remaining[0].fileName)
                    // init flow will call loadDocument(remaining[0].fileName)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "문서 삭제에 실패했습니다.") }
            }
        }
    }

    fun onNewDocumentCreated(fileName: String) {
        _uiState.update {
            it.copy(
                isNewDocDialogVisible = false,
                snackbarMessage = "새 문서를 생성했어요.",
                snackbarAction = null
            )
        }
        loadDocument(fileName)
        viewModelScope.launch {
            settingsRepository.setCurrentDocumentName(fileName)
        }
    }

    fun onShowNewDocDialog() {
        _uiState.update { it.copy(isNewDocDialogVisible = true) }
    }

    fun onDismissNewDocDialog() {
        _uiState.update { it.copy(isNewDocDialogVisible = false) }
    }

    fun onSnackbarDismissed() {
        _uiState.update { it.copy(snackbarMessage = null, snackbarAction = null) }
    }

    private fun saveCurrentDocument(doc: ParsedDocument) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val appFolderUri = settingsRepository.safAppFolderUri.filterNotNull().first()
                documentRepository.saveDocument(appFolderUri, doc)
            } catch (_: Exception) {}
        }
    }

    // Tree manipulation helpers

    private fun updateItemInTree(
        items: List<ChecklistItem>,
        targetId: String,
        isChecked: Boolean,
        childInteraction: Boolean
    ): List<ChecklistItem> {
        return items.map { item ->
            when {
                item.id == targetId -> {
                    val updatedChildren = if (childInteraction) {
                        setAllChildrenChecked(item.children, isChecked)
                    } else item.children
                    item.copy(isChecked = isChecked, children = updatedChildren)
                }
                item.children.isNotEmpty() -> {
                    val updatedChildren = updateItemInTree(item.children, targetId, isChecked, childInteraction)
                    val updatedItem = item.copy(children = updatedChildren)
                    if (childInteraction) {
                        val allChildrenChecked = updatedChildren.all { it.isChecked }
                        val anyChildChecked = updatedChildren.any { it.isChecked }
                        // If a child was the target, update parent accordingly
                        val childWasTarget = item.children.any { it.id == targetId } ||
                                updatedChildren != item.children
                        if (childWasTarget) {
                            updatedItem.copy(isChecked = if (isChecked) allChildrenChecked else false)
                        } else updatedItem
                    } else updatedItem
                }
                else -> item
            }
        }
    }

    private fun setAllChildrenChecked(items: List<ChecklistItem>, isChecked: Boolean): List<ChecklistItem> {
        return items.map { it.copy(isChecked = isChecked, children = setAllChildrenChecked(it.children, isChecked)) }
    }

    private fun removeItemFromTree(items: List<ChecklistItem>, targetId: String): List<ChecklistItem> {
        return items
            .filter { it.id != targetId }
            .map { it.copy(children = removeItemFromTree(it.children, targetId)) }
    }

    private fun updateItemTextInTree(items: List<ChecklistItem>, targetId: String, newText: String): List<ChecklistItem> {
        return items.map { item ->
            when {
                item.id == targetId -> item.copy(rawText = newText)
                item.children.isNotEmpty() -> item.copy(children = updateItemTextInTree(item.children, targetId, newText))
                else -> item
            }
        }
    }

    private fun addChildrenToItem(items: List<ChecklistItem>, parentId: String, newItems: List<ChecklistItem>): List<ChecklistItem> {
        return items.map { item ->
            if (item.id == parentId) {
                item.copy(children = item.children + newItems)
            } else {
                item.copy(children = addChildrenToItem(item.children, parentId, newItems))
            }
        }
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val documentRepository: DocumentRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(settingsRepository, documentRepository) as T
        }
    }
}
