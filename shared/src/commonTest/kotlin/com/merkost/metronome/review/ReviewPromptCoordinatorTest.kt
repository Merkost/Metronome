package com.merkost.metronome.review

import com.merkost.metronome.platform.AppVersionInfo
import com.merkost.metronome.platform.AppVersionProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewPromptCoordinatorTest {
    private val eligible = ReviewPromptSnapshot(
        totalPracticeMillis = 300_000L,
        sessionPracticeMillis = 300_000L,
        isPlaying = false,
        hasActiveTimer = false,
        hasActiveTempoTrainer = false,
        hasActiveGapTrainer = false,
        isTimerSheetVisible = false,
        isTempoSheetVisible = false,
        isOnboardingVisible = false,
    )

    @Test
    fun marksARequestOnlyWhenTheNativeHostAcceptsIt() = runTest {
        val store = FakeReviewPromptStore()
        val requester = FakeInAppReviewRequester(true)
        val coordinator = ReviewPromptCoordinator(
            store = store,
            requester = requester,
            appVersionProvider = FakeAppVersionProvider(),
            nowMillis = { 20_000_000_000L },
        )

        assertTrue(coordinator.requestIfEligible(eligible))
        assertEquals(1, requester.calls)
        assertEquals(ReviewPromptRecord("1.3.0", 20_000_000_000L), store.record)
    }

    @Test
    fun leavesTheRecordUntouchedWithoutANativeHost() = runTest {
        val store = FakeReviewPromptStore()
        val requester = FakeInAppReviewRequester(false)
        val coordinator = ReviewPromptCoordinator(
            store = store,
            requester = requester,
            appVersionProvider = FakeAppVersionProvider(),
            nowMillis = { 20_000_000_000L },
        )

        assertFalse(coordinator.requestIfEligible(eligible))
        assertEquals(1, requester.calls)
        assertEquals(ReviewPromptRecord(), store.record)
    }

    @Test
    fun awaitsAsyncNativeCompletionBeforeRecordingARequest() = runTest {
        val store = FakeReviewPromptStore()
        val requester = FakeAsyncInAppReviewRequester(accepted = true)
        val coordinator = ReviewPromptCoordinator(
            store = store,
            requester = requester,
            appVersionProvider = FakeAppVersionProvider(),
            nowMillis = { 20_000_000_000L },
        )

        assertTrue(coordinator.requestIfEligible(eligible))
        assertEquals(0, requester.syncCalls)
        assertEquals(1, requester.asyncCalls)
        assertEquals(ReviewPromptRecord("1.3.0", 20_000_000_000L), store.record)
    }

    @Test
    fun skipsTheNativeHostWhenPolicyRejectsThePause() = runTest {
        val store = FakeReviewPromptStore()
        val requester = FakeInAppReviewRequester(true)
        val coordinator = ReviewPromptCoordinator(
            store = store,
            requester = requester,
            appVersionProvider = FakeAppVersionProvider(),
            nowMillis = { 20_000_000_000L },
        )

        assertFalse(
            coordinator.requestIfEligible(
                eligible.copy(sessionPracticeMillis = 299_999L)
            )
        )
        assertEquals(0, requester.calls)
        assertEquals(ReviewPromptRecord(), store.record)
    }

    @Test
    fun skipsTheNativeHostWhenTheAppVersionIsUnavailable() = runTest {
        val store = FakeReviewPromptStore()
        val requester = FakeInAppReviewRequester(true)
        val coordinator = ReviewPromptCoordinator(
            store = store,
            requester = requester,
            appVersionProvider = FakeAppVersionProvider(null),
            nowMillis = { 20_000_000_000L },
        )

        assertFalse(coordinator.requestIfEligible(eligible))
        assertEquals(0, requester.calls)
        assertEquals(ReviewPromptRecord(), store.record)
    }

    @Test
    fun waitsForAQuietPauseBeforeRequesting() = runTest {
        val store = FakeReviewPromptStore()
        val requester = FakeInAppReviewRequester(true)
        val coordinator = ReviewPromptCoordinator(
            store = store,
            requester = requester,
            appVersionProvider = FakeAppVersionProvider(),
            nowMillis = { 20_000_000_000L },
        )

        val request = launch {
            coordinator.requestAfterQuietPause { eligible }
        }

        advanceTimeBy(REVIEW_PAUSE_DELAY_MILLIS - 1L)
        runCurrent()
        assertEquals(0, requester.calls)

        advanceTimeBy(1L)
        request.join()
        assertEquals(1, requester.calls)
    }

    @Test
    fun rechecksTheLatestStateAfterTheQuietPause() = runTest {
        val store = FakeReviewPromptStore()
        val requester = FakeInAppReviewRequester(true)
        val coordinator = ReviewPromptCoordinator(
            store = store,
            requester = requester,
            appVersionProvider = FakeAppVersionProvider(),
            nowMillis = { 20_000_000_000L },
        )
        var latest = eligible

        val request = launch {
            coordinator.requestAfterQuietPause { latest }
        }
        latest = eligible.copy(isPlaying = true)
        advanceTimeBy(REVIEW_PAUSE_DELAY_MILLIS)
        request.join()

        assertEquals(0, requester.calls)
        assertEquals(ReviewPromptRecord(), store.record)
    }

    private class FakeReviewPromptStore : ReviewPromptStore {
        var record = ReviewPromptRecord()

        override suspend fun read(): ReviewPromptRecord = record

        override suspend fun markRequested(version: String, atMillis: Long) {
            record = ReviewPromptRecord(version, atMillis)
        }
    }

    private class FakeInAppReviewRequester(
        private val accepted: Boolean,
    ) : InAppReviewRequester {
        var calls = 0

        override fun requestReview(): Boolean {
            calls += 1
            return accepted
        }
    }

    private class FakeAsyncInAppReviewRequester(
        private val accepted: Boolean,
    ) : InAppReviewRequester, AsyncInAppReviewRequester {
        var syncCalls = 0
        var asyncCalls = 0

        override fun requestReview(): Boolean {
            syncCalls += 1
            return false
        }

        override suspend fun awaitReviewRequest(): Boolean {
            asyncCalls += 1
            return accepted
        }
    }

    private class FakeAppVersionProvider(
        private val version: AppVersionInfo? = AppVersionInfo("1.3.0", 8L),
    ) : AppVersionProvider {
        override fun getAppVersion(): AppVersionInfo? = version
    }
}
