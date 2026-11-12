package com.quickben22.bitcoinlotto.crypto

import java.math.BigInteger

object Base58 {
    private const val ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
    private val INDEXES = IntArray(128) { -1 }.also { table ->
        ALPHABET.forEachIndexed { index, c -> table[c.code] = index }
    }
    private val FIFTY_EIGHT = BigInteger.valueOf(58)

    fun encode(input: ByteArray): String {
        if (input.isEmpty()) return ""
        var leadingZeros = 0
        while (leadingZeros < input.size && input[leadingZeros].toInt() == 0) {
            leadingZeros++
        }
        var number = BigInteger(1, input)
        val builder = StringBuilder()
        while (number > BigInteger.ZERO) {
            val (quotient, remainder) = number.divideAndRemainder(FIFTY_EIGHT)
            builder.append(ALPHABET[remainder.toInt()])
            number = quotient
        }
        repeat(leadingZeros) { builder.append('1') }
        return builder.reverse().toString()
    }

    fun decode(input: String): ByteArray {
        if (input.isEmpty()) return ByteArray(0)
        var leadingZeros = 0
        while (leadingZeros < input.length && input[leadingZeros] == '1') {
            leadingZeros++
        }
        var number = BigInteger.ZERO
        for (c in input) {
            val digit = if (c.code < INDEXES.size) INDEXES[c.code] else -1
            require(digit >= 0) { "Invalid Base58 character: $c" }
            number = number.multiply(FIFTY_EIGHT).add(BigInteger.valueOf(digit.toLong()))
        }
        if (number == BigInteger.ZERO) return ByteArray(leadingZeros)
        val bytes = number.toByteArray()
        val stripped = if (bytes.size > 1 && bytes[0].toInt() == 0) {
            bytes.copyOfRange(1, bytes.size)
        } else {
            bytes
        }
        return ByteArray(leadingZeros) + stripped
    }

    fun encodeChecked(payload: ByteArray): String =
        encode(payload + doubleSha256(payload).copyOf(4))

    fun decodeChecked(input: String): ByteArray {
        val decoded = decode(input)
        require(decoded.size >= 4) { "Base58Check value is too short" }
        val payload = decoded.copyOfRange(0, decoded.size - 4)
        val checksum = decoded.copyOfRange(decoded.size - 4, decoded.size)
        val expected = doubleSha256(payload).copyOf(4)
        require(checksum.contentEquals(expected)) { "Base58Check checksum mismatch" }
        return payload
    }
}
