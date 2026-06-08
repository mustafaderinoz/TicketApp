package com.mustafaderinoz.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

// Single Source garantilemek.
private val Context.authDataStore by preferencesDataStore(name="auth_prefs")

class TokenStore(private val context: Context)
{
    private object Keys {
        val ACCESS = stringPreferencesKey("access_token")
        val REFRESH = stringPreferencesKey("refresh_token")
        val ROLE = stringPreferencesKey("user_role")
    }

    // UI tarafından collect edilmek için
    val accessToken: Flow<String?> = context.authDataStore.data.map { it[Keys.ACCESS] }
    val refreshToken: Flow<String?> = context.authDataStore.data.map { it[Keys.REFRESH] }
    val userRole: Flow<String?> = context.authDataStore.data.map { it[Keys.ROLE] }


    suspend fun save(access:String, refresh: String,role:String) {
        context.authDataStore.edit {
                prefs ->
            prefs[Keys.ACCESS] = access
            prefs[Keys.REFRESH] = refresh
            prefs[Keys.ROLE] = role
        }
    }

    suspend fun clear() {
        context.authDataStore.edit { prefs ->
            prefs.remove(Keys.ACCESS)
            prefs.remove(Keys.REFRESH)
            prefs.remove(Keys.ROLE)
        }
    }

    fun accessTokenBlocking(): String? = runBlocking { accessToken.first() }
    fun refreshTokenBlocking(): String? = runBlocking { refreshToken.first() }
    fun userRoleBlocking(): String? = runBlocking { userRole.first() }
    fun saveBlocking(access: String, refresh: String, role: String) = runBlocking { save(access, refresh, role) }
    fun clearBlocking() = runBlocking { clear() }
}