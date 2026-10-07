# Practice Sets Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build durable Practice Sets from existing presets and provide a manual, recoverable, boundary-safe practice-session runner without displacing the main metronome.

**Architecture:** Add focused set and session models, versioned codecs, DataStore repositories, and a pure `PracticeSessionController`. `PracticeSetsViewModel` owns the library/editor; `MetronomeViewModel` remains the playback boundary and acknowledges safe preset application. Compose adds a full-screen library/editor plus one compact main-screen session strip and expanded controls sheet.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform Material 3, Kotlin coroutines and StateFlow, DataStore Preferences, Koin, Compose Resources, kotlin.test, kotlinx-coroutines-test.

**Spec:** `docs/superpowers/specs/2026-09-01-practice-sets-design.md`

## Global Constraints

- Preserve BPM, beat state, and playback as the dominant main-screen hierarchy; do not add bottom navigation or a dashboard.
- Keep all set and session behavior offline, local, account-free, ad-free, and subscription-free.
- Support at most 30 sets, 20 steps per set, 80-character names, 1–120 minute targets, and 1–999 bar targets.
- Targets never auto-advance or stop playback.
- Saved sets reference live preset identifiers; active sessions resolve immutable preset snapshots.
- Block deletion of presets referenced by saved sets and block editing or deletion of an active source set.
- Restored sessions are always paused and never start audio automatically.
- Use interfaces plus Koin for persistence; do not add platform service `expect`/`actual` declarations.
- Use Compose resources, Material theme roles, Lucide icons, existing dimensions, 48dp targets, and existing spring motion.
- Write no code comments.
- Preserve unrelated worktree changes and stage only files owned by each task.

---

## File map

### Set domain and persistence

- Create `practiceSets/PracticeSet.kt`: immutable set, step, targets, drafts, validation, and limits.
- Create `practiceSets/PracticeSetCodec.kt`: tolerant versioned set encoding.
- Create `practiceSets/PracticeSetRepository.kt`: repository contract and mutation results.
- Create `practiceSets/DataStorePracticeSetRepository.kt`: atomic DataStore collection operations and preset-reference lookup.
- Create matching common tests under `commonTest/practiceSets`.

### Session domain and persistence

- Create `practiceSets/PracticeSession.kt`: resolved session, step progress, and immutable UI-facing state.
- Create `practiceSets/PracticeSessionCodec.kt`: active-session snapshot encoding.
- Create `practiceSets/PracticeSessionRepository.kt` and `DataStorePracticeSessionRepository.kt`: observe, checkpoint, and clear one session.
- Create `practiceSets/PracticeSessionController.kt`: pure session transitions and playback commands.
- Create controller, codec, and repository common tests.

### Presentation and integration

- Create `viewModels/PracticeSetsViewModel.kt`: library/editor state and user events.
- Modify `PracticePresetsViewModel.kt`: prevent deletion of referenced presets.
- Modify `MetronomeViewModel.kt`: execute session commands, forward progress and bars, recover paused sessions, and block review prompts.
- Modify `CommonModule.kt`: repository, controller, and ViewModel registrations.

### Compose

- Create `PracticeSetComponents.kt`: set rows, step rows, target editor, session strip, and previews.
- Create `PracticeSetsScreen.kt`: library and editor flows.
- Create `PracticeSessionSheet.kt`: expanded runner controls.
- Modify `AppNavigation.kt`, `TempoTrainerSheet.kt`, and `MainScreen.kt`: entry, navigation, runner strip, dialogs, and session sheet.
- Modify `strings.xml`: all new copy and formatted labels.

---

### Task 1: Practice Set model and codec

**Files:**
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/practiceSets/PracticeSet.kt`
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/practiceSets/PracticeSetCodec.kt`
- Test: `shared/src/commonTest/kotlin/com/merkost/metronome/practiceSets/PracticeSetCodecTest.kt`

**Interfaces:**
- Consumes: existing `PracticePreset.id` values.
- Produces: `PracticeSet`, `PracticeSetStep`, `PracticeStepTarget`, `PracticeSetDraft`, `PracticeSetValidationError`, and `PracticeSetCodec`.

- [ ] **Step 1: Write the failing codec and validation tests**

```kotlin
@Test
fun roundTripsSetStepsAndEveryTargetType() {
    val set = PracticeSet(
        id = "set-1",
        name = "Daily foundations",
        createdAtEpochMillis = 10L,
        updatedAtEpochMillis = 20L,
        lastStartedAtEpochMillis = 30L,
        sortPosition = 2,
        steps = listOf(
            PracticeSetStep("step-1", "preset-a", PracticeStepTarget.None),
            PracticeSetStep("step-2", "preset-b", PracticeStepTarget.Duration(12)),
            PracticeSetStep("step-3", "preset-c", PracticeStepTarget.Bars(24)),
        ),
    )
    assertEquals(listOf(set), PracticeSetCodec.decode(PracticeSetCodec.encode(listOf(set))))
}

@Test
fun rejectsBlankEmptyAndOutOfRangeDrafts() {
    assertEquals(PracticeSetValidationError.EmptyName, draft(name = " ").validationError)
    assertEquals(PracticeSetValidationError.EmptySteps, draft(steps = emptyList()).validationError)
    assertEquals(
        PracticeSetValidationError.InvalidTarget,
        draft(target = PracticeStepTarget.Duration(121)).validationError,
    )
}
```

- [ ] **Step 2: Run the test and verify RED**

Run: `./gradlew :shared:testDebugUnitTest --tests '*PracticeSetCodecTest'`

Expected: compilation fails because the set model and codec do not exist.

- [ ] **Step 3: Implement the immutable model and validation**

```kotlin
sealed interface PracticeStepTarget {
    data object None : PracticeStepTarget
    data class Duration(val minutes: Int) : PracticeStepTarget
    data class Bars(val count: Int) : PracticeStepTarget
}

data class PracticeSetStep(
    val id: String,
    val presetId: String,
    val target: PracticeStepTarget,
)

data class PracticeSet(
    val id: String,
    val name: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val lastStartedAtEpochMillis: Long?,
    val sortPosition: Int,
    val steps: List<PracticeSetStep>,
) {
    companion object {
        const val MAX_SETS = 30
        const val MAX_STEPS = 20
        const val MAX_NAME_LENGTH = 80
        const val MAX_DURATION_MINUTES = 120
        const val MAX_BARS = 999
    }
}
```

Normalize trimmed names and copied step lists. Require unique non-blank step identifiers, non-blank preset identifiers, valid target ranges, and one to 20 steps.

- [ ] **Step 4: Implement and verify the `v1` codec**

Encode each set as one line with escaped scalar fields and an escaped nested step payload. Encode targets as `none`, `duration:<minutes>`, or `bars:<count>`. Decode records independently, ignore additive trailing fields, and skip only invalid records.

Run: `./gradlew :shared:testDebugUnitTest --tests '*PracticeSetCodecTest'`

Expected: all model and codec tests pass.

### Task 2: Practice Set DataStore repository

**Files:**
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/practiceSets/PracticeSetRepository.kt`
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/practiceSets/DataStorePracticeSetRepository.kt`
- Test: `shared/src/commonTest/kotlin/com/merkost/metronome/practiceSets/DataStorePracticeSetRepositoryTest.kt`
- Reuse: `shared/src/commonTest/kotlin/com/merkost/metronome/presets/InMemoryPreferencesDataStore.kt`

**Interfaces:**
- Consumes: `PracticeSet`, `PracticeSetDraft`, `PracticeSetCodec`.
- Produces: `PracticeSetRepository.sets`, `create`, `update`, `delete`, `reorder`, `markStarted`, and `setsReferencingPreset`.

- [ ] **Step 1: Write failing repository behavior tests**

```kotlin
@Test
fun createsUpdatesReordersAndFindsPresetReferences() = runTest {
    val repository = repository(ids = listOf("set-1", "step-1", "set-2", "step-2"))
    assertIs<PracticeSetMutationResult.Success>(repository.create(draft("Warmup", "preset-a")))
    assertIs<PracticeSetMutationResult.Success>(repository.create(draft("Repertoire", "preset-b")))
    assertEquals(listOf("set-2", "set-1"), ids(repository.reorder(listOf("set-2", "set-1"))))
    assertEquals(listOf("Warmup"), repository.setsReferencingPreset("preset-a").map { it.name })
}

@Test
fun enforcesSetAndStepLimitsWithoutReplacingStoredData() = runTest {
    val repository = repository()
    repeat(PracticeSet.MAX_SETS) { repository.create(draft("Set $it", "preset-$it")) }
    assertEquals(PracticeSetMutationResult.LimitReached, repository.create(draft("Overflow", "preset-x")))
    assertEquals(PracticeSet.MAX_SETS, repository.sets.first().size)
}
```

- [ ] **Step 2: Run the repository test and verify RED**

Run: `./gradlew :shared:testDebugUnitTest --tests '*DataStorePracticeSetRepositoryTest'`

Expected: compilation fails because the repository contract does not exist.

- [ ] **Step 3: Implement the contract and atomic DataStore operations**

```kotlin
interface PracticeSetRepository {
    val sets: Flow<List<PracticeSet>>
    suspend fun create(draft: PracticeSetDraft): PracticeSetMutationResult
    suspend fun update(id: String, expectedUpdatedAtEpochMillis: Long, draft: PracticeSetDraft): PracticeSetMutationResult
    suspend fun delete(id: String): PracticeSetMutationResult
    suspend fun reorder(orderedIds: List<String>): PracticeSetMutationResult
    suspend fun markStarted(id: String): PracticeSetMutationResult
    suspend fun setsReferencingPreset(presetId: String): List<PracticeSet>
}

sealed interface PracticeSetMutationResult {
    data class Success(val set: PracticeSet? = null) : PracticeSetMutationResult
    data object LimitReached : PracticeSetMutationResult
    data object NotFound : PracticeSetMutationResult
    data object Conflict : PracticeSetMutationResult
    data class Invalid(val error: PracticeSetValidationError) : PracticeSetMutationResult
    data class StorageFailure(val message: String) : PracticeSetMutationResult
}
```

Use one `PRACTICE_SETS` string preference. Decode and mutate inside a single `dataStore.edit`, normalize sort positions, compare `expectedUpdatedAtEpochMillis` before updates, and leave stored data unchanged on validation or exceptions.

- [ ] **Step 4: Run focused repository tests and verify GREEN**

Run: `./gradlew :shared:testDebugUnitTest --tests '*PracticeSetCodecTest' --tests '*DataStorePracticeSetRepositoryTest'`

Expected: all set persistence tests pass.

### Task 3: Active session model, codec, and repository

**Files:**
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/practiceSets/PracticeSession.kt`
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/practiceSets/PracticeSessionCodec.kt`
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/practiceSets/PracticeSessionRepository.kt`
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/practiceSets/DataStorePracticeSessionRepository.kt`
- Test: `shared/src/commonTest/kotlin/com/merkost/metronome/practiceSets/PracticeSessionPersistenceTest.kt`

**Interfaces:**
- Consumes: immutable `PracticePreset` snapshots and set targets.
- Produces: `ActivePracticeSession`, `ResolvedPracticeStep`, `PracticeSessionCodec`, and a one-session repository.

- [ ] **Step 1: Write failing session persistence tests**

```kotlin
@Test
fun roundTripsResolvedPresetSnapshotsAndRestoresPaused() = runTest {
    val running = session(playbackIntent = PracticePlaybackIntent.Running)
    val decoded = PracticeSessionCodec.decode(PracticeSessionCodec.encode(running))
    assertEquals(running.copy(playbackIntent = PracticePlaybackIntent.Paused, pendingStepIndex = null), decoded)
}

@Test
fun repositorySavesObservesAndClearsOneSession() = runTest {
    val repository = DataStorePracticeSessionRepository(InMemoryPreferencesDataStore())
    repository.save(session())
    assertNotNull(repository.session.first())
    repository.clear()
    assertNull(repository.session.first())
}
```

- [ ] **Step 2: Run the test and verify RED**

Run: `./gradlew :shared:testDebugUnitTest --tests '*PracticeSessionPersistenceTest'`

Expected: compilation fails because session types do not exist.

- [ ] **Step 3: Implement session types and tolerant codec**

```kotlin
enum class PracticePlaybackIntent { Paused, Running }

data class ResolvedPracticeStep(
    val stepId: String,
    val preset: PracticePreset,
    val target: PracticeStepTarget,
)

data class ActivePracticeSession(
    val id: String,
    val sourceSetId: String,
    val setName: String,
    val steps: List<ResolvedPracticeStep>,
    val currentStepIndex: Int,
    val pendingStepIndex: Int?,
    val elapsedMillis: Long,
    val completedBars: Int,
    val playbackIntent: PracticePlaybackIntent,
    val targetReached: Boolean,
    val currentStepEdited: Boolean,
    val startedAtEpochMillis: Long,
    val lastCheckpointAtEpochMillis: Long,
)
```

Use an explicit `v1` format. On decode, validate indexes and snapshots, clear pending navigation, and force `Paused`. Invalid snapshots decode to null.

- [ ] **Step 4: Implement `PracticeSessionRepository` and verify GREEN**

```kotlin
interface PracticeSessionRepository {
    val session: Flow<ActivePracticeSession?>
    suspend fun save(session: ActivePracticeSession): Boolean
    suspend fun clear(): Boolean
}
```

Run: `./gradlew :shared:testDebugUnitTest --tests '*PracticeSessionPersistenceTest'`

Expected: all session codec and DataStore tests pass.

### Task 4: Pure Practice Session controller

**Files:**
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/practiceSets/PracticeSessionController.kt`
- Test: `shared/src/commonTest/kotlin/com/merkost/metronome/practiceSets/PracticeSessionControllerTest.kt`

**Interfaces:**
- Consumes: `PracticeSessionRepository`, set values, resolved presets, current playback state, completed bars, and elapsed playback milliseconds.
- Produces: immutable `PracticeSessionState` and `PracticeSessionCommand` values for the playback boundary.

- [ ] **Step 1: Write failing lifecycle and target tests**

```kotlin
@Test
fun targetReachedNeverStopsOrAdvances() = runTest {
    val controller = controller(session(target = PracticeStepTarget.Bars(2)))
    controller.onBarCompleted()
    controller.onBarCompleted()
    assertTrue(controller.state.value.session!!.targetReached)
    assertEquals(0, controller.state.value.session!!.currentStepIndex)
    assertTrue(controller.commands.tryReceive().isFailure)
}

@Test
fun nextQueuesWhilePlayingAndResetsOnlyAfterAcknowledgement() = runTest {
    val controller = controller(session = session(stepCount = 2))
    controller.next(isPlaying = true)
    assertEquals(1, controller.state.value.session!!.pendingStepIndex)
    assertEquals(PracticeSessionCommand.ApplyStep(1, atBarBoundary = true), controller.commands.receive())
    controller.onStepApplied(1)
    assertEquals(1, controller.state.value.session!!.currentStepIndex)
    assertEquals(0L, controller.state.value.session!!.elapsedMillis)
}
```

- [ ] **Step 2: Run the controller test and verify RED**

Run: `./gradlew :shared:testDebugUnitTest --tests '*PracticeSessionControllerTest'`

Expected: compilation fails because the controller does not exist.

- [ ] **Step 3: Implement explicit commands and deterministic transitions**

```kotlin
sealed interface PracticeSessionCommand {
    data class ApplyStep(val index: Int, val atBarBoundary: Boolean) : PracticeSessionCommand
    data object StartPlayback : PracticeSessionCommand
    data object PausePlayback : PracticeSessionCommand
    data object SessionFinished : PracticeSessionCommand
}

data class PracticeSessionState(
    val session: ActivePracticeSession? = null,
    val isRecovered: Boolean = false,
    val persistenceWarning: Boolean = false,
)
```

Implement start, recover, previous, next, pause, resume, restart step, manual-change marking, elapsed progress, bar progress, step-application acknowledgement, finish, and discard recovery. Ignore repeated navigation while pending. Checkpoint every explicit transition and when five seconds have elapsed since the previous progress checkpoint.

- [ ] **Step 4: Add complete controller cases and verify GREEN**

Cover missing preset start rejection, duration counting only while playing, bar counting, first/last boundaries, early finish, recovery paused state, restart, edited state, persistence failure, and final completion metadata.

Run: `./gradlew :shared:testDebugUnitTest --tests '*PracticeSessionControllerTest' --tests '*PracticeSessionPersistenceTest'`

Expected: all session domain tests pass.

### Task 5: Collection ViewModel and preset deletion integrity

**Files:**
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/viewModels/PracticeSetsViewModel.kt`
- Test: `shared/src/commonTest/kotlin/com/merkost/metronome/viewModels/PracticeSetsViewModelTest.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/viewModels/PracticePresetsViewModel.kt`
- Modify: `shared/src/commonTest/kotlin/com/merkost/metronome/viewModels/PracticePresetsViewModelTest.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/di/CommonModule.kt`

**Interfaces:**
- Consumes: set, session, and preset repositories.
- Produces: `PracticeSetsUiState`, `PracticeSetEditorState`, `PracticeSetUiEvent`, and preset `InUse` events.

- [ ] **Step 1: Write failing ViewModel tests**

```kotlin
@Test
fun editorAddsRepeatedPresetsWithIndependentStepIdsAndTargets() = runTest {
    val viewModel = viewModel(stepIds = listOf("step-1", "step-2"))
    viewModel.beginCreate()
    viewModel.addPreset(preset("preset-a"))
    viewModel.addPreset(preset("preset-a"))
    viewModel.setTarget("step-2", PracticeStepTarget.Bars(8))
    assertEquals(listOf("step-1", "step-2"), viewModel.state.value.editor!!.steps.map { it.id })
}

@Test
fun presetDeletionIsBlockedWhenReferenced() = runTest {
    val sets = FakePracticeSetRepository(references = listOf(practiceSet("Warmup")))
    val viewModel = PracticePresetsViewModel(presets, countIn, sets)
    viewModel.delete("preset-a")
    advanceUntilIdle()
    assertEquals(PresetUiEvent.InUse(listOf("Warmup")), viewModel.events.first())
    assertTrue(presets.deleted.isEmpty())
}
```

- [ ] **Step 2: Run focused ViewModel tests and verify RED**

Run: `./gradlew :shared:testDebugUnitTest --tests '*PracticeSetsViewModelTest' --tests '*PracticePresetsViewModelTest'`

Expected: compilation fails for the new ViewModel and new preset usage dependency.

- [ ] **Step 3: Implement immutable editor state and events**

```kotlin
data class PracticeSetEditorState(
    val sourceId: String?,
    val expectedUpdatedAtEpochMillis: Long?,
    val name: String,
    val steps: List<PracticeSetStep>,
    val isReordering: Boolean,
    val hasUnsavedChanges: Boolean,
)

data class PracticeSetsUiState(
    val sets: List<PracticeSet> = emptyList(),
    val presets: List<PracticePreset> = emptyList(),
    val editor: PracticeSetEditorState? = null,
    val activeSourceSetId: String? = null,
)
```

Implement create/edit drafts, repeated preset insertion, remove, reorder, target updates, save validation, optimistic conflict reporting, delete blocking for active source sets, and one-shot storage events.

- [ ] **Step 4: Add preset reference blocking and Koin registrations**

Inject `PracticeSetRepository` into `PracticePresetsViewModel`. Before preset deletion, call `setsReferencingPreset`; emit `PresetUiEvent.InUse(setNames)` when non-empty and skip deletion.

Register both repositories, the controller singleton, and `PracticeSetsViewModel` in `CommonModule.kt`.

- [ ] **Step 5: Run focused state tests and verify GREEN**

Run: `./gradlew :shared:testDebugUnitTest --tests '*PracticeSet*' --tests '*PracticePreset*'`

Expected: collection, editor, reference-integrity, and existing preset tests pass.

### Task 6: Reusable Practice Sets Compose components

**Files:**
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/components/PracticeSetComponents.kt`
- Modify: `shared/src/commonMain/composeResources/values/strings.xml`

**Interfaces:**
- Consumes: set, preset, editor, and session state.
- Produces: `PracticeSetRow`, `PracticeSetStepRow`, `PracticeStepTargetEditor`, `PracticeSessionStrip`, and previews.

- [ ] **Step 1: Add every new string resource**

Add strings for Practice Sets, create/edit/save set, set name, add preset, step counts, targets, minutes, bars, no target, preset missing, start, resume, restart step, previous, next, pause, finish, target reached, next bar, recovery, conflicts, referenced preset, unsaved changes, limits, validation, storage failure, and accessibility-formatted labels.

- [ ] **Step 2: Build stateless rows and target editor**

```kotlin
@Composable
fun PracticeSetStepRow(
    step: PracticeSetStep,
    preset: PracticePreset?,
    isReordering: Boolean,
    onTargetChanged: (PracticeStepTarget) -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier,
)
```

Use existing tonal surfaces, concise preset summaries, explicit missing state, target choice chips, bounded steppers, Lucide icons, and accessible move actions. Keep management controls outside the row activation target.

- [ ] **Step 3: Build the responsive session strip**

```kotlin
@Composable
fun PracticeSessionStrip(
    state: PracticeSessionState,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNextOrFinish: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
)
```

Show set name, step position, current preset, progress, pending boundary, edited, and target-reached states. Use a horizontal compact form at normal scale and stack text above controls when `LocalDensity.current.fontScale >= 1.3f`.

- [ ] **Step 4: Add same-file previews and compile**

Include populated, missing-preset, normal session, target-reached, and accessibility-scale previews.

Run: `./gradlew :shared:compileDebugKotlinAndroid`

Expected: generated resources and all components compile.

### Task 7: Practice Sets library and editor screens

**Files:**
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/screens/PracticeSetsScreen.kt`
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/screens/PracticeSetEditorScreen.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/navigation/AppNavigation.kt`

**Interfaces:**
- Consumes: `PracticeSetsViewModel`, reusable set components, singleton `MetronomeViewModel` start entry.
- Produces: `PRACTICE_SETS` and `PRACTICE_SET_EDITOR` destinations and complete library/editor flows.

- [ ] **Step 1: Implement library states**

Use a centered top app bar, 480dp content width, one-action empty state, set count, create, explicit reorder mode, set rows, start/resume/edit actions, active-source lock, delete confirmation, limit state, and snackbars.

- [ ] **Step 2: Implement the editor draft flow**

Use a full-screen name field, ordered step list, Add preset action, favourites-first preset selection sheet, inline target editor, explicit reorder mode, save action, missing-reference recovery, and discard-changes confirmation. At accessibility sizes stack the header and full-width primary action.

- [ ] **Step 3: Add navigation transitions and back contracts**

Add routes using the existing slide/fade transitions. Route editor completion back to the library. Route a blocked preset deletion to the Practice Sets library. Do not add bottom navigation.

- [ ] **Step 4: Compile both destinations**

Run: `./gradlew :shared:compileDebugKotlinAndroid`

Expected: library, editor, dialogs, sheets, and navigation compile.

### Task 8: Playback integration, runner controls, and review policy

**Files:**
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/screens/PracticeSessionSheet.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/viewModels/MetronomeViewModel.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/screens/MainScreen.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/screens/TempoTrainerSheet.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/review/ReviewPromptPolicy.kt`
- Modify: `shared/src/commonTest/kotlin/com/merkost/metronome/review/ReviewPromptPolicyTest.kt`

**Interfaces:**
- Consumes: controller commands and session state.
- Produces: boundary-safe set start/navigation, progress, recovery, conflicts, strip, sheet, and review suppression.

- [ ] **Step 1: Write failing integration-policy tests**

```kotlin
@Test
fun rejectsActiveOrRecoverablePracticeSession() {
    assertFalse(
        shouldRequestReview(
            eligible.copy(hasActivePracticeSession = true),
            ReviewPromptRecord(),
            "1.3.0",
            now,
        )
    )
}
```

Add controller-facing tests proving step application acknowledgement happens immediately while paused and on `onBarCompleted` while playing.

- [ ] **Step 2: Run the focused tests and verify RED**

Run: `./gradlew :shared:testDebugUnitTest --tests '*PracticeSession*' --tests '*ReviewPromptPolicyTest'`

Expected: tests fail because review snapshots and playback integration do not yet expose active sessions.

- [ ] **Step 3: Bridge the controller at the playback boundary**

Inject `PracticeSessionController`. Collect commands in `MetronomeViewModel`: apply resolved step presets through the existing `ActivePresetTracker`, start/pause playback, acknowledge immediate application, and acknowledge pending application after `onBarCompleted`. Forward completed bars and measured playback elapsed time. Manual rhythmic mutations notify both active-preset and active-session edited state.

Starting a set validates conflicts, resolves all presets, creates the session, applies the first preset, returns to Main, and starts playback. Finishing pauses playback, preserves current configuration, and clears the session.

- [ ] **Step 4: Add review suppression and conflict handling**

Add `hasActivePracticeSession` to `ReviewPromptSnapshot` and block when true. Starting a standalone timer or trainer while a session is active opens a finish-session confirmation. Starting a set while another structured tool is active opens a stop-current-tool confirmation.

- [ ] **Step 5: Add the main strip and expanded sheet**

Place `PracticeSessionStrip` above `MainButtonsRow`. The expanded `PracticeSessionSheet` shows current summary, target progress, restart, upcoming steps, recovery context, Finish, and early-finish confirmation. Add `Practice sets` to the Tempo sheet near preset management.

- [ ] **Step 6: Run focused tests and verify GREEN**

Run: `./gradlew :shared:testDebugUnitTest --tests '*PracticeSession*' --tests '*ReviewPrompt*' --tests '*ActivePreset*'`

Expected: session, review, and preset boundary tests pass.

### Task 9: Full verification, runtime review, and roadmap update

**Files:**
- Modify: `docs/ROADMAP.md`
- Capture: `.impeccable/review/practice-sets-phone-ios.png`
- Capture: `.impeccable/review/practice-sets-accessibility-ios.png`

**Interfaces:**
- Consumes: the complete implementation.
- Produces: cross-platform build evidence and runtime UX/accessibility proof.

- [ ] **Step 1: Run the complete shared and packaging gate**

Run:

```bash
git diff --check
./gradlew :shared:testDebugUnitTest :androidApp:assembleDebug :shared:linkDebugFrameworkIosSimulatorArm64 --console=plain
```

Expected: all shared tests pass, Android debug APK assembles, and the iOS simulator framework links.

- [ ] **Step 2: Build the full iOS simulator app**

Run:

```bash
xcodebuild -project iosApp/Metronome.xcodeproj -scheme Metronome -configuration Debug -destination 'platform=iOS Simulator,id=3FFE81C7-F02A-4247-B9FA-88C71ABB1129' -derivedDataPath /tmp/metronome-derived-data build
```

Expected: `BUILD SUCCEEDED`; record existing warnings separately.

- [ ] **Step 3: Inspect runtime flows in one bounded normal-size pass**

Verify create set, repeated preset steps, time and bar targets, reorder, save, start, pause/resume, manual edited state, next at a bar boundary, target reached without auto-advance, early finish, completed finish, referenced-preset deletion block, missing-reference rendering, and recovery restored paused.

- [ ] **Step 4: Inspect accessibility-size flow once**

Set the simulator to `accessibility-extra-extra-extra-large`. Verify library, editor, preset selection, target controls, session strip, expanded sheet, dialogs, and centered BPM without clipping or hidden essential actions. Reset the simulator to `large` afterward.

- [ ] **Step 5: Perform the Impeccable native finish review**

Provide both screenshots, the original request, the approved spec, `PRODUCT.md`, `DESIGN.md`, the craft-floor reference, and native adaptive-platform references to the finish reviewer. Apply one batched fix round if the disposition is `fix`, then recapture and obtain a verdict.

- [ ] **Step 6: Update the roadmap with exact evidence**

Move P1B to implemented status only after fresh green tests, builds, and runtime inspection. Keep physical-device audio, background, VoiceOver, and TalkBack validation under the P0 release gate when unavailable.
