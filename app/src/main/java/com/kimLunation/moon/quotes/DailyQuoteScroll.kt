// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon.quotes

// Imports are used to bring in code from other parts of the project or from external libraries.
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kimLunation.moon.R // A class that contains all the resource IDs for the project.

/**
 * A composable function that displays a daily quote on a parchment scroll image.
 * This composable can be shown in a minimized or a full-size state.
 * The caller is responsible for controlling its visibility and handling the close action.
 */
@Composable
fun DailyQuoteScroll(
    quote: Quote, // The quote to be displayed.
    modifier: Modifier = Modifier, // A modifier for the composable.
    showText: Boolean = true, // Whether to show the text of the quote or not.
    showCloseButton: Boolean = false, // Whether to show a close button.
    showDebugBounds: Boolean = false, // A flag to show debug borders around the text box.
    onClose: () -> Unit // A function to be called when the close button is clicked.
) {
    // A 'Box' is used to stack the scroll image and the text on top of each other.
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        val scrollWidth = maxWidth.coerceAtMost(360.dp)
        val shadowInset = 12.dp
        val imageWidth = (scrollWidth - shadowInset * 2).coerceAtLeast(0.dp)
        // The text box is made smaller than the image to create a nice margin.
        val textBoxWidth = imageWidth * 0.6f
        Box(
            modifier = Modifier
                .sizeIn(maxWidth = scrollWidth)
        ) {
            // The parchment scroll image.
            Image(
                painter = painterResource(id = R.drawable.scroll),
                contentDescription = null, // Decorative image.
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(imageWidth),
                contentScale = ContentScale.Fit
            )
            // A 'Column' is used to arrange the quote text and the author vertically.
            Column(
                modifier = Modifier
                    .align(Alignment.Center) // Center the column within the scroll image.
                    .width(textBoxWidth)
                    .fillMaxHeight() // Fill the height to allow vertical centering.
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    // If 'showDebugBounds' is true, draw a red border around the text box.
                    .then(if (showDebugBounds) Modifier.border(1.dp, Color.Red) else Modifier),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center // This centers the content vertically.
            ) {
                if (showText) {
                    AnimatedContent(
                        targetState = quote,
                        transitionSpec = {
                            fadeIn(tween(900, delayMillis = 150)) togetherWith fadeOut(tween(500))
                        },
                        label = "quoteSwap"
                    ) { animatedQuote ->
                        val baseColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.94f)
                        val emberColor = Color(0xFF4B2F1A) // Darker brown for better contrast.
                        var burnPulse by remember(animatedQuote.id) { mutableStateOf(false) }
                        LaunchedEffect(animatedQuote.id) {
                            burnPulse = false
                            burnPulse = true
                        }
                        val animatedColor by animateColorAsState(
                            targetValue = if (burnPulse) baseColor else emberColor,
                            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                            label = "burnColor"
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Flowing cursive text with a brief ember-to-ink burn-in.
                            AutoSizeText(
                                text = animatedQuote.text,
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = FontFamily.Cursive,
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 22.sp,
                                    color = animatedColor
                                ),
                                textAlign = TextAlign.Start, // Keep text left-justified.
                                maxLines = 6
                            )
                            Text(
                                text = "~ ${animatedQuote.author}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Cursive,
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.SemiBold,
                                    color = animatedColor.copy(alpha = 0.9f)
                                ),
                                modifier = Modifier
                                    .padding(top = 14.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.End, // Keep author right-justified.
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
        // The close button, if it should be shown.
        if (showCloseButton) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * A composable that automatically adjusts the text size to fit within the available space.
 * This optimized version calculates the best font size more efficiently using a binary search.
 */
@Composable
private fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int,
    minSize: TextUnit = 12.sp,
    maxSize: TextUnit = 18.sp
) {
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(modifier = modifier) {
        val optimalSize = remember(
            text,
            style,
            constraints.maxWidth,
            constraints.maxHeight,
            maxLines,
            minSize,
            maxSize
        ) {
            binarySearchFontSize(text, style, textMeasurer, minSize, maxSize, maxLines, constraints)
        }

        Text(
            text = text,
            style = style.copy(fontSize = optimalSize),
            maxLines = maxLines,
            textAlign = textAlign,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun binarySearchFontSize(
    text: String,
    style: TextStyle,
    textMeasurer: TextMeasurer,
    low: TextUnit,
    high: TextUnit,
    maxLines: Int,
    constraints: androidx.compose.ui.unit.Constraints
): TextUnit {
    var optimal = low
    var lowSp = low.value
    var highSp = high.value

    repeat(10) { // Limit iterations to avoid infinite loops, 10 is enough for good precision.
        if (lowSp > highSp) return optimal
        val midSp = (lowSp + highSp) / 2
        val mid = midSp.sp
        val layoutResult = textMeasurer.measure(
            text = text,
            style = style.copy(fontSize = mid),
            maxLines = maxLines,
            constraints = constraints.copy(minWidth = 0, minHeight = 0)
        )
        if (layoutResult.hasVisualOverflow) {
            highSp = midSp
        } else {
            optimal = mid
            lowSp = midSp
        }
    }
    return optimal
}
