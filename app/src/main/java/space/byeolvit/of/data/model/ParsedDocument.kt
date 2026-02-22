package space.byeolvit.of.data.model

sealed class DocumentBlock {
    data class ChecklistBlock(val items: List<ChecklistItem>) : DocumentBlock()
    data class MemoBlock(val rawText: String) : DocumentBlock()
}

data class ParsedDocument(
    val fileName: String,
    val blocks: List<DocumentBlock>
) {
    val allChecklistItems: List<ChecklistItem>
        get() = blocks
            .filterIsInstance<DocumentBlock.ChecklistBlock>()
            .flatMap { flattenItems(it.items) }

    val totalCount: Int get() = allChecklistItems.count { it.indentLevel <= 2 }
    val completedCount: Int get() = allChecklistItems.count { it.isChecked && it.indentLevel <= 2 }

    private fun flattenItems(items: List<ChecklistItem>): List<ChecklistItem> {
        return items.flatMap { item -> listOf(item) + flattenItems(item.children) }
    }
}

data class DocumentMeta(
    val fileName: String
)
