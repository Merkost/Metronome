package com.merkost.metronome.ui.theme

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.merkost.metronome.model.AppDatastore
import org.koin.compose.koinInject

enum class AppColorScheme(val lightColor: ColorScheme, val darkColor: ColorScheme) {
    @RequiresApi(31)
    MATERIAL3(BlackNWhiteLightColorScheme, BlackNWhiteDarkColorScheme),

    BLACKNWHITE(BlackNWhiteLightColorScheme, BlackNWhiteDarkColorScheme),
    MELROSE(PurpleLightColorScheme, PurpleDarkColorScheme);
//    PERIWINKLE(Periwinkle),
//    MINTGREEN(MintGreen),
//    PINKLACE(PinkLace);

    companion object {
        fun defaultValues(): List<AppColorScheme> {
            return listOf(BLACKNWHITE, MELROSE)
        }
    }
}


private val BlackNWhiteDarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Color.DarkGray,
    surfaceVariant = Color.DarkGray,
)

private val BlackNWhiteLightColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    primaryContainer = Color.LightGray.copy(0.5f),
    surfaceVariant = Color.LightGray.copy(0.5f),
)

private val PurpleDarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val PurpleLightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)
/* Other default colors to override
background = Color(0xFFFFFBFE),
surface = Color(0xFFFFFBFE),
onPrimary = Color.White,
onSecondary = Color.White,
onTertiary = Color.White,
onBackground = Color(0xFF1C1B1F),
onSurface = Color(0xFF1C1B1F),
*/


@Composable
fun MetronomeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val appDatastore: AppDatastore = koinInject()
    val selectedColorScheme by appDatastore.colorScheme.collectAsState(AppColorScheme.BLACKNWHITE)

    val colorScheme = when {
        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && selectedColorScheme == AppColorScheme.MATERIAL3) -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> {
            if (darkTheme) selectedColorScheme.darkColor else selectedColorScheme.lightColor
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}