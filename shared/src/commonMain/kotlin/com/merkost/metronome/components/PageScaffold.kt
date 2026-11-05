package com.merkost.metronome.components

import metronome.shared.generated.resources.Res
import metronome.shared.generated.resources.back
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Lucide
import com.merkost.metronome.navigation.AppPresentation
import com.merkost.metronome.navigation.LocalAppPresentation
import com.merkost.metronome.ui.desktopContentWidth
import com.merkost.metronome.ui.horizontalPadding
import com.merkost.metronome.ui.maxContentWidth
import com.merkost.metronome.ui.spacingLarge
import com.merkost.metronome.ui.spacingMedium
import com.merkost.metronome.ui.spacingSmall

@Composable
fun pageContentWidth(): Dp =
    if (LocalAppPresentation.current == AppPresentation.Desktop) desktopContentWidth else maxContentWidth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    centerTitle: Boolean = true,
    snackbarHost: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (contentPadding: PaddingValues) -> Unit,
) {
    if (LocalAppPresentation.current == AppPresentation.Desktop) {
        Scaffold(modifier = modifier, snackbarHost = snackbarHost) {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                    Row(
                        modifier = Modifier
                            .widthIn(max = desktopContentWidth)
                            .fillMaxWidth()
                            .padding(
                                start = horizontalPadding,
                                end = horizontalPadding,
                                top = spacingLarge,
                                bottom = spacingMedium,
                            ),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(spacingMedium),
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacingSmall / 2)) {
                            Text(
                                title,
                                modifier = Modifier.semantics { heading() },
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                            )
                            if (subtitle != null) {
                                Text(
                                    subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        actions()
                    }
                }
                Box(Modifier.weight(1f).fillMaxWidth()) { content(PaddingValues()) }
            }
        }
    } else {
        Scaffold(
            modifier = modifier,
            snackbarHost = snackbarHost,
            topBar = {
                val titleContent: @Composable () -> Unit = {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                val navigation: @Composable () -> Unit = {
                    AppIconButton(onClick = onBack) {
                        Icon(
                            if (LocalLayoutDirection.current == LayoutDirection.Rtl) Lucide.ArrowRight else Lucide.ArrowLeft,
                            contentDescription = stringResource(Res.string.back),
                        )
                    }
                }
                if (centerTitle) {
                    CenterAlignedTopAppBar(title = titleContent, navigationIcon = navigation, actions = actions)
                } else {
                    TopAppBar(title = titleContent, navigationIcon = navigation, actions = actions)
                }
            },
        ) { contentPadding -> content(contentPadding) }
    }
}
