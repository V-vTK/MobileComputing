package com.example.myapplication.services

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

const val SETTINGS_DATASTORE = "settings_datastore"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SETTINGS_DATASTORE)

class SettingsStore(val context: Context) {

    companion object {
        val darkmode = stringPreferencesKey("darkmode")
    }

    suspend fun saveToDataStore(theme: Boolean) {
        context.dataStore.edit {
            it[darkmode] = theme.toString()

        }
    }

    fun getDarkMode() = context.dataStore.data.map {
        it[darkmode]?.toBoolean() ?: true
    }

    suspend fun clearDataStore() = context.dataStore.edit {
        it.clear()
    }

}