package com.kimLunation.moon.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.kimLunation.moon.R

@Composable
fun RollingCounter(
    count: Int,
    modifier: Modifier = Modifier
) {
    // Pad with leading zeros or just show the number? 
    // Usually counters have fixed width. Let's assume 3 digits for now based on "three digit counter" request.
    // If count < 100, pad? "007" looks cooler on a mechanical counter.
    val digitString = count.toString().padStart(3, '0')
    val digits = digitString.map { it.digitToInt() }

    Row(modifier = modifier) {
        digits.forEach { digit ->
            RollingDigit(digit)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RollingDigit(digit: Int) {
    AnimatedContent(
        targetState = digit,
        transitionSpec = {
            // Simulating a wheel rolling 'up' to the next number
            // Enter from bottom, Exit to top
            slideInVertically { height -> height } + fadeIn() with
            slideOutVertically { height -> -height } + fadeOut()
        }
    ) { targetDigit ->
        Image(
            painter = painterResource(getDigitResId(targetDigit)),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxHeight()
        )
    }
}

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
        else -> R.drawable.zero_tile
    }
}
