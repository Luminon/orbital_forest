package space.byeolvit.of.ui.screen.home.newdoc

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import space.byeolvit.of.OrbitalForestApp
import space.byeolvit.of.data.repository.DocumentRepository
import space.byeolvit.of.data.repository.DocumentRepositoryImpl
import space.byeolvit.of.data.repository.SettingsRepository
import space.byeolvit.of.data.repository.SettingsRepositoryImpl
import space.byeolvit.of.data.source.DocumentDataSource

@Composable
fun NewDocumentDialog(
    settingsRepository: SettingsRepository?,
    documentRepository: DocumentRepository?,
    onCreated: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as OrbitalForestApp

    val resolvedSettingsRepo = settingsRepository
        ?: SettingsRepositoryImpl(app.appDataStore)
    val resolvedDocumentRepo = documentRepository
        ?: DocumentRepositoryImpl(DocumentDataSource(context))

    val vm: NewDocumentViewModel = viewModel(
        factory = NewDocumentViewModel.Factory(resolvedSettingsRepo, resolvedDocumentRepo)
    )

    val uiState by vm.uiState.collectAsState()
    val focusRequester = androidx.compose.runtime.remember { FocusRequester() }

    LaunchedEffect(Unit) {
        vm.loadDefaultName()
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("새 문서 만들기") },
        text = {
            OutlinedTextField(
                value = uiState.nameText,
                onValueChange = { vm.onNameChanged(it) },
                label = { Text("문서 이름") },
                singleLine = true,
                isError = uiState.error != null,
                supportingText = uiState.error?.let { { Text(it) } },
                modifier = Modifier.focusRequester(focusRequester)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { vm.onCreate { fileName -> onCreated(fileName) } },
                enabled = uiState.nameText.isNotBlank() && !uiState.isLoading
            ) {
                Text("만들기")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
