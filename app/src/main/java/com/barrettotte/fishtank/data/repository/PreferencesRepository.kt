package com.barrettotte.fishtank.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

import com.barrettotte.fishtank.util.CredentialEncryptor
import com.barrettotte.fishtank.util.Logger

/** Extension property to create a single DataStore instance per context. */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fishtank_prefs")

/** Repository for reading and writing persistent app preferences via DataStore. */
class PreferencesRepository(private val context: Context) {

    /** In-memory cache of the access token for synchronous reads (e.g. OkHttp interceptor). */
    @Volatile
    var cachedAccessToken: String = ""
        private set

    companion object {
        private const val TAG = "Prefs"
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val LIVE_STREAM_TOKEN = stringPreferencesKey("live_stream_token")
        private val DISPLAY_NAME = stringPreferencesKey("display_name")
        private val EMAIL = stringPreferencesKey("email")
        private val PASSWORD = stringPreferencesKey("password")
        private val QUALITY = stringPreferencesKey("quality")
        private val SERVER = stringPreferencesKey("server")
    }

    /** Get the stored access token (decrypted), or empty string if not set. Also updates in-memory cache. */
    suspend fun getAccessToken(): String {
        val encrypted = context.dataStore.data.map { prefs ->
            prefs[ACCESS_TOKEN] ?: ""
        }.first()
        if (encrypted.isEmpty()) {
            cachedAccessToken = ""
            return ""
        }
        return try {
            val token = CredentialEncryptor.decrypt(encrypted)
            cachedAccessToken = token
            token
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to decrypt access token, clearing session", e)
            cachedAccessToken = ""
            clearSession()
            ""
        }
    }

    /** Get the stored live stream token (decrypted), or empty string if not set. */
    suspend fun getLiveStreamToken(): String {
        val encrypted = context.dataStore.data.map { prefs ->
            prefs[LIVE_STREAM_TOKEN] ?: ""
        }.first()
        if (encrypted.isEmpty()) {
            return ""
        }
        return try {
            CredentialEncryptor.decrypt(encrypted)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to decrypt live stream token, clearing session", e)
            clearSession()
            ""
        }
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

    /** Get the stored email (decrypted), or empty string if not set. */
    suspend fun getEmail(): String {
        val encrypted = context.dataStore.data.map { prefs ->
            prefs[EMAIL] ?: ""
        }.first()
        if (encrypted.isEmpty()) {
            return ""
        }
        return try {
            CredentialEncryptor.decrypt(encrypted)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to decrypt email, clearing stored credentials", e)
            clearCredentials()
            ""
        }
    }

    /** Get the stored password (decrypted), or empty string if not set. */
    suspend fun getPassword(): String {
        val encrypted = context.dataStore.data.map { prefs ->
            prefs[PASSWORD] ?: ""
        }.first()
        if (encrypted.isEmpty()) {
            return ""
        }
        return try {
            CredentialEncryptor.decrypt(encrypted)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to decrypt password, clearing stored credentials", e)
            clearCredentials()
            ""
        }
    }

    /** Save authentication tokens (encrypted) and display name after login. */
    suspend fun saveSession(accessToken: String, liveStreamToken: String, displayName: String) {
        cachedAccessToken = accessToken
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = CredentialEncryptor.encrypt(accessToken)
            prefs[LIVE_STREAM_TOKEN] = CredentialEncryptor.encrypt(liveStreamToken)
            prefs[DISPLAY_NAME] = displayName
        }
    }

    /** Save login credentials (encrypted) for automatic token refresh. */
    suspend fun saveCredentials(email: String, password: String) {
        context.dataStore.edit { prefs ->
            prefs[EMAIL] = CredentialEncryptor.encrypt(email)
            prefs[PASSWORD] = CredentialEncryptor.encrypt(password)
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

    /** Clear only encrypted credentials from DataStore. */
    private suspend fun clearCredentials() {
        context.dataStore.edit { prefs ->
            prefs.remove(EMAIL)
            prefs.remove(PASSWORD)
        }
    }

    /** Clear auth data on logout. Keeps quality and server preferences. */
    suspend fun clearSession() {
        cachedAccessToken = ""
        context.dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN)
            prefs.remove(LIVE_STREAM_TOKEN)
            prefs.remove(DISPLAY_NAME)
            prefs.remove(EMAIL)
            prefs.remove(PASSWORD)
        }
    }
}
