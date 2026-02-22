package space.byeolvit.of.data.source

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "of_settings")

object PreferenceKeys {
    val SAF_ROOT_URI = stringPreferencesKey("saf_root_uri")
    val SAF_APP_FOLDER_URI = stringPreferencesKey("saf_app_folder_uri")
    val HIDE_COMPLETED = booleanPreferencesKey("hide_completed")
    val HIDE_NON_CHECKLIST = booleanPreferencesKey("hide_non_checklist")
    val CHILD_INTERACTION = booleanPreferencesKey("child_interaction")
    val CURRENT_DOCUMENT_NAME = stringPreferencesKey("current_document_name")
}
