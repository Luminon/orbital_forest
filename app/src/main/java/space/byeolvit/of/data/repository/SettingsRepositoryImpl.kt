package space.byeolvit.of.data.repository

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import space.byeolvit.of.data.source.PreferenceKeys

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override val safRootUri: Flow<Uri?> = dataStore.data
        .map { prefs -> prefs[PreferenceKeys.SAF_ROOT_URI]?.let { Uri.parse(it) } }

    override val safAppFolderUri: Flow<Uri?> = dataStore.data
        .map { prefs -> prefs[PreferenceKeys.SAF_APP_FOLDER_URI]?.let { Uri.parse(it) } }

    override val hideCompleted: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[PreferenceKeys.HIDE_COMPLETED] ?: false }

    override val hideNonChecklist: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[PreferenceKeys.HIDE_NON_CHECKLIST] ?: false }

    override val childInteraction: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[PreferenceKeys.CHILD_INTERACTION] ?: true }

    override val currentDocumentName: Flow<String> = dataStore.data
        .map { prefs -> prefs[PreferenceKeys.CURRENT_DOCUMENT_NAME] ?: "" }

    override suspend fun setSafRootUri(uri: Uri) {
        dataStore.edit { it[PreferenceKeys.SAF_ROOT_URI] = uri.toString() }
    }

    override suspend fun setSafAppFolderUri(uri: Uri) {
        dataStore.edit { it[PreferenceKeys.SAF_APP_FOLDER_URI] = uri.toString() }
    }

    override suspend fun setHideCompleted(value: Boolean) {
        dataStore.edit { it[PreferenceKeys.HIDE_COMPLETED] = value }
    }

    override suspend fun setHideNonChecklist(value: Boolean) {
        dataStore.edit { it[PreferenceKeys.HIDE_NON_CHECKLIST] = value }
    }

    override suspend fun setChildInteraction(value: Boolean) {
        dataStore.edit { it[PreferenceKeys.CHILD_INTERACTION] = value }
    }

    override suspend fun setCurrentDocumentName(name: String) {
        dataStore.edit { it[PreferenceKeys.CURRENT_DOCUMENT_NAME] = name }
    }

    override suspend fun clearSafUri() {
        dataStore.edit {
            it.remove(PreferenceKeys.SAF_ROOT_URI)
            it.remove(PreferenceKeys.SAF_APP_FOLDER_URI)
            it.remove(PreferenceKeys.CURRENT_DOCUMENT_NAME)
        }
    }
}
