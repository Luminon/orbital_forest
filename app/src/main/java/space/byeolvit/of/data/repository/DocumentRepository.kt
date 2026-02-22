package space.byeolvit.of.data.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import space.byeolvit.of.data.model.DocumentMeta
import space.byeolvit.of.data.model.ParsedDocument

interface DocumentRepository {
    fun getAllDocuments(appFolderUri: Uri): Flow<List<DocumentMeta>>
    suspend fun readDocument(appFolderUri: Uri, fileName: String): ParsedDocument
    suspend fun saveDocument(appFolderUri: Uri, document: ParsedDocument)
    suspend fun createDocument(appFolderUri: Uri, fileName: String): ParsedDocument
    suspend fun deleteDocument(appFolderUri: Uri, fileName: String)
    suspend fun renameDocument(appFolderUri: Uri, oldName: String, newName: String): String
    suspend fun documentExists(appFolderUri: Uri, fileName: String): Boolean
    suspend fun generateUniqueName(appFolderUri: Uri, baseName: String = "내 할일"): String
    suspend fun createAppFolder(rootUri: Uri): Uri
}
