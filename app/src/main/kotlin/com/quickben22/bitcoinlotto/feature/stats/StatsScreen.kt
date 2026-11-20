package com.quickben22.bitcoinlotto.feature.stats

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quickben22.bitcoinlotto.R
import com.quickben22.bitcoinlotto.BitcoinLottoApp
import com.quickben22.bitcoinlotto.ads.RewardedAdState
import com.quickben22.bitcoinlotto.data.SearchAccess
import com.quickben22.bitcoinlotto.search.SearchState
import com.quickben22.bitcoinlotto.search.SearchStatus
import com.quickben22.bitcoinlotto.search.isSearchLocked
import com.quickben22.bitcoinlotto.search.keysUntilNextLock
import com.quickben22.bitcoinlotto.search.SearchHit
import com.quickben22.bitcoinlotto.ui.components.findActivity
import com.quickben22.bitcoinlotto.ui.components.copySensitive
import com.quickben22.bitcoinlotto.ui.components.HitDialog
import com.quickben22.bitcoinlotto.ui.components.MonospaceValueCard
import com.quickben22.bitcoinlotto.ui.components.RewardProgress
import com.quickben22.bitcoinlotto.ui.components.StatCard
import com.quickben22.bitcoinlotto.ui.components.StatusChip
import com.quickben22.bitcoinlotto.ui.components.formatCount
import com.quickben22.bitcoinlotto.ui.components.formatPrivateKey
import com.quickben22.bitcoinlotto.ui.components.formatSpeed
import com.quickben22.bitcoinlotto.ui.theme.SuccessGreen
import com.quickben22.bitcoinlotto.ui.theme.WarningAmber

@Composable
fun StatsRoute(factory: ViewModelProvider.Factory) {
    val viewModel: StatsViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val checkpoint by viewModel.checkpoint.collectAsStateWithLifecycle()
    val savedHit by viewModel.hit.collectAsStateWithLifecycle()
    val access by viewModel.access.collectAsStateWithLifecycle()
    val app = LocalContext.current.applicationContext as BitcoinLottoApp
    val rewardedAdManager = remember(app) { app.container.rewardedAdManager }
    val rewardedAdState by rewardedAdManager.state.collectAsStateWithLifecycle()
    val canRequestAds by app.container.adsConsentManager.adRequestsAllowed.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val locked = access.isSearchLocked()

    LaunchedEffect(locked, canRequestAds, rewardedAdState) {
        if (locked && canRequestAds && rewardedAdState == RewardedAdState.Idle) {
            rewardedAdManager.load()
        }
    }

    StatsScreen(
        state = state,
        hasCheckpoint = checkpoint != null,
        access = access,
        rewardedAdState = rewardedAdState,
        canRequestAds = canRequestAds,
        onStartStop = viewModel::startOrStop,
        onWatchRewardedAd = {
            context.findActivity()?.let { activity ->
                rewardedAdManager.show(activity, viewModel::onRewardEarned)
            }
        },
        onRetryRewardedAd = rewardedAdManager::load,
        hit = savedHit ?: state.hit,
        onDismissHit = viewModel::dismissHit,
    )
}

@Composable
fun StatsScreen(
    state: SearchState,
    hasCheckpoint: Boolean,
    access: SearchAccess,
    rewardedAdState: RewardedAdState,
    canRequestAds: Boolean,
    onStartStop: () -> Unit,
    onWatchRewardedAd: () -> Unit,
    onRetryRewardedAd: () -> Unit,
    hit: SearchHit?,
    onDismissHit: () -> Unit,
) {
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            StatCard(
                label = stringResource(R.string.keys),
                value = formatCount(state.keysChecked),
                caption = stringResource(R.string.stats_keys_caption),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusChip(
                    text = statusText(state),
                    color = statusColor(state),
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(
                        R.string.stats_total_searched,
                        formatCount(access.totalKeysChecked),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                )
            }

            Spacer(Modifier.height(14.dp))

            RewardProgress(
                progress = rewardProgress(access),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = access.keysUntilNextLock()?.let {
                    stringResource(R.string.search_next_unlock, formatCount(it))
                } ?: stringResource(R.string.search_unlimited),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            val keyLabel = if (state.lastKeyHex.isEmpty()) {
                stringResource(R.string.stats_private_key)
            } else if (state.pendingStartKeyHex.isNotEmpty() && !state.isRunning) {
                stringResource(R.string.stats_start_key)
            } else {
                stringResource(R.string.stats_private_key)
            }
            MonospaceValueCard(
                label = keyLabel,
                value = if (state.lastKeyHex.isEmpty()) {
                    stringResource(R.string.stats_private_key_none)
                } else {
                    formatPrivateKey(state.lastKeyHex)
                },
                onCopy = if (state.lastKeyHex.isEmpty()) {
                    null
                } else {
                    {
                        copySensitive(context, keyLabel, state.lastKeyHex)
                    }
                },
            )

            if (hasCheckpoint && !state.isRunning) {
                Spacer(Modifier.height(12.dp))
                StatusChip(
                    text = stringResource(R.string.stats_resume_available),
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (access.isSearchLocked()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(
                        if (access.firstRewardUsed) {
                            R.string.search_lock_second
                        } else {
                            R.string.search_lock_first
                        },
                        formatCount(access.totalKeysChecked),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarningAmber,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                enabled = hit == null && (state.isRunning || !access.isSearchLocked()),
                onClick = onStartStop,
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
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Icon(
                    painter = painterResource(
                        if (state.isRunning) R.drawable.ic_stop else R.drawable.ic_play,
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (state.isRunning) {
                        stringResource(R.string.stop_search)
                    } else if (access.isSearchLocked()) {
                        stringResource(R.string.search_locked)
                    } else {
                        stringResource(R.string.start_search)
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            if (access.isSearchLocked()) {
                Spacer(Modifier.height(10.dp))
                Button(
                    enabled = canRequestAds && rewardedAdState in
                        setOf(RewardedAdState.Ready, RewardedAdState.Failed),
                    onClick = {
                        if (rewardedAdState == RewardedAdState.Failed) {
                            onRetryRewardedAd()
                        } else {
                            onWatchRewardedAd()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(
                        text = stringResource(
                            when {
                                !canRequestAds -> R.string.search_reward_ads_unavailable
                                rewardedAdState == RewardedAdState.Ready -> R.string.search_watch_reward
                                rewardedAdState == RewardedAdState.Failed -> R.string.search_retry_reward
                                else -> R.string.search_loading_reward
                            },
                        ),
                        textAlign = TextAlign.Center,
                    )
                }
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

private fun rewardProgress(access: SearchAccess): Float {
    if (access.permanentlyUnlocked) return 1f
    val remaining = access.keysUntilNextLock() ?: return 1f
    val target = access.totalKeysChecked + remaining
    if (target <= 0L) return 0f
    return access.totalKeysChecked.toFloat() / target
}

@Composable
private fun statusText(state: SearchState): String = when (state.status) {
    SearchStatus.Running -> stringResource(
        R.string.stats_status_searching,
        formatSpeed(state.keysPerSecond),
    )

    SearchStatus.Found -> stringResource(R.string.stats_status_found)
    SearchStatus.LimitReached -> state.errorMessage?.let { stringResource(it) }
        ?: state.error ?: stringResource(R.string.stats_status_limit)
    SearchStatus.Stopped -> state.errorMessage?.let { stringResource(it) }
        ?: state.error ?: stringResource(R.string.stats_status_stopped)
    SearchStatus.InvalidKey -> state.errorMessage?.let { stringResource(it) }
        ?: state.error ?: stringResource(R.string.stats_status_stopped)
    SearchStatus.Error -> state.errorMessage?.let { stringResource(it) }
        ?: state.error ?: stringResource(R.string.stats_status_error)
    SearchStatus.Idle -> stringResource(R.string.stats_status_idle)
}

@Composable
private fun statusColor(state: SearchState): Color = when (state.status) {
    SearchStatus.Running, SearchStatus.Found -> SuccessGreen
    SearchStatus.LimitReached -> WarningAmber
    SearchStatus.Error, SearchStatus.InvalidKey -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}
