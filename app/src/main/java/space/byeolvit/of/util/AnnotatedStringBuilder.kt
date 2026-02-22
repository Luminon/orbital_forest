package space.byeolvit.of.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

fun parseInlineMarkdown(text: String): AnnotatedString = buildAnnotatedString {
    var i = 0
    val chars = text.toCharArray()

    while (i < chars.size) {
        when {
            // Bold: **text** or __text__
            i + 1 < chars.size && chars[i] == '*' && chars[i + 1] == '*' -> {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(text.substring(i + 2, end))
                    pop()
                    i = end + 2
                } else {
                    append(chars[i])
                    i++
                }
            }
            // Strikethrough: ~~text~~
            i + 1 < chars.size && chars[i] == '~' && chars[i + 1] == '~' -> {
                val end = text.indexOf("~~", i + 2)
                if (end != -1) {
                    pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                    append(text.substring(i + 2, end))
                    pop()
                    i = end + 2
                } else {
                    append(chars[i])
                    i++
                }
            }
            // Italic: *text* (single, not double)
            chars[i] == '*' && (i + 1 >= chars.size || chars[i + 1] != '*') -> {
                val end = text.indexOf('*', i + 1)
                if (end != -1 && (end + 1 >= chars.size || chars[end + 1] != '*')) {
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(text.substring(i + 1, end))
                    pop()
                    i = end + 1
                } else {
                    append(chars[i])
                    i++
                }
            }
            else -> {
                append(chars[i])
                i++
            }
        }
    }
}
