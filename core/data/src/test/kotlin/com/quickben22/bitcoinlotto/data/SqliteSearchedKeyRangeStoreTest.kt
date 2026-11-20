package com.quickben22.bitcoinlotto.data

import android.database.sqlite.SQLiteDatabase
import android.util.Base64
import com.google.common.truth.Truth.assertThat
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SqliteSearchedKeyRangeStoreTest {

    @Test
    fun `recorded range round trips`() = runTest {
        val store = createStore()

        store.record(key(10), key(20))

        assertThat(store.ranges().map { it.fromHex to it.toHex })
            .containsExactly(key(10) to key(20))
    }

    @Test
    fun `uppercase keys are normalized`() = runTest {
        val store = createStore()

        store.record(key(10).uppercase(), key(20).uppercase())

        assertThat(store.endOfContainingRange(key(15))).isEqualTo(key(20))
    }

    @Test
    fun `invalid and reversed ranges are ignored`() = runTest {
        val store = createStore()

        store.record("not-a-key", key(20))
        store.record(key(20), key(10))
        store.record(key(10), key(10))

        assertThat(store.ranges()).isEmpty()
    }

    @Test
    fun `adjacent ranges merge into one`() = runTest {
        val store = createStore()

        store.record(key(10), key(20))
        store.record(key(20), key(30))

        assertThat(store.ranges().map { it.fromHex to it.toHex })
            .containsExactly(key(10) to key(30))
    }

    @Test
    fun `overlapping ranges merge into one`() = runTest {
        val store = createStore()

        store.record(key(10), key(30))
        store.record(key(20), key(40))

        assertThat(store.ranges().map { it.fromHex to it.toHex })
            .containsExactly(key(10) to key(40))
    }

    @Test
    fun `disjoint ranges stay separate`() = runTest {
        val store = createStore()

        store.record(key(30), key(40))
        store.record(key(10), key(20))

        assertThat(store.ranges().map { it.fromHex to it.toHex })
            .containsExactly(
                key(10) to key(20),
                key(30) to key(40),
            )
    }

    @Test
    fun `containing range uses a half-open interval`() = runTest {
        val store = createStore()
        store.record(key(10), key(20))

        assertThat(store.endOfContainingRange(key(10))).isEqualTo(key(20))
        assertThat(store.endOfContainingRange(key(19))).isEqualTo(key(20))
        assertThat(store.endOfContainingRange(key(20))).isNull()
        assertThat(store.endOfContainingRange(key(9))).isNull()
        assertThat(store.endOfContainingRange("bad")).isNull()
    }

    @Test
    fun `containing range returns the furthest end`() = runTest {
        val store = createStore()
        store.record(key(10), key(20))
        store.record(key(15), key(30))

        // Overlap merges, so the containing range now ends at 30.
        assertThat(store.endOfContainingRange(key(12))).isEqualTo(key(30))
    }

    @Test
    fun `keys are encrypted at rest`() = runTest {
        val cipher = createCipher()
        val database = SQLiteDatabase.create(null)
        val store = SqliteSearchedKeyRangeStore(database, cipher, UnconfinedTestDispatcher())

        store.record(key(10), key(20))

        val (from, to) = database.rawQuery(
            "SELECT \"from\", \"to\" FROM searched_keys",
            null,
        ).use { cursor ->
            check(cursor.moveToFirst())
            cursor.getString(0) to cursor.getString(1)
        }

        assertThat(from).isNotEqualTo(key(10))
        assertThat(to).isNotEqualTo(key(20))
        assertThat(decrypt(cipher, from)).isEqualTo(key(10))
        assertThat(decrypt(cipher, to)).isEqualTo(key(20))
    }

    @Test
    fun `ranges survive reopening the database`() = runTest {
        val cipher = createCipher()
        val file = File(
            RuntimeEnvironment.getApplication().cacheDir,
            "ranges-${UUID.randomUUID()}.sqlite",
        )
        val first = SqliteSearchedKeyRangeStore(
            SQLiteDatabase.openOrCreateDatabase(file, null),
            cipher,
            UnconfinedTestDispatcher(),
        )
        first.record(key(10), key(20))
        first.record(key(20), key(30))

        val reopened = SqliteSearchedKeyRangeStore(
            SQLiteDatabase.openOrCreateDatabase(file, null),
            cipher,
            UnconfinedTestDispatcher(),
        )

        assertThat(reopened.ranges().map { it.fromHex to it.toHex })
            .containsExactly(key(10) to key(30))
    }

    @Test
    fun `entries that cannot be decrypted are ignored`() = runTest {
        val database = SQLiteDatabase.create(null)
        val store = SqliteSearchedKeyRangeStore(database, createCipher(), UnconfinedTestDispatcher())
        store.record(key(10), key(20))
        database.execSQL(
            "INSERT INTO searched_keys (\"from\", \"to\") VALUES ('garbage', 'garbage')",
        )

        assertThat(store.ranges().map { it.fromHex to it.toHex })
            .containsExactly(key(10) to key(20))
    }

    private fun createStore(): SqliteSearchedKeyRangeStore =
        SqliteSearchedKeyRangeStore(
            SQLiteDatabase.create(null),
            createCipher(),
            UnconfinedTestDispatcher(),
        )

    private fun createCipher(): CheckpointCipher {
        AeadConfig.register()
        val keysetHandle = KeysetHandle.generateNew(
            KeyTemplates.get("AES256_GCM").toParameters(),
            RegistryConfiguration.get(),
        )
        return AeadCheckpointCipher(
            keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead::class.java),
        )
    }

    private fun decrypt(cipher: CheckpointCipher, value: String): String =
        String(
            cipher.decrypt(Base64.decode(value, Base64.NO_WRAP)),
            StandardCharsets.UTF_8,
        )

    private fun key(value: Long): String = value.toString(16).padStart(64, '0')
}
