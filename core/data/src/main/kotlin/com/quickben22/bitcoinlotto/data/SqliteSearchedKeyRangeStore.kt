package com.quickben22.bitcoinlotto.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Base64
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * SQLite-backed searched-key range store.
 *
 * The key bounds are private material, so they are encrypted with [cipher] before being
 * written to the `from` / `to` columns.
 */
class SqliteSearchedKeyRangeStore internal constructor(
    private val database: SQLiteDatabase,
    private val cipher: CheckpointCipher,
    private val ioDispatcher: CoroutineDispatcher,
) : SearchedKeyRangeStore {

    constructor(
        context: Context,
        cipher: CheckpointCipher,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) : this(
        SearchedKeysOpenHelper(context.applicationContext).writableDatabase,
        cipher,
        ioDispatcher,
    )

    init {
        database.execSQL(CREATE_TABLE)
    }

    override suspend fun record(fromHex: String, toHex: String) = withContext(ioDispatcher) {
        val from = normalize(fromHex)
        val to = normalize(toHex)
        if (from == null || to == null || from >= to) return@withContext

        val existing = readRanges().map { it.fromHex to it.toHex }
        val merged = merge(existing + (from to to))
        if (merged == existing) return@withContext

        database.beginTransaction()
        try {
            database.delete(TABLE_NAME, null, null)
            merged.forEach { (rangeFrom, rangeTo) ->
                database.execSQL(
                    "INSERT INTO $TABLE_NAME (\"$COLUMN_FROM\", \"$COLUMN_TO\") VALUES (?, ?)",
                    arrayOf(encrypt(rangeFrom), encrypt(rangeTo)),
                )
            }
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }

    override suspend fun endOfContainingRange(keyHex: String): String? = withContext(ioDispatcher) {
        val key = normalize(keyHex) ?: return@withContext null
        readRanges()
            .asSequence()
            .filter { key >= it.fromHex && key < it.toHex }
            .maxOfOrNull { it.toHex }
    }

    override suspend fun ranges(): List<SearchedKeyRange> = withContext(ioDispatcher) {
        readRanges()
    }

    private fun readRanges(): List<SearchedKeyRange> {
        val ranges = mutableListOf<SearchedKeyRange>()
        database.rawQuery(
            "SELECT $COLUMN_ID, \"$COLUMN_FROM\", \"$COLUMN_TO\" FROM $TABLE_NAME ORDER BY $COLUMN_ID",
            null,
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val from = decrypt(cursor.getString(1)) ?: continue
                val to = decrypt(cursor.getString(2)) ?: continue
                if (from < to) {
                    ranges += SearchedKeyRange(cursor.getLong(0), from, to)
                }
            }
        }
        return ranges
    }

    private fun merge(ranges: List<Pair<String, String>>): List<Pair<String, String>> {
        val sorted = ranges.filter { it.first < it.second }.sortedBy { it.first }
        val merged = mutableListOf<Pair<String, String>>()
        for (range in sorted) {
            val last = merged.lastOrNull()
            if (last != null && range.first <= last.second) {
                merged[merged.lastIndex] = last.first to maxOf(last.second, range.second)
            } else {
                merged += range
            }
        }
        return merged
    }

    private fun normalize(value: String): String? {
        val normalized = value.trim().lowercase()
        return normalized.takeIf { HEX_PATTERN.matches(it) }
    }

    private fun encrypt(value: String): String = Base64.encodeToString(
        cipher.encrypt(value.toByteArray(StandardCharsets.UTF_8)),
        Base64.NO_WRAP,
    )

    private fun decrypt(value: String): String? = try {
        String(
            cipher.decrypt(Base64.decode(value, Base64.NO_WRAP)),
            StandardCharsets.UTF_8,
        ).takeIf { HEX_PATTERN.matches(it) }
    } catch (_: Exception) {
        null
    }

    private class SearchedKeysOpenHelper(context: Context) : SQLiteOpenHelper(
        context,
        DATABASE_NAME,
        null,
        DATABASE_VERSION,
    ) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(CREATE_TABLE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    }

    private companion object {
        const val DATABASE_NAME = "searched_keys.sqlite"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "searched_keys"
        const val COLUMN_ID = "id"
        const val COLUMN_FROM = "from"
        const val COLUMN_TO = "to"
        val HEX_PATTERN = Regex("[0-9a-f]{64}")

        val CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                "$COLUMN_FROM" TEXT NOT NULL,
                "$COLUMN_TO" TEXT NOT NULL
            )
        """.trimIndent()
    }
}
