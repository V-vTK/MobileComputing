package com.example.myapplication

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map


const val USER_DATASTORE ="user_datastore"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_DATASTORE)

data class userStore(
    val username: String = "",
    val password: String = "",
    val profile_picture: String = "",
)

// https://medium.com/jetpack-composers/android-jetpack-datastore-5dfdfea4a3ea
class DataStoreManager (val context: Context) {

    companion object {
        val username = stringPreferencesKey("username")
        val password = stringPreferencesKey("password")
        val profile_picture = stringPreferencesKey("profile_picture")
    }

    suspend fun saveToDataStore(userStore: userStore) {
        context.dataStore.edit {
            it[username] = userStore.username
            it[password] = userStore.password
            it[profile_picture] = userStore.profile_picture
        }
    }

    fun getFromDataStore() = context.dataStore.data.map {
        userStore(
            username = it[username]?:"",
            password = it[password]?:"",
            profile_picture = it[profile_picture]?:""
        )
    }

    suspend fun clearDataStore() = context.dataStore.edit {
        it.clear()
    }

}