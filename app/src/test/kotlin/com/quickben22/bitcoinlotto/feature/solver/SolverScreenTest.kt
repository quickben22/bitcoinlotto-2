package com.quickben22.bitcoinlotto.feature.solver

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.quickben22.bitcoinlotto.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SolverScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `setting a private key reveals the address cards`() {
        val key = "d9eb4e2909d646dd931bb306a2b492475cff04097a2456416ec9b7dba5ac98b8"
        val derived = DerivedKey(
            privateKeyHex = key,
            compressedAddress = "1CompressedAddressExample",
            uncompressedAddress = "1UncompressedAddressExample",
            compressedWif = "KzCompressedWifExample",
            uncompressedWif = "5UncompressedWifExample",
        )
        var state by mutableStateOf(SolverUiState(input = key))

        composeRule.setContent {
            SolverScreen(
                state = state,
                onInputChange = { state = state.copy(input = it) },
                onSetPrivateKey = {
                    state = state.copy(
                        derived = derived,
                        derivedVersion = state.derivedVersion + 1,
                        message = R.string.solver_key_set,
                    )
                },
                onRandom = {},
                onWalletKey = {},
                onDismissGuessed = {},
                onShowInfo = {},
                onDismissInfo = {},
            )
        }

        composeRule.onNodeWithText("Enter a private key to see its compressed and uncompressed addresses.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Set key").performClick()
        composeRule.onNodeWithText("1CompressedAddressExample").assertIsDisplayed()
        composeRule.onNodeWithText("1UncompressedAddressExample").assertIsDisplayed()
    }
}
