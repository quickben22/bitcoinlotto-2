package com.quickben22.bitcoinlotto

import com.quickben22.bitcoinlotto.data.AddressRepository

class FakeAddressRepository(addresses: Set<String>) : AddressRepository {
    private val addresses = addresses.toSet()

    var containsCalls: Int = 0
        private set

    override fun contains(address: String): Boolean {
        containsCalls++
        return address in addresses
    }

    override fun count(): Int = addresses.size

    override fun close() = Unit
}
