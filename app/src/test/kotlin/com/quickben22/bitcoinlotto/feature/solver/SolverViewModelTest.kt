package com.quickben22.bitcoinlotto.feature.solver

import com.google.common.truth.Truth.assertThat
import com.quickben22.bitcoinlotto.FakeAddressRepository
import com.quickben22.bitcoinlotto.crypto.Hex
import com.quickben22.bitcoinlotto.data.AddressRepository
import com.quickben22.bitcoinlotto.data.FixtureAddresses
import com.quickben22.bitcoinlotto.domain.usecase.CheckPrivateKeyUseCase
import com.quickben22.bitcoinlotto.domain.usecase.PrivateKeyGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SolverViewModelTest {

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fixture key is guessed with compressed address and derived values`() = runTest {
        val viewModel = createViewModel(
            scheduler = testScheduler,
            repository = FakeAddressRepository(setOf(FixtureAddresses.KEY_1_COMPRESSED)),
        )

        viewModel.onInputChange(KEY_1)
        viewModel.onSetPrivateKey()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.error).isNull()
        val derived = checkNotNull(state.derived)
        assertThat(derived.compressedAddress).isEqualTo(FixtureAddresses.KEY_1_COMPRESSED)
        assertThat(derived.uncompressedAddress)
            .isEqualTo(FixtureAddresses.KEY_1_UNCOMPRESSED)
        val guessed = checkNotNull(state.guessed)
        assertThat(guessed.privateKeyHex).isEqualTo(KEY_1)
        assertThat(guessed.address).isEqualTo(FixtureAddresses.KEY_1_COMPRESSED)
        assertThat(guessed.compressed).isTrue()
        assertThat(state.showWalletKeys).isFalse()
    }

    @Test
    fun `uncompressed-only match reports the uncompressed address`() = runTest {
        val viewModel = createViewModel(
            scheduler = testScheduler,
            repository = FakeAddressRepository(setOf(FixtureAddresses.KEY_2_UNCOMPRESSED)),
        )

        viewModel.onInputChange(KEY_2)
        viewModel.onSetPrivateKey()
        advanceUntilIdle()

        val guessed = checkNotNull(viewModel.state.value.guessed)
        assertThat(guessed.address).isEqualTo(FixtureAddresses.KEY_2_UNCOMPRESSED)
        assertThat(guessed.compressed).isFalse()
    }

    @Test
    fun `unknown valid key is derived but not guessed`() = runTest {
        val viewModel = createViewModel(
            scheduler = testScheduler,
            repository = FakeAddressRepository(FixtureAddresses.all),
        )

        viewModel.onInputChange(PRIVATE_KEY_ONE)
        viewModel.onSetPrivateKey()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.error).isNull()
        assertThat(state.derived).isNotNull()
        assertThat(state.guessed).isNull()
    }

    @Test
    fun `invalid input reports an error and clears derived values`() = runTest {
        val viewModel = createViewModel(
            scheduler = testScheduler,
            repository = FakeAddressRepository(FixtureAddresses.all),
        )

        viewModel.onInputChange("zz")
        viewModel.onSetPrivateKey()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.error).contains("64")
        assertThat(state.derived).isNull()
        assertThat(state.guessed).isNull()
    }

    @Test
    fun `random generates a key and checks it immediately`() = runTest {
        val viewModel = createViewModel(
            scheduler = testScheduler,
            repository = FakeAddressRepository(setOf(FixtureAddresses.KEY_1_COMPRESSED)),
            generator = PrivateKeyGenerator { Hex.decode(KEY_1) },
        )

        viewModel.onRandom()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.input).isEqualTo(KEY_1)
        val guessed = checkNotNull(state.guessed)
        assertThat(guessed.address).isEqualTo(FixtureAddresses.KEY_1_COMPRESSED)
    }

    @Test
    fun `wallet key reveals WIF values and generate hides them again`() = runTest {
        val viewModel = createViewModel(
            scheduler = testScheduler,
            repository = FakeAddressRepository(FixtureAddresses.all),
        )

        viewModel.onInputChange(PRIVATE_KEY_ONE)
        viewModel.onWalletKey()

        val revealed = viewModel.state.value
        assertThat(revealed.showWalletKeys).isTrue()
        val derived = checkNotNull(revealed.derived)
        assertThat(derived.compressedWif).isEqualTo(KEY_1_WIF_COMPRESSED)
        assertThat(derived.uncompressedWif).isEqualTo(KEY_1_WIF_UNCOMPRESSED)

        viewModel.onGeneratePublicKeys()

        assertThat(viewModel.state.value.showWalletKeys).isFalse()
    }

    @Test
    fun `changing the input clears derived and guessed values`() = runTest {
        val viewModel = createViewModel(
            scheduler = testScheduler,
            repository = FakeAddressRepository(setOf(FixtureAddresses.KEY_1_COMPRESSED)),
        )
        viewModel.onInputChange(KEY_1)
        viewModel.onSetPrivateKey()
        advanceUntilIdle()
        assertThat(viewModel.state.value.guessed).isNotNull()

        viewModel.onInputChange(KEY_1.dropLast(1))

        val state = viewModel.state.value
        assertThat(state.derived).isNull()
        assertThat(state.guessed).isNull()
        assertThat(state.error).isNull()
        assertThat(state.showWalletKeys).isFalse()
    }

    @Test
    fun `dismissing the guessed dialog clears it`() = runTest {
        val viewModel = createViewModel(
            scheduler = testScheduler,
            repository = FakeAddressRepository(setOf(FixtureAddresses.KEY_1_COMPRESSED)),
        )
        viewModel.onInputChange(KEY_1)
        viewModel.onSetPrivateKey()
        advanceUntilIdle()

        viewModel.onDismissGuessed()

        assertThat(viewModel.state.value.guessed).isNull()
        assertThat(viewModel.state.value.derived).isNotNull()
    }

    @Test
    fun `info dialog can be shown and dismissed`() = runTest {
        val viewModel = createViewModel(
            scheduler = testScheduler,
            repository = FakeAddressRepository(FixtureAddresses.all),
        )

        viewModel.onShowInfo()
        assertThat(viewModel.state.value.showInfo).isTrue()

        viewModel.onDismissInfo()
        assertThat(viewModel.state.value.showInfo).isFalse()
    }

    private fun TestScope.createViewModel(
        scheduler: TestCoroutineScheduler,
        repository: AddressRepository,
        generator: PrivateKeyGenerator = PrivateKeyGenerator { Hex.decode(KEY_1) },
    ): SolverViewModel {
        val dispatcher = StandardTestDispatcher(scheduler)
        Dispatchers.setMain(dispatcher)
        return SolverViewModel(
            checkPrivateKey = CheckPrivateKeyUseCase(repository),
            privateKeyGenerator = generator,
            ioDispatcher = dispatcher,
        )
    }

    private companion object {
        const val KEY_1 = "d9eb4e2909d646dd931bb306a2b492475cff04097a2456416ec9b7dba5ac98b8"
        const val PRIVATE_KEY_ONE =
            "0000000000000000000000000000000000000000000000000000000000000001"
        const val KEY_2 = "b7777a6bd712f26654e64bc6b609f311c252360f045f98b661704eb4c57b3772"
        const val KEY_1_WIF_COMPRESSED =
            "KwDiBf89QgGbjEhKnhXJuH7LrciVrZi3qYjgd9M7rFU73sVHnoWn"
        const val KEY_1_WIF_UNCOMPRESSED =
            "5HpHagT65TZzG1PH3CSu63k8DbpvD8s5ip4nEB3kEsreAnchuDf"
    }
}
