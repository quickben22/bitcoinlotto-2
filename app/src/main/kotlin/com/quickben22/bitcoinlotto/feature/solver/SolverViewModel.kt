package com.quickben22.bitcoinlotto.feature.solver

import androidx.lifecycle.ViewModel
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.quickben22.bitcoinlotto.R
import com.quickben22.bitcoinlotto.crypto.BitcoinKey
import com.quickben22.bitcoinlotto.crypto.Hex
import com.quickben22.bitcoinlotto.domain.usecase.CheckPrivateKeyUseCase
import com.quickben22.bitcoinlotto.domain.usecase.KeyCheckResult
import com.quickben22.bitcoinlotto.domain.usecase.PrivateKeyGenerator
import com.quickben22.bitcoinlotto.domain.usecase.PrivateKeyParseResult
import com.quickben22.bitcoinlotto.domain.usecase.PrivateKeyParser
import com.quickben22.bitcoinlotto.search.SearchStateHolder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    @StringRes val error: Int? = null,
    @StringRes val message: Int? = null,
    val derived: DerivedKey? = null,
    val guessed: GuessedResult? = null,
    val showWalletKeys: Boolean = false,
    val showInfo: Boolean = false,
    val derivedVersion: Int = 0,
    val messageVersion: Int = 0,
)

class SolverViewModel(
    private val checkPrivateKey: CheckPrivateKeyUseCase,
    private val privateKeyGenerator: PrivateKeyGenerator,
    private val searchStateHolder: SearchStateHolder,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val mutableState = MutableStateFlow(SolverUiState())
    val state: StateFlow<SolverUiState> = mutableState.asStateFlow()
    private var checkJob: Job? = null

    fun onInputChange(value: String) {
        checkJob?.cancel()
        mutableState.update {
            it.copy(
                input = value,
                error = null,
                message = null,
                derived = null,
                guessed = null,
                showWalletKeys = false,
            )
        }
    }

    fun onSetPrivateKey() {
        val input = mutableState.value.input
        val parsed = PrivateKeyParser.parse(input)
        if (parsed is PrivateKeyParseResult.Valid) {
            publishKey(parsed.key.privateKeyHex)
        }
        check(input)
    }

    fun onRandom() {
        val privateKeyHex = Hex.encode(privateKeyGenerator.generate())
        publishKey(privateKeyHex)
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
        checkJob?.cancel()
        checkJob = viewModelScope.launch {
            when (val result = withContext(ioDispatcher) { checkPrivateKey(input) }) {
                is KeyCheckResult.Guessed -> mutableState.update {
                    if (it.input != input) return@update it
                    it.copy(
                        error = null,
                        derived = result.key.toDerivedKey(),
                        guessed = GuessedResult(
                            privateKeyHex = result.key.privateKeyHex,
                            address = result.address,
                            compressed = result.compressed,
                        ),
                        showWalletKeys = false,
                        derivedVersion = it.derivedVersion + 1,
                    )
                }

                is KeyCheckResult.NotGuessed -> mutableState.update {
                    if (it.input != input) return@update it
                    it.copy(
                        error = null,
                        derived = result.key.toDerivedKey(),
                        guessed = null,
                        showWalletKeys = false,
                        derivedVersion = it.derivedVersion + 1,
                    )
                }

                is KeyCheckResult.Invalid -> mutableState.update {
                    if (it.input != input) return@update it
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
        checkJob?.cancel()
        val input = mutableState.value.input
        checkJob = viewModelScope.launch {
            val parsed = withContext(ioDispatcher) { PrivateKeyParser.parse(input) }
            val derived = (parsed as? PrivateKeyParseResult.Valid)?.key?.toDerivedKey()
            mutableState.update { state ->
                if (state.input != input) return@update state
                when (parsed) {
                    is PrivateKeyParseResult.Valid -> state.copy(
                        error = null,
                        message = R.string.solver_addresses_generated,
                        derived = derived,
                        guessed = null,
                        showWalletKeys = showWalletKeys,
                        derivedVersion = state.derivedVersion + 1,
                        messageVersion = state.messageVersion + 1,
                    )

                    is PrivateKeyParseResult.Invalid -> state.copy(
                        error = parsed.reason,
                        derived = null,
                        guessed = null,
                        showWalletKeys = false,
                    )
                }
            }
        }
    }

    private fun publishKey(keyHex: String) {
        searchStateHolder.update {
            it.copy(
                lastKeyHex = keyHex,
                pendingStartKeyHex = keyHex,
            )
        }
        mutableState.update {
            it.copy(
                message = R.string.solver_key_set,
                messageVersion = it.messageVersion + 1,
            )
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
