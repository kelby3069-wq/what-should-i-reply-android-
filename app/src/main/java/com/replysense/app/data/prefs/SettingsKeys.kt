package com.replysense.app.data.prefs

import androidx.datastore.preferences.core.booleanPreferencesKey

object SettingsKeys {

    val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    val PRO_UNLOCKED = booleanPreferencesKey("pro_unlocked")
}
