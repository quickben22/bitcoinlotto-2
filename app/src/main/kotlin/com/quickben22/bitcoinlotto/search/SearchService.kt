package com.quickben22.bitcoinlotto.search

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.quickben22.bitcoinlotto.AppContainer
import com.quickben22.bitcoinlotto.BitcoinLottoApp
import com.quickben22.bitcoinlotto.MainActivity
import com.quickben22.bitcoinlotto.R
import com.quickben22.bitcoinlotto.crypto.Hex
import com.quickben22.bitcoinlotto.data.SearchCheckpoint
import com.quickben22.bitcoinlotto.data.SavedSearchHit
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchService : LifecycleService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var searchJob: Job? = null
    private var commandGeneration = 0L
    private var wakeLock: PowerManager.WakeLock? = null

    private var sessionStartKeyHex: String = ""
    private var sessionInitialKeyHex: String = ""
    private var sessionLastKeyHex: String = ""
    private var sessionLimit: Long? = null
    private var sessionKeysChecked: Long = 0L
    private var sessionRecordedKeysChecked: Long = 0L
    private var sessionElapsedMillis: Long = 0L
    private var lastCheckpointAt: Long = 0L
    private var lastNotificationAt: Long = 0L

    private val container: AppContainer
        get() = (application as BitcoinLottoApp).container

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START -> {
                val startKeyHex = intent.getStringExtra(EXTRA_START_KEY_HEX)
                val limit = intent.getLongExtra(EXTRA_LIMIT, -1L).takeIf { it >= 0L }
                val resume = intent.getBooleanExtra(EXTRA_RESUME, false)
                if (startKeyHex == null && !resume) {
                    stopSelf(startId)
                    return START_NOT_STICKY
                }
                startSearch(startKeyHex, limit, resume)
            }

            ACTION_STOP -> {
                stopSearch(userStopped = true)
                return START_NOT_STICKY
            }

            null -> {
                if (!resumeFromCheckpoint()) {
                    stopSelf(startId)
                    return START_NOT_STICKY
                }
            }
        }

        return START_STICKY
    }

    private fun startSearch(startKeyHex: String?, limit: Long?, resume: Boolean) {
        val generation = ++commandGeneration
        startForegroundNow(SearchState(status = SearchStatus.Running))
        val previous = searchJob
        previous?.cancel()
        searchJob = serviceScope.launch {
            try {
                previous?.join()
                val checkpoint = if (resume) container.checkpointStore.checkpoint.first() else null
                val access = container.checkpointStore.searchAccess.first()
                if (access.isSearchLocked()) {
                    container.searchState.update {
                        it.copy(
                            status = SearchStatus.LimitReached,
                            error = getString(R.string.search_unlock_required),
                        )
                    }
                    finishSearch(generation)
                    return@launch
                }
                val effectiveStartKeyHex = checkpoint?.nextKeyHex ?: startKeyHex
                if (effectiveStartKeyHex == null) {
                    stopSelfSafely()
                    return@launch
                }
                val initialKeysChecked = checkpoint?.keysChecked ?: 0L
                val gateLimit = access.keysUntilNextLock()
                    ?.let { Math.addExact(initialKeysChecked, it) }
                val effectiveLimit = listOfNotNull(limit, gateLimit).minOrNull()
                launchSearch(
                    startKeyHex = effectiveStartKeyHex,
                    limit = effectiveLimit,
                    checkpoint = checkpoint,
                    generation = generation,
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                android.util.Log.e("SearchService", "Search failed", exception)
                container.searchState.update {
                    it.copy(status = SearchStatus.Error, error = getString(R.string.stats_status_error))
                }
                finishSearch(generation)
            }
        }
    }

    private fun resumeFromCheckpoint(): Boolean {
        startSearch(null, null, resume = true)
        return true
    }

    private suspend fun launchSearch(
        startKeyHex: String,
        limit: Long?,
        checkpoint: SearchCheckpoint?,
        generation: Long,
    ) {
        val startKey = try {
            Hex.decode(startKeyHex)
        } catch (_: IllegalArgumentException) {
            handleInvalidKey(getString(R.string.search_invalid_start_key))
            return
        }
        if (startKey.size != 32) {
            handleInvalidKey(getString(R.string.search_start_key_size))
            return
        }

        sessionStartKeyHex = checkpoint?.startKeyHex ?: startKeyHex
        sessionInitialKeyHex = startKeyHex
        sessionLastKeyHex = ""
        sessionLimit = limit
        sessionKeysChecked = checkpoint?.keysChecked ?: 0L
        sessionRecordedKeysChecked = sessionKeysChecked
        sessionElapsedMillis = checkpoint?.elapsedMillis ?: 0L
        lastCheckpointAt = 0L
        lastNotificationAt = 0L

        acquireWakeLock()
        container.searchState.update {
            SearchState(
                status = SearchStatus.Running,
                keysChecked = sessionKeysChecked,
                elapsedMillis = sessionElapsedMillis,
                lastKeyHex = startKeyHex,
            )
        }

        container.searchEngine.search(
            startKey = startKey,
            limit = limit,
            initialKeysChecked = sessionKeysChecked,
            initialElapsedMillis = sessionElapsedMillis,
        ).flowOn(Dispatchers.Default).collect { event ->
            handleEvent(event, generation)
        }
    }

    private suspend fun handleEvent(event: SearchEvent, generation: Long) {
        when (event) {
            is SearchEvent.Progress -> {
                sessionKeysChecked = event.keysChecked
                sessionLastKeyHex = event.lastKeyHex
                sessionElapsedMillis = event.elapsedMillis
                val state = SearchState(
                    status = SearchStatus.Running,
                    keysChecked = event.keysChecked,
                    elapsedMillis = event.elapsedMillis,
                    keysPerSecond = event.keysPerSecond,
                    lastKeyHex = event.lastKeyHex,
                )
                container.searchState.update { state }
                maybePersistCheckpoint()
                maybeUpdateNotification(state)
            }

            is SearchEvent.Found -> {
                sessionKeysChecked = event.keysChecked
                sessionLastKeyHex = event.privateKeyHex
                sessionElapsedMillis = event.elapsedMillis
                val state = SearchState(
                    status = SearchStatus.Found,
                    keysChecked = event.keysChecked,
                    elapsedMillis = event.elapsedMillis,
                    keysPerSecond = keysPerSecond(event.keysChecked, event.elapsedMillis),
                    lastKeyHex = event.privateKeyHex,
                    hit = SearchHit(
                        privateKeyHex = event.privateKeyHex,
                        address = event.address,
                        compressed = event.compressed,
                        keysChecked = event.keysChecked,
                        elapsedMillis = event.elapsedMillis,
                    ),
                )
                withContext(NonCancellable) {
                    try {
                        container.checkpointStore.saveHit(
                            SavedSearchHit(
                                privateKeyHex = event.privateKeyHex,
                                address = event.address,
                                compressed = event.compressed,
                                keysChecked = event.keysChecked,
                                elapsedMillis = event.elapsedMillis,
                            ),
                            searchedKeysDelta = (event.keysChecked - sessionRecordedKeysChecked)
                                .coerceAtLeast(0L),
                        )
                    } catch (exception: Exception) {
                        android.util.Log.e("SearchService", "Failed to save found key", exception)
                        container.searchState.update {
                            state.copy(status = SearchStatus.Error, error = getString(R.string.search_hit_save_error))
                        }
                        finishSearch(generation)
                        return@withContext
                    }
                    sessionRecordedKeysChecked = event.keysChecked
                    container.searchState.update { state }
                    postResultNotification(state)
                    finishSearch(generation)
                }
            }

            is SearchEvent.LimitReached -> {
                sessionKeysChecked = event.keysChecked
                sessionLastKeyHex = event.lastKeyHex
                sessionElapsedMillis = event.elapsedMillis
                maybePersistCheckpoint(force = true)
                val lockMessage = if (container.checkpointStore.searchAccess.first().isSearchLocked()) {
                    getString(R.string.search_unlock_required)
                } else {
                    null
                }
                val state = SearchState(
                    status = SearchStatus.LimitReached,
                    keysChecked = event.keysChecked,
                    elapsedMillis = event.elapsedMillis,
                    keysPerSecond = keysPerSecond(event.keysChecked, event.elapsedMillis),
                    lastKeyHex = event.lastKeyHex,
                    error = lockMessage,
                )
                container.searchState.update { state }
                postResultNotification(state)
                finishSearch(generation)
            }

            is SearchEvent.InvalidKey -> {
                container.searchState.update {
                    it.copy(status = SearchStatus.InvalidKey, error = getString(event.reason))
                }
                container.checkpointStore.clear()
                finishSearch(generation)
            }
        }
    }

    private fun stopSearch(userStopped: Boolean, timedOut: Boolean = false) {
        val generation = ++commandGeneration
        val job = searchJob
        searchJob = null
        job?.cancel()
        val timeoutWatchdog = if (timedOut) serviceScope.launch {
            delay(TIMEOUT_STOP_DEADLINE_MILLIS)
            if (generation == commandGeneration) {
                ++commandGeneration
                android.util.Log.e("SearchService", "Timed out while saving search progress")
                container.searchState.update {
                    it.copy(status = SearchStatus.Error, errorMessage = R.string.search_timeout_save_error)
                }
                stopSelfSafely()
            }
        } else null
        serviceScope.launch {
            try {
                job?.join()
                if (generation != commandGeneration || searchJob != null) return@launch
                if (userStopped && sessionStartKeyHex.isNotEmpty() &&
                    container.searchState.state.value.hit == null
                ) {
                    try {
                        maybePersistCheckpoint(force = true)
                        if (generation != commandGeneration) return@launch
                        container.searchState.update {
                            it.copy(
                                status = SearchStatus.Stopped,
                                error = if (timedOut) getString(R.string.search_timeout) else null,
                            )
                        }
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (exception: Exception) {
                        if (generation != commandGeneration) return@launch
                        android.util.Log.e("SearchService", "Failed to save checkpoint", exception)
                        container.searchState.update {
                            it.copy(status = SearchStatus.Error, errorMessage = R.string.search_saved_data_error)
                        }
                    }
                }
                stopSelfSafely()
            } finally {
                timeoutWatchdog?.cancel()
            }
        }
    }

    private suspend fun handleInvalidKey(reason: String) {
        container.searchState.update {
            it.copy(status = SearchStatus.InvalidKey, error = reason)
        }
        container.checkpointStore.clear()
        stopSelfSafely()
    }

    private suspend fun maybePersistCheckpoint(force: Boolean = false) {
        if (sessionStartKeyHex.isEmpty()) return
        val now = SystemClock.elapsedRealtime()
        if (!force && now - lastCheckpointAt < CHECKPOINT_INTERVAL_MILLIS) return
        lastCheckpointAt = now

        val nextKeyHex = if (sessionLastKeyHex.isEmpty()) {
            sessionInitialKeyHex
        } else {
            val nextKey = try {
                Hex.decode(sessionLastKeyHex)
            } catch (_: IllegalArgumentException) {
                return
            }
            if (!incrementKey(nextKey)) return
            Hex.encode(nextKey)
        }

        val searchedKeysDelta = (sessionKeysChecked - sessionRecordedKeysChecked).coerceAtLeast(0L)
        container.checkpointStore.save(
            SearchCheckpoint(
                keysChecked = sessionKeysChecked,
                nextKeyHex = nextKeyHex,
                elapsedMillis = sessionElapsedMillis,
                startKeyHex = sessionStartKeyHex,
                limit = sessionLimit,
            ),
            searchedKeysDelta = searchedKeysDelta,
        )
        sessionRecordedKeysChecked = sessionKeysChecked
    }

    private fun maybeUpdateNotification(state: SearchState) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastNotificationAt < NOTIFICATION_INTERVAL_MILLIS) return
        lastNotificationAt = now

        if (Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, buildNotification(state))
        }
    }

    private fun startForegroundNow(state: SearchState) {
        lastNotificationAt = SystemClock.elapsedRealtime()
        val foregroundServiceType = if (Build.VERSION.SDK_INT >= 34) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(state),
            foregroundServiceType,
        )
    }

    private fun finishSearch(generation: Long) {
        if (generation != commandGeneration) return
        releaseWakeLock()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun stopSelfSafely() {
        finishSearch(commandGeneration)
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "BitcoinLotto:Search",
        ).apply {
            setReferenceCounted(false)
            acquire()
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { lock ->
            if (lock.isHeld) lock.release()
        }
        wakeLock = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < 26) return
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.notification_channel_description)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
        manager.createNotificationChannel(
            NotificationChannel(
                RESULT_CHANNEL_ID,
                getString(R.string.search_result_channel),
                NotificationManager.IMPORTANCE_HIGH,
            ),
        )
    }

    private fun postResultNotification(state: SearchState) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return
        val title = if (state.hit != null) R.string.search_notification_hit else R.string.search_notification_complete
        val text = state.hit?.address ?: state.error ?: getString(R.string.search_notification_complete)
        val notification = NotificationCompat.Builder(this, RESULT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(title))
            .setContentText(text)
            .setContentIntent(contentPendingIntent())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(this).notify(RESULT_NOTIFICATION_ID, notification)
    }

    private fun buildNotification(state: SearchState): Notification {
        val title = when {
            state.hit != null -> getString(R.string.search_notification_hit)
            state.status == SearchStatus.LimitReached -> getString(R.string.search_notification_complete)
            state.status == SearchStatus.Stopped -> getString(R.string.search_notification_stopped)
            state.status == SearchStatus.InvalidKey || state.status == SearchStatus.Error ->
                getString(R.string.search_notification_stopped)
            state.lastKeyHex.isEmpty() -> getString(R.string.search_notification_preparing)
            else -> getString(R.string.search_notification_title)
        }
        val text = when {
            state.hit != null -> state.hit.address
            state.error != null -> state.error
            else -> getString(
                R.string.search_notification_progress,
                NumberFormat.getIntegerInstance().format(state.keysChecked),
                formatSpeed(state.keysPerSecond),
            )
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(contentPendingIntent())
            .setOngoing(state.isRunning)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setColor(ContextCompat.getColor(this, R.color.search_notification_accent))
            .addAction(0, getString(R.string.notification_stop), stopPendingIntent())
            .build()
    }

    private fun contentPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        return PendingIntent.getActivity(
            this,
            REQUEST_CONTENT,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun stopPendingIntent(): PendingIntent {
        val intent = Intent(this, SearchService::class.java).setAction(ACTION_STOP)
        return PendingIntent.getService(
            this,
            REQUEST_STOP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun formatSpeed(keysPerSecond: Double): String =
        String.format(Locale.getDefault(), "%.0f", keysPerSecond)

    private fun keysPerSecond(keysChecked: Long, elapsedMillis: Long): Double =
        if (elapsedMillis <= 0L) 0.0 else keysChecked * 1_000.0 / elapsedMillis

    override fun onTimeout(startId: Int, fgsType: Int) {
        stopSearch(userStopped = true, timedOut = true)
    }

    override fun onDestroy() {
        searchJob?.cancel()
        releaseWakeLock()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.quickben22.bitcoinlotto.action.START_SEARCH"
        const val ACTION_STOP = "com.quickben22.bitcoinlotto.action.STOP_SEARCH"
        const val EXTRA_START_KEY_HEX = "start_key_hex"
        const val EXTRA_LIMIT = "limit"
        const val EXTRA_RESUME = "resume"

        private const val CHANNEL_ID = "search"
        private const val RESULT_CHANNEL_ID = "search_result"
        private const val NOTIFICATION_ID = 1001
        private const val RESULT_NOTIFICATION_ID = 1002
        private const val TIMEOUT_STOP_DEADLINE_MILLIS = 2_500L
        private const val CHECKPOINT_INTERVAL_MILLIS = 2_000L
        private const val NOTIFICATION_INTERVAL_MILLIS = 250L
        private const val REQUEST_CONTENT = 0
        private const val REQUEST_STOP = 1

        fun start(
            context: Context,
            startKeyHex: String,
            limit: Long? = null,
            resume: Boolean = false,
        ) {
            val intent = Intent(context, SearchService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_START_KEY_HEX, startKeyHex)
                if (limit != null) putExtra(EXTRA_LIMIT, limit)
                putExtra(EXTRA_RESUME, resume)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.startService(
                Intent(context, SearchService::class.java).setAction(ACTION_STOP),
            )
        }
    }
}
