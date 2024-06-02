// UserRepository.kt
package com.slf.module.data.model

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

// Define keys for the preferences.
val USER_ID_KEY = stringPreferencesKey("user_id")
val user_KEY = stringPreferencesKey("user_name")
val USER_EMAIL_KEY = stringPreferencesKey("user_email")
val USER_BORN_KEY = stringPreferencesKey("user_born")
val USER_ADDRESS_KEY = stringPreferencesKey("user_address")
val USER_USERNAME_KEY = stringPreferencesKey("user_username")
val USER_PASSWORD_KEY = stringPreferencesKey("user_password")
val USER_PROFILE_PHOTO_KEY = stringPreferencesKey("user_profile_photo")


// Create the data store.
val Context.dataStore by preferencesDataStore("user_prefs")

class UserRepository(context: Context) {
    private val dataStore = context.dataStore

    // Save user to Preferences DataStore.
    suspend fun saveUser(user: User) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = user.id?.toString() ?: "0"
            preferences[user_KEY] = user.name ?: ""
            preferences[USER_PROFILE_PHOTO_KEY] = user.profilePhoto ?: ""
            preferences[USER_EMAIL_KEY] = user.email ?: ""
            preferences[USER_BORN_KEY] = user.born ?: ""
            preferences[USER_ADDRESS_KEY] = user.address ?: ""
            preferences[USER_USERNAME_KEY] = user.username ?: ""
            preferences[USER_PASSWORD_KEY] = user.password ?: ""
        }
        Log.d("UserRepository", "Saved user: $user")
    }

    // Get user from Preferences DataStore.
    suspend fun getUser(): User {
        val preferences = dataStore.data.first()
        val userId = preferences[USER_ID_KEY]?.toInt() ?: 0
        val userName = preferences[user_KEY] ?: ""
        val userProfilePhoto = preferences[USER_PROFILE_PHOTO_KEY] ?: ""
        val userEmail = preferences[USER_EMAIL_KEY] ?: ""
        val userBorn = preferences[USER_BORN_KEY] ?: ""
        val userAddress = preferences[USER_ADDRESS_KEY] ?: ""
        val userUsername = preferences[USER_USERNAME_KEY] ?: ""
        val userPassword = preferences[USER_PASSWORD_KEY] ?: ""
        val user = User(userId, userName, userProfilePhoto, userEmail, userBorn, userAddress, userUsername, userPassword)
        Log.d("UserRepository", "Retrieved user: $user")
        return user
    }

    // Delete user from Preferences DataStore.
    suspend fun deleteUser() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
            preferences.remove(user_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(USER_BORN_KEY)
            preferences.remove(USER_ADDRESS_KEY)
            preferences.remove(USER_USERNAME_KEY)
            preferences.remove(USER_PASSWORD_KEY)
            preferences.remove(USER_PROFILE_PHOTO_KEY)
        }
    }
}
