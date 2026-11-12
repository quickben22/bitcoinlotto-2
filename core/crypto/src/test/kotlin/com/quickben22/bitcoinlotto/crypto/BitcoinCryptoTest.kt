package com.quickben22.bitcoinlotto.crypto

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class BitcoinCryptoTest {

    @Test
    fun `key one matches known bitcoin vectors`() {
        val key = BitcoinKey.fromHex(
            "0000000000000000000000000000000000000000000000000000000000000001",
        )

        assertThat(key.privateKeyHex)
            .isEqualTo("0000000000000000000000000000000000000000000000000000000000000001")
        assertThat(key.addressCompressed).isEqualTo("1BgGZ9tcN4rm9KBzDn7KprQz87SZ26SAMH")
        assertThat(key.addressUncompressed).isEqualTo("1EHNa6Q4Jz2uvNExL497mE43ikXhwF6kZm")
        assertThat(key.wifCompressed).isEqualTo("KwDiBf89QgGbjEhKnhXJuH7LrciVrZi3qYjgd9M7rFU73sVHnoWn")
        assertThat(key.wifUncompressed).isEqualTo("5HpHagT65TZzG1PH3CSu63k8DbpvD8s5ip4nEB3kEsreAnchuDf")
    }

    @Test
    fun `fixture key 1 derives both expected addresses`() {
        val key = BitcoinKey.fromHex(
            "d9eb4e2909d646dd931bb306a2b492475cff04097a2456416ec9b7dba5ac98b8",
        )

        assertThat(key.addressCompressed).isEqualTo("185iZWp5JuBnK2tUWMB39WagHuPmJvRBRW")
        assertThat(key.addressUncompressed).isEqualTo("19MoUVhGQ7kKCoWk8UuYAqqqnCYdw96ASi")
        assertThat(key.wifCompressed).isEqualTo("L4XKMVq5ohFMcZR7FZathMkBXc9fZXCzJDurFCPkNV5dmBfHK8z6")
        assertThat(key.wifUncompressed).isEqualTo("5KUG2vf5iPiKGJmRymYEyirfBZS5CuA4U1fFnMhx93HTTLkzTtZ")
    }

    @Test
    fun `fixture key 2 derives both expected addresses`() {
        val key = BitcoinKey.fromHex(
            "b7777a6bd712f26654e64bc6b609f311c252360f045f98b661704eb4c57b3772",
        )

        assertThat(key.addressCompressed).isEqualTo("1g4yqipc6uTfrdn14AfLhxdUePexjgvnq")
        assertThat(key.addressUncompressed).isEqualTo("1DotngYyJFzqdZ43DS1ZLFHB59sLYRDNVM")
        assertThat(key.wifCompressed).isEqualTo("L3NM2rN6GcVapQVPggtkeKSU2b4hZnLwXLGB8TTxgF4bX5P38Heh")
        assertThat(key.wifUncompressed).isEqualTo("5KD5zWmCci7vt4xpjXLRqnQttZe67XDDokLJFNrcGgtZk7HmVJm")
    }

    @Test
    fun `fixture key 3 derives both expected addresses`() {
        val key = BitcoinKey.fromHex(
            "420e1776b2289ca21ce009195ebf67c514ce6259fc4be6036b6940cc545a7762",
        )

        assertThat(key.addressCompressed).isEqualTo("1Q5o8PCPxKEwPPV2MzWWjSRgQYgnohnsmF")
        assertThat(key.addressUncompressed).isEqualTo("14v2rFnEYpjz4gpUTR7dMifMfpgEZ5Vg6s")
        assertThat(key.wifCompressed).isEqualTo("KyS7XFfgSj9CkBqHw6i1yWLUEsUH7bMFoMK2FjeAFX8TmfXM1Nxp")
        assertThat(key.wifUncompressed).isEqualTo("5JKNsMYRLsnMnaikiFtS9Gi7dDo4CTpBbyFxZnuprxmHVGQP2P5")
    }

    @Test
    fun `wif round trips preserve key and compression flag`() {
        val compressed = BitcoinKey.fromHex(
            "d9eb4e2909d646dd931bb306a2b492475cff04097a2456416ec9b7dba5ac98b8",
        )
        val uncompressed = BitcoinKey.fromPrivateKey(compressed.privateKey, compressed = false)

        assertThat(BitcoinKey.fromWif(compressed.wifCompressed).privateKey)
            .isEqualTo(compressed.privateKey)
        assertThat(BitcoinKey.fromWif(compressed.wifCompressed).compressed).isTrue()
        assertThat(BitcoinKey.fromWif(uncompressed.wifUncompressed).privateKey)
            .isEqualTo(uncompressed.privateKey)
        assertThat(BitcoinKey.fromWif(uncompressed.wifUncompressed).compressed).isFalse()
    }

    @Test
    fun `invalid wif checksum is rejected`() {
        val valid = BitcoinKey.fromHex(
            "d9eb4e2909d646dd931bb306a2b492475cff04097a2456416ec9b7dba5ac98b8",
        ).wifCompressed
        val corrupted = valid.dropLast(1) + if (valid.last() == 'a') 'b' else 'a'

        assertThat(BitcoinCrypto.isValidWif(corrupted)).isFalse()
        assertThrows(IllegalArgumentException::class.java) { BitcoinKey.fromWif(corrupted) }
    }

    @Test
    fun `out of range private keys are rejected`() {
        val zero = ByteArray(32)
        val order = ByteArray(32) { 0xFF.toByte() }

        assertThat(Secp256k1.isValidPrivateKey(zero)).isFalse()
        assertThat(Secp256k1.isValidPrivateKey(order)).isFalse()
        assertThrows(IllegalArgumentException::class.java) {
            BitcoinKey.fromPrivateKey(zero)
        }
    }

    @Test
    fun `p2pkh addresses validate and expose hash160`() {
        val address = "1BgGZ9tcN4rm9KBzDn7KprQz87SZ26SAMH"

        assertThat(BitcoinCrypto.isValidP2pkhAddress(address)).isTrue()
        assertThat(BitcoinCrypto.isValidP2pkhAddress("not-an-address")).isFalse()
        assertThat(BitcoinAddress.decodeP2pkh(address)).hasLength(20)
    }

    @Test
    fun `base58 known vectors`() {
        assertThat(Base58.encode("Hello World!".toByteArray())).isEqualTo("2NEpo7TZRRrLZSi2U")
        assertThat(Base58.decode("2NEpo7TZRRrLZSi2U")).isEqualTo("Hello World!".toByteArray())
        assertThat(Base58.encode(byteArrayOf(0, 0, 1))).isEqualTo("112")
        assertThat(Base58.decode("112")).isEqualTo(byteArrayOf(0, 0, 1))
    }
}
