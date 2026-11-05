package com.merkost.metronome.screens

import com.composables.icons.lucide.Layers
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Flag
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Music
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.TrendingDown
import com.composables.icons.lucide.TrendingUp
import com.composables.icons.lucide.VolumeX
import com.merkost.metronome.components.AppBottomSheet
import com.merkost.metronome.components.AppChip
import com.merkost.metronome.components.ExpandableSection
import com.merkost.metronome.components.MySecondaryButton
import com.merkost.metronome.components.PracticeAgainRow
import com.merkost.metronome.components.ValueStepper
import com.merkost.metronome.model.GapTrainerConfig
import com.merkost.metronome.model.GradualTempoConfig
import com.merkost.metronome.model.MAX_BPM
import com.merkost.metronome.model.MIN_BPM
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.model.TempoStepMode
import com.merkost.metronome.model.asString
import com.merkost.metronome.model.trainerProgressCaption
import com.merkost.metronome.model.trainerRuleCaption
import com.merkost.metronome.model.trainerSummaryFor
import com.merkost.metronome.presets.ActivePresetState
import com.merkost.metronome.presets.PracticePreset
import com.merkost.metronome.practiceSets.PracticeSet
import com.merkost.metronome.ui.AnimatedNumberText
import com.merkost.metronome.ui.AppAnimations
import com.merkost.metronome.ui.sheetButtonHeight
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall
import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.applies_next_bar
import metronome.shared.generated.resources.favourites
import metronome.shared.generated.resources.manage_presets
import metronome.shared.generated.resources.practice_sets
import metronome.shared.generated.resources.recent
import metronome.shared.generated.resources.save_current_setup
import org.jetbrains.compose.resources.stringResource
import metronome.shared.generated.resources.gap_bars_caption
import metronome.shared.generated.resources.gap_title
import metronome.shared.generated.resources.hub_tempo_trainer
import metronome.shared.generated.resources.rhythm_subdivision
import metronome.shared.generated.resources.trainer_bars_count
import metronome.shared.generated.resources.trainer_bpm
import metronome.shared.generated.resources.trainer_config_hint
import metronome.shared.generated.resources.trainer_edit_settings
import metronome.shared.generated.resources.trainer_every
import metronome.shared.generated.resources.trainer_gap_hint
import metronome.shared.generated.resources.trainer_gap_mute
import metronome.shared.generated.resources.trainer_gap_play
import metronome.shared.generated.resources.trainer_gap_start
import metronome.shared.generated.resources.trainer_gap_start_and_play
import metronome.shared.generated.resources.trainer_gap_stop
import metronome.shared.generated.resources.trainer_off
import metronome.shared.generated.resources.trainer_phrase
import metronome.shared.generated.resources.trainer_reset_to
import metronome.shared.generated.resources.trainer_sheet_title
import metronome.shared.generated.resources.trainer_start
import metronome.shared.generated.resources.trainer_start_and_play
import metronome.shared.generated.resources.trainer_start_button
import metronome.shared.generated.resources.trainer_step
import metronome.shared.generated.resources.trainer_stop_keep
import metronome.shared.generated.resources.trainer_subdivision_hint
import metronome.shared.generated.resources.trainer_target
import org.jetbrains.compose.resources.pluralStringResource

private val TEMPO_PRESETS = listOf(
    "Largo" to 50,
    "Andante" to 80,
    "Moderato" to 110,
    "Allegro" to 140,
    "Presto" to 180,
)

private val INCREMENT_OPTIONS = listOf(1, 2, 3, 5)
private val BARS_OPTIONS = listOf(1, 2, 4, 8)
private val GAP_PLAY_OPTIONS = listOf(2, 4, 8)
private val GAP_MUTE_OPTIONS = listOf(1, 2, 4)

enum class TempoSheetSection { SUBDIVISION, TRAINER, GAP, PHRASE }

@Composable
fun TempoTrainerSheet(
    currentBpm: Int,
    beatsPerBar: Int,
    isPlaying: Boolean,
    subdivision: Subdivision,
    favouritePresets: List<PracticePreset>,
    recentPresets: List<PracticePreset>,
    recentPracticeSet: PracticeSet?,
    activePresetState: ActivePresetState,
    activeConfig: GradualTempoConfig?,
    currentBar: Int,
    lastConfig: GradualTempoConfig?,
    activeGapConfig: GapTrainerConfig?,
    lastGapConfig: GapTrainerConfig?,
    initialSection: TempoSheetSection?,
    onPresetSelected: (Int) -> Unit,
    onApplyPracticePreset: (PracticePreset) -> Unit,
    onSaveCurrentSetup: () -> Unit,
    onManagePresets: () -> Unit,
    onManagePracticeSets: () -> Unit,
    onPracticeAgain: (PracticeSet) -> Unit,
    onSubdivisionChanged: (Subdivision) -> Unit,
    onStartTrainer: (GradualTempoConfig) -> Unit,
    onStopTrainer: (resetToStart: Boolean) -> Unit,
    onStartGapTrainer: (GapTrainerConfig) -> Unit,
    onUpdateGapTrainer: (GapTrainerConfig) -> Unit,
    onStopGapTrainer: () -> Unit,
    onDismiss: () -> Unit,
    phraseLength: Int = 0,
    onPhraseLengthChanged: (Int) -> Unit = {},
    onStepTrainerForward: () -> Unit = {},
    onStepTrainerBack: () -> Unit = {},
) {
    AppBottomSheet(
        title = when (initialSection) {
            TempoSheetSection.TRAINER -> stringResource(Res.string.hub_tempo_trainer)
            TempoSheetSection.GAP -> stringResource(Res.string.gap_title)
            else -> stringResource(Res.string.trainer_sheet_title)
        },
        onDismiss = onDismiss,
    ) { dismissAnimated ->
        var expandedSection by remember {
            mutableStateOf(
                initialSection ?: when {
                    activeConfig != null -> TempoSheetSection.TRAINER
                    activeGapConfig != null -> TempoSheetSection.GAP
                    else -> null
                }
            )
        }

        fun toggle(section: TempoSheetSection) {
            expandedSection = if (expandedSection == section) null else section
        }

        var lastActiveConfig by remember { mutableStateOf(activeConfig) }
        LaunchedEffect(activeConfig) {
            activeConfig?.let { lastActiveConfig = it }
        }

        Column {
            if (initialSection != TempoSheetSection.TRAINER && initialSection != TempoSheetSection.GAP) {
                FlowRowPresets(
                    currentBpm = currentBpm,
                    onPresetSelected = {
                        onPresetSelected(it)
                        dismissAnimated()
                    },
                )
                Spacer(Modifier.height(spacingSmall))
                PracticePresetsQuickSection(
                    favourites = favouritePresets,
                    recents = recentPresets,
                    activeState = activePresetState,
                    recentPracticeSet = recentPracticeSet,
                    onApply = {
                        onApplyPracticePreset(it)
                        dismissAnimated()
                    },
                    onSave = {
                        dismissAnimated()
                        onSaveCurrentSetup()
                    },
                    onManage = {
                        dismissAnimated()
                        onManagePresets()
                    },
                    onManageSets = {
                        dismissAnimated()
                        onManagePracticeSets()
                    },
                    onPracticeAgain = {
                        dismissAnimated()
                        onPracticeAgain(it)
                    },
                )
                Spacer(Modifier.height(spacingSmall))
                HorizontalDivider()

            }
            ExpandableSection(
                icon = { SectionIcon(Lucide.Music, subdivision != Subdivision.QUARTER) },
                title = stringResource(Res.string.rhythm_subdivision),
                summary = stringResource(subdivision.labelRes),
                summaryActive = subdivision != Subdivision.QUARTER,
                expanded = expandedSection == TempoSheetSection.SUBDIVISION,
                onToggle = { toggle(TempoSheetSection.SUBDIVISION) },
            ) {
                SectionCaption(stringResource(Res.string.trainer_subdivision_hint))
                FlowRowChips(
                    options = Subdivision.entries,
                    selected = subdivision,
                    display = { stringResource(it.labelRes) },
                    onSelect = onSubdivisionChanged,
                )
            }
            HorizontalDivider()

            ExpandableSection(
                icon = { SectionIcon(Lucide.Flag, phraseLength != 0) },
                title = stringResource(Res.string.trainer_phrase),
                summary = phraseSummary(phraseLength),
                summaryActive = phraseLength != 0,
                expanded = expandedSection == TempoSheetSection.PHRASE,
                onToggle = { toggle(TempoSheetSection.PHRASE) },
            ) {
                PhraseSectionContent(
                    phraseLength = phraseLength,
                    onPhraseLengthChanged = onPhraseLengthChanged,
                )
            }
            HorizontalDivider()

            ExpandableSection(
                icon = {
                    SectionIcon(
                        if (activeConfig?.ascending == false) {
                            Lucide.TrendingDown
                        } else {
                            Lucide.TrendingUp
                        },
                        activeConfig != null,
                    )
                },
                title = stringResource(Res.string.hub_tempo_trainer),
                summary = activeConfig?.let { "$currentBpm → ${it.endBpm}" } ?: stringResource(Res.string.trainer_off),
                summaryActive = activeConfig != null,
                expanded = expandedSection == TempoSheetSection.TRAINER,
                onToggle = { toggle(TempoSheetSection.TRAINER) },
            ) {
                var editing by remember { mutableStateOf(false) }
                AnimatedContent(
                    targetState = activeConfig != null && !editing,
                    transitionSpec = { AppAnimations.fadeScaleTransform },
                    label = "trainerSectionMode"
                ) { active ->
                    val shownConfig = activeConfig ?: lastActiveConfig
                    if (active && shownConfig != null) {
                        ActiveTrainerContent(
                            config = shownConfig,
                            currentBpm = currentBpm,
                            currentBar = currentBar,
                            isPlaying = isPlaying,
                            gapRunning = activeGapConfig != null,
                            onStepForward = onStepTrainerForward,
                            onStepBack = onStepTrainerBack,
                            onStopKeep = {
                                onStopTrainer(false)
                                dismissAnimated()
                            },
                            onStopReset = {
                                onStopTrainer(true)
                                dismissAnimated()
                            },
                            onEdit = { editing = true },
                        )
                    } else {
                        TrainerConfigContent(
                            currentBpm = currentBpm,
                            beatsPerBar = beatsPerBar,
                            isPlaying = isPlaying,
                            seedConfig = activeConfig ?: lastConfig,
                            gapCycleBars = activeGapConfig?.cycleBars,
                            onStartTrainer = {
                                onStartTrainer(it)
                                dismissAnimated()
                            },
                        )
                    }
                }
            }
            HorizontalDivider()

            ExpandableSection(
                icon = { SectionIcon(Lucide.VolumeX, activeGapConfig != null) },
                title = stringResource(Res.string.gap_title),
                summary = activeGapConfig?.let { pluralStringResource(Res.plurals.gap_bars_caption, it.muteBars, it.playBars, it.muteBars) } ?: stringResource(Res.string.trainer_off),
                summaryActive = activeGapConfig != null,
                expanded = expandedSection == TempoSheetSection.GAP,
                onToggle = { toggle(TempoSheetSection.GAP) },
            ) {
                GapTrainerContent(
                    isPlaying = isPlaying,
                    activeConfig = activeGapConfig,
                    seedConfig = lastGapConfig,
                    onUpdate = onUpdateGapTrainer,
                    onStartAndDismiss = {
                        onStartGapTrainer(it)
                        dismissAnimated()
                    },
                    onStop = onStopGapTrainer,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PracticePresetsQuickSection(
    favourites: List<PracticePreset>,
    recents: List<PracticePreset>,
    activeState: ActivePresetState,
    recentPracticeSet: PracticeSet?,
    onApply: (PracticePreset) -> Unit,
    onSave: () -> Unit,
    onManage: () -> Unit,
    onManageSets: () -> Unit,
    onPracticeAgain: (PracticeSet) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
        if (activeState.pending != null) {
            SectionCaption("${activeState.pending.name} · ${stringResource(Res.string.applies_next_bar)}")
        }
        if (favourites.isNotEmpty()) {
            SectionCaption(stringResource(Res.string.favourites))
            PresetChipRow(favourites, activeState.active?.id, onApply)
        }
        val recentOnly = recents.filterNot { recent -> favourites.any { it.id == recent.id } }
        if (recentOnly.isNotEmpty()) {
            SectionCaption(stringResource(Res.string.recent))
            PresetChipRow(recentOnly, activeState.active?.id, onApply)
        }
        val manageAction: @Composable (Modifier) -> Unit = { modifier ->
            TextButton(onClick = onManage, modifier = modifier) {
                Text(stringResource(Res.string.manage_presets))
            }
        }
        val saveAction: @Composable (Modifier) -> Unit = { modifier ->
            TextButton(onClick = onSave, modifier = modifier) {
                Icon(Lucide.Plus, contentDescription = null)
                Spacer(Modifier.width(spacingSmall))
                Text(stringResource(Res.string.save_current_setup))
            }
        }
        if (LocalDensity.current.fontScale >= 1.3f) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacingSmall),
            ) {
                manageAction(Modifier.fillMaxWidth())
                saveAction(Modifier.fillMaxWidth())
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                manageAction(Modifier)
                saveAction(Modifier)
            }
        }
        recentPracticeSet?.let { practiceSet ->
            PracticeAgainRow(
                practiceSet = practiceSet,
                onClick = { onPracticeAgain(practiceSet) },
            )
        }
        TextButton(
            onClick = onManageSets,
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth().heightIn(min = sheetButtonHeight),
        ) {
            Icon(Lucide.Layers, contentDescription = null)
            Spacer(Modifier.width(spacingSmall))
            Text(stringResource(Res.string.practice_sets))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PresetChipRow(
    presets: List<PracticePreset>,
    activeId: String?,
    onApply: (PracticePreset) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(spacingSmall),
        verticalArrangement = Arrangement.spacedBy(spacingSmall),
    ) {
        presets.forEach { preset ->
            AppChip(
                selected = preset.id == activeId,
                onClick = { onApply(preset) },
                label = preset.name,
                leadingIcon = if (preset.isFavourite) Lucide.Star else null,
            )
        }
    }
}

@Composable
private fun SectionIcon(icon: ImageVector, active: Boolean) {
    val tint by animateColorAsState(
        targetValue = if (active) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = AppAnimations.standard(),
        label = "sectionIconTint",
    )
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(20.dp)
    )
}

@Composable
private fun SectionCaption(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun AnimatedSectionCaption(text: String) {
    AnimatedContent(
        targetState = text,
        transitionSpec = { AppAnimations.fadeThrough },
        label = "sectionCaption",
    ) { caption ->
        SectionCaption(caption)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> FlowRowChips(
    options: List<T>,
    selected: T,
    display: @Composable (T) -> String,
    onSelect: (T) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(spacingSmall),
        verticalArrangement = Arrangement.spacedBy(spacingSmall),
    ) {
        options.forEach { option ->
            AppChip(
                selected = selected == option,
                onClick = { onSelect(option) },
                label = display(option),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowPresets(currentBpm: Int, onPresetSelected: (Int) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(spacingSmall),
        verticalArrangement = Arrangement.spacedBy(spacingSmall),
    ) {
        TEMPO_PRESETS.forEach { (name, bpm) ->
            AppChip(
                selected = currentBpm == bpm,
                onClick = { onPresetSelected(bpm) },
                label = "$name $bpm",
            )
        }
    }
}

@Composable
private fun OptionChipsRow(
    label: String,
    options: List<Int>,
    selected: Int,
    display: @Composable (Int) -> String,
    onSelect: (Int) -> Unit,
) {
    if (LocalDensity.current.fontScale >= 1.3f) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
            SectionCaption(label)
            FlowRowChips(options = options, selected = selected, display = display, onSelect = onSelect)
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacingSmall),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(44.dp),
            )
            FlowRowChips(options = options, selected = selected, display = display, onSelect = onSelect)
        }
    }
}

@Composable
internal fun TrainerConfigContent(
    currentBpm: Int,
    beatsPerBar: Int,
    isPlaying: Boolean,
    seedConfig: GradualTempoConfig?,
    gapCycleBars: Int? = null,
    onStartTrainer: (GradualTempoConfig) -> Unit,
) {
    var startBpm by remember { mutableStateOf(seedConfig?.startBpm ?: currentBpm) }
    var endBpm by remember {
        mutableStateOf(seedConfig?.endBpm ?: (currentBpm + 40).coerceAtMost(MAX_BPM))
    }
    var increment by remember { mutableStateOf(seedConfig?.increment ?: 2) }
    var barsPerStep by remember { mutableStateOf(seedConfig?.barsPerStep ?: 4) }
    var stepMode by remember { mutableStateOf(seedConfig?.stepMode ?: TempoStepMode.BARS) }

    Column(verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
        SectionCaption(stringResource(Res.string.trainer_config_hint))

        Column(verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
            listOf(true to startBpm, false to endBpm).forEach { (isStart, value) ->
                val label = stringResource(if (isStart) Res.string.trainer_start else Res.string.trainer_target)
                if (LocalDensity.current.fontScale >= 1.3f) {
                    ValueStepper(
                        value = value,
                        onValueChange = { if (isStart) startBpm = it else endBpm = it },
                        range = MIN_BPM..MAX_BPM,
                        label = label,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                } else {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        ValueStepper(
                            value = value,
                            onValueChange = { if (isStart) startBpm = it else endBpm = it },
                            range = MIN_BPM..MAX_BPM,
                        )
                    }
                }
            }
        }

        OptionChipsRow(
            label = stringResource(Res.string.trainer_step),
            options = INCREMENT_OPTIONS,
            selected = increment,
            display = { if (endBpm >= startBpm) "+$it" else "−$it" },
            onSelect = { increment = it },
        )
        TrainerStepModeRow(
            selected = stepMode,
            gapRunning = gapCycleBars != null,
            barsPerStep = barsPerStep,
            onSelect = { mode ->
                stepMode = mode
                when (mode) {
                    TempoStepMode.BARS -> if (barsPerStep !in BARS_OPTIONS) barsPerStep = 4
                    TempoStepMode.GAP_CYCLE -> barsPerStep = gapCycleBars ?: barsPerStep
                    TempoStepMode.MANUAL -> Unit
                }
            },
        )
        if (stepMode == TempoStepMode.BARS) {
            OptionChipsRow(
                label = stringResource(Res.string.trainer_every),
                options = BARS_OPTIONS,
                selected = barsPerStep,
                display = { pluralStringResource(Res.plurals.trainer_bars_count, it, it) },
                onSelect = { barsPerStep = it },
            )
        }

        val config = GradualTempoConfig(startBpm, endBpm, increment, barsPerStep, stepMode)
        SectionCaption(trainerSummaryFor(config, beatsPerBar).asString())

        Button(
            onClick = { onStartTrainer(config) },
            enabled = startBpm != endBpm,
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth().heightIn(min = sheetButtonHeight),
        ) {
            Text(
                text = stringResource(if (isPlaying) Res.string.trainer_start_button else Res.string.trainer_start_and_play),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ActiveTrainerContent(
    config: GradualTempoConfig,
    currentBpm: Int,
    currentBar: Int,
    isPlaying: Boolean,
    gapRunning: Boolean,
    onStepForward: () -> Unit,
    onStepBack: () -> Unit,
    onStopKeep: () -> Unit,
    onStopReset: () -> Unit,
    onEdit: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(spacingMedium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedNumberText(
                    value = currentBpm,
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                )
                Spacer(Modifier.width(spacingSmall))
                Text(
                    text = stringResource(Res.string.trainer_bpm),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            SectionCaption(trainerRuleCaption(config).asString())
            AnimatedSectionCaption(
                trainerProgressCaption(config, currentBpm, currentBar, isPlaying, gapRunning).asString(),
            )
        }

        if (config.stepMode == TempoStepMode.MANUAL) {
            ManualStepControls(
                config = config,
                currentBpm = currentBpm,
                onStepForward = onStepForward,
                onStepBack = onStepBack,
            )
        }

        val stopActions: @Composable (Modifier) -> Unit = { actionModifier ->
            MySecondaryButton(onClick = onStopKeep, shape = CircleShape, modifier = actionModifier) {
                SheetSecondaryLabel(stringResource(Res.string.trainer_stop_keep, currentBpm))
            }
            MySecondaryButton(onClick = onStopReset, shape = CircleShape, modifier = actionModifier) {
                SheetSecondaryLabel(stringResource(Res.string.trainer_reset_to, config.startBpm))
            }
        }
        if (LocalDensity.current.fontScale >= 1.3f) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
                stopActions(Modifier.fillMaxWidth())
            }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacingSmall)) {
                stopActions(Modifier.weight(1f))
            }
        }

        TextButton(onClick = onEdit) {
            Text(stringResource(Res.string.trainer_edit_settings), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun GapTrainerContent(
    isPlaying: Boolean,
    activeConfig: GapTrainerConfig?,
    seedConfig: GapTrainerConfig?,
    onUpdate: (GapTrainerConfig) -> Unit,
    onStartAndDismiss: (GapTrainerConfig) -> Unit,
    onStop: () -> Unit,
) {
    val seed = activeConfig ?: seedConfig ?: GapTrainerConfig()
    var playBars by remember { mutableStateOf(seed.playBars) }
    var muteBars by remember { mutableStateOf(seed.muteBars) }
    val active = activeConfig != null

    Column(verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
        SectionCaption(stringResource(Res.string.trainer_gap_hint))
        OptionChipsRow(
            label = stringResource(Res.string.trainer_gap_play),
            options = GAP_PLAY_OPTIONS,
            selected = playBars,
            display = { pluralStringResource(Res.plurals.trainer_bars_count, it, it) },
            onSelect = {
                playBars = it
                if (active) onUpdate(GapTrainerConfig(it, muteBars))
            },
        )
        OptionChipsRow(
            label = stringResource(Res.string.trainer_gap_mute),
            options = GAP_MUTE_OPTIONS,
            selected = muteBars,
            display = { pluralStringResource(Res.plurals.trainer_bars_count, it, it) },
            onSelect = {
                muteBars = it
                if (active) onUpdate(GapTrainerConfig(playBars, it))
            },
        )
        AnimatedContent(
            targetState = active,
            transitionSpec = { AppAnimations.fadeScaleTransform },
            label = "gapTrainerButton"
        ) { running ->
            if (running) {
                MySecondaryButton(
                    onClick = onStop,
                    shape = CircleShape,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SheetSecondaryLabel(stringResource(Res.string.trainer_gap_stop))
                }
            } else {
                Button(
                    onClick = { onStartAndDismiss(GapTrainerConfig(playBars, muteBars)) },
                    shape = CircleShape,
                    modifier = Modifier.fillMaxWidth().heightIn(min = sheetButtonHeight),
                ) {
                    Text(
                        text = stringResource(if (isPlaying) Res.string.trainer_gap_start else Res.string.trainer_gap_start_and_play),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
internal fun SheetSecondaryLabel(text: String) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().heightIn(min = sheetButtonHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}
