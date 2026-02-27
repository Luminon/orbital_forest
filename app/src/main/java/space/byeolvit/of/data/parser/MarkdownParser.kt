package space.byeolvit.of.data.parser

import space.byeolvit.of.data.model.ChecklistItem
import space.byeolvit.of.data.model.DocumentBlock
import space.byeolvit.of.data.model.ParsedDocument

object MarkdownParser {

    private val CHECKLIST_REGEX = Regex("""^(\s*)- \[( |x)] (.*)$""")

    private data class RawChecklistLine(
        val indent: Int,
        val isChecked: Boolean,
        val text: String
    )

    fun parse(content: String, fileName: String): ParsedDocument {
        val lines = content.lines()
        val blocks = mutableListOf<DocumentBlock>()
        val memoBuffer = StringBuilder()
        val checklistBuffer = mutableListOf<RawChecklistLine>()

        fun flushMemo() {
            val memo = memoBuffer.toString().trim()
            if (memo.isNotEmpty()) {
                blocks.add(DocumentBlock.MemoBlock(memo))
            }
            memoBuffer.clear()
        }

        fun flushChecklist() {
            if (checklistBuffer.isNotEmpty()) {
                blocks.add(DocumentBlock.ChecklistBlock(buildTree(checklistBuffer.toList())))
                checklistBuffer.clear()
            }
        }

        for (line in lines) {
            val match = CHECKLIST_REGEX.matchEntire(line)
            if (match != null) {
                flushMemo()
                val indent = countIndent(match.groupValues[1])
                checklistBuffer.add(
                    RawChecklistLine(
                        indent = indent,
                        isChecked = match.groupValues[2] == "x",
                        text = match.groupValues[3]
                    )
                )
            } else {
                flushChecklist()
                memoBuffer.appendLine(line)
            }
        }

        flushChecklist()
        flushMemo()

        return ParsedDocument(fileName, blocks)
    }

    private fun countIndent(prefix: String): Int {
        return if (prefix.contains('\t')) {
            prefix.count { it == '\t' }
        } else {
            prefix.length / 2
        }
    }

    private fun buildTree(lines: List<RawChecklistLine>): List<ChecklistItem> {
        if (lines.isEmpty()) return emptyList()
        val result = mutableListOf<ChecklistItem>()
        var i = 0

        while (i < lines.size) {
            val current = lines[i]
            val children = mutableListOf<RawChecklistLine>()

            var j = i + 1
            while (j < lines.size && lines[j].indent > current.indent) {
                children.add(lines[j])
                j++
            }

            val childItems = if (children.isNotEmpty()) buildTree(children) else emptyList()

            result.add(
                ChecklistItem(
                    rawText = current.text,
                    isChecked = current.isChecked,
                    indentLevel = current.indent,
                    children = childItems
                )
            )

            i = j
        }

        return result
    }
}
