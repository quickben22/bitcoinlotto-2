package com.quickben22.bitcoinlotto.feature.solver

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quickben22.bitcoinlotto.R
import com.quickben22.bitcoinlotto.ui.components.GuessedDialog
import com.quickben22.bitcoinlotto.ui.components.InfoDialog
import com.quickben22.bitcoinlotto.ui.components.KeyValueCard
import com.quickben22.bitcoinlotto.ui.components.SecureScreen
import com.quickben22.bitcoinlotto.ui.components.copySensitive

@Composable
fun SolverRoute(factory: ViewModelProvider.Factory) {
    val viewModel: SolverViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsStateWithLifecycle()

    SolverScreen(
        state = state,
        onInputChange = viewModel::onInputChange,
        onSetPrivateKey = viewModel::onSetPrivateKey,
        onRandom = viewModel::onRandom,
        onGeneratePublicKeys = viewModel::onGeneratePublicKeys,
        onWalletKey = viewModel::onWalletKey,
        onDismissGuessed = viewModel::onDismissGuessed,
        onShowInfo = viewModel::onShowInfo,
        onDismissInfo = viewModel::onDismissInfo,
    )
}

@Composable
fun SolverScreen(
    state: SolverUiState,
    onInputChange: (String) -> Unit,
    onSetPrivateKey: () -> Unit,
    onRandom: () -> Unit,
    onGeneratePublicKeys: () -> Unit,
    onWalletKey: () -> Unit,
    onDismissGuessed: () -> Unit,
    onShowInfo: () -> Unit,
    onDismissInfo: () -> Unit,
) {
    SecureScreen()
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 720.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OutlinedTextField(
                value = state.input,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.solver_private_key_label)) },
                placeholder = { Text(stringResource(R.string.solver_private_key_hint)) },
                supportingText = {
                    Text(
                        text = state.error ?: stringResource(
                            R.string.solver_private_key_counter,
                            state.input.count { !it.isWhitespace() },
                        ),
                    )
                },
                isError = state.error != null,
                minLines = 2,
                maxLines = 3,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                ),
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onSetPrivateKey,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                ) {
                    Text(
                        text = stringResource(R.string.solver_set_private_key),
                        maxLines = 1,
                    )
                }
                OutlinedButton(
                    onClick = onRandom,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(stringResource(R.string.solver_random))
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onGeneratePublicKeys,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                ) {
                    Text(
                        text = stringResource(R.string.solver_generate_public_keys),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                    )
                }
                OutlinedButton(
                    onClick = onWalletKey,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                ) {
                    Text(
                        text = stringResource(R.string.solver_wallet_key),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onShowInfo,
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text(stringResource(R.string.solver_info))
            }

            state.derived?.let { derived ->
                val compressedAddressLabel = stringResource(R.string.solver_compressed_address)
                val uncompressedAddressLabel = stringResource(R.string.solver_uncompressed_address)
                val compressedWifLabel = stringResource(R.string.solver_compressed_wif)
                val uncompressedWifLabel = stringResource(R.string.solver_uncompressed_wif)

                Spacer(Modifier.height(16.dp))

                KeyValueCard(
                    title = compressedAddressLabel,
                    value = derived.compressedAddress,
                    onCopy = {
                        copySensitive(
                            context,
                            compressedAddressLabel,
                            derived.compressedAddress,
                        )
                        Toast.makeText(context, R.string.solver_copied, Toast.LENGTH_SHORT).show()
                    },
                )

                Spacer(Modifier.height(8.dp))

                KeyValueCard(
                    title = uncompressedAddressLabel,
                    value = derived.uncompressedAddress,
                    onCopy = {
                        copySensitive(
                            context,
                            uncompressedAddressLabel,
                            derived.uncompressedAddress,
                        )
                        Toast.makeText(context, R.string.solver_copied, Toast.LENGTH_SHORT).show()
                    },
                )

                if (state.showWalletKeys) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.solver_wallet_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.height(8.dp))

                    KeyValueCard(
                        title = compressedWifLabel,
                        value = derived.compressedWif,
                        onCopy = {
                            copySensitive(
                                context,
                                compressedWifLabel,
                                derived.compressedWif,
                            )
                            Toast.makeText(context, R.string.solver_copied, Toast.LENGTH_SHORT).show()
                        },
                    )

                    Spacer(Modifier.height(8.dp))

                    KeyValueCard(
                        title = uncompressedWifLabel,
                        value = derived.uncompressedWif,
                        onCopy = {
                            copySensitive(
                                context,
                                uncompressedWifLabel,
                                derived.uncompressedWif,
                            )
                            Toast.makeText(context, R.string.solver_copied, Toast.LENGTH_SHORT).show()
                        },
                    )
                }
            }
        }
    }

    state.guessed?.let { guessed ->
        GuessedDialog(
            guessed = guessed,
            onDismiss = onDismissGuessed,
        )
    }

    if (state.showInfo) {
        InfoDialog(onDismiss = onDismissInfo)
    }
}
