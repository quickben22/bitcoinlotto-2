package com.quickben22.bitcoinlotto.data

import android.content.Context
import android.util.Base64
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

private val Context.searchCheckpointDataStore by preferencesDataStore(name = "search_checkpoint")

internal val CHECKPOINT_CIPHERTEXT_KEY = stringPreferencesKey("checkpoint_ciphertext")
internal val HIT_CIPHERTEXT_KEY = stringPreferencesKey("hit_ciphertext")

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
        .map { preferences ->
            val encoded = preferences[CHECKPOINT_CIPHERTEXT_KEY] ?: return@map null
            val plaintext = cipher.decrypt(Base64.decode(encoded, Base64.NO_WRAP))
            checkNotNull(SearchCheckpointCodec.decode(plaintext)) { "Invalid saved search checkpoint" }
        }

    val hit: Flow<SavedSearchHit?> = dataStore.data.map { preferences ->
        val encoded = preferences[HIT_CIPHERTEXT_KEY] ?: return@map null
        try {
            val plaintext = cipher.decrypt(Base64.decode(encoded, Base64.NO_WRAP))
            DataInputStream(ByteArrayInputStream(plaintext)).use { input ->
                require(input.readUnsignedByte() == 1)
                val hit = SavedSearchHit(
                    privateKeyHex = input.readUTF(),
                    address = input.readUTF(),
                    compressed = input.readBoolean(),
                    keysChecked = input.readLong(),
                    elapsedMillis = input.readLong(),
                )
                require(input.available() == 0)
                hit
            }
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to read saved search hit", exception)
            throw exception
        }
    }

    suspend fun saveHit(hit: SavedSearchHit) {
        val plaintext = ByteArrayOutputStream().use { bytes ->
            DataOutputStream(bytes).use { output ->
                output.writeByte(1)
                output.writeUTF(hit.privateKeyHex)
                output.writeUTF(hit.address)
                output.writeBoolean(hit.compressed)
                output.writeLong(hit.keysChecked)
                output.writeLong(hit.elapsedMillis)
            }
            bytes.toByteArray()
        }
        val encoded = Base64.encodeToString(cipher.encrypt(plaintext), Base64.NO_WRAP)
        dataStore.edit { preferences ->
            preferences[HIT_CIPHERTEXT_KEY] = encoded
            preferences.remove(CHECKPOINT_CIPHERTEXT_KEY)
        }
    }

    suspend fun clearHit() {
        dataStore.edit { preferences -> preferences.remove(HIT_CIPHERTEXT_KEY) }
    }

    suspend fun save(checkpoint: SearchCheckpoint) {
        val ciphertext = cipher.encrypt(SearchCheckpointCodec.encode(checkpoint))
        val encoded = Base64.encodeToString(ciphertext, Base64.NO_WRAP)
        dataStore.edit { preferences ->
            preferences[CHECKPOINT_CIPHERTEXT_KEY] = encoded
            preferences.remove(LEGACY_KEYS_CHECKED)
            preferences.remove(LEGACY_NEXT_KEY)
            preferences.remove(LEGACY_ELAPSED_MILLIS)
            preferences.remove(LEGACY_START_KEY)
            preferences.remove(LEGACY_LIMIT)
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences -> preferences.remove(CHECKPOINT_CIPHERTEXT_KEY) }
    }

    private suspend fun migrateLegacyCheckpoint() {
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
