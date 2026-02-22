package space.byeolvit.of.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import space.byeolvit.of.util.parseInlineMarkdown

@Composable
fun MemoBlock(
    rawText: String,
    modifier: Modifier = Modifier
) {
    val annotated = remember(rawText) { parseInlineMarkdown(rawText) }

    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyMedium.copy(
            lineBreak = LineBreak.Paragraph
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
