# Native In-App Review Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Request the native App Store or Google Play review sheet after a musician has accumulated five minutes of playback and reaches a quiet, qualified pause.

**Architecture:** Keep eligibility and persistence in shared Kotlin, expose the store sheet behind an `InAppReviewRequester` interface, and inject platform implementations through Koin. Android tracks the resumed `Activity` and uses Play Core Review; iOS implements the Kotlin interface in Swift and uses StoreKit, matching the existing `LiveActivityController` bridge.

**Tech Stack:** Kotlin Multiplatform, Kotlin coroutines and Flow, DataStore Preferences, Koin 4.2.2, Google Play In-App Review 2.0.2, StoreKit, Swift, common Kotlin tests.

**Spec:** `docs/aso-strategy.md`

## Global Constraints

- Trigger after at least `300_000L` milliseconds of cumulative playback in the current app process.
- Evaluate only after a playing-to-paused transition remains paused for `2_000L` milliseconds.
- Do not request while a countdown timer, Tempo Trainer, Gap Trainer, timer sheet, tempo sheet or onboarding coach mark is active.
- Do not show a custom pre-prompt, sentiment gate, rating request dialog, success message or failure message.
- Request at most once per app version and never more frequently than once every 180 days.
- Keep the existing manual **Rate the App** Settings action.
- Use interface plus Koin platform abstraction; do not add `expect`/`actual` services.
- Add no comments to production or test code.
- Preserve all unrelated working-tree changes.

---

### Task 1: Shared eligibility policy and persisted request record

**Files:**
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/review/ReviewPromptPolicy.kt`
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/review/ReviewPromptStore.kt`
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/review/DataStoreReviewPromptStore.kt`
- Create: `shared/src/commonTest/kotlin/com/merkost/metronome/review/ReviewPromptPolicyTest.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/di/CommonModule.kt`

**Interfaces:**
- Produces: `ReviewPromptSnapshot`, `ReviewPromptRecord`, `shouldRequestReview`, and `ReviewPromptStore`.
- Consumes: the existing shared `DataStore<Preferences>` Koin singleton.

- [ ] **Step 1: Write the failing policy tests**

Create `ReviewPromptPolicyTest.kt`:

```kotlin
package com.merkost.metronome.review

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReviewPromptPolicyTest {
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
    fun requestsAtFiveMinutesOnAnIdlePause() {
        assertTrue(
            shouldRequestReview(
                snapshot = eligible,
                record = ReviewPromptRecord(),
                currentVersion = "1.3.0",
                nowMillis = 20_000_000_000L,
            )
        )
    }

    @Test
    fun rejectsPracticeBelowFiveMinutes() {
        assertFalse(
            shouldRequestReview(
                snapshot = eligible.copy(sessionPracticeMillis = 299_999L),
                record = ReviewPromptRecord(),
                currentVersion = "1.3.0",
                nowMillis = 20_000_000_000L,
            )
        )
    }

    @Test
    fun rejectsPlaybackThatResumed() {
        assertFalse(
            shouldRequestReview(
                snapshot = eligible.copy(isPlaying = true),
                record = ReviewPromptRecord(),
                currentVersion = "1.3.0",
                nowMillis = 20_000_000_000L,
            )
        )
    }

    @Test
    fun rejectsEveryActivePracticeSurface() {
        val blocked = listOf(
            eligible.copy(hasActiveTimer = true),
            eligible.copy(hasActiveTempoTrainer = true),
            eligible.copy(hasActiveGapTrainer = true),
            eligible.copy(isTimerSheetVisible = true),
            eligible.copy(isTempoSheetVisible = true),
            eligible.copy(isOnboardingVisible = true),
        )
        assertTrue(
            blocked.all {
                !shouldRequestReview(
                    snapshot = it,
                    record = ReviewPromptRecord(),
                    currentVersion = "1.3.0",
                    nowMillis = 20_000_000_000L,
                )
            }
        )
    }

    @Test
    fun rejectsTheSameVersionAndTheCooldownWindow() {
        val now = 20_000_000_000L
        assertFalse(
            shouldRequestReview(
                snapshot = eligible,
                record = ReviewPromptRecord("1.3.0", now - REVIEW_COOLDOWN_MILLIS - 1L),
                currentVersion = "1.3.0",
                nowMillis = now,
            )
        )
        assertFalse(
            shouldRequestReview(
                snapshot = eligible,
                record = ReviewPromptRecord("1.2.0", now - REVIEW_COOLDOWN_MILLIS + 1L),
                currentVersion = "1.3.0",
                nowMillis = now,
            )
        )
    }

    @Test
    fun allowsANewVersionAfterCooldown() {
        val now = 20_000_000_000L
        assertTrue(
            shouldRequestReview(
                snapshot = eligible,
                record = ReviewPromptRecord("1.2.0", now - REVIEW_COOLDOWN_MILLIS),
                currentVersion = "1.3.0",
                nowMillis = now,
            )
        )
    }
}
```

- [ ] **Step 2: Run the focused test and verify red**

Run:

```bash
./gradlew :shared:testDebugUnitTest --tests 'com.merkost.metronome.review.ReviewPromptPolicyTest'
```

Expected: compilation fails because the review policy types do not exist.

- [ ] **Step 3: Implement the pure policy**

Create `ReviewPromptPolicy.kt`:

```kotlin
package com.merkost.metronome.review

const val MIN_REVIEW_PRACTICE_MILLIS = 300_000L
const val REVIEW_PAUSE_DELAY_MILLIS = 2_000L
const val REVIEW_COOLDOWN_MILLIS = 180L * 24L * 60L * 60L * 1_000L

data class ReviewPromptSnapshot(
    val totalPracticeMillis: Long,
    val sessionPracticeMillis: Long,
    val isPlaying: Boolean,
    val hasActiveTimer: Boolean,
    val hasActiveTempoTrainer: Boolean,
    val hasActiveGapTrainer: Boolean,
    val isTimerSheetVisible: Boolean,
    val isTempoSheetVisible: Boolean,
    val isOnboardingVisible: Boolean,
)

data class ReviewPromptRecord(
    val lastRequestedVersion: String? = null,
    val lastRequestedAtMillis: Long? = null,
)

fun shouldRequestReview(
    snapshot: ReviewPromptSnapshot,
    record: ReviewPromptRecord,
    currentVersion: String,
    nowMillis: Long,
): Boolean {
    if (snapshot.totalPracticeMillis < MIN_REVIEW_PRACTICE_MILLIS) return false
    if (snapshot.sessionPracticeMillis < MIN_REVIEW_PRACTICE_MILLIS) return false
    if (snapshot.isPlaying) return false
    if (snapshot.hasActiveTimer) return false
    if (snapshot.hasActiveTempoTrainer) return false
    if (snapshot.hasActiveGapTrainer) return false
    if (snapshot.isTimerSheetVisible) return false
    if (snapshot.isTempoSheetVisible) return false
    if (snapshot.isOnboardingVisible) return false
    if (record.lastRequestedVersion == currentVersion) return false
    val lastRequestedAtMillis = record.lastRequestedAtMillis
    if (lastRequestedAtMillis != null && nowMillis - lastRequestedAtMillis < REVIEW_COOLDOWN_MILLIS) return false
    return true
}
```

- [ ] **Step 4: Implement isolated DataStore persistence**

Create `ReviewPromptStore.kt`:

```kotlin
package com.merkost.metronome.review

interface ReviewPromptStore {
    suspend fun read(): ReviewPromptRecord
    suspend fun markRequested(version: String, atMillis: Long)
}
```

Create `DataStoreReviewPromptStore.kt`:

```kotlin
package com.merkost.metronome.review

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

class DataStoreReviewPromptStore(
    private val dataStore: DataStore<Preferences>,
) : ReviewPromptStore {
    private companion object {
        val LAST_REVIEW_VERSION = stringPreferencesKey("last_review_version")
        val LAST_REVIEW_AT_MILLIS = longPreferencesKey("last_review_at_millis")
    }

    override suspend fun read(): ReviewPromptRecord {
        val preferences = dataStore.data.first()
        return ReviewPromptRecord(
            lastRequestedVersion = preferences[LAST_REVIEW_VERSION],
            lastRequestedAtMillis = preferences[LAST_REVIEW_AT_MILLIS],
        )
    }

    override suspend fun markRequested(version: String, atMillis: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_REVIEW_VERSION] = version
            preferences[LAST_REVIEW_AT_MILLIS] = atMillis
        }
    }
}
```

Register it in `CommonModule.kt` beside `AppDatastore`:

```kotlin
single<ReviewPromptStore> { DataStoreReviewPromptStore(get()) }
```

- [ ] **Step 5: Run the focused test and full shared tests**

Run:

```bash
./gradlew :shared:testDebugUnitTest --tests 'com.merkost.metronome.review.ReviewPromptPolicyTest'
./gradlew :shared:testDebugUnitTest
```

Expected: `ReviewPromptPolicyTest` passes, then all shared unit tests pass.

- [ ] **Step 6: Commit the shared policy**

```bash
git add shared/src/commonMain/kotlin/com/merkost/metronome/review \
        shared/src/commonTest/kotlin/com/merkost/metronome/review \
        shared/src/commonMain/kotlin/com/merkost/metronome/di/CommonModule.kt
git commit -m "feat: add native review eligibility policy"
```

---

### Task 2: Native requester implementations

**Files:**
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/review/InAppReviewRequester.kt`
- Create: `shared/src/androidMain/kotlin/com/merkost/metronome/review/CurrentActivityProvider.kt`
- Create: `shared/src/androidMain/kotlin/com/merkost/metronome/review/AndroidInAppReviewRequester.kt`
- Create: `iosApp/iosApp/IosInAppReviewRequester.swift`
- Modify: `shared/src/androidMain/kotlin/com/merkost/metronome/di/AndroidModule.kt`
- Modify: `shared/src/iosMain/kotlin/com/merkost/metronome/InitKoin.kt`
- Modify: `iosApp/iosApp/iOSApp.swift`
- Modify: `iosApp/Metronome.xcodeproj/project.pbxproj`
- Modify: `gradle/libs.versions.toml`
- Modify: `shared/build.gradle.kts`

**Interfaces:**
- Produces: `InAppReviewRequester.requestReview(): Boolean` on both platforms.
- Consumes: Android's currently resumed `Activity` and iOS's foreground-active `UIWindowScene`.

- [ ] **Step 1: Add the shared interface**

Create `InAppReviewRequester.kt`:

```kotlin
package com.merkost.metronome.review

interface InAppReviewRequester {
    fun requestReview(): Boolean
}
```

- [ ] **Step 2: Add the Play Review dependency**

Add to `[versions]` in `gradle/libs.versions.toml`:

```toml
playReview = "2.0.2"
```

Add to `[libraries]`:

```toml
play-review = { module = "com.google.android.play:review", version.ref = "playReview" }
```

Add to `androidMain.dependencies` in `shared/build.gradle.kts`:

```kotlin
implementation(libs.play.review)
```

- [ ] **Step 3: Implement resumed-activity tracking on Android**

Create `CurrentActivityProvider.kt`:

```kotlin
package com.merkost.metronome.review

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

class CurrentActivityProvider(application: Application) : Application.ActivityLifecycleCallbacks {
    private var resumedActivity = WeakReference<Activity>(null)

    init {
        application.registerActivityLifecycleCallbacks(this)
    }

    fun current(): Activity? = resumedActivity.get()

    override fun onActivityResumed(activity: Activity) {
        resumedActivity = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        if (resumedActivity.get() === activity) resumedActivity.clear()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
```

Create `AndroidInAppReviewRequester.kt`:

```kotlin
package com.merkost.metronome.review

import com.google.android.play.core.review.ReviewManagerFactory

class AndroidInAppReviewRequester(
    private val activityProvider: CurrentActivityProvider,
) : InAppReviewRequester {
    override fun requestReview(): Boolean {
        val activity = activityProvider.current() ?: return false
        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow().addOnCompleteListener { request ->
            if (request.isSuccessful) {
                manager.launchReviewFlow(activity, request.result)
            }
        }
        return true
    }
}
```

Register both in `AndroidModule.kt`:

```kotlin
single { CurrentActivityProvider(androidContext() as android.app.Application) }
single<InAppReviewRequester> { AndroidInAppReviewRequester(get()) }
```

- [ ] **Step 4: Implement StoreKit review presentation in Swift**

Create `iosApp/iosApp/IosInAppReviewRequester.swift`:

```swift
import ComposeApp
import StoreKit
import UIKit

final class IosInAppReviewRequester: InAppReviewRequester {
    func requestReview() -> Bool {
        guard Thread.isMainThread else { return false }
        guard let scene = UIApplication.shared.connectedScenes
            .compactMap({ $0 as? UIWindowScene })
            .first(where: { $0.activationState == .foregroundActive }) else {
            return false
        }
        if #available(iOS 18.0, *) {
            AppStore.requestReview(in: scene)
        } else {
            SKStoreReviewController.requestReview(in: scene)
        }
        return true
    }
}
```

Change `InitKoin.kt` to accept and bind both Swift implementations:

```kotlin
fun initKoin(
    liveActivityController: LiveActivityController,
    inAppReviewRequester: InAppReviewRequester,
) {
    startKoin {
        modules(
            commonModule,
            iosModule,
            module {
                single { liveActivityController }
                single { inAppReviewRequester }
            }
        )
    }
    KoinPlatform.getKoin().get<LiveActivityObserver>().start()
}
```

Change `iOSApp.swift`:

```swift
InitKoinKt.doInitKoin(
    liveActivityController: MetronomeLiveActivityManager(),
    inAppReviewRequester: IosInAppReviewRequester()
)
```

- [ ] **Step 5: Add the Swift file to the Metronome Xcode target**

Run once after the file exists:

```bash
ruby -rxcodeproj -e '
project = Xcodeproj::Project.open("iosApp/Metronome.xcodeproj")
group = project.main_group["iosApp"]
path = "IosInAppReviewRequester.swift"
reference = group.files.find { |file| file.path == path } || group.new_file(path)
target = project.targets.find { |item| item.name == "Metronome" }
target.add_file_references([reference]) unless target.source_build_phase.files_references.include?(reference)
project.save
'
```

- [ ] **Step 6: Build both native requesters**

Run:

```bash
./gradlew :androidApp:assembleDebug :shared:linkDebugFrameworkIosSimulatorArm64
xcodebuild -project iosApp/Metronome.xcodeproj \
  -scheme Metronome \
  -sdk iphonesimulator \
  -configuration Debug \
  CODE_SIGNING_ALLOWED=NO \
  build
```

Expected: Gradle and Xcode builds complete successfully. No review sheet is expected during builds.

- [ ] **Step 7: Commit the platform requesters**

```bash
git add gradle/libs.versions.toml shared/build.gradle.kts \
        shared/src/commonMain/kotlin/com/merkost/metronome/review/InAppReviewRequester.kt \
        shared/src/androidMain/kotlin/com/merkost/metronome/review \
        shared/src/androidMain/kotlin/com/merkost/metronome/di/AndroidModule.kt \
        shared/src/iosMain/kotlin/com/merkost/metronome/InitKoin.kt \
        iosApp/iosApp/IosInAppReviewRequester.swift \
        iosApp/iosApp/iOSApp.swift \
        iosApp/Metronome.xcodeproj/project.pbxproj
git commit -m "feat: add native in-app review requesters"
```

---

### Task 3: Coordinator and playback-pause integration

**Files:**
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/review/ReviewPromptCoordinator.kt`
- Create: `shared/src/commonTest/kotlin/com/merkost/metronome/review/ReviewPromptCoordinatorTest.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/viewModels/MetronomeViewModel.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/di/CommonModule.kt`

**Interfaces:**
- Consumes: `ReviewPromptStore`, `InAppReviewRequester`, `AppVersionProvider`, and `ReviewPromptSnapshot`.
- Produces: `ReviewPromptCoordinator.requestIfEligible(snapshot): Boolean` and the qualified pause hook in `MetronomeViewModel`.

- [ ] **Step 1: Write failing coordinator tests**

Create `ReviewPromptCoordinatorTest.kt`:

```kotlin
package com.merkost.metronome.review

import com.merkost.metronome.platform.AppVersionInfo
import com.merkost.metronome.platform.AppVersionProvider
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

    private class FakeAppVersionProvider : AppVersionProvider {
        override fun getAppVersion(): AppVersionInfo = AppVersionInfo("1.3.0", 8L)
    }
}
```

Add the coroutines test dependency to `commonTest.dependencies` in `shared/build.gradle.kts`:

Add to `[libraries]` in `gradle/libs.versions.toml`:

```toml
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
```

Then add to `commonTest.dependencies`:

```kotlin
implementation(libs.kotlinx.coroutines.test)
```

- [ ] **Step 2: Run the coordinator test and verify red**

Run:

```bash
./gradlew :shared:testDebugUnitTest --tests 'com.merkost.metronome.review.ReviewPromptCoordinatorTest'
```

Expected: compilation fails because `ReviewPromptCoordinator` does not exist.

- [ ] **Step 3: Implement the coordinator**

Create `ReviewPromptCoordinator.kt`:

```kotlin
package com.merkost.metronome.review

import com.merkost.metronome.platform.AppVersionProvider
import com.merkost.metronome.platform.currentTimeMillis

class ReviewPromptCoordinator(
    private val store: ReviewPromptStore,
    private val requester: InAppReviewRequester,
    private val appVersionProvider: AppVersionProvider,
    private val nowMillis: () -> Long = ::currentTimeMillis,
) {
    suspend fun requestIfEligible(snapshot: ReviewPromptSnapshot): Boolean {
        val version = appVersionProvider.getAppVersion()?.versionName ?: return false
        val now = nowMillis()
        val record = store.read()
        if (!shouldRequestReview(snapshot, record, version, now)) return false
        if (!requester.requestReview()) return false
        store.markRequested(version, now)
        return true
    }
}
```

Register it in `CommonModule.kt` and pass it to the existing singleton ViewModel:

```kotlin
single { ReviewPromptCoordinator(get(), get(), get()) }
single { MetronomeViewModel(get(), get(), get()) }
```

- [ ] **Step 4: Integrate the qualified pause**

Add `reviewPromptCoordinator: ReviewPromptCoordinator` as the third `MetronomeViewModel` constructor parameter.

In the paused branch of `startTimerCoroutine`, keep the existing persistence first, then add the cancellable delay and eligibility call:

```kotlin
val elapsed = metronomeState.value.stopWatchState.elapsedTime
appDatastore.addTotalTime(elapsed - sessionBaseMillis)
sessionBaseMillis = elapsed
delay(REVIEW_PAUSE_DELAY_MILLIS)
reviewPromptCoordinator.requestIfEligible(
    ReviewPromptSnapshot(
        totalPracticeMillis = appDatastore.totalTime.first(),
        sessionPracticeMillis = elapsed,
        isPlaying = metronomeState.value.playing,
        hasActiveTimer = practiceTimerGoal.value != null,
        hasActiveTempoTrainer = gradualTempoConfig.value != null,
        hasActiveGapTrainer = gapTrainerConfig.value != null,
        isTimerSheetVisible = timerSheetVisible,
        isTempoSheetVisible = tempoSheetVisible,
        isOnboardingVisible = onboardingStep.value >= 0,
    )
)
```

Import the review types and constants. Because the enclosing playback collector uses `collectLatest`, resuming playback during the two-second delay cancels the pending request automatically.

- [ ] **Step 5: Run focused and full shared tests**

Run:

```bash
./gradlew :shared:testDebugUnitTest --tests 'com.merkost.metronome.review.*'
./gradlew :shared:testDebugUnitTest
```

Expected: all review tests and all existing shared tests pass.

- [ ] **Step 6: Commit the coordinator and pause trigger**

```bash
git add shared/build.gradle.kts \
        gradle/libs.versions.toml \
        shared/src/commonMain/kotlin/com/merkost/metronome/review/ReviewPromptCoordinator.kt \
        shared/src/commonTest/kotlin/com/merkost/metronome/review/ReviewPromptCoordinatorTest.kt \
        shared/src/commonMain/kotlin/com/merkost/metronome/viewModels/MetronomeViewModel.kt \
        shared/src/commonMain/kotlin/com/merkost/metronome/di/CommonModule.kt
git commit -m "feat: request reviews after qualified practice pauses"
```

---

### Task 4: Verification and native behaviour proof

**Files:**
- Verify only; no planned source changes.

**Interfaces:**
- Consumes: the complete shared policy and both native requesters.
- Produces: focused test, full test, build and manual native evidence.

- [ ] **Step 1: Run the automated gates**

```bash
./gradlew :shared:testDebugUnitTest
./gradlew :androidApp:assembleDebug
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
xcodebuild -project iosApp/Metronome.xcodeproj \
  -scheme Metronome \
  -sdk iphonesimulator \
  -configuration Debug \
  CODE_SIGNING_ALLOWED=NO \
  build
```

Expected: all four commands complete successfully. Report unit-test counts separately from Android, framework and Xcode build proof.

- [ ] **Step 2: Verify iOS debug behaviour**

1. Install a Debug build directly from Xcode, not TestFlight.
2. Complete onboarding.
3. Play for five minutes without an active timer or trainer.
4. Pause and remain paused.
5. Confirm the native App Store review sheet appears after approximately two seconds.
6. Resume within two seconds on a fresh install and confirm no sheet appears.
7. Repeat with a countdown timer configured and confirm no sheet appears.
8. Repeat with Tempo Trainer and Gap Trainer active and confirm no sheet appears.
9. Confirm the Settings **Rate the App** action still opens App Store review entry.

Apple's native prompt always appears in a development build when eligible for testing and has no effect in TestFlight. The shipping system may suppress it according to StoreKit policy.

- [ ] **Step 3: Verify Android internal-test behaviour**

1. Upload the build to a Google Play internal test track.
2. Install it from Google Play with an eligible tester account that has not reviewed the app.
3. Complete onboarding.
4. Play for five minutes without an active timer or trainer.
5. Pause and remain paused.
6. Confirm the native Google Play review card appears without leaving the app.
7. Resume within two seconds on a reset test install and confirm no card appears.
8. Repeat with a countdown timer configured and confirm no card appears.
9. Repeat with Tempo Trainer and Gap Trainer active and confirm no card appears.
10. Confirm the Settings **Rate the App** action still opens the Play Store listing.

- [ ] **Step 4: Audit persisted throttling**

1. Complete one accepted request attempt.
2. Repeat the five-minute pause in the same app version.
3. Confirm no second request is attempted.
4. Change only the test app version and keep the request timestamp inside 180 days.
5. Confirm no request is attempted.
6. Seed the stored timestamp to exactly 180 days earlier under a new version.
7. Confirm the next qualified pause requests once.

- [ ] **Step 5: Commit any test-fixture-only adjustments, if the verification required them**

Stage only the named review feature files and inspect the diff before committing:

```bash
git diff --check
git status --short
git diff -- shared/src iosApp gradle/libs.versions.toml shared/build.gradle.kts
```

Expected: no whitespace errors, no unrelated files staged and no production comments added.
