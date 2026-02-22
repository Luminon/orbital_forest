package space.byeolvit.of.data.repository

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import space.byeolvit.of.data.model.DocumentMeta
import space.byeolvit.of.data.model.ParsedDocument
import space.byeolvit.of.data.parser.MarkdownParser
import space.byeolvit.of.data.parser.MarkdownSerializer
import space.byeolvit.of.data.source.DocumentDataSource

class DocumentRepositoryImpl(
    private val dataSource: DocumentDataSource
) : DocumentRepository {

    override fun getAllDocuments(appFolderUri: Uri): Flow<List<DocumentMeta>> = flow {
        val names = dataSource.listMdFiles(appFolderUri)
        emit(names.map { DocumentMeta(it) })
    }.flowOn(Dispatchers.IO)

    override suspend fun readDocument(appFolderUri: Uri, fileName: String): ParsedDocument {
        val content = dataSource.readFile(appFolderUri, fileName)
        return MarkdownParser.parse(content, fileName)
    }

    override suspend fun saveDocument(appFolderUri: Uri, document: ParsedDocument) {
        val content = MarkdownSerializer.serialize(document)
        dataSource.writeFile(appFolderUri, document.fileName, content)
    }

    override suspend fun createDocument(appFolderUri: Uri, fileName: String): ParsedDocument {
        dataSource.createFile(appFolderUri, fileName)
        return ParsedDocument(fileName = fileName, blocks = emptyList())
    }

    override suspend fun deleteDocument(appFolderUri: Uri, fileName: String) {
        dataSource.deleteFile(appFolderUri, fileName)
    }

    override suspend fun renameDocument(appFolderUri: Uri, oldName: String, newName: String): String {
        val nameWithExt = if (newName.endsWith(".md")) newName else "$newName.md"
        return dataSource.renameFile(appFolderUri, oldName, nameWithExt)
    }

    override suspend fun documentExists(appFolderUri: Uri, fileName: String): Boolean {
        return dataSource.fileExists(appFolderUri, fileName)
    }

    override suspend fun generateUniqueName(appFolderUri: Uri, baseName: String): String {
        val defaultName = "$baseName.md"
        if (!dataSource.fileExists(appFolderUri, defaultName)) {
            return defaultName
        }
        var n = 2
        while (true) {
            val candidate = "$baseName $n.md"
            if (!dataSource.fileExists(appFolderUri, candidate)) {
                return candidate
            }
            n++
        }
    }

    override suspend fun createAppFolder(rootUri: Uri): Uri {
        return dataSource.createAppFolder(rootUri)
    }
}
