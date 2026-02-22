package space.byeolvit.of.data.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val safRootUri: Flow<Uri?>
    val safAppFolderUri: Flow<Uri?>
    val hideCompleted: Flow<Boolean>
    val hideNonChecklist: Flow<Boolean>
    val childInteraction: Flow<Boolean>
    val currentDocumentName: Flow<String>

    suspend fun setSafRootUri(uri: Uri)
    suspend fun setSafAppFolderUri(uri: Uri)
    suspend fun setHideCompleted(value: Boolean)
    suspend fun setHideNonChecklist(value: Boolean)
    suspend fun setChildInteraction(value: Boolean)
    suspend fun setCurrentDocumentName(name: String)
    suspend fun clearSafUri()
}
