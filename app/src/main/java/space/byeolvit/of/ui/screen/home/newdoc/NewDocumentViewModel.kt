package space.byeolvit.of.ui.screen.home.newdoc

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
import space.byeolvit.of.data.repository.DocumentRepository
import space.byeolvit.of.data.repository.SettingsRepository

data class NewDocumentUiState(
    val nameText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class NewDocumentViewModel(
    private val settingsRepository: SettingsRepository,
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewDocumentUiState())
    val uiState: StateFlow<NewDocumentUiState> = _uiState.asStateFlow()

    fun loadDefaultName() {
        viewModelScope.launch {
            try {
                val appFolderUri = settingsRepository.safAppFolderUri.filterNotNull().first()
                val defaultName = documentRepository.generateUniqueName(appFolderUri, "내 할일")
                _uiState.update { it.copy(nameText = defaultName.removeSuffix(".md")) }
            } catch (_: Exception) {
                _uiState.update { it.copy(nameText = "내 할일") }
            }
        }
    }

    fun onNameChanged(text: String) {
        _uiState.update { it.copy(nameText = text, error = null) }
    }

    fun onCreate(onSuccess: (String) -> Unit) {
        val nameText = _uiState.value.nameText.trim()
        if (nameText.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val appFolderUri = settingsRepository.safAppFolderUri.filterNotNull().first()
                val fileName = if (nameText.endsWith(".md")) nameText else "$nameText.md"

                if (documentRepository.documentExists(appFolderUri, fileName)) {
                    _uiState.update { it.copy(isLoading = false, error = "같은 이름의 문서가 이미 있습니다.") }
                    return@launch
                }

                documentRepository.createDocument(appFolderUri, fileName)
                settingsRepository.setCurrentDocumentName(fileName)
                _uiState.update { it.copy(isLoading = false) }
                onSuccess(fileName)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "문서 생성에 실패했습니다.") }
            }
        }
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val documentRepository: DocumentRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NewDocumentViewModel(settingsRepository, documentRepository) as T
        }
    }
}
