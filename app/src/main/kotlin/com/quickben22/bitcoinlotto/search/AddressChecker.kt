package com.quickben22.bitcoinlotto.search

import com.quickben22.bitcoinlotto.crypto.BitcoinKey
import com.quickben22.bitcoinlotto.crypto.BitcoinAddress
import com.quickben22.bitcoinlotto.data.AddressRepository

data class AddressMatch(
    val address: String,
    val compressed: Boolean,
)

fun interface AddressChecker {
    fun match(key: BitcoinKey): AddressMatch?

    fun matchPublicKeys(privateKey: ByteArray, compressed: ByteArray, uncompressed: ByteArray): AddressMatch? =
        match(BitcoinKey.fromPrivateKey(privateKey))
}

class RepositoryAddressChecker(
    private val repositoryProvider: () -> AddressRepository,
) : AddressChecker {
    constructor(repository: AddressRepository) : this({ repository })

    override fun match(key: BitcoinKey): AddressMatch? {
        val repository = repositoryProvider()
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

    override fun matchPublicKeys(privateKey: ByteArray, compressed: ByteArray, uncompressed: ByteArray): AddressMatch? {
        val repository = repositoryProvider()
        if (repository.containsPublicKey(compressed)) {
            return AddressMatch(BitcoinAddress.fromPublicKey(compressed), true)
        }
        if (repository.containsPublicKey(uncompressed)) {
            return AddressMatch(BitcoinAddress.fromPublicKey(uncompressed), false)
        }
        return null
    }
}
