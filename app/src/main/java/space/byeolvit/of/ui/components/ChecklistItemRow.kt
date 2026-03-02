package space.byeolvit.of.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import space.byeolvit.of.R
import space.byeolvit.of.data.model.ChecklistItem
import space.byeolvit.of.util.parseInlineMarkdown

private const val MAX_DISPLAY_DEPTH = 1
private val INDENT_DP = 20.dp

private val ErrorBg = Color(0xFF3B1A1A)
private val ErrorText = Color(0xFFFF7474)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChecklistItemRow(
    item: ChecklistItem,
    depth: Int = 0,
    onChecked: (ChecklistItem, Boolean) -> Unit,
    onLongPress: (ChecklistItem) -> Unit,
    onOpenFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemBg = if (item.isChecked) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
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

        val validChildren = item.children.filter { it.indentLevel <= MAX_DISPLAY_DEPTH }
        val hasErrorChildren = validChildren.size < item.children.size

        validChildren.forEach { child ->
            ChecklistItemRow(
                item = child,
                depth = depth + 1,
                onChecked = onChecked,
                onLongPress = onLongPress,
                onOpenFile = onOpenFile
            )
        }

        if (hasErrorChildren) {
            ErrorChecklistItem(onOpenFile = onOpenFile)
        }
    }
}

@Composable
private fun ErrorChecklistItem(onOpenFile: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ErrorBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.error_too_deep),
            style = MaterialTheme.typography.labelMedium,
            color = ErrorText,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(R.string.btn_open_file),
            style = MaterialTheme.typography.labelMedium.copy(
                textDecoration = TextDecoration.Underline
            ),
            color = ErrorText,
            modifier = Modifier.clickable { onOpenFile() }
        )
    }
}
