package com.example.myapplication.services

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

const val USER_DATASTORE = "user_datastore"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_DATASTORE)

class AuthStoreManager(val context: Context) {

    companion object {
        val avatar = stringPreferencesKey("avatar")
        val collectionId = stringPreferencesKey("collectionId")
        val collectionName = stringPreferencesKey("collectionName")
        val created = stringPreferencesKey("created")
        val email = stringPreferencesKey("email")
        val emailVisibility = stringPreferencesKey("emailVisibility")
        val id = stringPreferencesKey("id")
        val name = stringPreferencesKey("name")
        val updated = stringPreferencesKey("updated")
        val verified = stringPreferencesKey("verified")
        val token = stringPreferencesKey("token")
    }

    suspend fun saveToDataStore(authResponse: AuthResponse) {
        context.dataStore.edit {
            it[avatar] = authResponse.record.avatar
            it[collectionId] = authResponse.record.collectionId
            it[collectionName] = authResponse.record.collectionName
            it[created] = authResponse.record.created
            it[email] = authResponse.record.email
            it[emailVisibility] = authResponse.record.emailVisibility.toString()
            it[id] = authResponse.record.id
            it[name] = authResponse.record.name
            it[updated] = authResponse.record.updated
            it[verified] = authResponse.record.verified.toString()
            it[token] = authResponse.token
        }
    }

    fun getFromDataStore() = context.dataStore.data.map {
        AuthResponse(
            record = UserRecord(
                avatar = it[avatar] ?: "",
                collectionId = it[collectionId] ?: "",
                collectionName = it[collectionName] ?: "",
                created = it[created] ?: "",
                email = it[email] ?: "",
                emailVisibility = it[emailVisibility]?.toBoolean() ?: false,
                id = it[id] ?: "",
                name = it[name] ?: "",
                updated = it[updated] ?: "",
                verified = it[verified]?.toBoolean() ?: false
            ),
            token = it[token] ?: ""
        )
    }

    suspend fun clearDataStore() = context.dataStore.edit {
        it.clear()
    }
}
