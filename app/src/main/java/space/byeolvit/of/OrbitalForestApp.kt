package space.byeolvit.of

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import space.byeolvit.of.data.source.dataStore

class OrbitalForestApp : Application() {

    val appDataStore: DataStore<Preferences> by lazy { dataStore }
}
