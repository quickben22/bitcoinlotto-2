package com.quickben22.bitcoinlotto.data

import java.io.Closeable

interface AddressRepository : Closeable {
    fun contains(address: String): Boolean

    fun count(): Int
}
