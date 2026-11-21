package com.quickben22.bitcoinlotto.feature.solver

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quickben22.bitcoinlotto.R
import com.quickben22.bitcoinlotto.ui.components.AppCard
import com.quickben22.bitcoinlotto.ui.components.EmptyStateCard
import com.quickben22.bitcoinlotto.ui.components.GuessedDialog
import com.quickben22.bitcoinlotto.ui.components.InfoDialog
import com.quickben22.bitcoinlotto.ui.components.KeyValueCard
import com.quickben22.bitcoinlotto.ui.components.SectionHeader
import com.quickben22.bitcoinlotto.ui.components.copySensitive
import com.quickben22.bitcoinlotto.ui.theme.SuccessGreen
import com.quickben22.bitcoinlotto.ui.theme.WarningAmber

@Composable
fun SolverRoute(factory: ViewModelProvider.Factory) {
    val viewModel: SolverViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsStateWithLifecycle()

    SolverScreen(
        state = state,
        onInputChange = viewModel::onInputChange,
        onSetPrivateKey = viewModel::onSetPrivateKey,
        onRandom = viewModel::onRandom,
        onWalletKey = viewModel::onWalletKey,
        onDismissGuessed = viewModel::onDismissGuessed,
        onShowInfo = viewModel::onShowInfo,
        onDismissInfo = viewModel::onDismissInfo,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Suppress("DEPRECATION")
@Composable
fun SolverScreen(
    state: SolverUiState,
    onInputChange: (String) -> Unit,
    onSetPrivateKey: () -> Unit,
    onRandom: () -> Unit,
    onWalletKey: () -> Unit,
    onDismissGuessed: () -> Unit,
    onShowInfo: () -> Unit,
    onDismissInfo: () -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val resultsRequester = remember { BringIntoViewRequester() }
    val snackbarHostState = remember { SnackbarHostState() }
    val messageText = state.message?.let { stringResource(it) }

    LaunchedEffect(state.messageVersion) {
        if (messageText != null) {
            snackbarHostState.showSnackbar(messageText)
        }
    }

    LaunchedEffect(state.derivedVersion) {
        if (state.derivedVersion > 0) {
            keyboard?.hide()
            focusManager.clearFocus()
            // Let the new layout settle before revealing the address cards.
            withFrameNanos { }
            resultsRequester.bringIntoView()
        }
    }

    val keyLength = state.input.count { !it.isWhitespace() }
    val helperColor = when {
        state.error != null -> MaterialTheme.colorScheme.error
        keyLength == 64 -> SuccessGreen
        keyLength > 64 -> WarningAmber
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.solver_input_title),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = onShowInfo,
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_info),
                                contentDescription = stringResource(R.string.solver_info),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        IconButton(
                            onClick = {
                                val text = clipboard.getText()?.text?.trim().orEmpty()
                                if (text.isNotEmpty()) onInputChange(text)
                            },
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_paste),
                                contentDescription = stringResource(R.string.solver_paste),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        IconButton(
                            onClick = { onInputChange("") },
                            enabled = state.input.isNotEmpty(),
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_clear),
                                contentDescription = stringResource(R.string.solver_clear),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    OutlinedTextField(
                        value = state.input,
                        onValueChange = onInputChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.solver_private_key_hint)) },
                        supportingText = {
                            Text(
                                text = state.error?.let { stringResource(it) } ?: stringResource(
                                    R.string.solver_private_key_counter,
                                    keyLength,
                                ),
                                color = helperColor,
                            )
                        },
                        isError = state.error != null,
                        minLines = 2,
                        maxLines = 3,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Ascii,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { onSetPrivateKey() },
                        ),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onSetPrivateKey,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_key),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.solver_set_private_key),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onRandom,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_random),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.solver_random),
                        maxLines = 1,
                    )
                }
                OutlinedButton(
                    onClick = onWalletKey,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_wallet),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.solver_wallet_key),
                        maxLines = 1,
                    )
                }
            }

            state.message?.let { message ->
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(message),
                        style = MaterialTheme.typography.labelLarge,
                        color = SuccessGreen,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(resultsRequester),
            ) {
                SectionHeader(text = stringResource(R.string.solver_results_title))

                Crossfade(
                    targetState = state.derived,
                    animationSpec = tween(durationMillis = 220),
                    label = "solver-results",
                ) { derived ->
                    if (derived == null) {
                        EmptyStateCard(text = stringResource(R.string.solver_results_empty))
                    } else {
                        val compressedAddressLabel = stringResource(R.string.solver_compressed_address)
                        val uncompressedAddressLabel = stringResource(R.string.solver_uncompressed_address)
                        val compressedWifLabel = stringResource(R.string.solver_compressed_wif)
                        val uncompressedWifLabel = stringResource(R.string.solver_uncompressed_wif)

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            KeyValueCard(
                                title = compressedAddressLabel,
                                value = derived.compressedAddress,
                                onCopy = {
                                    copySensitive(
                                        context,
                                        compressedAddressLabel,
                                        derived.compressedAddress,
                                    )
                                },
                            )
                            KeyValueCard(
                                title = uncompressedAddressLabel,
                                value = derived.uncompressedAddress,
                                onCopy = {
                                    copySensitive(
                                        context,
                                        uncompressedAddressLabel,
                                        derived.uncompressedAddress,
                                    )
                                },
                            )

                            if (state.showWalletKeys) {
                                Text(
                                    text = stringResource(R.string.solver_wallet_warning),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = WarningAmber,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                                KeyValueCard(
                                    title = compressedWifLabel,
                                    value = derived.compressedWif,
                                    onCopy = {
                                        copySensitive(
                                            context,
                                            compressedWifLabel,
                                            derived.compressedWif,
                                        )
                                    },
                                )
                                KeyValueCard(
                                    title = uncompressedWifLabel,
                                    value = derived.uncompressedWif,
                                    onCopy = {
                                        copySensitive(
                                            context,
                                            uncompressedWifLabel,
                                            derived.uncompressedWif,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
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
