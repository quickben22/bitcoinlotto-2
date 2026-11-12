package com.quickben22.bitcoinlotto.crypto

import org.bouncycastle.crypto.digests.RIPEMD160Digest
import org.bouncycastle.crypto.digests.SHA256Digest

fun sha256(data: ByteArray): ByteArray {
    val digest = SHA256Digest()
    digest.update(data, 0, data.size)
    val out = ByteArray(digest.digestSize)
    digest.doFinal(out, 0)
    return out
}

fun ripemd160(data: ByteArray): ByteArray {
    val digest = RIPEMD160Digest()
    digest.update(data, 0, data.size)
    val out = ByteArray(digest.digestSize)
    digest.doFinal(out, 0)
    return out
}

fun hash160(data: ByteArray): ByteArray = ripemd160(sha256(data))

fun doubleSha256(data: ByteArray): ByteArray = sha256(sha256(data))
