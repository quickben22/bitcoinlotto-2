package com.quickben22.bitcoinlotto.feature.stats

import com.google.common.truth.Truth.assertThat
import com.quickben22.bitcoinlotto.crypto.Hex
import com.quickben22.bitcoinlotto.data.SearchCheckpoint
import com.quickben22.bitcoinlotto.data.SearchCheckpointStore
import com.quickben22.bitcoinlotto.data.SearchAccess
import com.quickben22.bitcoinlotto.domain.usecase.PrivateKeyGenerator
import com.quickben22.bitcoinlotto.search.SearchController
import com.quickben22.bitcoinlotto.search.SearchStateHolder
import com.quickben22.bitcoinlotto.search.SearchStatus
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class StatsViewModelTest {

    private val stateHolder = SearchStateHolder()

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `start resumes from the saved checkpoint`() = runTest {
        val checkpoint = SearchCheckpoint(
            keysChecked = 5L,
            nextKeyHex = "ab",
            elapsedMillis = 1_000L,
            startKeyHex = "aa",
            limit = 10L,
        )
        val store = mockk<SearchCheckpointStore>()
        every { store.checkpoint } returns flowOf(checkpoint)
        val controller = RecordingController()
        val viewModel = createViewModel(store, controller, testScheduler)

        viewModel.startOrStop()
        advanceUntilIdle()

        assertThat(controller.starts).containsExactly(
            RecordingController.Start("ab", null, resume = true),
        )
        assertThat(controller.stops).isEqualTo(0)
    }

    @Test
    fun `start without a checkpoint uses a fresh random key`() = runTest {
        val store = mockk<SearchCheckpointStore>()
        every { store.checkpoint } returns flowOf(null)
        val controller = RecordingController()
        val viewModel = createViewModel(store, controller, testScheduler)

        viewModel.startOrStop()
        advanceUntilIdle()

        assertThat(controller.starts).containsExactly(
            RecordingController.Start(KEY_1, limit = null, resume = false),
        )
    }

    @Test
    fun `a checkpoint read failure reports an error instead of starting a new search`() = runTest {
        val store = mockk<SearchCheckpointStore>()
        every { store.checkpoint } returns flow { error("checkpoint unavailable") }
        val controller = RecordingController()
        val viewModel = createViewModel(store, controller, testScheduler)

        viewModel.startOrStop()
        advanceUntilIdle()

        assertThat(controller.starts).isEmpty()
        assertThat(viewModel.state.value.status).isEqualTo(SearchStatus.Error)
    }

    @Test
    fun `start while running stops the search instead of starting another`() = runTest {
        val store = mockk<SearchCheckpointStore>()
        every { store.checkpoint } returns flowOf(null)
        val controller = RecordingController()
        stateHolder.update { it.copy(status = SearchStatus.Running) }
        val viewModel = createViewModel(store, controller, testScheduler)

        viewModel.startOrStop()

        assertThat(controller.stops).isEqualTo(1)
        assertThat(controller.starts).isEmpty()
    }

    private fun TestScope.createViewModel(
        store: SearchCheckpointStore,
        controller: SearchController,
        scheduler: TestCoroutineScheduler,
    ): StatsViewModel {
        val dispatcher = StandardTestDispatcher(scheduler)
        Dispatchers.setMain(dispatcher)
        every { store.hit } returns flowOf(null)
        every { store.searchAccess } returns flowOf(SearchAccess())
        return StatsViewModel(
            searchStateHolder = stateHolder,
            checkpointStore = store,
            searchController = controller,
            privateKeyGenerator = PrivateKeyGenerator { Hex.decode(KEY_1) },
        )
    }

    private class RecordingController : SearchController {
        data class Start(
            val startKeyHex: String,
            val limit: Long?,
            val resume: Boolean,
        )

        val starts = mutableListOf<Start>()
        var stops = 0
            private set

        override fun start(startKeyHex: String, limit: Long?, resume: Boolean) {
            starts += Start(startKeyHex, limit, resume)
        }

        override fun stop() {
            stops++
        }
    }

    private companion object {
        const val KEY_1 = "d9eb4e2909d646dd931bb306a2b492475cff04097a2456416ec9b7dba5ac98b8"
    }
}
