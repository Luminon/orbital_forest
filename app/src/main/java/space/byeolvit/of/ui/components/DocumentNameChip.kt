package space.byeolvit.of.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.byeolvit.of.ui.theme.PrimaryBadge

@Composable
fun DocumentNameChip(
    fileName: String,
    modifier: Modifier = Modifier
) {
    val nameWithoutExt = fileName.removeSuffix(".md")
    val hasExt = fileName.endsWith(".md")

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = nameWithoutExt,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                lineHeight = 48.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (hasExt) {
            Text(
                text = ".md",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .background(
                        color = PrimaryBadge,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}
