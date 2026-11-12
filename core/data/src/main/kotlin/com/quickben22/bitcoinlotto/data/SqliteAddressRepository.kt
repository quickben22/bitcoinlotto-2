package com.quickben22.bitcoinlotto.data

import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import com.quickben22.bitcoinlotto.crypto.BitcoinAddress
import com.quickben22.bitcoinlotto.crypto.hash160
import java.io.File

class SqliteAddressRepository private constructor(
    private val database: SQLiteDatabase,
) : AddressRepository {
    private class HashKey(private val bytes: ByteArray) {
        override fun equals(other: Any?): Boolean = other is HashKey && bytes.contentEquals(other.bytes)
        override fun hashCode(): Int = bytes.contentHashCode()
    }

    private val addressHashes: Set<HashKey> by lazy {
        val hashes = HashSet<HashKey>()
        database.rawQuery("SELECT _id FROM bitcoin", null).use { cursor ->
            while (cursor.moveToNext()) {
                val address = cursor.getString(0)
                val hash = requireNotNull(BitcoinAddress.decodeP2pkh(address)) {
                    "Address database contains an invalid P2PKH address"
                }
                hashes.add(HashKey(hash))
            }
        }
        hashes
    }

    override fun containsPublicKey(publicKey: ByteArray): Boolean =
        addressHashes.contains(HashKey(hash160(publicKey)))

    override fun contains(address: String): Boolean {
        database.rawQuery(
            "SELECT 1 FROM bitcoin WHERE _id = ? LIMIT 1",
            arrayOf(address),
        ).use { cursor ->
            return cursor.moveToFirst()
        }
    }

    override fun count(): Int =
        DatabaseUtils.queryNumEntries(database, TABLE_NAME).toInt()

    override fun close() {
        database.close()
    }

    companion object {
        private const val ASSET_NAME = "blockchain.sqlite"
        private const val DATABASE_NAME = "blockchain.sqlite"
        private const val TABLE_NAME = "bitcoin"
        private const val PREFS_NAME = "address_database"
        private const val KEY_VERSION = "version"
        private const val DATABASE_VERSION = 1
        private val installLock = Any()

        fun open(context: Context): SqliteAddressRepository {
            val appContext = context.applicationContext
            val databaseFile = synchronized(installLock) {
                installIfNeeded(appContext)
            }
            val database = SQLiteDatabase.openDatabase(
                databaseFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY,
            )
            return SqliteAddressRepository(database)
        }

        private fun installIfNeeded(context: Context): File {
            val databaseFile = context.getDatabasePath(DATABASE_NAME)
            val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val installedVersion = preferences.getInt(KEY_VERSION, 0)

            if (databaseFile.exists() && installedVersion == DATABASE_VERSION) {
                return databaseFile
            }

            val parent = databaseFile.parentFile
                ?: error("Database directory is unavailable")
            parent.mkdirs()

            val temporaryFile = File(parent, "$DATABASE_NAME.tmp")
            try {
                context.assets.open(ASSET_NAME).use { input ->
                    temporaryFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                if (databaseFile.exists() && !databaseFile.delete()) {
                    error("Could not replace the existing address database")
                }
                check(temporaryFile.renameTo(databaseFile)) {
                    "Could not install the address database"
                }
                preferences.edit().putInt(KEY_VERSION, DATABASE_VERSION).commit()
            } finally {
                if (temporaryFile.exists()) {
                    temporaryFile.delete()
                }
            }

            return databaseFile
        }
    }
}
