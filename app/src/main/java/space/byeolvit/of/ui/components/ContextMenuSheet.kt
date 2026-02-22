package space.byeolvit.of.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import space.byeolvit.of.data.model.ChecklistItem
import space.byeolvit.of.util.parseInlineMarkdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextMenuSheet(
    item: ChecklistItem,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onAddChild: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val isSubItem = item.indentLevel >= 1

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "항목",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = parseInlineMarkdown(item.rawText),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            HorizontalDivider()

            TextButton(
                onClick = {
                    onDelete()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "삭제",
                    color = MaterialTheme.colorScheme.error
                )
            }

            TextButton(
                onClick = {
                    onCopy()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("복사")
            }

            TextButton(
                onClick = {
                    onAddChild()
                },
                enabled = !isSubItem,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "하위 항목",
                    color = if (!isSubItem) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }
    }
}
