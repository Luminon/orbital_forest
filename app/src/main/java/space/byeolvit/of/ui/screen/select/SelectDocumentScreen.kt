package space.byeolvit.of.ui.screen.select

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import space.byeolvit.of.data.model.DocumentMeta
import space.byeolvit.of.data.model.ParsedDocument
import space.byeolvit.of.ui.components.DocumentMenuSheet
import space.byeolvit.of.ui.components.DocumentNameChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectDocumentScreen(
    viewModel: SelectDocumentViewModel,
    onDocumentSelected: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("문서 선택") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 안내 배너
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "같은 폴더에 저장되어 있는 .md 문서가 모두 표시됩니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }

            LazyColumn {
                items(uiState.documents, key = { it.fileName }) { doc ->
                    DocumentListItem(
                        doc = doc,
                        isSelected = doc.fileName == uiState.currentDocumentName,
                        onClick = {
                            viewModel.onDocumentSelected(doc.fileName)
                            onDocumentSelected()
                        },
                        onLongClick = { viewModel.onLongPress(doc) }
                    )
                }
            }
        }
    }

    // 길게 누름 시 DocumentMenuSheet
    uiState.contextMenuTarget?.let { target ->
        // ParsedDocument stub for menu (no blocks needed here)
        val stub = ParsedDocument(fileName = target.fileName, blocks = emptyList())
        DocumentMenuSheet(
            document = stub,
            onDismiss = { viewModel.onContextMenuDismiss() },
            onDelete = { viewModel.onDeleteDocument(target.fileName) },
            onRename = { newName -> viewModel.onRenameDocument(target.fileName, newName) }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DocumentListItem(
    doc: DocumentMeta,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DocumentNameChip(fileName = doc.fileName)
        Spacer(modifier = Modifier.width(8.dp))
        if (isSelected) {
            Text(
                text = "현재",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
