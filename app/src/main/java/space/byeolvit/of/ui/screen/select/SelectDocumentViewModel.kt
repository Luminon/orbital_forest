package space.byeolvit.of.ui.screen.select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import space.byeolvit.of.data.model.DocumentMeta
import space.byeolvit.of.data.repository.DocumentRepository
import space.byeolvit.of.data.repository.SettingsRepository

data class SelectDocumentUiState(
    val documents: List<DocumentMeta> = emptyList(),
    val currentDocumentName: String = "",
    val isLoading: Boolean = false,
    val contextMenuTarget: DocumentMeta? = null
)

class SelectDocumentViewModel(
    private val settingsRepository: SettingsRepository,
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SelectDocumentUiState())
    val uiState: StateFlow<SelectDocumentUiState> = _uiState.asStateFlow()

    init {
        loadDocuments()
    }

    fun loadDocuments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val appFolderUri = settingsRepository.safAppFolderUri.filterNotNull().first()
                val currentName = settingsRepository.currentDocumentName.first()
                val docs = documentRepository.getAllDocuments(appFolderUri).first()
                _uiState.update {
                    it.copy(
                        documents = docs,
                        currentDocumentName = currentName,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onDocumentSelected(fileName: String, onNavigate: (String) -> Unit) {
        viewModelScope.launch {
            settingsRepository.setCurrentDocumentName(fileName)
            _uiState.update { it.copy(currentDocumentName = fileName) }
            onNavigate(fileName)
        }
    }

    fun onLongPress(doc: DocumentMeta) {
        _uiState.update { it.copy(contextMenuTarget = doc) }
    }

    fun onContextMenuDismiss() {
        _uiState.update { it.copy(contextMenuTarget = null) }
    }

    fun onDeleteDocument(fileName: String) {
        viewModelScope.launch {
            try {
                val appFolderUri = settingsRepository.safAppFolderUri.filterNotNull().first()
                documentRepository.deleteDocument(appFolderUri, fileName)
                val currentName = settingsRepository.currentDocumentName.first()
                if (currentName == fileName) {
                    val remaining = documentRepository.getAllDocuments(appFolderUri).first()
                    settingsRepository.setCurrentDocumentName(remaining.firstOrNull()?.fileName ?: "")
                }
                loadDocuments()
                _uiState.update { it.copy(contextMenuTarget = null) }
            } catch (_: Exception) {}
        }
    }

    fun onRenameDocument(oldName: String, newName: String) {
        viewModelScope.launch {
            try {
                val appFolderUri = settingsRepository.safAppFolderUri.filterNotNull().first()
                val actualName = documentRepository.renameDocument(appFolderUri, oldName, newName)
                val currentName = settingsRepository.currentDocumentName.first()
                if (currentName == oldName) {
                    settingsRepository.setCurrentDocumentName(actualName)
                }
                loadDocuments()
                _uiState.update { it.copy(contextMenuTarget = null) }
            } catch (_: Exception) {}
        }
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val documentRepository: DocumentRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SelectDocumentViewModel(settingsRepository, documentRepository) as T
        }
    }
}
