# Practice Presets Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace anonymous saved tempos with named, durable Practice Presets and deliver complete create, recall, edit, favourite, reorder, duplicate, delete, migration, and accessibility flows.

**Architecture:** Add an immutable common `PracticePreset` model, a versioned codec, and a DataStore-backed repository with idempotent migration from `SavedTempo`. A focused `PracticePresetsViewModel` owns collection UI state, while `MetronomeViewModel` owns atomic application and active/edited identity. Quick recall remains in the Tempo sheet; management uses a dedicated Presets destination.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform Material 3, Kotlin coroutines and StateFlow, DataStore Preferences, Koin, Compose Resources, kotlin.test, kotlinx-coroutines-test.

**Spec:** `docs/superpowers/specs/2026-09-01-practice-presets-foundation-design.md`

## Global Constraints

- Preserve the main metronome as the primary surface; do not add bottom navigation or a dashboard.
- Core operation remains offline, local, account-free, ad-free, and subscription-free.
- Capture BPM, time signature, subdivision, beats, and count-in; keep audio and appearance preferences global.
- Support at most 50 presets and migrate every legacy `SavedTempo` idempotently.
- All new user-facing strings use Compose resources.
- Use Material theme roles, Lucide icons, existing dimensions, 48dp minimum targets, and existing spring motion.
- Use interface plus Koin for the new repository; do not add an `expect`/`actual` service.
- Write no code comments.
- Preserve unrelated worktree changes and stage only files owned by this feature.

---

## File map

### Domain and persistence

- Create `shared/src/commonMain/kotlin/com/merkost/metronome/presets/PracticePreset.kt`: immutable preset, draft, validation, and collection limit.
- Create `shared/src/commonMain/kotlin/com/merkost/metronome/presets/PracticePresetCodec.kt`: versioned record encoding and tolerant decoding.
- Create `shared/src/commonMain/kotlin/com/merkost/metronome/presets/PracticePresetRepository.kt`: repository contract and mutation results.
- Create `shared/src/commonMain/kotlin/com/merkost/metronome/presets/DataStorePracticePresetRepository.kt`: DataStore storage, migration, ordering, favourites, recency, and mutations.
- Create matching common tests under `shared/src/commonTest/kotlin/com/merkost/metronome/presets/`.

### State and integration

- Create `shared/src/commonMain/kotlin/com/merkost/metronome/viewModels/PracticePresetsViewModel.kt`: collection flow, create/edit/delete/reorder state, and user-facing failure events.
- Create `shared/src/commonMain/kotlin/com/merkost/metronome/presets/ActivePresetTracker.kt`: pure active, pending, and edited-state reducer.
- Modify `MetronomeViewModel.kt`: atomic preset application, next-bar queuing, edited tracking, and recency updates.
- Modify `CommonModule.kt`: repository and ViewModel registration.

### Compose flows

- Create `PresetNameDialog.kt`: create, rename, update, and save-as-new naming flow.
- Create `PresetRow.kt`: accessible summary, favourite, active, overflow, and reorder affordances.
- Create `PresetsScreen.kt`: empty, populated, reorder, limit, failure, and confirmation states.
- Modify `AppNavigation.kt`: Presets destination and navigation callbacks.
- Modify `TempoTrainerSheet.kt`: favourites, recents, save-current, and manage-presets entry.
- Modify `MainScreen.kt`: quiet active/edited/pending preset context and navigation handoff.
- Modify `strings.xml`: every new string and formatted label.

---

### Task 1: PracticePreset model and versioned codec

**Files:**
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/presets/PracticePreset.kt`
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/presets/PracticePresetCodec.kt`
- Test: `shared/src/commonTest/kotlin/com/merkost/metronome/presets/PracticePresetCodecTest.kt`

**Interfaces:**
- Produces: `PracticePreset`, `PracticePresetDraft`, `PracticePreset.Companion.MAX_PRESETS`, `PracticePresetCodec.encode(List<PracticePreset>): String`, and `PracticePresetCodec.decode(String?): List<PracticePreset>`.

- [ ] **Step 1: Write failing model and codec tests**

```kotlin
class PracticePresetCodecTest {
    private val preset = PracticePreset(
        id = "preset-1",
        name = "Warmup % line\nA",
        createdAtEpochMillis = 100L,
        lastUsedAtEpochMillis = 200L,
        isFavourite = true,
        sortPosition = 2,
        bpm = 96,
        timeSignature = TimeSignature.FOUR_FOUR,
        subdivision = Subdivision.TRIPLET,
        beats = listOf(Beat.HIGH, Beat.LOW, Beat.MUTE, Beat.LOW),
        countInEnabled = true,
    )

    @Test fun roundTripsEveryField() {
        assertEquals(listOf(preset), PracticePresetCodec.decode(PracticePresetCodec.encode(listOf(preset))))
    }

    @Test fun ignoresUnknownTrailingFieldsAndInvalidRecords() {
        val valid = PracticePresetCodec.encode(listOf(preset)).trimEnd() + "\tignored"
        val raw = "$valid\nv9\tbroken"
        assertEquals(listOf(preset), PracticePresetCodec.decode(raw))
    }
}
```

- [ ] **Step 2: Run the focused test and verify RED**

Run: `./gradlew :shared:testDebugUnitTest --tests '*PracticePresetCodecTest'`

Expected: compilation fails because `PracticePreset` and `PracticePresetCodec` do not exist.

- [ ] **Step 3: Implement the immutable model and codec**

```kotlin
data class PracticePreset(
    val id: String,
    val name: String,
    val createdAtEpochMillis: Long,
    val lastUsedAtEpochMillis: Long?,
    val isFavourite: Boolean,
    val sortPosition: Int,
    val bpm: Int,
    val timeSignature: TimeSignature,
    val subdivision: Subdivision,
    val beats: List<Beat>,
    val countInEnabled: Boolean,
) {
    companion object {
        const val MAX_PRESETS = 50
        const val MAX_NAME_LENGTH = 80
    }
}

data class PracticePresetDraft(
    val name: String,
    val bpm: Int,
    val timeSignature: TimeSignature,
    val subdivision: Subdivision,
    val beats: List<Beat>,
    val countInEnabled: Boolean,
)
```

Use a `v1` tab-delimited record. Escape `%`, tab, carriage return, and newline in names. Decode records independently, require at least twelve fields, ignore trailing fields, validate BPM and beat count, and skip only invalid records.

- [ ] **Step 4: Run the focused test and verify GREEN**

Run: `./gradlew :shared:testDebugUnitTest --tests '*PracticePresetCodecTest'`

Expected: all codec tests pass.

---

### Task 2: DataStore repository and legacy migration

**Files:**
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/presets/PracticePresetRepository.kt`
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/presets/DataStorePracticePresetRepository.kt`
- Create: `shared/src/commonTest/kotlin/com/merkost/metronome/presets/InMemoryPreferencesDataStore.kt`
- Test: `shared/src/commonTest/kotlin/com/merkost/metronome/presets/DataStorePracticePresetRepositoryTest.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/di/CommonModule.kt`

**Interfaces:**
- Consumes: `PracticePreset`, `PracticePresetDraft`, `PracticePresetCodec`, and legacy `SavedTempo`.
- Produces: `PracticePresetRepository.presets: Flow<List<PracticePreset>>`, `migrateLegacy`, `create`, `update`, `duplicate`, `delete`, `reorder`, `toggleFavourite`, and `markUsed`.

- [ ] **Step 1: Write failing repository tests**

```kotlin
@Test fun migratesLegacyOnceWithoutDroppingSourceData() = runTest {
    val store = InMemoryPreferencesDataStore(
        mutablePreferencesOf(stringPreferencesKey("SAVED_TEMPOS") to "120:FOUR_FOUR:QUARTER")
    )
    val repository = repository(store, ids = ArrayDeque(listOf("legacy-1", "unused")))

    assertEquals(MigrationResult.Migrated(1), repository.migrateLegacy(false))
    assertEquals(MigrationResult.AlreadyComplete, repository.migrateLegacy(false))
    assertEquals("120 BPM · 4/4", repository.presets.first().single().name)
    assertEquals("120:FOUR_FOUR:QUARTER", store.data.first()[stringPreferencesKey("SAVED_TEMPOS")])
}

@Test fun enforcesLimitAndMaintainsStableOrdering() = runTest {
    val repository = repository(InMemoryPreferencesDataStore())
    repeat(PracticePreset.MAX_PRESETS) { repository.create(draft("Preset $it")) }
    assertEquals(PresetMutationResult.LimitReached, repository.create(draft("Overflow")))
    val ids = repository.presets.first().take(3).map { it.id }.reversed()
    assertEquals(PresetMutationResult.Success, repository.reorder(ids))
}
```

- [ ] **Step 2: Run the focused repository test and verify RED**

Run: `./gradlew :shared:testDebugUnitTest --tests '*DataStorePracticePresetRepositoryTest'`

Expected: compilation fails because the repository contract and implementation do not exist.

- [ ] **Step 3: Implement the repository contract**

```kotlin
interface PracticePresetRepository {
    val presets: Flow<List<PracticePreset>>
    suspend fun migrateLegacy(countInEnabled: Boolean): MigrationResult
    suspend fun create(draft: PracticePresetDraft): PresetMutationResult
    suspend fun update(id: String, draft: PracticePresetDraft): PresetMutationResult
    suspend fun duplicate(id: String, name: String): PresetMutationResult
    suspend fun delete(id: String): PresetMutationResult
    suspend fun reorder(orderedIds: List<String>): PresetMutationResult
    suspend fun toggleFavourite(id: String): PresetMutationResult
    suspend fun markUsed(id: String): PresetMutationResult
}

sealed interface PresetMutationResult {
    data object Success : PresetMutationResult
    data object LimitReached : PresetMutationResult
    data object NotFound : PresetMutationResult
    data class InvalidName(val reason: PresetNameError) : PresetMutationResult
    data class StorageFailure(val message: String) : PresetMutationResult
}

sealed interface MigrationResult {
    data object AlreadyComplete : MigrationResult
    data class Migrated(val count: Int) : MigrationResult
    data class Failed(val message: String) : MigrationResult
}
```

- [ ] **Step 4: Implement atomic DataStore operations**

Use `PRACTICE_PRESETS` and `PRACTICE_PRESETS_MIGRATED` preference keys. Every mutation must decode and modify inside one `dataStore.edit` call, normalise sort positions after mutations, and return `StorageFailure` on exceptions without replacing the observable collection. Migration reads `SAVED_TEMPOS`, assigns collision-safe generated names, writes the new collection, then marks migration complete in the same edit.

- [ ] **Step 5: Register the repository in Koin**

```kotlin
single<PracticePresetRepository> {
    DataStorePracticePresetRepository(
        dataStore = get(),
        nextId = { "preset-${currentTimeMillis()}-${Random.nextInt().toUInt().toString(16)}" },
        nowMillis = ::currentTimeMillis,
    )
}
```

- [ ] **Step 6: Run the focused tests and verify GREEN**

Run: `./gradlew :shared:testDebugUnitTest --tests '*PracticePreset*' --tests '*DataStorePracticePresetRepositoryTest'`

Expected: model, codec, migration, limit, ordering, favourite, recency, duplicate, update, and delete tests pass.

---

### Task 3: Preset collection state and atomic metronome application

**Files:**
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/presets/ActivePresetTracker.kt`
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/viewModels/PracticePresetsViewModel.kt`
- Test: `shared/src/commonTest/kotlin/com/merkost/metronome/presets/ActivePresetTrackerTest.kt`
- Test: `shared/src/commonTest/kotlin/com/merkost/metronome/viewModels/PracticePresetsViewModelTest.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/viewModels/MetronomeViewModel.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/di/CommonModule.kt`

**Interfaces:**
- Consumes: `PracticePresetRepository` and immutable `PracticePreset` values.
- Produces: `PracticePresetsUiState`, one-shot `PresetUiEvent`, `ActivePresetState`, and `MetronomeViewModel.applyPracticePreset(PracticePreset)`.

- [ ] **Step 1: Write failing reducer and ViewModel tests**

```kotlin
@Test fun manualChangeMarksAppliedPresetEditedButPendingDoesNot() {
    val preset = preset("one")
    val tracker = ActivePresetTracker()
    tracker.applied(preset)
    tracker.changed()
    assertEquals("one", tracker.state.value.active?.id)
    assertTrue(tracker.state.value.isEdited)
    tracker.pending(preset("two"))
    assertEquals("two", tracker.state.value.pending?.id)
}

@Test fun createTrimsNameAndEmitsSavedEvent() = runTest {
    val repository = FakePracticePresetRepository()
    val viewModel = PracticePresetsViewModel(repository, FakeAppDatastore())
    viewModel.create(draft("  Warmup  "))
    advanceUntilIdle()
    assertEquals("Warmup", repository.created.single().name)
    assertEquals(PresetUiEvent.Saved("Warmup"), viewModel.events.first())
}
```

- [ ] **Step 2: Run the focused state tests and verify RED**

Run: `./gradlew :shared:testDebugUnitTest --tests '*ActivePresetTrackerTest' --tests '*PracticePresetsViewModelTest'`

Expected: compilation fails because the tracker and ViewModel do not exist.

- [ ] **Step 3: Implement focused collection state**

```kotlin
data class PracticePresetsUiState(
    val presets: List<PracticePreset> = emptyList(),
    val favourites: List<PracticePreset> = emptyList(),
    val recents: List<PracticePreset> = emptyList(),
    val isReordering: Boolean = false,
    val dialog: PresetDialogState? = null,
    val deleteCandidate: PracticePreset? = null,
    val migrationFailed: Boolean = false,
)
```

The ViewModel migrates legacy data once during initialization, derives at most four favourites and four recents, validates names before repository calls, exposes explicit dialog/delete/reorder actions, and converts repository failures into one-shot events without clearing the collection.

- [ ] **Step 4: Add active and edited tracking to MetronomeViewModel**

`applyPracticePreset` queues a preset when playing and applies it immediately when paused. `onBarCompleted` commits any queued preset before trainer advancement. The atomic update sets BPM, signature, beats, and subdivision in one `_metronomeState.update`; count-in is persisted in the same coroutine. After completion, call `repository.markUsed(id)`. Every manual rhythmic mutation calls `activePresetTracker.changed()`.

- [ ] **Step 5: Register PracticePresetsViewModel**

```kotlin
viewModel { PracticePresetsViewModel(get(), get()) }
```

Pass `AppDatastore` so initialization can read legacy tempos and count-in state.

- [ ] **Step 6: Run focused and existing tests**

Run: `./gradlew :shared:testDebugUnitTest --tests '*Preset*' --tests '*MetronomeTimingTest' --tests '*SubdivisionTimingTest'`

Expected: all selected tests pass.

---

### Task 4: Localized reusable preset UI

**Files:**
- Modify: `shared/src/commonMain/composeResources/values/strings.xml`
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/components/PresetNameDialog.kt`
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/components/PresetRow.kt`

**Interfaces:**
- Consumes: `PracticePreset`, `PresetDialogState`, Material theme tokens, Lucide icons.
- Produces: reusable naming dialog and row used by Tempo sheet and Presets screen.

- [ ] **Step 1: Add all copy to Compose resources**

Add resource keys for Presets, save current setup, favourites, recent, manage presets, active, edited, applying, empty-state copy, create, rename, update, save as new, duplicate, reorder, done, cancel, delete confirmation, limit reached, retry migration, move up/down, validation errors, and formatted rhythm summaries.

- [ ] **Step 2: Implement the naming dialog**

```kotlin
@Composable
fun PresetNameDialog(
    state: PresetDialogState,
    onNameChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
)
```

Use `AppDialog`, a Material 3 `OutlinedTextField`, single-line input capped at 80 characters, visible configuration summary, IME Done, initial focus, error semantics, and focus restoration through the caller.

- [ ] **Step 3: Implement the accessible row**

```kotlin
@Composable
fun PresetRow(
    preset: PracticePreset,
    active: Boolean,
    edited: Boolean,
    reordering: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onApply: () -> Unit,
    onToggleFavourite: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
)
```

Use a 48dp minimum row, two-line name allowance, short rhythm summary, Lucide Star/MoreVertical/GripVertical/ChevronUp/ChevronDown icons, state descriptions for favourite/active/edited, and separate accessible move actions in reorder mode.

- [ ] **Step 4: Compile resources and UI**

Run: `./gradlew :shared:compileDebugKotlinAndroid`

Expected: generated resource accessors and both composables compile.

---

### Task 5: Full Presets management flow and navigation

**Files:**
- Create: `shared/src/commonMain/kotlin/com/merkost/metronome/screens/PresetsScreen.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/navigation/AppNavigation.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/screens/MainScreen.kt`

**Interfaces:**
- Consumes: `PracticePresetsViewModel`, `PresetRow`, `PresetNameDialog`, and `MetronomeViewModel.applyPracticePreset`.
- Produces: `MainDestinations.PRESETS`, `PresetsScreen(upPress, onApplyPreset)`, and a navigation callback from MainScreen.

- [ ] **Step 1: Implement PresetsScreen states**

```kotlin
@Composable
fun PresetsScreen(
    upPress: () -> Unit,
    onApplyPreset: (PracticePreset) -> Unit,
) {
    val viewModel: PracticePresetsViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsState()
}
```

Use a centered top app bar and existing 480dp content width. Empty state has one primary `Save current setup` action. Populated state shows favourites first and remaining manual order. Reorder mode exposes Done and accessible move actions. Add `Modifier.pointerInput` with `detectDragGesturesAfterLongPress`; crossing the adjacent row midpoint calls the same identifier-based `move(id, targetIndex)` reducer as the move-up and move-down controls. Render naming and delete confirmation dialogs above the list. Present transient failures with a Material Snackbar.

- [ ] **Step 2: Add navigation without bottom navigation**

```kotlin
const val PRESETS = "presets"
```

Add the destination with the existing slide/fade transition. `MainScreen` receives `onPresetsClicked`; Presets applies through the shared singleton `MetronomeViewModel`, then pops to Main.

- [ ] **Step 3: Pause before full management**

The MainScreen navigation callback closes the Tempo sheet, calls `onStopClicked()` when playback is active, and navigates only after state is paused. Quick application from the Tempo sheet remains boundary-safe and does not open the management screen.

- [ ] **Step 4: Compile the navigation flow**

Run: `./gradlew :shared:compileDebugKotlinAndroid`

Expected: Main, Settings, and Presets destinations compile with no route ambiguity.

---

### Task 6: Tempo-sheet quick recall and main-screen context

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/screens/TempoTrainerSheet.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/screens/MainScreen.kt`
- Modify: `shared/src/commonMain/kotlin/com/merkost/metronome/viewModels/MetronomeViewModel.kt`

**Interfaces:**
- Consumes: favourite/recent lists, active preset state, preset dialog actions, and atomic apply.
- Produces: one-tap quick recall, save-current flow, manage entry, and visible active/edited/pending context.

- [ ] **Step 1: Replace SavedTemposRow with Practice Presets quick access**

```kotlin
@Composable
private fun PracticePresetsQuickAccess(
    favourites: List<PracticePreset>,
    recents: List<PracticePreset>,
    activePresetId: String?,
    onApply: (PracticePreset) -> Unit,
    onSaveCurrent: () -> Unit,
    onManage: () -> Unit,
)
```

Show favourites first, then recent presets not already present, each capped at four. Empty state uses `Save current setup`; populated state keeps that action and adds `Manage presets`. Remove destructive close icons from quick recall.

- [ ] **Step 2: Add quiet active context to MainScreen**

Render `Preset name`, `Preset name · Edited`, or `Applying Preset name…` as a small state-aware label above the tempo chip. It must not exceed `titleMedium`, must wrap to two lines, and must not displace the BPM or play controls on compact screens.

- [ ] **Step 3: Wire create/update/save-as-new dialogs**

Build `PracticePresetDraft` from the current `MetronomeState`, beats, and `countInEnabled`. Saving an edited active preset asks between Update and Save as new. On successful create or update, set the active identity without changing playback state.

- [ ] **Step 4: Remove obsolete saved-tempo UI usage**

Keep `SavedTempo` and its DataStore key only for migration. Remove `savedTempos`, `saveCurrentTempo`, `applySavedTempo`, and `deleteSavedTempo` from UI and `MetronomeViewModel` after migration callers no longer depend on them.

- [ ] **Step 5: Run feature tests and compile**

Run: `./gradlew :shared:testDebugUnitTest --tests '*Preset*' :shared:compileDebugKotlinAndroid`

Expected: preset tests pass and shared Android compilation succeeds.

---

### Task 7: UX self-review, hardening, and full verification

**Files:**
- Modify as findings require: files owned by Tasks 1–6
- Modify: `docs/superpowers/specs/2026-09-01-practice-presets-foundation-design.md`
- Modify: `docs/superpowers/plans/2026-09-01-practice-presets-foundation.md`

**Interfaces:**
- Consumes: the complete P1A feature.
- Produces: verified Android/iOS implementation and documentation status.

- [ ] **Step 1: Run static self-review**

Check every spec section against implementation. Search for hardcoded new strings, Material icons, hardcoded UI colours, sub-48dp interactive targets, comments, stale `SavedTemposRow` calls, placeholder text, unsafe list-index identity, and mutation paths that can partially apply a preset.

Run:

```bash
rg -n 'SavedTemposRow|TODO|FIXME|androidx.compose.material.icons|Color\(' shared/src/commonMain
rg -n '"(Presets|Save current|Manage presets|Favourite|Recent|Delete preset|Reorder)' shared/src/commonMain/kotlin
git diff --check
```

- [ ] **Step 2: Run the complete shared test suite**

Run: `./gradlew :shared:testDebugUnitTest`

Expected: every shared unit test passes, including all new preset tests.

- [ ] **Step 3: Build Android and iOS together**

Run: `./gradlew :androidApp:assembleDebug :shared:linkDebugFrameworkIosSimulatorArm64`

Expected: Android debug APK and iOS simulator framework build successfully.

- [ ] **Step 4: Inspect the shipped device classes once**

Render or run compact Android and iOS simulator flows in light/dark appearance and large text. Inspect empty, populated, naming, active, edited, pending, reorder, limit, and delete states in one bounded pass. Fix all material findings in one batch, then perform at most one confirmation pass.

- [ ] **Step 5: Verify migration and boundary-safe playback manually**

Seed current-release saved tempos on Android and iOS, launch the new build, and confirm all entries migrate exactly once. Apply presets while paused and while playing at low/high BPM with every subdivision; confirm playback changes only on a full bar boundary.

- [ ] **Step 6: Update documentation status and report evidence**

Mark the spec implemented only after automated builds pass. Record any device-only checks that could not run as open validation rather than claiming them complete. Do not modify unrelated dirty files.
