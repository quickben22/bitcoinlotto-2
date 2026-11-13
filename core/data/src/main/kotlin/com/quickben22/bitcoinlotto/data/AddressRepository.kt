package com.quickben22.bitcoinlotto.data

import java.io.Closeable
import com.quickben22.bitcoinlotto.crypto.BitcoinAddress

interface AddressRepository : Closeable {
    fun contains(address: String): Boolean

    fun containsPublicKey(publicKey: ByteArray): Boolean =
        contains(BitcoinAddress.fromPublicKey(publicKey))

    fun count(): Int
}
