package com.replysense.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

// SINGLE source of truth for DataStore
val Context.settingsDataStore by preferencesDataStore(
    name = "replysense_settings"
)
