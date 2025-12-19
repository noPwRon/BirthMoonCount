// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon.ui.theme

// Imports are used to bring in code from other parts of the project or from external libraries.
import androidx.compose.material3.Typography // A class that holds the typography styles for a Material Theme.
import androidx.compose.ui.text.TextStyle // Represents a collection of text styling parameters.
import androidx.compose.ui.text.font.FontFamily // Represents a font family.
import androidx.compose.ui.text.font.FontWeight // Represents the weight (boldness) of a font.
import androidx.compose.ui.unit.sp // A unit of measurement for scalable pixels (for text).

// This object defines the set of typography styles that will be used in the application.
// Typography includes things like font family, font weight, font size, line height, and letter spacing.
// By defining these styles here, we can ensure that the text throughout our app is consistent.
val Typography = Typography(
    // 'bodyLarge' is one of the standard Material Design text styles. It's typically used for long-form text.
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default, // Use the system's default font.
        fontWeight = FontWeight.Normal, // Normal font weight (not bold).
        fontSize = 16.sp, // Set the font size to 16 scalable pixels.
        lineHeight = 24.sp, // Set the space between lines of text.
        letterSpacing = 0.5.sp // Set the space between characters.
    )
    /* You can override other default text styles here as well.
       For example, you could define styles for titles, labels, etc.
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)
