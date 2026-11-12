package com.quickben22.bitcoinlotto.search

import com.quickben22.bitcoinlotto.crypto.BitcoinKey
import com.quickben22.bitcoinlotto.data.AddressRepository

data class AddressMatch(
    val address: String,
    val compressed: Boolean,
)

fun interface AddressChecker {
    fun match(key: BitcoinKey): AddressMatch?
}

class RepositoryAddressChecker(
    private val repository: AddressRepository,
) : AddressChecker {
    override fun match(key: BitcoinKey): AddressMatch? {
        val compressedAddress = key.addressCompressed
        if (repository.contains(compressedAddress)) {
            return AddressMatch(compressedAddress, compressed = true)
        }
        val uncompressedAddress = key.addressUncompressed
        if (repository.contains(uncompressedAddress)) {
            return AddressMatch(uncompressedAddress, compressed = false)
        }
        return null
    }
}
