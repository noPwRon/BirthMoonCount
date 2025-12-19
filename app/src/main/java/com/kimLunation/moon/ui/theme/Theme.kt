// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon.ui.theme

// Imports are used to bring in code from other parts of the project or from external libraries.
import android.app.Activity
import android.os.Build // Provides information about the current device's Android version.
import androidx.compose.foundation.isSystemInDarkTheme // A composable function to check if the system is currently in dark mode.
import androidx.compose.material3.MaterialTheme // The main theme composable for Material Design 3.
import androidx.compose.material3.darkColorScheme // A function to create a color scheme for a dark theme.
import androidx.compose.material3.dynamicDarkColorScheme // A function to create a dynamic color scheme for a dark theme (based on the user's wallpaper).
import androidx.compose.material3.dynamicLightColorScheme // A function to create a dynamic color scheme for a light theme.
import androidx.compose.material3.lightColorScheme // A function to create a color scheme for a light theme.
import androidx.compose.runtime.Composable // An annotation that marks a function as a Jetpack Compose UI component.
import androidx.compose.ui.platform.LocalContext // Provides the current Android Context.

// A predefined color scheme for the dark theme. It uses the colors defined in Color.kt.
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

// A predefined color scheme for the light theme.
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* You can override other default colors here if you want.
    For example:
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

/**
 * This is the main theme composable for the entire application.
 * It wraps the content of the app and provides the color scheme, typography, and shapes.
 */
@Composable
fun KimsLunationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // By default, the theme will match the system setting (light or dark).
    // Dynamic color is a feature on Android 12 and newer that creates a color scheme from the user's wallpaper.
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit // The content of the app that will be themed.
) {
    // Determine which color scheme to use.
    val colorScheme = when {
        // If dynamic color is enabled and the device is Android 12+...
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            // ...use the dynamic color scheme.
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        // If dynamic color is not used, fall back to our predefined color schemes.
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // 'MaterialTheme' is the composable that applies the theme to its content.
    MaterialTheme(
        colorScheme = colorScheme, // The color scheme we determined above.
        typography = Typography,   // The typography (font styles) defined in Type.kt.
        content = content          // The actual UI of the app.
    )
}
