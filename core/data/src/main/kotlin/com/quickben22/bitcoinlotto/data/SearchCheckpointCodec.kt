package com.quickben22.bitcoinlotto.data

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

internal object SearchCheckpointCodec {
    private const val VERSION = 1

    fun encode(checkpoint: SearchCheckpoint): ByteArray {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { output ->
            output.writeByte(VERSION)
            output.writeLong(checkpoint.keysChecked)
            output.writeUTF(checkpoint.nextKeyHex)
            output.writeLong(checkpoint.elapsedMillis)
            output.writeUTF(checkpoint.startKeyHex)
            output.writeBoolean(checkpoint.limit != null)
            checkpoint.limit?.let(output::writeLong)
        }
        return bytes.toByteArray()
    }

    fun decode(bytes: ByteArray): SearchCheckpoint? = runCatching {
        DataInputStream(ByteArrayInputStream(bytes)).use { input ->
            require(input.readUnsignedByte() == VERSION)
            val keysChecked = input.readLong()
            val nextKeyHex = input.readUTF()
            val elapsedMillis = input.readLong()
            val startKeyHex = input.readUTF()
            val hasLimit = input.readBoolean()
            val limit = if (hasLimit) input.readLong() else null
            require(input.available() == 0)
            SearchCheckpoint(
                keysChecked = keysChecked,
                nextKeyHex = nextKeyHex,
                elapsedMillis = elapsedMillis,
                startKeyHex = startKeyHex,
                limit = limit,
            )
        }
    }.getOrNull()
}
