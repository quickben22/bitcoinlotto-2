package com.quickben22.bitcoinlotto.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
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
class SearchAccessStoreTest {
    @Test
    fun `searched keys and rewarded unlock stages persist`() = runTest {
        val store = SearchCheckpointStore(createDataStore(), createCipher())
        val checkpoint = SearchCheckpoint(
            keysChecked = 0L,
            nextKeyHex = "00".repeat(32),
            elapsedMillis = 0L,
            startKeyHex = "00".repeat(32),
            limit = null,
        )

        store.save(checkpoint, searchedKeysDelta = 6_000_000L)
        assertThat(store.searchAccess.first()).isEqualTo(SearchAccess(totalKeysChecked = 6_000_000L))

        store.grantRewardedUnlock()
        assertThat(store.searchAccess.first()).isEqualTo(
            SearchAccess(totalKeysChecked = 6_000_000L, firstRewardUsed = true),
        )

        store.save(checkpoint, searchedKeysDelta = 54_000_000L)
        store.grantRewardedUnlock()
        assertThat(store.searchAccess.first()).isEqualTo(
            SearchAccess(
                totalKeysChecked = 60_000_000L,
                firstRewardUsed = true,
                permanentlyUnlocked = true,
            ),
        )
    }

    private fun TestScope.createDataStore(): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = {
                File(
                    RuntimeEnvironment.getApplication().cacheDir,
                    "search-access-${UUID.randomUUID()}.preferences_pb",
                )
            },
        )

    private fun createCipher(): CheckpointCipher {
        AeadConfig.register()
        val keyset = KeysetHandle.generateNew(KeyTemplates.get("AES256_GCM"))
        return AeadCheckpointCipher(keyset.getPrimitive(Aead::class.java))
    }
}
