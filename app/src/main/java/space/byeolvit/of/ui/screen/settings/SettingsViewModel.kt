package space.byeolvit.of.ui.screen.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import space.byeolvit.of.data.repository.DocumentRepository
import space.byeolvit.of.data.repository.SettingsRepository

data class SettingsUiState(
    val hideCompleted: Boolean = false,
    val hideNonChecklist: Boolean = false,
    val childInteraction: Boolean = true,
    val appVersion: String = "",
    val isLoading: Boolean = false
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val documentRepository: DocumentRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
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

        loadAppVersion()
    }

    private fun loadAppVersion() {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            _uiState.update { it.copy(appVersion = packageInfo.versionName ?: "1.0.0") }
        } catch (_: Exception) {
            _uiState.update { it.copy(appVersion = "1.0.0") }
        }
    }

    fun onHideCompletedChanged(value: Boolean) {
        viewModelScope.launch { settingsRepository.setHideCompleted(value) }
    }

    fun onHideNonChecklistChanged(value: Boolean) {
        viewModelScope.launch { settingsRepository.setHideNonChecklist(value) }
    }

    fun onChildInteractionChanged(value: Boolean) {
        viewModelScope.launch { settingsRepository.setChildInteraction(value) }
    }

    fun onChangeFolderSelected(rootUri: Uri, onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(rootUri, flags)

                val appFolderUri = documentRepository.createAppFolder(rootUri)
                settingsRepository.setSafRootUri(rootUri)
                settingsRepository.setSafAppFolderUri(appFolderUri)
                settingsRepository.setCurrentDocumentName("")
                _uiState.update { it.copy(isLoading = false) }
                onComplete()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
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
            return SettingsViewModel(settingsRepository, documentRepository, context.applicationContext) as T
        }
    }
}
