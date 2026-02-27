package space.byeolvit.of.data.source

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DocumentDataSource(private val context: Context) {

    private val contentResolver: ContentResolver get() = context.contentResolver

    suspend fun createAppFolder(rootUri: Uri): Uri = withContext(Dispatchers.IO) {
        val rootDoc = DocumentFile.fromTreeUri(context, rootUri)
            ?: error("루트 URI를 열 수 없습니다.")

        val existing = rootDoc.findFile(APP_FOLDER_NAME)
        if (existing != null && existing.isDirectory) {
            return@withContext existing.uri
        }

        val created = rootDoc.createDirectory(APP_FOLDER_NAME)
            ?: error("앱 폴더를 생성할 수 없습니다.")
        created.uri
    }

    suspend fun listMdFiles(appFolderUri: Uri): List<String> = withContext(Dispatchers.IO) {
        val folder = DocumentFile.fromTreeUri(context, appFolderUri)
            ?: return@withContext emptyList()
        folder.listFiles()
            .filter { it.isFile && it.name?.endsWith(".md") == true }
            .mapNotNull { it.name }
            .sorted()
    }

    suspend fun readFile(appFolderUri: Uri, fileName: String): String = withContext(Dispatchers.IO) {
        val folder = DocumentFile.fromTreeUri(context, appFolderUri)
            ?: error("앱 폴더를 열 수 없습니다.")
        val file = folder.findFile(fileName)
            ?: error("파일을 찾을 수 없습니다: $fileName")
        contentResolver.openInputStream(file.uri)?.use { stream ->
            stream.bufferedReader().readText()
        } ?: ""
    }

    suspend fun writeFile(appFolderUri: Uri, fileName: String, content: String) = withContext(Dispatchers.IO) {
        val folder = DocumentFile.fromTreeUri(context, appFolderUri)
            ?: error("앱 폴더를 열 수 없습니다.")
        val file = folder.findFile(fileName) ?: folder.createFile(mimeTypeFor(fileName), fileName)
            ?: error("파일을 생성할 수 없습니다: $fileName")
        contentResolver.openOutputStream(file.uri, "wt")?.use { stream ->
            stream.bufferedWriter().use { it.write(content) }
        }
    }

    suspend fun createFile(appFolderUri: Uri, fileName: String): Uri = withContext(Dispatchers.IO) {
        val folder = DocumentFile.fromTreeUri(context, appFolderUri)
            ?: error("앱 폴더를 열 수 없습니다.")
        val existing = folder.findFile(fileName)
        if (existing != null) return@withContext existing.uri
        val created = folder.createFile(mimeTypeFor(fileName), fileName)
            ?: error("파일을 생성할 수 없습니다: $fileName")
        created.uri
    }

    suspend fun deleteFile(appFolderUri: Uri, fileName: String) = withContext(Dispatchers.IO) {
        val folder = DocumentFile.fromTreeUri(context, appFolderUri)
            ?: error("앱 폴더를 열 수 없습니다.")
        folder.findFile(fileName)?.delete()
    }

    suspend fun renameFile(appFolderUri: Uri, oldName: String, newName: String): String = withContext(Dispatchers.IO) {
        val folder = DocumentFile.fromTreeUri(context, appFolderUri)
            ?: error("앱 폴더를 열 수 없습니다.")
        val file = folder.findFile(oldName) ?: error("파일을 찾을 수 없습니다: $oldName")
        file.renameTo(newName)
        // DocumentFile.renameTo 이후 실제 파일명 확인
        val renamed = folder.findFile(newName)
            ?: folder.findFile(newName.removeSuffix(".md"))
        renamed?.name ?: newName
    }

    suspend fun fileExists(appFolderUri: Uri, fileName: String): Boolean = withContext(Dispatchers.IO) {
        val folder = DocumentFile.fromTreeUri(context, appFolderUri) ?: return@withContext false
        folder.findFile(fileName) != null
    }

    suspend fun isUriValid(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val flags = contentResolver.persistedUriPermissions
            flags.any { it.uri == uri && it.isReadPermission && it.isWritePermission }
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        const val APP_FOLDER_NAME = "OrbitalForest"

        /** 파일명의 확장자에 맞는 MIME 타입을 반환. 기기 MimeTypeMap에서 동적 조회하므로
         *  `.md` → `text/markdown`(Android 10+) 또는 기기별 매핑값을 정확히 반환한다.
         *  매핑이 없으면 `text/plain` 사용. */
        private fun mimeTypeFor(fileName: String): String {
            val ext = fileName.substringAfterLast(".", "")
            return if (ext.isNotEmpty()) {
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "text/plain"
            } else "text/plain"
        }
    }
}
