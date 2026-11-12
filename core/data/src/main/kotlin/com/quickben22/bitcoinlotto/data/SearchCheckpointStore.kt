package com.quickben22.bitcoinlotto.data

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

private val Context.searchCheckpointDataStore by preferencesDataStore(name = "search_checkpoint")

internal val CHECKPOINT_CIPHERTEXT_KEY = stringPreferencesKey("checkpoint_ciphertext")

class SearchCheckpointStore(
    private val dataStore: DataStore<Preferences>,
    private val cipher: CheckpointCipher,
) {
    constructor(context: Context) : this(
        context.applicationContext.searchCheckpointDataStore,
        TinkCheckpointCipher(context.applicationContext),
    )

    val checkpoint: Flow<SearchCheckpoint?> = dataStore.data
        .onStart { migrateLegacyCheckpoint() }
        .catch { throwable ->
            Log.w(TAG, "Failed to read checkpoint", throwable)
            emit(emptyPreferences())
        }
        .map { preferences ->
            val encoded = preferences[CHECKPOINT_CIPHERTEXT_KEY] ?: return@map null
            val ciphertext = runCatching {
                Base64.decode(encoded, Base64.NO_WRAP)
            }.getOrNull() ?: return@map null
            val plaintext = runCatching {
                cipher.decrypt(ciphertext)
            }.getOrNull() ?: return@map null
            SearchCheckpointCodec.decode(plaintext)
        }

    suspend fun save(checkpoint: SearchCheckpoint) {
        runCatching {
            val ciphertext = cipher.encrypt(SearchCheckpointCodec.encode(checkpoint))
            val encoded = Base64.encodeToString(ciphertext, Base64.NO_WRAP)
            dataStore.edit { preferences ->
                preferences.clear()
                preferences[CHECKPOINT_CIPHERTEXT_KEY] = encoded
            }
        }.onFailure { throwable ->
            Log.w(TAG, "Failed to save checkpoint", throwable)
        }
    }

    suspend fun clear() {
        runCatching {
            dataStore.edit { preferences -> preferences.clear() }
        }.onFailure { throwable ->
            Log.w(TAG, "Failed to clear checkpoint", throwable)
        }
    }

    private suspend fun migrateLegacyCheckpoint() {
        runCatching {
            val preferences = dataStore.data.first()
            if (preferences[CHECKPOINT_CIPHERTEXT_KEY] != null) return
            val nextKeyHex = preferences[LEGACY_NEXT_KEY] ?: return
            val startKeyHex = preferences[LEGACY_START_KEY] ?: return
            save(
                SearchCheckpoint(
                    keysChecked = preferences[LEGACY_KEYS_CHECKED] ?: 0L,
                    nextKeyHex = nextKeyHex,
                    elapsedMillis = preferences[LEGACY_ELAPSED_MILLIS] ?: 0L,
                    startKeyHex = startKeyHex,
                    limit = preferences[LEGACY_LIMIT]?.takeIf { it >= 0L },
                ),
            )
            if (dataStore.data.first()[CHECKPOINT_CIPHERTEXT_KEY] == null) {
                dataStore.edit { legacy ->
                    legacy.remove(LEGACY_KEYS_CHECKED)
                    legacy.remove(LEGACY_NEXT_KEY)
                    legacy.remove(LEGACY_ELAPSED_MILLIS)
                    legacy.remove(LEGACY_START_KEY)
                    legacy.remove(LEGACY_LIMIT)
                }
            }
        }.onFailure { throwable ->
            Log.w(TAG, "Failed to migrate legacy checkpoint", throwable)
        }
    }

    private companion object {
        const val TAG = "SearchCheckpointStore"
        val LEGACY_KEYS_CHECKED = longPreferencesKey("keys_checked")
        val LEGACY_NEXT_KEY = stringPreferencesKey("next_key")
        val LEGACY_ELAPSED_MILLIS = longPreferencesKey("elapsed_millis")
        val LEGACY_START_KEY = stringPreferencesKey("start_key")
        val LEGACY_LIMIT = longPreferencesKey("limit")
    }
}
