package space.byeolvit.of.ui.screen.select

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import space.byeolvit.of.R
import space.byeolvit.of.data.model.DocumentMeta
import space.byeolvit.of.data.model.ParsedDocument
import space.byeolvit.of.ui.components.DocumentMenuSheet

@Composable
fun SelectDocumentScreen(
    viewModel: SelectDocumentViewModel,
    onDocumentSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 배경 글로우
        val glowColor = MaterialTheme.colorScheme.primary
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(size.width / 2f, size.height + 22.dp.toPx()),
                    radius = size.width * 0.74f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // 앱바
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        onClick = onBack,
                        shape = RoundedCornerShape(100.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "뒤로",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Text(
                    text = "문서 선택",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 안내 배너
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF172E33),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "같은 폴더에 저장되어 있는 .md 문서가 모두 표시됩니다.",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF74E8FF),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 문서 목록
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(16.dp)),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                uiState.documents.forEach { doc ->
                    val isSelected = doc.fileName == uiState.currentDocumentName
                    DocumentListItem(
                        doc = doc,
                        isSelected = isSelected,
                        onClick = {
                            viewModel.onDocumentSelected(doc.fileName) { onDocumentSelected(it) }
                        },
                        onLongClick = { viewModel.onLongPress(doc) }
                    )
                }
            }
        }
    }

    // 길게 눌렀을 때 문서 메뉴 시트
    uiState.contextMenuTarget?.let { target ->
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
                if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.surface
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = doc.fileName,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
