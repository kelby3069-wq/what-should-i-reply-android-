package com.replysense.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.replysense.app.domain.prefs.*

private val Context.dataStore by preferencesDataStore(name = "user_presentation_prefs")

class UserPreferencesDataStore(private val context: Context) {

    private object Keys {
        val OPTED_IN = booleanPreferencesKey("opted_in")
        val TONE = stringPreferencesKey("tone")
        val DENSITY = stringPreferencesKey("density")
    }

    private val store: DataStore<Preferences> = context.dataStore

    val prefsFlow: Flow<UserPresentationPrefs> = store.data.map { p ->
        val optedIn = p[Keys.OPTED_IN] ?: false
        if (!optedIn) {
            UserPresentationPrefs(optedIn = false)
        } else {
            UserPresentationPrefs(
                optedIn = true,
                tone = runCatching { CoachingTone.valueOf(p[Keys.TONE] ?: CoachingTone.BALANCED.name) }
                    .getOrDefault(CoachingTone.BALANCED),
                density = runCatching { ExplanationDensity.valueOf(p[Keys.DENSITY] ?: ExplanationDensity.STANDARD.name) }
                    .getOrDefault(ExplanationDensity.STANDARD)
            )
        }
    }

    suspend fun optInAndSave(tone: CoachingTone, density: ExplanationDensity) {
        store.updateData { prefs ->
            prefs.toMutablePreferences().apply {
                this[Keys.OPTED_IN] = true
                this[Keys.TONE] = tone.name
                this[Keys.DENSITY] = density.name
            }
        }
    }

    suspend fun revokeConsent() {
        store.updateData { prefs ->
            prefs.toMutablePreferences().apply {
                this[Keys.OPTED_IN] = false
                remove(Keys.TONE)
                remove(Keys.DENSITY)
            }
        }
    }
}
