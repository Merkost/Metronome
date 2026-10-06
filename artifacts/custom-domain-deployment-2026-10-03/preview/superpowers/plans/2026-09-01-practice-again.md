# Practice Again Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let musicians restart the most recently explicitly completed saved Practice Set from the existing practice-tools sheet with one reliable action.

**Architecture:** Persist completion recency on `PracticeSet`, propagate an explicit Completed or Replaced finish reason through `PracticeSessionController`, and derive the recent set from repository state. `MetronomeViewModel` coordinates completion writes and starts; Compose adds one stateless accessible row inside the existing sheet.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform Material 3, DataStore Preferences, Koin, StateFlow and SharedFlow, Compose Resources, kotlin.test.

**Spec:** `docs/superpowers/specs/2026-09-01-precision-practice-again-design.md`

## Global Constraints

- Only an explicit completed finish updates Practice Again; replacement and recovery discard do not.
- A failed session clear retains the original finish reason through retry.
- Practice Again always resolves the current saved set and current presets.
- Hide Practice Again during an active Practice Session; Resume remains authoritative.
- Keep the action in the existing practice-tools sheet, not on the main instrument.
- Add no history, scoring, celebration, auto-advance, cloud data, bottom navigation, or new destination.
- Use Material theme roles, existing dimensions, Lucide icons, Compose resources, 48dp targets, and existing spring interactions.
- Write no code comments.
- Preserve unrelated worktree changes. Source commits are conditional on an isolated clean tree; otherwise record a targeted diff checkpoint instead.

---

## File map

- Modify `practiceSets/PracticeSet.kt`: add completion timestamp and recent selection helper.
- Modify `practiceSets/PracticeSetCodec.kt`: append tolerant completion field.
- Modify `practiceSets/PracticeSetRepository.kt` and `DataStorePracticeSetRepository.kt`: add atomic `markCompleted`.
- Modify Practice Set codec and repository tests plus sample constructors.
- Modify `practiceSets/PracticeSessionController.kt`: explicit finish reason and durable clear intent.
- Modify `MetronomeViewModel.kt`: derive recent set, record completed finish, expose failure events, and mark replacement finishes.
- Modify `PracticeSetComponents.kt`: stateless `PracticeAgainRow` with previews.
- Modify `TempoTrainerSheet.kt` and `MainScreen.kt`: render and start Practice Again through existing conflicts and errors.
- Modify `strings.xml` and `docs/ROADMAP.md`: accessible copy and verified feature status.

### Task 1: Completion metadata and tolerant codec

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/practiceSets/PracticeSet.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/practiceSets/PracticeSetCodec.kt`
- Test: `shared/src/commonTest/kotlin/com/merkost/metronome/practiceSets/PracticeSetCodecTest.kt`
- Modify: every `PracticeSet(...)` constructor reported by `rg -n 'PracticeSet\(' shared/src -g '*.kt'`

**Interfaces:**
- Consumes: existing `PracticeSet` v1 records.
- Produces: `lastCompletedAtEpochMillis: Long?` and `List<PracticeSet>.mostRecentlyCompleted()`.

- [ ] **Step 1: Write failing codec and selection tests**

```kotlin
@Test
fun roundTripsCompletionTimestampAndReadsOlderRecordsAsIncomplete() {
    val completed = practiceSet.copy(lastCompletedAtEpochMillis = 40L)
    assertEquals(listOf(completed), PracticeSetCodec.decode(PracticeSetCodec.encode(listOf(completed))))

    val oldRecord = PracticeSetCodec.encode(listOf(practiceSet)).substringBeforeLast('\t')
    assertNull(PracticeSetCodec.decode(oldRecord).single().lastCompletedAtEpochMillis)
}

@Test
fun selectsTheMostRecentlyCompletedExistingSet() {
    val sets = listOf(
        practiceSet.copy(id = "older", lastCompletedAtEpochMillis = 10L),
        practiceSet.copy(id = "recent", lastCompletedAtEpochMillis = 20L),
        practiceSet.copy(id = "never", lastCompletedAtEpochMillis = null),
    )
    assertEquals("recent", sets.mostRecentlyCompleted()!!.id)
}
```

- [ ] **Step 2: Run the codec test and verify RED**

Run: `./gradlew :shared:testDebugUnitTest --tests '*PracticeSetCodecTest' --console=plain`

Expected: compilation fails because completion metadata does not exist.

- [ ] **Step 3: Add completion metadata and selection**

```kotlin
data class PracticeSet(
    val id: String,
    val name: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val lastStartedAtEpochMillis: Long?,
    val lastCompletedAtEpochMillis: Long?,
    val sortPosition: Int,
    val steps: List<PracticeSetStep>,
)

fun List<PracticeSet>.mostRecentlyCompleted(): PracticeSet? =
    filter { it.lastCompletedAtEpochMillis != null }
        .maxWithOrNull(compareBy<PracticeSet> { it.lastCompletedAtEpochMillis }.thenBy { it.id })
```

- [ ] **Step 4: Append the optional codec field**

Keep `FIELD_COUNT = 8` so old records remain valid. Append `lastCompletedAtEpochMillis` after the encoded steps. Decode with:

```kotlin
lastCompletedAtEpochMillis = fields.getOrNull(8)
    ?.takeIf(String::isNotEmpty)
    ?.toLong()
    ?.coerceAtLeast(0L)
```

Do not change the meaning or order of the original eight fields.

- [ ] **Step 5: Update all constructors with `lastCompletedAtEpochMillis = null` and run tests**

Run: `./gradlew :shared:testDebugUnitTest --tests '*PracticeSetCodecTest' --console=plain`

Expected: all codec, validation, and recency tests pass.

- [ ] **Step 6: Record the targeted checkpoint**

Run: `git diff --check -- shared/src/commonMain/kotlin/com/merkost/metronome/practiceSets/PracticeSet.kt shared/src/commonMain/kotlin/com/merkost/metronome/practiceSets/PracticeSetCodec.kt shared/src/commonTest`

Expected: no output. Commit only in an isolated clean tree with `feat: track completed practice sets`.

### Task 2: Atomic completion persistence

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/practiceSets/PracticeSetRepository.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/practiceSets/DataStorePracticeSetRepository.kt`
- Test: `shared/src/commonTest/kotlin/com/merkost/metronome/practiceSets/DataStorePracticeSetRepositoryTest.kt`
- Modify: fake `PracticeSetRepository` implementations under `shared/src/commonTest`

**Interfaces:**
- Consumes: `PracticeSet.lastCompletedAtEpochMillis`.
- Produces: `suspend fun markCompleted(id: String): PracticeSetMutationResult`.

- [ ] **Step 1: Write failing persistence tests**

```kotlin
@Test
fun marksOnlyTheRequestedSetCompletedAndSelectsItAsMostRecent() = runTest {
    var now = 100L
    val repository = repository(ids = listOf("one", "two"), nowMillis = { now++ })
    repository.create(draft("One", "preset-a"))
    repository.create(draft("Two", "preset-b"))

    assertIs<PracticeSetMutationResult.Success>(repository.markCompleted("one"))

    assertEquals(102L, repository.sets.first().single { it.id == "one" }.lastCompletedAtEpochMillis)
    assertEquals("one", repository.sets.first().mostRecentlyCompleted()!!.id)
    assertNull(repository.sets.first().single { it.id == "two" }.lastCompletedAtEpochMillis)
}
```

Add a storage-failure case using `InMemoryPreferencesDataStore.failUpdates = true` and assert that the prior completion timestamp remains unchanged.

- [ ] **Step 2: Run the repository test and verify RED**

Run: `./gradlew :shared:testDebugUnitTest --tests '*DataStorePracticeSetRepositoryTest' --console=plain`

Expected: compilation fails because `markCompleted` is missing.

- [ ] **Step 3: Implement the atomic mutation**

```kotlin
override suspend fun markCompleted(id: String): PracticeSetMutationResult = mutate { current ->
    val existing = current.firstOrNull { it.id == id }
        ?: return@mutate current to PracticeSetMutationResult.NotFound
    val now = nowMillis()
    val updated = existing.copy(
        updatedAtEpochMillis = now,
        lastCompletedAtEpochMillis = now,
    )
    current.map { if (it.id == id) updated else it } to PracticeSetMutationResult.Success(updated)
}
```

- [ ] **Step 4: Update every test fake explicitly**

Each fake returns `PracticeSetMutationResult.Success` for `markCompleted` unless its test needs failure. Do not add default interface implementations.

- [ ] **Step 5: Run repository and ViewModel tests**

Run: `./gradlew :shared:testDebugUnitTest --tests '*DataStorePracticeSetRepositoryTest' --tests 'com.merkost.metronome.viewModels.*' --console=plain`

Expected: all selected tests pass.

- [ ] **Step 6: Record the targeted checkpoint**

Run: `git diff --check -- shared/src/commonMain/kotlin/com/merkost/metronome/practiceSets shared/src/commonTest/kotlin/com/merkost/metronome`

Expected: no output. Commit only in an isolated clean tree with `feat: persist practice completion recency`.

### Task 3: Explicit completed and replaced finishes

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/practiceSets/PracticeSessionController.kt`
- Test: `shared/src/commonTest/kotlin/com/merkost/metronome/practiceSets/PracticeSessionControllerTest.kt`

**Interfaces:**
- Consumes: active session `sourceSetId` and durable clear outcome.
- Produces: `PracticeSessionFinishReason` and `PracticeSessionCommand.SessionFinished(sourceSetId, reason)`.

- [ ] **Step 1: Write failing finish-reason tests**

```kotlin
@Test
fun completedFinishEmitsItsSourceSetAndReason() = runTest {
    val controller = recoveredController(session())
    controller.finish(PracticeSessionFinishReason.Completed)
    assertEquals(PracticeSessionCommand.PausePlayback, controller.commands.receive())
    assertEquals(
        PracticeSessionCommand.SessionFinished("set-1", PracticeSessionFinishReason.Completed),
        controller.commands.receive(),
    )
}

@Test
fun failedClearRetryRetainsReplacementReason() = runTest {
    val repository = FakeSessionRepository(session()).apply { failClears = true }
    val controller = controller(repository)
    controller.recover()
    controller.finish(PracticeSessionFinishReason.Replaced)
    controller.commands.receive()
    repository.failClears = false
    controller.retryPersistence()
    assertEquals(
        PracticeSessionCommand.SessionFinished("set-1", PracticeSessionFinishReason.Replaced),
        controller.commands.receive(),
    )
}
```

- [ ] **Step 2: Run the controller test and verify RED**

Run: `./gradlew :shared:testDebugUnitTest --tests '*PracticeSessionControllerTest' --console=plain`

Expected: compilation fails because finish reasons and command payloads do not exist.

- [ ] **Step 3: Implement finish reason and pending clear intent**

```kotlin
enum class PracticeSessionFinishReason { Completed, Replaced }

sealed interface PracticeSessionCommand {
    data class SessionFinished(
        val sourceSetId: String,
        val reason: PracticeSessionFinishReason,
    ) : PracticeSessionCommand
}
```

Replace `PendingPersistenceAction.FinishClear` with a value carrying `sourceSetId` and `reason`. Both immediate clear success and retry success emit the identical payload. `DiscardClear` remains distinct and emits no finish command.

- [ ] **Step 4: Update all finish call sites explicitly**

`finishPracticeSession()` passes `Completed`. Starting a different set while one is active and every `replacePracticeSession` path pass `Replaced`. Tests must not rely on a default reason.

- [ ] **Step 5: Run controller tests and compile common callers**

Run: `./gradlew :shared:testDebugUnitTest --tests '*PracticeSessionControllerTest' :shared:compileDebugKotlinAndroid --console=plain`

Expected: all controller tests pass and every call site handles the new command.

- [ ] **Step 6: Record the targeted checkpoint**

Run: `git diff --check -- shared/src/commonMain/kotlin/com/merkost/metronome/practiceSets/PracticeSessionController.kt shared/src/commonMain/kotlin/com/merkost/metronome/viewModels/MetronomeViewModel.kt shared/src/commonTest/kotlin/com/merkost/metronome/practiceSets/PracticeSessionControllerTest.kt`

Expected: no output. Commit only in an isolated clean tree with `feat: distinguish completed practice sessions`.

### Task 4: Recent-set presentation state and completion orchestration

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/viewModels/MetronomeViewModel.kt`
- Test: create `shared/src/commonTest/kotlin/com/merkost/metronome/practiceSets/PracticeAgainPolicyTest.kt`

**Interfaces:**
- Consumes: `PracticeSetRepository.sets`, `mostRecentlyCompleted()`, and session-finished commands.
- Produces: `StateFlow<PracticeSet?> recentPracticeSet` and `SharedFlow<PracticeCompletionEvent>`.

- [ ] **Step 1: Write failing pure policy tests**

```kotlin
@Test
fun activeSessionSuppressesPracticeAgain() {
    assertNull(practiceAgainSet(listOf(completedSet()), activeSourceSetId = "active"))
}

@Test
fun noActiveSessionUsesMostRecentCompletion() {
    assertEquals(
        "recent",
        practiceAgainSet(
            listOf(completedSet("older", 10L), completedSet("recent", 20L)),
            activeSourceSetId = null,
        )!!.id,
    )
}

@Test
fun recentSelectionUsesTheCurrentRenamedSetRegardlessOfSortPosition() {
    val renamed = completedSet("recent", 20L).copy(name = "Current name", sortPosition = 99)

    assertEquals(
        renamed,
        practiceAgainSet(
            listOf(completedSet("older", 10L).copy(sortPosition = 0), renamed),
            activeSourceSetId = null,
        ),
    )
}
```

- [ ] **Step 2: Run policy tests and verify RED**

Run: `./gradlew :shared:testDebugUnitTest --tests '*PracticeAgainPolicyTest' --console=plain`

Expected: compilation fails because `practiceAgainSet` does not exist.

- [ ] **Step 3: Implement the pure selector and ViewModel state**

```kotlin
fun practiceAgainSet(
    sets: List<PracticeSet>,
    activeSourceSetId: String?,
): PracticeSet? = if (activeSourceSetId == null) sets.mostRecentlyCompleted() else null
```

Combine repository sets with `practiceSessionState` and expose an eagerly started `StateFlow<PracticeSet?>`. Use an empty nullable flow when the optional repository dependency is unavailable.

- [ ] **Step 4: Record completion only for `Completed`**

In `handlePracticeSessionCommand`, call `practiceSetRepository.markCompleted(sourceSetId)` only for `Completed`. Emit `PracticeCompletionEvent.StorageFailure` for `NotFound`, `Conflict`, `Invalid`, `LimitReached`, or `StorageFailure`. Always invoke `pendingAfterSessionFinish` after completion handling so confirmed replacements cannot deadlock.

- [ ] **Step 5: Run policy, controller, repository, and ViewModel tests**

Run: `./gradlew :shared:testDebugUnitTest --tests '*PracticeAgainPolicyTest' --tests '*PracticeSessionControllerTest' --tests '*DataStorePracticeSetRepositoryTest' --tests 'com.merkost.metronome.viewModels.*' --console=plain`

Expected: all selected tests pass.

- [ ] **Step 6: Record the targeted checkpoint**

Run: `git diff --check -- shared/src/commonMain/kotlin/com/merkost/metronome/viewModels/MetronomeViewModel.kt shared/src/commonMain/kotlin/com/merkost/metronome/practiceSets shared/src/commonTest`

Expected: no output. Commit only in an isolated clean tree with `feat: expose recent completed practice set`.

### Task 5: Accessible Practice Again row

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/components/PracticeSetComponents.kt`
- Modify: `shared/src/commonMain/composeResources/values/strings.xml`

**Interfaces:**
- Consumes: one `PracticeSet`, `onClick: () -> Unit`.
- Produces: `PracticeAgainRow(practiceSet, onClick, modifier)` and normal plus large-text previews.

- [ ] **Step 1: Add Compose resources**

```xml
<string name="practice_again">Practice again</string>
<string name="practice_again_description">Practice %1$s again, %2$d steps</string>
<string name="practice_completion_storage_failed">Session finished, but recent practice could not be updated.</string>
```

- [ ] **Step 2: Implement the stateless row**

Use `Surface` with `Modifier.minimumInteractiveComponentSize().clickable(role = Role.Button)`, existing `cornerRadiusLarge`, theme surface roles, a Lucide replay or play icon, and a text column. Render `Practice again` as the label, current set name as the title with `maxLines = 2`, and the existing step-count resource as supporting text. Merge semantics into the row and use the formatted description once.

- [ ] **Step 3: Add normal and maximum-font previews**

```kotlin
@Preview
@Composable
private fun PracticeAgainRowPreview() {
    MaterialTheme { PracticeAgainRow(previewSet(), onClick = {}) }
}

@Preview(fontScale = 2f)
@Composable
private fun PracticeAgainRowLargeTextPreview() {
    MaterialTheme { PracticeAgainRow(previewSet(), onClick = {}) }
}
```

- [ ] **Step 4: Compile Android and iOS Compose sources**

Run: `./gradlew :shared:compileDebugKotlinAndroid :shared:compileKotlinIosSimulatorArm64 --console=plain`

Expected: both targets compile with no missing resources.

- [ ] **Step 5: Record the targeted checkpoint**

Run: `git diff --check -- shared/src/commonMain/kotlin/com/merkost/metronome/components/PracticeSetComponents.kt shared/src/commonMain/composeResources/values/strings.xml`

Expected: no output. Commit only in an isolated clean tree with `feat: add practice again action`.

### Task 6: Sheet and start-flow integration

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/screens/TempoTrainerSheet.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/screens/MainScreen.kt`

**Interfaces:**
- Consumes: `recentPracticeSet`, `practiceSessionStartResults`, and `practiceCompletionEvents` from `MetronomeViewModel`.
- Produces: one-tap Practice Again with existing replacement dialog and specific snackbar failures.

- [ ] **Step 1: Extend the sheet contract**

Add `recentPracticeSet: PracticeSet?` and `onPracticeAgain: (PracticeSet) -> Unit` beside the existing Practice Sets management parameters. Immediately before the management action, render `PracticeAgainRow` only when the recent value is non-null.

- [ ] **Step 2: Wire the Main screen action**

On click, close the sheet. If a timer, gradual trainer, or gap trainer is active, set `pendingStructuredAction = { viewModel.startPracticeSet(set) }`; otherwise call `startPracticeSet(set)` immediately. Active sessions cannot reach this callback because the recent selector returns null.

- [ ] **Step 3: Surface start and completion results**

Collect `practiceSessionStartResults` on Main. `Started` needs no navigation because Main is already visible. Map MissingPreset, InvalidSet, and PersistenceFailed to the same resources used by `PracticeSetsScreen`. Collect `practiceCompletionEvents` and show `practice_completion_storage_failed` for storage failure.

- [ ] **Step 4: Update every preview and call site**

Supply a realistic completed preview set and no-op callback in `TempoTrainerSheet` previews. Do not introduce a new destination or duplicate Practice Again on the main instrument.

- [ ] **Step 5: Run shared tests and both platform compilations**

Run: `./gradlew :shared:testDebugUnitTest :shared:compileDebugKotlinAndroid :shared:compileKotlinIosSimulatorArm64 --console=plain`

Expected: zero test failures and both targets compile.

- [ ] **Step 6: Record the targeted checkpoint**

Run: `git diff --check -- shared/src/commonMain/kotlin/com/merkost/metronome/screens shared/src/commonMain/kotlin/com/merkost/metronome/components/PracticeSetComponents.kt shared/src/commonMain/composeResources/values/strings.xml`

Expected: no output. Commit only in an isolated clean tree with `feat: integrate practice again flow`.

### Task 7: Final verification and roadmap

**Files:**
- Modify: `docs/ROADMAP.md`
- Create: `.impeccable/review/practice-again-ios.png`
- Create: `.impeccable/review/practice-again-accessibility-ios.png`

**Interfaces:**
- Consumes: all feature behavior and build evidence.
- Produces: truthful roadmap status, runtime captures, and final acceptance report.

- [ ] **Step 1: Run the full Gradle gate**

Run: `git diff --check && ./gradlew :shared:testDebugUnitTest :androidApp:assembleDebug :shared:linkDebugFrameworkIosSimulatorArm64 --console=plain`

Expected: zero test failures, Android APK assembled, iOS simulator framework linked.

- [ ] **Step 2: Run the full Xcode simulator build**

Run: `xcodebuild -project iosApp/Metronome.xcodeproj -scheme Metronome -configuration Debug -destination 'platform=iOS Simulator,id=3FFE81C7-F02A-4247-B9FA-88C71ABB1129' -derivedDataPath /tmp/metronome-derived-data build`

Expected: `** BUILD SUCCEEDED **`.

- [ ] **Step 3: Inspect normal and maximum Dynamic Type**

Install and launch the built app on the booted iPhone simulator. Capture the practice-tools sheet with a seeded completed set at normal and maximum content sizes. Verify the row remains reachable, its name is bounded to two lines, its complete target is at least 48dp, the main tempo remains centered, and Settings remains present.

- [ ] **Step 4: Update the roadmap with verified status**

Move `Practice again` from Next to the implemented structured-practice foundation only after automated and simulator gates pass. Keep physical-device timing, audio route, VoiceOver, TalkBack, and live gesture checks verification-pending unless actually run.

- [ ] **Step 5: Run the final independent review and diff audit**

Review completion semantics, replacement behavior, failure states, accessibility, resource use, Navigation 3 invariants, and screenshots. Run `git diff --check`, confirm no Navigation 2 references, and report the exact test count.

- [ ] **Step 6: Record the final milestone**

Run: `git status --short` and `git diff --stat`.

Expected: all requested files visible, unrelated dirt preserved, and no broad staging. Commit only in an isolated clean tree with `feat: add practice again`.
