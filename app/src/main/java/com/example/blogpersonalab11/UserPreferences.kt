package com.example.blogpersonalab11

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión para crear DataStore
val Context.userPreferencesDataStore by preferencesDataStore("user_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        val NAME_KEY = stringPreferencesKey("user_name")
        val SURNAME_KEY = stringPreferencesKey("user_surname")
        val EMAIL_KEY = stringPreferencesKey("user_email")
        val BIRTH_DATE_KEY = stringPreferencesKey("user_birth_date")
    }

    // Función para guardar datos de usuario
    suspend fun saveUserPreferences(name: String, surname: String, email: String, birthDate: String) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[NAME_KEY] = name
            preferences[SURNAME_KEY] = surname
            preferences[EMAIL_KEY] = email
            preferences[BIRTH_DATE_KEY] = birthDate
        }
    }

    // Función para leer datos de usuario
    val userPreferencesFlow: Flow<UserProfile> = context.userPreferencesDataStore.data
        .map { preferences ->
            UserProfile(
                name = preferences[NAME_KEY] ?: "",
                surname = preferences[SURNAME_KEY] ?: "",
                email = preferences[EMAIL_KEY] ?: "",
                birthDate = preferences[BIRTH_DATE_KEY] ?: ""
            )
        }
}