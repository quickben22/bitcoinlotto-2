package com.quickben22.bitcoinlotto.feature.stats

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quickben22.bitcoinlotto.R
import com.quickben22.bitcoinlotto.search.SearchState
import com.quickben22.bitcoinlotto.search.SearchStatus
import com.quickben22.bitcoinlotto.search.SearchHit
import com.quickben22.bitcoinlotto.ui.components.HitDialog
import com.quickben22.bitcoinlotto.ui.components.MonospaceValueCard
import com.quickben22.bitcoinlotto.ui.components.StatCard
import com.quickben22.bitcoinlotto.ui.components.formatCount
import com.quickben22.bitcoinlotto.ui.components.formatDuration
import com.quickben22.bitcoinlotto.ui.components.formatPrivateKey
import com.quickben22.bitcoinlotto.ui.components.formatSpeed

@Composable
fun StatsRoute(factory: ViewModelProvider.Factory) {
    val viewModel: StatsViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val checkpoint by viewModel.checkpoint.collectAsStateWithLifecycle()
    val savedHit by viewModel.hit.collectAsStateWithLifecycle()

    StatsScreen(
        state = state,
        hasCheckpoint = checkpoint != null,
        onStartStop = viewModel::startOrStop,
        hit = savedHit ?: state.hit,
        onDismissHit = viewModel::dismissHit,
    )
}

@Composable
fun StatsScreen(
    state: SearchState,
    hasCheckpoint: Boolean,
    onStartStop: () -> Unit,
    hit: SearchHit?,
    onDismissHit: () -> Unit,
) {
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 720.dp)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatCard(
                    label = stringResource(R.string.keys),
                    value = formatCount(state.keysChecked),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = stringResource(R.string.duration),
                    value = formatDuration(state.elapsedMillis),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = stringResource(R.string.key_frequency),
                    value = formatSpeed(state.keysPerSecond),
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(8.dp))

            MonospaceValueCard(
                label = stringResource(R.string.stats_private_key),
                value = if (state.lastKeyHex.isEmpty()) {
                    stringResource(R.string.stats_private_key_none)
                } else {
                    formatPrivateKey(state.lastKeyHex)
                },
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = statusText(state),
                style = MaterialTheme.typography.titleMedium,
                color = statusColor(state),
                textAlign = TextAlign.Center,
            )

            if (hasCheckpoint && !state.isRunning) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.stats_resume_available),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                enabled = hit == null,
                onClick = {
                    if (!state.isRunning &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS,
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS,
                        )
                    }
                    onStartStop()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isRunning) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    contentColor = if (state.isRunning) {
                        MaterialTheme.colorScheme.onError
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    },
                ),
            ) {
                Text(
                    text = if (state.isRunning) {
                        stringResource(R.string.stop_search)
                    } else {
                        stringResource(R.string.start_search)
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (hit != null) {
        HitDialog(
            hit = hit,
            onDismiss = onDismissHit,
        )
    }
}

@Composable
private fun statusText(state: SearchState): String = when (state.status) {
    SearchStatus.Running -> stringResource(
        R.string.stats_status_searching,
        formatSpeed(state.keysPerSecond),
    )

    SearchStatus.Found -> stringResource(R.string.stats_status_found)
    SearchStatus.LimitReached -> stringResource(R.string.stats_status_limit)
    SearchStatus.Stopped -> state.error ?: stringResource(R.string.stats_status_stopped)
    SearchStatus.InvalidKey -> state.error ?: stringResource(R.string.stats_status_stopped)
    SearchStatus.Error -> state.errorMessage?.let { stringResource(it) }
        ?: state.error ?: stringResource(R.string.stats_status_error)
    SearchStatus.Idle -> stringResource(R.string.stats_status_idle)
}

@Composable
private fun statusColor(state: SearchState): Color = when (state.status) {
    SearchStatus.Error, SearchStatus.InvalidKey -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
}
