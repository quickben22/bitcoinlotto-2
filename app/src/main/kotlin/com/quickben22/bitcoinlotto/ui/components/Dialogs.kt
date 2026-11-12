package com.quickben22.bitcoinlotto.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.quickben22.bitcoinlotto.R
import com.quickben22.bitcoinlotto.feature.solver.GuessedResult
import com.quickben22.bitcoinlotto.search.SearchHit

@Composable
fun HitDialog(
    hit: SearchHit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val privateKeyLabel = stringResource(R.string.hit_private_key)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.address_guessed)) },
        text = {
            SecureDialogWindow()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.hit_address),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                )
                Text(
                    text = hit.address,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                )
                Text(
                    text = privateKeyLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                )
                Text(
                    text = hit.privateKeyHex,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                )
                Text(
                    text = stringResource(
                        R.string.hit_keys_checked,
                        formatCount(hit.keysChecked),
                        formatDuration(hit.elapsedMillis),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    copySensitive(
                        context,
                        privateKeyLabel,
                        hit.privateKeyHex,
                    )
                    Toast.makeText(context, R.string.solver_copied, Toast.LENGTH_SHORT).show()
                },
            ) {
                Text(stringResource(R.string.hit_copy_key))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
    )
}

@Composable
fun GuessedDialog(
    guessed: GuessedResult,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val addressLabel = stringResource(R.string.hit_address)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.address_guessed)) },
        text = {
            SecureDialogWindow()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.guessed_message))
                Text(
                    text = guessed.address,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    copySensitive(
                        context,
                        addressLabel,
                        guessed.address,
                    )
                    Toast.makeText(context, R.string.solver_copied, Toast.LENGTH_SHORT).show()
                },
            ) {
                Text(stringResource(R.string.hit_copy_address))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
    )
}

@Composable
fun InfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.solver_info_title)) },
        text = { Text(stringResource(R.string.solver_info_body)) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
    )
}

@Composable
fun PrivacyDialog(
    onDismiss: () -> Unit,
    onManagePrivacy: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.privacy_options_title)) },
        text = { Text(stringResource(R.string.privacy_options_message)) },
        confirmButton = {
            TextButton(onClick = onManagePrivacy) {
                Text(stringResource(R.string.privacy_options_manage))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
    )
}
