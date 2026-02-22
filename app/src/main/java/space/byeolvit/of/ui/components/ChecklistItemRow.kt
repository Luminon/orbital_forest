package space.byeolvit.of.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import space.byeolvit.of.data.model.ChecklistItem
import space.byeolvit.of.util.parseInlineMarkdown

private const val MAX_DISPLAY_DEPTH = 2
private val INDENT_DP = 20.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChecklistItemRow(
    item: ChecklistItem,
    depth: Int = 0,
    onChecked: (ChecklistItem, Boolean) -> Unit,
    onLongPress: (ChecklistItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (item.indentLevel > MAX_DISPLAY_DEPTH) {
        ErrorChecklistItem(item = item)
        return
    }

    val itemBg = if (item.isChecked) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(itemBg)
                .combinedClickable(
                    onClick = { onChecked(item, !item.isChecked) },
                    onLongClick = { onLongPress(item) }
                )
                .padding(start = INDENT_DP * depth, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { checked -> onChecked(item, checked) }
            )
            val annotated = remember(item.rawText) { parseInlineMarkdown(item.rawText) }
            Text(
                text = annotated,
                style = MaterialTheme.typography.titleMedium.copy(
                    lineBreak = LineBreak.Paragraph
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp)
            )
        }

        item.children.forEach { child ->
            ChecklistItemRow(
                item = child,
                depth = depth + 1,
                onChecked = onChecked,
                onLongPress = onLongPress
            )
        }
    }
}

@Composable
private fun ErrorChecklistItem(item: ChecklistItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "⚠ 들여쓰기가 너무 깊습니다: ${item.rawText}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}
