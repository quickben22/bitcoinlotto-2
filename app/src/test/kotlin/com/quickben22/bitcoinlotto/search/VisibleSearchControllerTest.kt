package com.quickben22.bitcoinlotto.search

import android.app.Application
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.startup.InitializationProvider
import com.google.common.truth.Truth.assertThat
import com.quickben22.bitcoinlotto.R
import com.quickben22.bitcoinlotto.data.SearchAccess
import com.quickben22.bitcoinlotto.data.SearchCheckpoint
import com.quickben22.bitcoinlotto.data.SearchCheckpointStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class VisibleSearchControllerTest {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val state = SearchStateHolder()
    private val store = mockk<SearchCheckpointStore>()
    private val engine = mockk<SearchEngine>()
    private val checkpoints = MutableStateFlow<SearchCheckpoint?>(null)
    private val access = MutableStateFlow(SearchAccess())
    private val owner = object : LifecycleOwner {
        override val lifecycle = LifecycleRegistry(this)
    }
    private val controller = VisibleSearchController(engine, store, state, scope)

    init {
        every { store.hit } returns flowOf(null)
        every { store.checkpoint } returns checkpoints
        every { store.searchAccess } returns access
        every { engine.search(any(), any(), any(), any()) } returns flow {
            emit(SearchEvent.Progress(1, KEY, 10, 100.0))
            awaitCancellation()
        }
        coEvery { store.save(any(), any()) } coAnswers {
            checkpoints.value = firstArg()
            access.value = access.value.copy(
                totalKeysChecked = access.value.totalKeysChecked + secondArg<Long>(),
            )
        }
        owner.lifecycle.addObserver(controller)
        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    @After
    fun tearDown() {
        controller.stop()
        waitFor { !state.state.value.isRunning }
        scope.cancel()
    }

    @Test
    fun `merged app manifest does not request foreground work`() {
        val app = RuntimeEnvironment.getApplication()
        val info = app.packageManager.getPackageInfo(
            app.packageName,
            PackageManager.GET_PERMISSIONS or PackageManager.GET_SERVICES,
        )
        val permissions = checkNotNull(info.requestedPermissions).toList()
        val services = checkNotNull(info.services).map { it.name }
        assertThat(permissions)
            .doesNotContain("android.permission.FOREGROUND_SERVICE")
        assertThat(permissions)
            .doesNotContain("android.permission.FOREGROUND_SERVICE_SPECIAL_USE")
        assertThat(services)
            .doesNotContain("androidx.work.impl.foreground.SystemForegroundService")
        assertThat(services)
            .doesNotContain("com.quickben22.bitcoinlotto.search.SearchService")
    }

    @Test
    fun `backgrounding cancels search and saves next unchecked key`() {
        start()
        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        waitFor { state.state.value.status == SearchStatus.Stopped }
        assertThat(checkpoints.value?.nextKeyHex).isEqualTo(NEXT_KEY)
        assertThat(checkpoints.value?.keysChecked).isEqualTo(1)
        assertThat(state.state.value.errorMessage).isEqualTo(R.string.search_background_paused)
        assertThat(access.value.totalKeysChecked).isEqualTo(1)
    }

    @Test
    fun `returning does not resume until explicitly started`() {
        start()
        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        waitFor { state.state.value.status == SearchStatus.Stopped }
        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
        verify(exactly = 1) { engine.search(any(), any(), any(), any()) }
        every { engine.search(any(), any(), any(), any()) } returns flow { awaitCancellation() }
        controller.start(KEY, resume = true)
        waitFor { state.state.value.isRunning }
        verify { engine.search(match { it.last() == 2.toByte() }, any(), 1L, 10L) }
    }

    @Test
    fun `stop waits for final saving before a rapid resume`() {
        val saving = AtomicBoolean(false)
        val saved = CompletableDeferred<Unit>()
        coEvery { store.save(any(), any()) } coAnswers {
            saving.set(true)
            saved.await()
            checkpoints.value = firstArg()
        }
        start()
        controller.stop()
        waitFor { saving.get() }
        every { engine.search(any(), any(), any(), any()) } returns flow { awaitCancellation() }
        controller.start(KEY, resume = true)
        verify(exactly = 1) { engine.search(any(), any(), any(), any()) }
        saved.complete(Unit)
        waitFor { state.state.value.isRunning && state.state.value.keysChecked == 1L }
        verify { engine.search(match { it.last() == 2.toByte() }, any(), 1L, 10L) }
    }

    @Test
    fun `pending initialization is cancelled when app becomes hidden`() {
        val loading = AtomicBoolean(false)
        every { store.hit } returns flow {
            loading.set(true)
            awaitCancellation()
        }
        controller.start(KEY)
        waitFor { loading.get() }
        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        waitFor { state.state.value.status == SearchStatus.Stopped }
        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
        verify(exactly = 0) { engine.search(any(), any(), any(), any()) }
        coVerify(exactly = 0) { store.save(any(), any()) }
    }

    @Test
    fun `start after app is hidden reports visible-only requirement`() {
        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        controller.start(KEY)
        assertThat(state.state.value.status).isEqualTo(SearchStatus.Stopped)
        assertThat(state.state.value.errorMessage).isEqualTo(R.string.search_visible_only)
        verify(exactly = 0) { engine.search(any(), any(), any(), any()) }
    }

    @Test
    fun `activity pause alone does not cancel search`() {
        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        start()
        owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        assertThat(state.state.value.isRunning).isTrue()
        coVerify(exactly = 0) { store.save(any(), any()) }
    }

    @Test
    fun `process lifecycle retains search through recreation but stops when hidden`() {
        owner.lifecycle.removeObserver(controller)
        controller.onStop(owner)
        Robolectric.buildContentProvider(InitializationProvider::class.java).create()
        val lifecycle = ProcessLifecycleOwner.get().lifecycle
        lifecycle.addObserver(controller)
        var activity = Robolectric.buildActivity(Activity::class.java).setup()
        try {
            start()
            activity.pause().stop().destroy()
            activity = Robolectric.buildActivity(Activity::class.java).setup()
            shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS)
            assertThat(state.state.value.isRunning).isTrue()

            activity.pause().stop()
            shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS)
            waitFor { state.state.value.status == SearchStatus.Stopped }
            assertThat(checkpoints.value?.nextKeyHex).isEqualTo(NEXT_KEY)
            activity.start().resume()
            assertThat(state.state.value.isRunning).isFalse()
            verify(exactly = 1) { engine.search(any(), any(), any(), any()) }
        } finally {
            lifecycle.removeObserver(controller)
            activity.pause().stop().destroy()
        }
    }

    @Test
    fun `explicit stop saves progress and checkpoint resume keeps counts`() {
        start()
        controller.stop()
        waitFor { state.state.value.status == SearchStatus.Stopped }
        assertThat(state.state.value.errorMessage).isNull()
        every { engine.search(any(), any(), any(), any()) } returns flow { awaitCancellation() }
        controller.start(KEY, resume = true)
        waitFor { state.state.value.isRunning }
        assertThat(state.state.value.keysChecked).isEqualTo(1)
        assertThat(state.state.value.elapsedMillis).isEqualTo(10)
    }

    @Test
    fun `checkpoint save failure is visible`() {
        coEvery { store.save(any(), any()) } throws IllegalStateException("storage unavailable")
        start()
        controller.stop()
        waitFor { state.state.value.status == SearchStatus.Error }
        assertThat(state.state.value.errorMessage).isEqualTo(R.string.search_saved_data_error)
    }

    @Test
    fun `stalled final saving reports an error instead of blocking resume forever`() {
        val saving = AtomicBoolean(false)
        coEvery { store.save(any(), any()) } coAnswers {
            saving.set(true)
            awaitCancellation()
        }
        start()
        controller.stop()
        waitFor { saving.get() }
        shadowOf(Looper.getMainLooper()).idleFor(6, TimeUnit.SECONDS)
        waitFor { state.state.value.status == SearchStatus.Error }
        assertThat(state.state.value.errorMessage).isEqualTo(R.string.search_saved_data_error)
    }

    @Test
    fun `checkpoint read failure never starts fresh search`() {
        every { store.checkpoint } returns flow { error("unreadable checkpoint") }
        controller.start(KEY, resume = true)
        waitFor { state.state.value.status == SearchStatus.Error }
        verify(exactly = 0) { engine.search(any(), any(), any(), any()) }
    }

    @Test
    fun `reward threshold saves progress and resume recalculates unlocked limit`() {
        access.value = SearchAccess(totalKeysChecked = 5_999_998)
        every { engine.search(any(), 2L, 0L, 0L) } returns flowOf(
            SearchEvent.Progress(2, NEXT_KEY, 20, 100.0),
            SearchEvent.LimitReached(2, NEXT_KEY, 20),
        )
        controller.start(KEY)
        waitFor { state.state.value.status == SearchStatus.LimitReached }
        assertThat(state.state.value.errorMessage).isEqualTo(R.string.search_unlock_required)
        assertThat(access.value.totalKeysChecked).isEqualTo(6_000_000)
        access.value = access.value.copy(firstRewardUsed = true)
        every { engine.search(any(), any(), any(), any()) } returns flow { awaitCancellation() }
        controller.start(KEY, resume = true)
        waitFor { state.state.value.isRunning }
        verify { engine.search(any(), 54_000_002L, 2L, 20L) }
    }

    @Test
    fun `found key remains saved and visible without a notification`() {
        every { engine.search(any(), any(), any(), any()) } returns flowOf(
            SearchEvent.Found(KEY, "address", true, 1, 10),
        )
        coEvery { store.saveHit(any(), any()) } returns Unit
        controller.start(KEY)
        waitFor { state.state.value.status == SearchStatus.Found }
        assertThat(state.state.value.hit?.privateKeyHex).isEqualTo(KEY)
        coVerify(exactly = 1) {
            store.saveHit(match { it.privateKeyHex == KEY && it.address == "address" }, 1L)
        }
        coVerify(exactly = 0) { store.save(any(), any()) }
    }

    @Test
    fun `failed hit saving preserves key in error state`() {
        every { engine.search(any(), any(), any(), any()) } returns flowOf(
            SearchEvent.Found(KEY, "address", true, 1, 10),
        )
        coEvery { store.saveHit(any(), any()) } throws IllegalStateException("storage unavailable")
        controller.start(KEY)
        waitFor { state.state.value.status == SearchStatus.Error }
        assertThat(state.state.value.hit?.privateKeyHex).isEqualTo(KEY)
        assertThat(state.state.value.errorMessage).isEqualTo(R.string.search_hit_save_error)
    }

    private fun start() {
        controller.start(KEY)
        waitFor { state.state.value.keysChecked == 1L }
    }

    private fun waitFor(condition: () -> Boolean) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10)
        while (!condition() && System.nanoTime() < deadline) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(5)
        }
        assertThat(condition()).isTrue()
    }

    private companion object {
        val KEY = "0".repeat(63) + "1"
        val NEXT_KEY = "0".repeat(63) + "2"
    }
}
