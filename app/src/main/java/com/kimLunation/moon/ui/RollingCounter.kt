// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon.ui

// Imports are used to bring in code from other parts of the project or from external libraries.
import androidx.compose.animation.AnimatedContent // A container that animates its content as it changes.
import androidx.compose.animation.ExperimentalAnimationApi // An annotation for experimental animation APIs.
import androidx.compose.animation.fadeIn // An animation that fades in content.
import androidx.compose.animation.fadeOut // An animation that fades out content.
import androidx.compose.animation.slideInVertically // An animation that slides content in vertically.
import androidx.compose.animation.slideOutVertically // An animation that slides content out vertically.
import androidx.compose.animation.with // A function to combine animations.
import androidx.compose.foundation.Image // A composable for displaying images.
import androidx.compose.foundation.layout.Row // A composable that arranges its children in a horizontal sequence.
import androidx.compose.foundation.layout.fillMaxHeight // A modifier to make a composable fill its available height.
import androidx.compose.runtime.Composable // An annotation that marks a function as a Jetpack Compose UI component.
import androidx.compose.ui.Modifier // An object that can be used to add behavior or decoration to a composable.
import androidx.compose.ui.layout.ContentScale // Defines how to scale content within a composable.
import androidx.compose.ui.res.painterResource // A function to load a drawable resource as a Painter.
import com.kimLunation.moon.R // A class that contains all the resource IDs for the project.

/**
 * A composable function that displays a number using a rolling animation effect, similar to a mechanical counter.
 * It displays the number as a series of individual digits.
 */
@Composable
fun RollingCounter(
    count: Int, // The number to display.
    modifier: Modifier = Modifier // A modifier for the composable.
) {
    // To make the counter look like a mechanical one with a fixed width, we pad the number with leading zeros.
    // Here, we assume a 3-digit counter, so a number like 7 will be displayed as "007".
    val digitString = count.toString().padStart(3, '0')
    // Convert the string of digits into a list of integers.
    val digits = digitString.map { it.digitToInt() }

    // Arrange the digits in a horizontal row.
    Row(modifier = modifier) {
        // Loop through each digit and display it using the 'RollingDigit' composable.
        digits.forEach { digit ->
            RollingDigit(digit)
        }
    }
}

/**
 * A composable that displays a single digit with a rolling animation when it changes.
 */
@OptIn(ExperimentalAnimationApi::class) // This API is experimental and might change.
@Composable
fun RollingDigit(digit: Int) {
    // 'AnimatedContent' is a powerful composable that handles the animation between different content states.
    AnimatedContent(
        targetState = digit, // The target digit we want to display.
        // The 'transitionSpec' defines how the animation should look.
        transitionSpec = {
            // This creates an animation that looks like a wheel rolling upwards.
            // The new digit slides in from the bottom.
            // The old digit slides out towards the top.
            (slideInVertically { height -> height } + fadeIn()) with
            (slideOutVertically { height -> -height } + fadeOut())
        }
    ) { targetDigit ->
        // The content of the animation is an 'Image' of the digit.
        Image(
            painter = painterResource(getDigitResId(targetDigit)),
            contentDescription = null, // The digits are decorative, no need for a content description.
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxHeight()
        )
    }
}

/**
 * A private helper function to get the correct drawable resource ID for a given digit.
 */
private fun getDigitResId(digit: Int): Int {
    return when (digit) {
        0 -> R.drawable.zero_tile
        1 -> R.drawable.one_tile
        2 -> R.drawable.two_tile
        3 -> R.drawable.three_tile
        4 -> R.drawable.four_tile
        5 -> R.drawable.five_tile
        6 -> R.drawable.six_tile
        7 -> R.drawable.seven_tile
        8 -> R.drawable.eight_tile
        9 -> R.drawable.nine_tile
        else -> R.drawable.zero_tile // Default to the zero tile if the digit is invalid.
    }
}
