package com.quickben22.bitcoinlotto.data

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.common.truth.Truth.assertThat
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import java.io.File
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SearchCheckpointStoreTest {

    @Test
    fun `save and load round trip`() = runTest {
        val store = createStore()
        val checkpoint = checkpoint()

        store.save(checkpoint)

        assertThat(store.checkpoint.first()).isEqualTo(checkpoint)
    }

    @Test
    fun `null limit round trips`() = runTest {
        val store = createStore()
        val checkpoint = checkpoint(limit = null)

        store.save(checkpoint)

        assertThat(store.checkpoint.first()?.limit).isNull()
    }

    @Test
    fun `clear removes the checkpoint`() = runTest {
        val store = createStore()
        store.save(checkpoint())

        store.clear()

        assertThat(store.checkpoint.first()).isNull()
    }

    @Test
    fun `legacy plaintext checkpoint is migrated and removed`() = runTest {
        val dataStore = createDataStore()
        val legacy = checkpoint()
        dataStore.edit { preferences ->
            preferences[longPreferencesKey("keys_checked")] = legacy.keysChecked
            preferences[stringPreferencesKey("next_key")] = legacy.nextKeyHex
            preferences[longPreferencesKey("elapsed_millis")] = legacy.elapsedMillis
            preferences[stringPreferencesKey("start_key")] = legacy.startKeyHex
            preferences[longPreferencesKey("limit")] = legacy.limit ?: -1L
        }
        val store = SearchCheckpointStore(dataStore, createCipher())

        assertThat(store.checkpoint.first()).isEqualTo(legacy)

        val preferences = dataStore.data.first()
        assertThat(preferences[CHECKPOINT_CIPHERTEXT_KEY]).isNotNull()
        assertThat(preferences[stringPreferencesKey("next_key")]).isNull()
        assertThat(preferences[stringPreferencesKey("start_key")]).isNull()
    }

    @Test
    fun `saved preferences do not contain plaintext keys`() = runTest {
        val dataStore = createDataStore()
        val store = SearchCheckpointStore(dataStore, createCipher())
        val checkpoint = checkpoint()

        store.save(checkpoint)

        val values = dataStore.data.first().asMap().values.filterIsInstance<String>()
        assertThat(values).doesNotContain(checkpoint.nextKeyHex)
        assertThat(values).doesNotContain(checkpoint.startKeyHex)
        assertThat(values).isNotEmpty()
    }

    @Test
    fun `corrupted ciphertext fails safely`() = runTest {
        val dataStore = createDataStore()
        val store = SearchCheckpointStore(dataStore, createCipher())
        val corrupted = Base64.encodeToString(byteArrayOf(1, 2, 3, 4), Base64.NO_WRAP)

        dataStore.edit { preferences ->
            preferences[CHECKPOINT_CIPHERTEXT_KEY] = corrupted
        }

        assertThat(store.checkpoint.first()).isNull()
    }

    @Test
    fun `ciphertext from another keyset fails safely`() = runTest {
        val dataStore = createDataStore()
        SearchCheckpointStore(dataStore, createCipher()).save(checkpoint())
        val store = SearchCheckpointStore(dataStore, createCipher())

        assertThat(store.checkpoint.first()).isNull()
    }

    private fun TestScope.createStore(): SearchCheckpointStore =
        SearchCheckpointStore(createDataStore(), createCipher())

    private fun TestScope.createDataStore(): DataStore<Preferences> {
        val file = File(
            RuntimeEnvironment.getApplication().cacheDir,
            "checkpoint-${UUID.randomUUID()}.preferences_pb",
        )
        return PreferenceDataStoreFactory.create(scope = backgroundScope) { file }
    }

    private fun createCipher(): CheckpointCipher {
        AeadConfig.register()
        val keysetHandle = KeysetHandle.generateNew(KeyTemplates.get("AES256_GCM"))
        return AeadCheckpointCipher(keysetHandle.getPrimitive(Aead::class.java))
    }

    private fun checkpoint(limit: Long? = 1_000_000L) = SearchCheckpoint(
        keysChecked = 42L,
        nextKeyHex = "000000000000000000000000000000000000000000000000000000000000002b",
        elapsedMillis = 1_500L,
        startKeyHex = "0000000000000000000000000000000000000000000000000000000000000001",
        limit = limit,
    )
}
