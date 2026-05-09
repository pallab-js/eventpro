package com.eventpro.admin.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

enum class ThemeMode(val value: String) {
    SYSTEM("SYSTEM"), LIGHT("LIGHT"), DARK("DARK")
}

@Singleton
class AppPreferences @Inject constructor(@ApplicationContext private val context: Context) {
    private val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    private val THEME_MODE = stringPreferencesKey("theme_mode")

    val isFirstLaunch = context.dataStore.data.map { it[IS_FIRST_LAUNCH] ?: true }

    val themeMode: Flow<String> = context.dataStore.data.map { it[THEME_MODE] ?: ThemeMode.SYSTEM.value }

    suspend fun setFirstLaunchDone() {
        context.dataStore.edit { it[IS_FIRST_LAUNCH] = false }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[THEME_MODE] = mode }
    }
}
