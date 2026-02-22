package space.byeolvit.of.ui.screen.launch

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import space.byeolvit.of.data.repository.DocumentRepository
import space.byeolvit.of.data.repository.SettingsRepository

enum class LaunchStep { BRAND, FOLDER }

data class LaunchUiState(
    val step: LaunchStep = LaunchStep.BRAND,
    val isLoading: Boolean = false,
    val error: String? = null
)

class LaunchViewModel(
    private val settingsRepository: SettingsRepository,
    private val documentRepository: DocumentRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(LaunchUiState())
    val uiState: StateFlow<LaunchUiState> = _uiState.asStateFlow()

    private val _setupComplete = MutableSharedFlow<Unit>()
    val setupComplete: SharedFlow<Unit> = _setupComplete.asSharedFlow()

    fun onStartClicked() {
        _uiState.update { it.copy(step = LaunchStep.FOLDER) }
    }

    fun onFolderSelected(rootUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(rootUri, flags)

                val appFolderUri = documentRepository.createAppFolder(rootUri)

                settingsRepository.setSafRootUri(rootUri)
                settingsRepository.setSafAppFolderUri(appFolderUri)

                val defaultName = documentRepository.generateUniqueName(appFolderUri, "내 할일")
                documentRepository.createDocument(appFolderUri, defaultName)
                settingsRepository.setCurrentDocumentName(defaultName)

                _uiState.update { it.copy(isLoading = false) }
                _setupComplete.emit(Unit)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "폴더 설정에 실패했습니다: ${e.message}") }
            }
        }
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val documentRepository: DocumentRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LaunchViewModel(settingsRepository, documentRepository, context.applicationContext) as T
        }
    }
}
