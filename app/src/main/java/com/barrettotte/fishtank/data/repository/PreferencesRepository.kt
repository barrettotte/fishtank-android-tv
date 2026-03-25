package com.barrettotte.fishtank.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Extension property to create a single DataStore instance per context. */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fishtank_prefs")

/** Repository for reading and writing persistent app preferences via DataStore. */
class PreferencesRepository(private val context: Context) {

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val LIVE_STREAM_TOKEN = stringPreferencesKey("live_stream_token")
        private val DISPLAY_NAME = stringPreferencesKey("display_name")
        private val QUALITY = stringPreferencesKey("quality")
        private val SERVER = stringPreferencesKey("server")
    }

    /** Get the stored access token, or empty string if not set. */
    suspend fun getAccessToken(): String {
        return context.dataStore.data.map { prefs ->
            prefs[ACCESS_TOKEN] ?: ""
        }.first()
    }

    /** Get the stored live stream token, or empty string if not set. */
    suspend fun getLiveStreamToken(): String {
        return context.dataStore.data.map { prefs ->
            prefs[LIVE_STREAM_TOKEN] ?: ""
        }.first()
    }

    /** Get the stored display name, or empty string if not set. */
    suspend fun getDisplayName(): String {
        return context.dataStore.data.map { prefs ->
            prefs[DISPLAY_NAME] ?: ""
        }.first()
    }

    /** Get the stored quality preference, defaulting to "maxbps" (high). */
    suspend fun getQuality(): String {
        return context.dataStore.data.map { prefs ->
            prefs[QUALITY] ?: "maxbps"
        }.first()
    }

    /** Get the stored server preference, defaulting to "auto". */
    suspend fun getServer(): String {
        return context.dataStore.data.map { prefs ->
            prefs[SERVER] ?: "auto"
        }.first()
    }

    /** Save authentication tokens and display name after login. */
    suspend fun saveSession(accessToken: String, liveStreamToken: String, displayName: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = accessToken
            prefs[LIVE_STREAM_TOKEN] = liveStreamToken
            prefs[DISPLAY_NAME] = displayName
        }
    }

    /** Save quality preference. */
    suspend fun saveQuality(quality: String) {
        context.dataStore.edit { prefs ->
            prefs[QUALITY] = quality
        }
    }

    /** Save server preference. */
    suspend fun saveServer(server: String) {
        context.dataStore.edit { prefs ->
            prefs[SERVER] = server
        }
    }

    /** Clear auth data on logout. Keeps quality and server preferences. */
    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN)
            prefs.remove(LIVE_STREAM_TOKEN)
            prefs.remove(DISPLAY_NAME)
        }
    }
}
