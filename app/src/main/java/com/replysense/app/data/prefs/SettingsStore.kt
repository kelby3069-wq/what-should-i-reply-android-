package com.replysense.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsStore(context: Context) {

    private val dataStore = context.settingsDataStore

    val onboardingComplete: Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[SettingsKeys.ONBOARDING_COMPLETE] ?: false
        }

    val proUnlocked: Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[SettingsKeys.PRO_UNLOCKED] ?: false
        }

    suspend fun setOnboardingComplete(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[SettingsKeys.ONBOARDING_COMPLETE] = value
        }
    }

    suspend fun setProUnlocked(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[SettingsKeys.PRO_UNLOCKED] = value
        }
    }
}
