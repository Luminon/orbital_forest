package space.byeolvit.of.data.model

import java.util.UUID

data class ChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val rawText: String,
    val isChecked: Boolean,
    val indentLevel: Int,
    val children: List<ChecklistItem> = emptyList()
)
