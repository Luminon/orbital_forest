package space.byeolvit.of.data.parser

import space.byeolvit.of.data.model.ChecklistItem
import space.byeolvit.of.data.model.DocumentBlock
import space.byeolvit.of.data.model.ParsedDocument

object MarkdownSerializer {

    fun serialize(document: ParsedDocument): String {
        val sb = StringBuilder()
        val blocks = document.blocks

        blocks.forEachIndexed { index, block ->
            when (block) {
                is DocumentBlock.ChecklistBlock -> {
                    serializeItems(sb, block.items)
                }
                is DocumentBlock.MemoBlock -> {
                    sb.append(block.rawText)
                }
            }
            if (index < blocks.lastIndex) {
                sb.append("\n\n")
            }
        }

        return sb.toString()
    }

    private fun serializeItems(sb: StringBuilder, items: List<ChecklistItem>, depth: Int = 0) {
        items.forEachIndexed { index, item ->
            val indent = "  ".repeat(depth)
            val check = if (item.isChecked) "x" else " "
            sb.append("$indent- [$check] ${item.rawText}")

            val hasMoreItems = index < items.lastIndex || item.children.isNotEmpty()
            if (hasMoreItems || depth > 0) {
                sb.append("\n")
            }

            if (item.children.isNotEmpty()) {
                serializeItems(sb, item.children, depth + 1)
            }
        }
    }
}
