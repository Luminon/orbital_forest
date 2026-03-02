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
import space.byeolvit.of.R
import space.byeolvit.of.data.repository.DocumentRepository
import space.byeolvit.of.data.repository.SettingsRepository
import space.byeolvit.of.util.UiText

data class NewDocumentUiState(
    val nameText: String = "",
    val isLoading: Boolean = false,
    val error: UiText? = null
)

class NewDocumentViewModel(
    private val settingsRepository: SettingsRepository,
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewDocumentUiState())
    val uiState: StateFlow<NewDocumentUiState> = _uiState.asStateFlow()

    fun loadDefaultName(baseName: String) {
        viewModelScope.launch {
            try {
                val appFolderUri = settingsRepository.safAppFolderUri.filterNotNull().first()
                val defaultName = documentRepository.generateUniqueName(appFolderUri, baseName)
                _uiState.update { it.copy(nameText = defaultName.removeSuffix(".md")) }
            } catch (_: Exception) {
                _uiState.update { it.copy(nameText = baseName) }
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
                    _uiState.update { it.copy(isLoading = false, error = UiText.StringResource(R.string.error_doc_exists)) }
                    return@launch
                }

                documentRepository.createDocument(appFolderUri, fileName)
                settingsRepository.setCurrentDocumentName(fileName)
                _uiState.update { it.copy(isLoading = false) }
                onSuccess(fileName)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = UiText.StringResource(R.string.error_doc_create_failed)) }
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
