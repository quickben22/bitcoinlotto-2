package com.quickben22.bitcoinlotto.search

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Looper
import com.google.common.truth.Truth.assertThat
import com.quickben22.bitcoinlotto.AppContainer
import com.quickben22.bitcoinlotto.BitcoinLottoApp
import com.quickben22.bitcoinlotto.R
import com.quickben22.bitcoinlotto.data.SearchAccess
import com.quickben22.bitcoinlotto.data.SearchCheckpointStore
import com.quickben22.bitcoinlotto.data.SearchCheckpoint
import com.quickben22.bitcoinlotto.crypto.Hex
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32], application = BitcoinLottoApp::class)
class SearchServiceTest {
    @Test
    fun `stop saves next unchecked key after cancelling search`() = verifyStop(timedOut = false)

    @Test
    fun `timeout saves next unchecked key and offers resume`() = verifyStop(timedOut = true)

    private fun verifyStop(timedOut: Boolean) {
        val app = RuntimeEnvironment.getApplication() as BitcoinLottoApp
        val originalContainer = app.container
        val holder = SearchStateHolder()
        val store = mockk<SearchCheckpointStore>()
        val engine = mockk<SearchEngine>()
        val container = mockk<AppContainer>()
        every { container.searchState } returns holder
        every { container.checkpointStore } returns store
        every { container.searchEngine } returns engine
        every { store.searchAccess } returns flowOf(SearchAccess())
        every { engine.search(any(), any(), any(), any()) } returns flow {
            emit(SearchEvent.Progress(1, KEY, 10, 100.0))
            awaitCancellation()
        }
        coEvery { store.save(any(), any()) } returns Unit

        val field = BitcoinLottoApp::class.java.getDeclaredField("container")
        field.isAccessible = true
        field.set(app, container)
        val controller = Robolectric.buildService(SearchService::class.java).create()
        try {
            controller.get().onStartCommand(
                Intent(app, SearchService::class.java).setAction(SearchService.ACTION_START)
                    .putExtra(SearchService.EXTRA_START_KEY_HEX, KEY), 0, 1,
            )
            waitFor { holder.state.value.keysChecked == 1L }
            if (timedOut) {
                controller.get().onTimeout(1, 0)
            } else {
                controller.get().onStartCommand(
                    Intent(app, SearchService::class.java).setAction(SearchService.ACTION_STOP), 0, 2,
                )
            }
            waitFor { holder.state.value.status == SearchStatus.Stopped }
            if (timedOut) assertThat(holder.state.value.error).contains("resume")
            coVerify(atLeast = 1) {
                store.save(
                    match { it.keysChecked == 1L && it.nextKeyHex == Hex.encode(ByteArray(31) + byteArrayOf(2)) },
                    any(),
                )
            }
        } finally {
            controller.destroy()
            field.set(app, originalContainer)
        }
    }

    @Test
    fun `resume starts at saved next key with accumulated counts`() {
        val app = RuntimeEnvironment.getApplication() as BitcoinLottoApp
        val originalContainer = app.container
        val holder = SearchStateHolder()
        val store = mockk<SearchCheckpointStore>()
        val engine = mockk<SearchEngine>()
        val container = mockk<AppContainer>()
        val start = slot<ByteArray>()
        val nextKey = "0".repeat(63) + "2"
        every { container.searchState } returns holder
        every { container.checkpointStore } returns store
        every { container.searchEngine } returns engine
        every { store.searchAccess } returns flowOf(SearchAccess(totalKeysChecked = 5_999_993L))
        every { store.checkpoint } returns flowOf(SearchCheckpoint(3, nextKey, 500, KEY, 10))
        every { engine.search(capture(start), 10L, 3L, 500L) } returns flow { awaitCancellation() }

        val field = BitcoinLottoApp::class.java.getDeclaredField("container")
        field.isAccessible = true
        field.set(app, container)
        val controller = Robolectric.buildService(SearchService::class.java).create()
        try {
            controller.get().onStartCommand(
                Intent(app, SearchService::class.java).setAction(SearchService.ACTION_START)
                    .putExtra(SearchService.EXTRA_START_KEY_HEX, KEY)
                    .putExtra(SearchService.EXTRA_RESUME, true), 0, 1,
            )
            waitFor { start.isCaptured }
            assertThat(Hex.encode(start.captured)).isEqualTo(nextKey)
            assertThat(holder.state.value.keysChecked).isEqualTo(3L)
        } finally {
            controller.destroy()
            field.set(app, originalContainer)
        }
    }

    @Test
    fun `search pauses at the first reward threshold and saves a resume checkpoint`() {
        val app = RuntimeEnvironment.getApplication() as BitcoinLottoApp
        val originalContainer = app.container
        val holder = SearchStateHolder()
        val store = mockk<SearchCheckpointStore>()
        val engine = mockk<SearchEngine>()
        val container = mockk<AppContainer>()
        val access = MutableStateFlow(SearchAccess(totalKeysChecked = 5_999_998L))
        every { container.searchState } returns holder
        every { container.checkpointStore } returns store
        every { container.searchEngine } returns engine
        every { store.searchAccess } returns access
        every { engine.search(any(), 2L, 0L, 0L) } returns flowOf(
            SearchEvent.Progress(2L, KEY, 20L, 100.0),
            SearchEvent.LimitReached(2L, KEY, 20L),
        )
        coEvery { store.save(any(), any()) } coAnswers {
            access.value = access.value.copy(
                totalKeysChecked = access.value.totalKeysChecked + secondArg<Long>(),
            )
        }

        val field = BitcoinLottoApp::class.java.getDeclaredField("container")
        field.isAccessible = true
        field.set(app, container)
        val controller = Robolectric.buildService(SearchService::class.java).create()
        try {
            controller.get().onStartCommand(
                Intent(app, SearchService::class.java).setAction(SearchService.ACTION_START)
                    .putExtra(SearchService.EXTRA_START_KEY_HEX, KEY), 0, 1,
            )

            waitFor { holder.state.value.status == SearchStatus.LimitReached }

            assertThat(holder.state.value.error).contains("rewarded")
            coVerify(atLeast = 1) {
                store.save(
                    match {
                        it.keysChecked == 2L &&
                            it.nextKeyHex == Hex.encode(ByteArray(31) + byteArrayOf(2))
                    },
                    any(),
                )
            }
        } finally {
            controller.destroy()
            field.set(app, originalContainer)
        }
    }

    @Test
    fun `timeout stops the service if saving stalls`() {
        val app = RuntimeEnvironment.getApplication() as BitcoinLottoApp
        val originalContainer = app.container
        val holder = SearchStateHolder()
        val store = mockk<SearchCheckpointStore>()
        val engine = mockk<SearchEngine>()
        val container = mockk<AppContainer>()
        val saving = AtomicBoolean(false)
        every { container.searchState } returns holder
        every { container.checkpointStore } returns store
        every { container.searchEngine } returns engine
        every { store.searchAccess } returns flowOf(SearchAccess())
        every { engine.search(any(), any(), any(), any()) } returns flow { awaitCancellation() }
        coEvery { store.save(any(), any()) } coAnswers {
            saving.set(true)
            awaitCancellation()
        }

        val field = BitcoinLottoApp::class.java.getDeclaredField("container")
        field.isAccessible = true
        field.set(app, container)
        val controller = Robolectric.buildService(SearchService::class.java).create()
        try {
            controller.get().onStartCommand(
                Intent(app, SearchService::class.java).setAction(SearchService.ACTION_START)
                    .putExtra(SearchService.EXTRA_START_KEY_HEX, KEY), 0, 1,
            )
            waitFor { holder.state.value.status == SearchStatus.Running }
            controller.get().onTimeout(1, 0)
            waitFor { saving.get() }
            shadowOf(Looper.getMainLooper()).idleFor(3, TimeUnit.SECONDS)
            assertThat(holder.state.value.status).isEqualTo(SearchStatus.Error)
            assertThat(holder.state.value.errorMessage).isEqualTo(R.string.search_timeout_save_error)
            assertThat(shadowOf(controller.get()).isStoppedBySelf).isTrue()
        } finally {
            controller.destroy()
            field.set(app, originalContainer)
        }
    }

    @Test
    fun `found key is saved and a result notification is posted`() {
        val app = RuntimeEnvironment.getApplication() as BitcoinLottoApp
        val originalContainer = app.container
        val holder = SearchStateHolder()
        val store = mockk<SearchCheckpointStore>()
        val engine = mockk<SearchEngine>()
        val container = mockk<AppContainer>()
        every { container.searchState } returns holder
        every { container.checkpointStore } returns store
        every { container.searchEngine } returns engine
        every { store.searchAccess } returns flowOf(SearchAccess())
        every { engine.search(any(), any(), any(), any()) } returns flowOf(
            SearchEvent.Found(KEY, "address", true, 1, 10),
        )
        coEvery { store.saveHit(any(), any()) } returns Unit

        val field = BitcoinLottoApp::class.java.getDeclaredField("container")
        field.isAccessible = true
        field.set(app, container)
        val controller = Robolectric.buildService(SearchService::class.java).create()
        try {
            val intent = Intent(app, SearchService::class.java)
                .setAction(SearchService.ACTION_START)
                .putExtra(SearchService.EXTRA_START_KEY_HEX, KEY)
            controller.get().onStartCommand(intent, 0, 1)

            waitFor { holder.state.value.status == SearchStatus.Found }

            assertThat(holder.state.value.status).isEqualTo(SearchStatus.Found)
            coVerify(exactly = 1) {
                store.saveHit(match { it.privateKeyHex == KEY && it.address == "address" }, 1L)
            }
            val manager = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            assertThat(manager.activeNotifications.map { it.id }).contains(1002)
        } finally {
            controller.destroy()
            field.set(app, originalContainer)
        }
    }

    private fun waitFor(condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + 10_000
        while (!condition() && System.currentTimeMillis() < deadline) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(10)
        }
        shadowOf(Looper.getMainLooper()).idle()
        check(condition()) { "Timed out waiting for service state" }
    }

    private companion object {
        const val KEY = "0000000000000000000000000000000000000000000000000000000000000001"
    }
}
