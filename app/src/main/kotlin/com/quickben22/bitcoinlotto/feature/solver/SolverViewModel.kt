package com.quickben22.bitcoinlotto.feature.solver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickben22.bitcoinlotto.crypto.BitcoinKey
import com.quickben22.bitcoinlotto.crypto.Hex
import com.quickben22.bitcoinlotto.domain.usecase.CheckPrivateKeyUseCase
import com.quickben22.bitcoinlotto.domain.usecase.KeyCheckResult
import com.quickben22.bitcoinlotto.domain.usecase.PrivateKeyGenerator
import com.quickben22.bitcoinlotto.domain.usecase.PrivateKeyParseResult
import com.quickben22.bitcoinlotto.domain.usecase.PrivateKeyParser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DerivedKey(
    val privateKeyHex: String,
    val compressedAddress: String,
    val uncompressedAddress: String,
    val compressedWif: String,
    val uncompressedWif: String,
)

data class GuessedResult(
    val privateKeyHex: String,
    val address: String,
    val compressed: Boolean,
)

data class SolverUiState(
    val input: String = "",
    val error: String? = null,
    val derived: DerivedKey? = null,
    val guessed: GuessedResult? = null,
    val showWalletKeys: Boolean = false,
    val showInfo: Boolean = false,
)

class SolverViewModel(
    private val checkPrivateKey: CheckPrivateKeyUseCase,
    private val privateKeyGenerator: PrivateKeyGenerator,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val mutableState = MutableStateFlow(SolverUiState())
    val state: StateFlow<SolverUiState> = mutableState.asStateFlow()

    fun onInputChange(value: String) {
        mutableState.update {
            it.copy(
                input = value,
                error = null,
                derived = null,
                guessed = null,
                showWalletKeys = false,
            )
        }
    }

    fun onSetPrivateKey() {
        check(mutableState.value.input)
    }

    fun onRandom() {
        val privateKeyHex = Hex.encode(privateKeyGenerator.generate())
        mutableState.update {
            it.copy(
                input = privateKeyHex,
                error = null,
                derived = null,
                guessed = null,
                showWalletKeys = false,
            )
        }
        check(privateKeyHex)
    }

    fun onGeneratePublicKeys() {
        derive(showWalletKeys = false)
    }

    fun onWalletKey() {
        derive(showWalletKeys = true)
    }

    fun onDismissGuessed() {
        mutableState.update { it.copy(guessed = null) }
    }

    fun onShowInfo() {
        mutableState.update { it.copy(showInfo = true) }
    }

    fun onDismissInfo() {
        mutableState.update { it.copy(showInfo = false) }
    }

    private fun check(input: String) {
        viewModelScope.launch {
            when (val result = withContext(ioDispatcher) { checkPrivateKey(input) }) {
                is KeyCheckResult.Guessed -> mutableState.update {
                    it.copy(
                        error = null,
                        derived = result.key.toDerivedKey(),
                        guessed = GuessedResult(
                            privateKeyHex = result.key.privateKeyHex,
                            address = result.address,
                            compressed = result.compressed,
                        ),
                        showWalletKeys = false,
                    )
                }

                is KeyCheckResult.NotGuessed -> mutableState.update {
                    it.copy(
                        error = null,
                        derived = result.key.toDerivedKey(),
                        guessed = null,
                        showWalletKeys = false,
                    )
                }

                is KeyCheckResult.Invalid -> mutableState.update {
                    it.copy(
                        error = result.reason,
                        derived = null,
                        guessed = null,
                        showWalletKeys = false,
                    )
                }
            }
        }
    }

    private fun derive(showWalletKeys: Boolean) {
        when (val parsed = PrivateKeyParser.parse(mutableState.value.input)) {
            is PrivateKeyParseResult.Valid -> mutableState.update {
                it.copy(
                    error = null,
                    derived = parsed.key.toDerivedKey(),
                    guessed = null,
                    showWalletKeys = showWalletKeys,
                )
            }

            is PrivateKeyParseResult.Invalid -> mutableState.update {
                it.copy(
                    error = parsed.reason,
                    derived = null,
                    guessed = null,
                    showWalletKeys = false,
                )
            }
        }
    }

    private fun BitcoinKey.toDerivedKey(): DerivedKey = DerivedKey(
        privateKeyHex = privateKeyHex,
        compressedAddress = addressCompressed,
        uncompressedAddress = addressUncompressed,
        compressedWif = wifCompressed,
        uncompressedWif = wifUncompressed,
    )
}
