package com.quickben22.bitcoinlotto.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.quickben22.bitcoinlotto.FakeAddressRepository
import com.quickben22.bitcoinlotto.data.FixtureAddresses
import org.junit.Test

class CheckPrivateKeyUseCaseTest {

    private data class Fixture(
        val privateKeyHex: String,
        val compressedAddress: String,
        val uncompressedAddress: String,
    )

    private val fixtures = listOf(
        Fixture(
            privateKeyHex = "d9eb4e2909d646dd931bb306a2b492475cff04097a2456416ec9b7dba5ac98b8",
            compressedAddress = FixtureAddresses.KEY_1_COMPRESSED,
            uncompressedAddress = FixtureAddresses.KEY_1_UNCOMPRESSED,
        ),
        Fixture(
            privateKeyHex = "b7777a6bd712f26654e64bc6b609f311c252360f045f98b661704eb4c57b3772",
            compressedAddress = FixtureAddresses.KEY_2_COMPRESSED,
            uncompressedAddress = FixtureAddresses.KEY_2_UNCOMPRESSED,
        ),
        Fixture(
            privateKeyHex = "420e1776b2289ca21ce009195ebf67c514ce6259fc4be6036b6940cc545a7762",
            compressedAddress = FixtureAddresses.KEY_3_COMPRESSED,
            uncompressedAddress = FixtureAddresses.KEY_3_UNCOMPRESSED,
        ),
    )

    @Test
    fun `every fixture key is guessed with its compressed address`() {
        val useCase = CheckPrivateKeyUseCase(FakeAddressRepository(FixtureAddresses.all))

        fixtures.forEach { fixture ->
            val result = useCase(fixture.privateKeyHex)
            assertThat(result).isInstanceOf(KeyCheckResult.Guessed::class.java)
            result as KeyCheckResult.Guessed
            assertThat(result.key.privateKeyHex).isEqualTo(fixture.privateKeyHex)
            assertThat(result.address).isEqualTo(fixture.compressedAddress)
            assertThat(result.compressed).isTrue()
        }
    }

    @Test
    fun `uncompressed-only match is detected`() {
        val useCase = CheckPrivateKeyUseCase(
            FakeAddressRepository(setOf(FixtureAddresses.KEY_2_UNCOMPRESSED)),
        )

        val result = useCase(fixtures[1].privateKeyHex)

        assertThat(result).isInstanceOf(KeyCheckResult.Guessed::class.java)
        result as KeyCheckResult.Guessed
        assertThat(result.address).isEqualTo(FixtureAddresses.KEY_2_UNCOMPRESSED)
        assertThat(result.compressed).isFalse()
    }

    @Test
    fun `both variants produce exactly one result and stop after the first lookup`() {
        val repository = FakeAddressRepository(
            setOf(FixtureAddresses.KEY_3_COMPRESSED, FixtureAddresses.KEY_3_UNCOMPRESSED),
        )
        val useCase = CheckPrivateKeyUseCase(repository)

        val result = useCase(fixtures[2].privateKeyHex)

        assertThat(result).isInstanceOf(KeyCheckResult.Guessed::class.java)
        result as KeyCheckResult.Guessed
        assertThat(result.address).isEqualTo(FixtureAddresses.KEY_3_COMPRESSED)
        assertThat(repository.containsCalls).isEqualTo(1)
    }

    @Test
    fun `unknown valid key is not guessed`() {
        val useCase = CheckPrivateKeyUseCase(FakeAddressRepository(FixtureAddresses.all))

        val result = useCase(
            "0000000000000000000000000000000000000000000000000000000000000001",
        )

        assertThat(result).isInstanceOf(KeyCheckResult.NotGuessed::class.java)
    }

    @Test
    fun `input normalization accepts whitespace and uppercase`() {
        val useCase = CheckPrivateKeyUseCase(FakeAddressRepository(FixtureAddresses.all))
        val formatted = fixtures[0].privateKeyHex.uppercase().chunked(8).joinToString(" \n ")

        val result = useCase(formatted)

        assertThat(result).isInstanceOf(KeyCheckResult.Guessed::class.java)
    }

    @Test
    fun `invalid inputs are rejected without a repository lookup`() {
        val repository = FakeAddressRepository(FixtureAddresses.all)
        val useCase = CheckPrivateKeyUseCase(repository)
        val curveOrder =
            "fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141"
        val invalidInputs = listOf(
            "",
            "0".repeat(63),
            "0".repeat(65),
            "z".repeat(64),
            "0".repeat(64),
            curveOrder,
        )

        invalidInputs.forEach { input ->
            val result = useCase(input)
            assertThat(result).isInstanceOf(KeyCheckResult.Invalid::class.java)
        }

        assertThat(repository.containsCalls).isEqualTo(0)
    }
}
