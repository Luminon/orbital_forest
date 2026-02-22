package space.byeolvit.of

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import space.byeolvit.of.data.model.DocumentBlock
import space.byeolvit.of.data.parser.MarkdownParser
import space.byeolvit.of.data.parser.MarkdownSerializer

class MarkdownParserTest {

    @Test
    fun `parse empty content`() {
        val doc = MarkdownParser.parse("", "test.md")
        assertTrue(doc.blocks.isEmpty())
    }

    @Test
    fun `parse single unchecked item`() {
        val content = "- [ ] 할 일"
        val doc = MarkdownParser.parse(content, "test.md")
        assertEquals(1, doc.blocks.size)
        val block = doc.blocks[0] as DocumentBlock.ChecklistBlock
        assertEquals(1, block.items.size)
        assertEquals("할 일", block.items[0].rawText)
        assertFalse(block.items[0].isChecked)
        assertEquals(0, block.items[0].indentLevel)
    }

    @Test
    fun `parse single checked item`() {
        val content = "- [x] 완료된 항목"
        val doc = MarkdownParser.parse(content, "test.md")
        val block = doc.blocks[0] as DocumentBlock.ChecklistBlock
        assertTrue(block.items[0].isChecked)
    }

    @Test
    fun `parse indented child items`() {
        val content = """
            - [ ] 상위 항목
              - [ ] 하위 항목
        """.trimIndent()
        val doc = MarkdownParser.parse(content, "test.md")
        val block = doc.blocks[0] as DocumentBlock.ChecklistBlock
        assertEquals(1, block.items.size)
        assertEquals(1, block.items[0].children.size)
        assertEquals("하위 항목", block.items[0].children[0].rawText)
        assertEquals(1, block.items[0].children[0].indentLevel)
    }

    @Test
    fun `parse two-level indented items`() {
        val content = """
            - [ ] 1단계
              - [ ] 2단계
                - [ ] 3단계
        """.trimIndent()
        val doc = MarkdownParser.parse(content, "test.md")
        val block = doc.blocks[0] as DocumentBlock.ChecklistBlock
        val level1 = block.items[0]
        val level2 = level1.children[0]
        val level3 = level2.children[0]
        assertEquals(0, level1.indentLevel)
        assertEquals(1, level2.indentLevel)
        assertEquals(2, level3.indentLevel)
    }

    @Test
    fun `parse memo block between checklists`() {
        val content = """
            - [ ] 체크리스트 항목

            메모 내용입니다

            - [ ] 또 다른 체크리스트
        """.trimIndent()
        val doc = MarkdownParser.parse(content, "test.md")
        assertTrue(doc.blocks.any { it is DocumentBlock.MemoBlock })
        assertTrue(doc.blocks.any { it is DocumentBlock.ChecklistBlock })
    }

    @Test
    fun `parse inline markdown preserved in rawText`() {
        val content = "- [ ] **굵은** 텍스트와 *기울기* 텍스트"
        val doc = MarkdownParser.parse(content, "test.md")
        val block = doc.blocks[0] as DocumentBlock.ChecklistBlock
        assertEquals("**굵은** 텍스트와 *기울기* 텍스트", block.items[0].rawText)
    }

    @Test
    fun `serialize and parse roundtrip`() {
        val content = """
            - [ ] 항목 1
              - [x] 하위 항목
            - [x] 항목 2
        """.trimIndent()
        val doc = MarkdownParser.parse(content, "test.md")
        val serialized = MarkdownSerializer.serialize(doc)
        val reparsed = MarkdownParser.parse(serialized, "test.md")

        assertEquals(doc.blocks.size, reparsed.blocks.size)
        val original = (doc.blocks[0] as DocumentBlock.ChecklistBlock).items
        val result = (reparsed.blocks[0] as DocumentBlock.ChecklistBlock).items
        assertEquals(original.size, result.size)
        assertEquals(original[0].rawText, result[0].rawText)
        assertEquals(original[0].isChecked, result[0].isChecked)
        assertEquals(original[0].children.size, result[0].children.size)
        assertEquals(original[1].isChecked, result[1].isChecked)
    }

    @Test
    fun `document totalCount and completedCount`() {
        val content = """
            - [ ] 항목 1
            - [x] 항목 2
            - [x] 항목 3
        """.trimIndent()
        val doc = MarkdownParser.parse(content, "test.md")
        assertEquals(3, doc.totalCount)
        assertEquals(2, doc.completedCount)
    }
}
