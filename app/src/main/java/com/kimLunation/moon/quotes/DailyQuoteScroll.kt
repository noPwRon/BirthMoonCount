// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon.quotes

// Imports are used to bring in code from other parts of the project or from external libraries.
import androidx.compose.animation.AnimatedContent // Animates content changes between states.
import androidx.compose.animation.AnimatedVisibility // A composable that animates the appearance and disappearance of its content.
import androidx.compose.animation.fadeIn // An animation that fades in content.
import androidx.compose.animation.fadeOut // An animation that fades out content.
import androidx.compose.animation.core.FastOutSlowInEasing // Easing function for smooth animations.
import androidx.compose.animation.core.animateColorAsState // Animates color changes smoothly.
import androidx.compose.animation.core.tween // A timing function for animations.
import androidx.compose.animation.core.togetherWith // Combines enter/exit transitions.
import androidx.compose.foundation.Image // A composable for displaying images.
import androidx.compose.foundation.background // A modifier to set the background color of a composable.
import androidx.compose.foundation.border // A modifier to draw a border around a composable.
import androidx.compose.foundation.layout.Box // A composable that stacks its children on top of each other.
import androidx.compose.foundation.layout.BoxWithConstraints // A Box that provides the size constraints of its parent.
import androidx.compose.foundation.layout.Column // A composable that arranges its children in a vertical sequence.
import androidx.compose.foundation.layout.fillMaxWidth // A modifier to make a composable fill its available width.
import androidx.compose.foundation.layout.heightIn // A modifier to set a minimum and/or maximum height.
import androidx.compose.foundation.layout.padding // A modifier to add padding around a composable.
import androidx.compose.foundation.layout.sizeIn // A modifier to set a minimum and/or maximum size.
import androidx.compose.foundation.layout.width // A modifier to set the width of a composable.
import androidx.compose.foundation.shape.RoundedCornerShape // A shape with rounded corners.
import androidx.compose.material3.Icon // A composable for displaying an icon.
import androidx.compose.material3.IconButton // A composable for a clickable icon button.
import androidx.compose.material3.MaterialTheme // Provides styling for Material Design components.
import androidx.compose.material3.Surface // A container that can have a background color and elevation.
import androidx.compose.material3.Text // A composable for displaying text.
import androidx.compose.runtime.Composable // An annotation that marks a function as a Jetpack Compose UI component.
import androidx.compose.runtime.LaunchedEffect // A coroutine scope that is tied to the lifecycle of a composable.
import androidx.compose.runtime.mutableStateOf // Creates a mutable state object that is observable by Compose.
import androidx.compose.runtime.remember // Remembers a value across recompositions.
import androidx.compose.runtime.getValue // A delegate to get the value of a State object.
import androidx.compose.runtime.setValue // A delegate to set the value of a State object.
import androidx.compose.ui.Alignment // Used to specify the alignment of a composable within its parent.
import androidx.compose.ui.Modifier // An object that can be used to add behavior or decoration to a composable.
import androidx.compose.ui.draw.shadow // A modifier to add a shadow to a composable.
import androidx.compose.ui.graphics.Color // Represents a color.
import androidx.compose.ui.layout.ContentScale // Defines how to scale content within a composable.
import androidx.compose.ui.res.painterResource // A function to load a drawable resource as a Painter.
import androidx.compose.ui.text.TextStyle // Represents the style of text.
import androidx.compose.ui.text.font.FontFamily // Represents a font family.
import androidx.compose.ui.text.font.FontStyle // Represents the style of a font (e.g., italic).
import androidx.compose.ui.text.font.FontWeight // Represents the weight (boldness) of a font.
import androidx.compose.ui.text.style.TextAlign // Defines the alignment of text.
import androidx.compose.ui.text.style.TextOverflow // Defines how to handle text that overflows its container.
import androidx.compose.ui.unit.Dp // A unit of measurement for density-independent pixels.
import androidx.compose.ui.unit.dp // A unit of measurement for density-independent pixels.
import androidx.compose.ui.unit.sp // A unit of measurement for scalable pixels (for text).
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
    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        // 'BoxWithConstraints' provides the maximum width and height available, which is useful for responsive layouts.
        BoxWithConstraints(
            modifier = Modifier
                .shadow(8.dp, RoundedCornerShape(12.dp)) // Add a shadow to the scroll.
        ) {
            val maxWidth = maxWidth
            // Clamp the width of the image to a maximum of 360.dp.
            val imageWidth = maxWidth.coerceAtMost(360.dp)
            // The text box is made smaller than the image to create a nice margin.
            val textBoxWidth = imageWidth * 0.6f
            val textBoxMinHeight = textBoxWidth * 1.5f
            Box(
                modifier = Modifier
                    .sizeIn(maxWidth = imageWidth)
                    .align(Alignment.TopCenter)
            ) {
                // The parchment scroll image.
                Image(
                    painter = painterResource(id = R.drawable.scroll),
                    contentDescription = null, // Decorative image.
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 8.dp),
                    contentScale = ContentScale.Fit
                )
                // A 'Column' is used to arrange the quote text and the author vertically.
                Column(
                    modifier = Modifier
                        .align(Alignment.Center) // Center the column within the scroll image.
                        .width(textBoxWidth)
                        .heightIn(min = textBoxMinHeight) // Set a minimum height for the text box.
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                        // If 'showDebugBounds' is true, draw a red border around the text box.
                        .then(if (showDebugBounds) Modifier.border(1.dp, Color.Red) else Modifier),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (showText) {
                        AnimatedContent(
                            targetState = quote,
                            transitionSpec = {
                                fadeOut(tween(500)) togetherWith fadeIn(tween(900, delayMillis = 150))
                            },
                            label = "quoteSwap"
                        ) { animatedQuote ->
                            val baseColor = MaterialTheme.colorScheme.onSurface
                            val emberColor = Color(0xFFC07A3F)
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

                            // Flowing cursive text with a brief ember-to-ink burn-in.
                            AutoSizeText(
                                text = animatedQuote.text,
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = FontFamily.Cursive,
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 22.sp,
                                    color = animatedColor
                                ),
                                textAlign = TextAlign.Start,
                                maxLines = 6
                            )
                            Text(
                                text = "~ ${animatedQuote.author}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Cursive,
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Medium,
                                    color = animatedColor.copy(alpha = 0.85f)
                                ),
                                modifier = Modifier
                                    .padding(top = 12.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.End,
                                maxLines = 1,
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
 * A small, pill-shaped button for reopening the quote scroll when it's hidden.
 */
@Composable
fun QuoteReopenChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 6.dp, // Add a subtle tint to the surface.
        shadowElevation = 6.dp, // Add a shadow.
        onClick = onClick
    ) {
        Text(
            text = "Today's quote",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

/**
 * A composable that automatically adjusts the text size to fit within the available space.
 * It starts with a large font size and shrinks it down until the text no longer overflows.
 */
@Composable
private fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int,
    minSize: Dp = 12.dp, // The minimum font size.
    maxSize: Dp = 18.dp  // The maximum font size.
) {
    var textStyle by remember { mutableStateOf(style.copy(fontSize = maxSize.value.sp)) }
    var ready by remember { mutableStateOf(false) } // A flag to prevent unnecessary recompositions.

    Text(
        text = text,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        style = textStyle,
        modifier = modifier,
        // The 'onTextLayout' callback is called after the text has been laid out.
        onTextLayout = { result ->
            // If the text has overflowed its container...
            if (!ready && result.didOverflowHeight) {
                // ...shrink the font size by 1sp.
                val nextSize = (textStyle.fontSize.value - 1).coerceAtLeast(minSize.value)
                if (nextSize < textStyle.fontSize.value) {
                    textStyle = textStyle.copy(fontSize = nextSize.sp)
                } else {
                    // We've reached the minimum size, so we're done.
                    ready = true
                }
            } else {
                // The text fits, so we're done.
                ready = true
            }
        }
    )
}
