// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon.ui

// Imports are used to bring in code from other parts of the project or from external libraries.
import androidx.annotation.DrawableRes // An annotation to indicate that an integer is a drawable resource ID.
import androidx.compose.animation.AnimatedContent // A container that animates its content as it changes.
import androidx.compose.animation.Crossfade // A composable that crossfades between two layouts.
import androidx.compose.animation.ExperimentalAnimationApi // An annotation for experimental animation APIs.
import androidx.compose.animation.fadeIn // An animation that fades in content.
import androidx.compose.animation.fadeOut // An animation that fades out content.
import androidx.compose.animation.slideInVertically // An animation that slides content in vertically.
import androidx.compose.animation.slideOutVertically // An animation that slides content out vertically.
import androidx.compose.animation.togetherWith // A function to combine animations.
import androidx.compose.animation.core.Animatable // A value holder that can be animated.
import androidx.compose.animation.core.FastOutSlowInEasing // An easing function that starts fast and slows down.
import androidx.compose.animation.core.RepeatMode // Defines how a repeatable animation should behave.
import androidx.compose.animation.core.animateFloat // Animates a float value.
import androidx.compose.animation.core.infiniteRepeatable // Creates an infinitely repeating animation.
import androidx.compose.animation.core.rememberInfiniteTransition // Remembers an infinite transition.
import androidx.compose.animation.core.tween // An animation that interpolates values over a specific duration.
import androidx.compose.foundation.Image // A composable for displaying images.
import androidx.compose.foundation.layout.Box // A composable that stacks its children on top of each other.
import androidx.compose.foundation.layout.Row // A composable that arranges its children in a horizontal sequence.
import androidx.compose.foundation.layout.Spacer // A composable that creates an empty space.
import androidx.compose.foundation.layout.fillMaxHeight // A modifier to make a composable fill its available height.
import androidx.compose.foundation.layout.fillMaxWidth // A modifier to make a composable fill its available width.
import androidx.compose.foundation.layout.height // A modifier to set the height of a composable.
import androidx.compose.foundation.layout.offset // A modifier to offset a composable from its original position.
import androidx.compose.foundation.layout.width // A modifier to set the width of a composable.
import androidx.compose.material3.Text // A composable for displaying text.
import androidx.compose.runtime.Composable // An annotation that marks a function as a Jetpack Compose UI component.
import androidx.compose.runtime.LaunchedEffect // A coroutine scope that is tied to the lifecycle of a composable.
import androidx.compose.runtime.SideEffect // A composable that runs a side effect after every recomposition.
import androidx.compose.runtime.getValue // A delegate to get the value of a State object.
import androidx.compose.runtime.mutableStateOf // Creates a mutable state object that is observable by Compose.
import androidx.compose.runtime.remember // Remembers a value across recompositions.
import androidx.compose.runtime.setValue // A delegate to set the value of a State object.
import androidx.compose.ui.Alignment // Used to specify the alignment of a composable within its parent.
import androidx.compose.ui.Modifier // An object that can be used to add behavior or decoration to a composable.
import androidx.compose.ui.draw.drawBehind // A modifier to draw behind the content of a composable.
import androidx.compose.ui.draw.drawWithContent // A modifier to draw with the content of a composable.
import androidx.compose.ui.layout.ContentScale // Defines how to scale content within a composable.
import androidx.compose.ui.res.painterResource // A function to load a drawable resource as a Painter.
import androidx.compose.ui.graphics.BlendMode // Defines how to blend two graphical objects.
import androidx.compose.ui.graphics.Brush // A brush to paint a shape with.
import androidx.compose.ui.graphics.Color // Represents a color.
import androidx.compose.ui.graphics.Shadow // Represents a shadow to be drawn under text.
import androidx.compose.ui.text.TextStyle // Represents the style of text.
import androidx.compose.ui.text.font.FontFamily // Represents a font family.
import androidx.compose.ui.text.style.TextAlign // Defines the alignment of text.
import androidx.compose.ui.unit.Dp // A unit of measurement for density-independent pixels.
import androidx.compose.ui.unit.dp // A unit of measurement for density-independent pixels.
import androidx.compose.ui.unit.sp // A unit of measurement for scalable pixels (for text).
import com.kimLunation.moon.DpOffset // A data class for representing a 2D offset using Dp units.
import com.kimLunation.moon.R // A class that contains all the resource IDs for the project.
import kotlin.math.abs // A function to get the absolute value of a number.
import androidx.compose.ui.draw.scale // A modifier to scale a composable up or down.

/**
 * A data class that holds the drawable resource IDs for all the layers of the HUD plaque.
 * Using '@DrawableRes' ensures that only valid drawable resource IDs can be passed.
 * Defaulting to 0 means that if a resource is not provided, it will not be drawn.
 */
data class HudLayerRes(
    @DrawableRes val hudPlaque: Int = 0,

    // Drawable layers for illumination and moon sign.
    @DrawableRes val illumDrawable: Int = 0,
    @DrawableRes val moonInDrawable: Int = 0,

    // Border layers for different sections of the HUD.
    @DrawableRes val lunationBorder: Int = 0,
    @DrawableRes val illumBorder: Int = 0,
    @DrawableRes val moonInBorder: Int = 0,

    // Layers for the compass element.
    @DrawableRes val compassUnderlay: Int = 0,
    @DrawableRes val compassCircle: Int = 0,
    @DrawableRes val compassRidge: Int = 0,
    @DrawableRes val arrow: Int = 0,
    @DrawableRes val compassDetailLower: Int = 0
) {
    /**
     * A 'companion object' is an object that is tied to a class, rather than to an instance of the class.
     * It's similar to static members in other languages.
     */
    companion object {
        /**
         * This function creates a 'HudLayerRes' object with the default drawable resources from the project.
         * This is a convenient way to initialize the HUD with all the necessary images.
         */
        fun fromProjectAssets(): HudLayerRes = HudLayerRes(
            hudPlaque = R.drawable.hud_plaque,
            illumDrawable = 0, // Placeholder, will be replaced with the actual illumination display.
            moonInDrawable = 0, // Placeholder, will be replaced with the current moon sign tile.

            lunationBorder = R.drawable.lunation_border,
            illumBorder = R.drawable.illum_border,
            moonInBorder = R.drawable.moon_in_border,

            compassCircle = R.drawable.compass_circle,
            compassRidge = R.drawable.compass_ridge,
            arrow = R.drawable.compass_arrow,
            compassDetailLower = R.drawable.compass_detail_lower
        )
    }
}

/**
 * A data class to represent a transformation for a HUD layer.
 * It includes an offset (position) and a scale (size).
 */
data class HudLayerTransform(
    val offset: DpOffset = DpOffset(0.dp, 0.dp), // The position offset of the layer.
    val scale: Float = 1.0f // The scale of the layer.
)

/**
 * A data class that holds all the individual transformations for each layer of the HUD plaque.
 * This allows for fine-grained control over the position and scale of each element.
 */
data class HudPlaqueTransforms(
    val plaque: HudLayerTransform = HudLayerTransform(),
    val illumDrawable: HudLayerTransform = HudLayerTransform(),
    val moonInDrawable: HudLayerTransform = HudLayerTransform(),
    val moonPhaseLabel: HudLayerTransform = HudLayerTransform(),
    val lunationBorder: HudLayerTransform = HudLayerTransform(),
    val illumBorder: HudLayerTransform = HudLayerTransform(),
    val moonInBorder: HudLayerTransform = HudLayerTransform(),
    val compassUnderlay: HudLayerTransform = HudLayerTransform(),
    val compassCircle: HudLayerTransform = HudLayerTransform(),
    val compassRidge: HudLayerTransform = HudLayerTransform(),
    val compassArrow: HudLayerTransform = HudLayerTransform(),
    val compassDetailLower: HudLayerTransform = HudLayerTransform(),
    val digits: HudLayerTransform = HudLayerTransform()
)

/**
 * The main composable function for the HUD plaque.
 * This function takes all the necessary data (lunation count, illumination, etc.) and composes the final HUD image
 * by stacking and transforming all the individual layers.
 */
@Composable
fun HudPlaque(
    modifier: Modifier = Modifier,
    lunationCount: Int, // The current lunation count.
    illuminationPercent: Int? = null, // The current illumination percentage of the moon.
    moonPhaseLabel: String? = null, // The name of the current moon phase.
    layers: HudLayerRes = HudLayerRes.fromProjectAssets(), // The drawable resources for the layers.
    contentScale: ContentScale = ContentScale.Fit, // How to scale the images.
    digitHeight: Dp = 44.dp, // The height of the digit tiles.
    digitSpacing: Dp = 0.dp, // The spacing between the digit tiles.
    lunationBorderHeight: Dp = digitHeight, // The height of the lunation border.
    transforms: HudPlaqueTransforms = HudPlaqueTransforms() // The transformations for each layer.
) {
    // A 'Box' is used to stack all the layers on top of each other.
    Box(modifier = modifier) {
        // The base plaque image.
        LayerImage(
            resId = layers.hudPlaque,
            contentScale = contentScale,
            modifier = Modifier.applyTransform(transforms.plaque)
        )

        // The illumination and moon-in layers.
        IlluminationLayer(
            percent = illuminationPercent,
            resId = layers.illumDrawable,
            contentScale = contentScale,
            modifier = Modifier.applyTransform(transforms.illumDrawable)
        )
        AnimatedMoonInLayer(
            resId = layers.moonInDrawable,
            contentScale = contentScale,
            modifier = Modifier.applyTransform(transforms.moonInDrawable)
        )

        // The border layers.
        LayerImage(
            resId = layers.lunationBorder,
            contentScale = contentScale,
            modifier = Modifier
                .height(lunationBorderHeight)
                .applyTransform(transforms.lunationBorder)
        )
        LayerImage(
            resId = layers.illumBorder,
            contentScale = contentScale,
            modifier = Modifier.applyTransform(transforms.illumBorder)
        )
        LayerImage(
            resId = layers.moonInBorder,
            contentScale = contentScale,
            modifier = Modifier.applyTransform(transforms.moonInBorder)
        )

        // The compass layers.
        LayerImage(
            resId = layers.compassUnderlay,
            contentScale = contentScale,
            modifier = Modifier.applyTransform(transforms.compassUnderlay)
        )
        LayerImage(
            resId = layers.compassCircle,
            contentScale = contentScale,
            modifier = Modifier.applyTransform(transforms.compassCircle)
        )
        LayerImage(
            resId = layers.compassRidge,
            contentScale = contentScale,
            modifier = Modifier.applyTransform(transforms.compassRidge)
        )
        LayerImage(
            resId = layers.compassDetailLower,
            contentScale = contentScale,
            modifier = Modifier.applyTransform(transforms.compassDetailLower)
        )

        // The lunation counter digits are drawn on top of the other layers.
        LunationDigits(
                count = lunationCount,
                modifier = Modifier
                    .height(digitHeight)
                    .applyTransform(transforms.digits),
                digitSpacing = digitSpacing
            )
        // The moon phase label, if it exists.
        if (!moonPhaseLabel.isNullOrBlank()) {
            MoonPhaseLabel(
                text = moonPhaseLabel,
                modifier = Modifier.applyTransform(transforms.moonPhaseLabel)
            )
        }
    }
}

/**
 * A composable function to display the lunation count using animated digit tiles.
 */
@Composable
private fun LunationDigits(
    count: Int,
    modifier: Modifier,
    digitSpacing: Dp
) {
    val n = abs(count).coerceAtMost(999) // Ensure the number is between 0 and 999.
    var prevCount by remember { mutableStateOf(n) }
    val direction = if (n >= prevCount) 1 else -1 // Determine the direction of the animation.
    SideEffect { prevCount = n } // Update the previous count after each recomposition.
    val s = n.toString().padStart(3, '0') // Format the number as a 3-digit string.
    val d0 = s[0].digitToInt() // Get the first digit.
    val d1 = s[1].digitToInt() // Get the second digit.
    val d2 = s[2].digitToInt() // Get the third digit.

    // Arrange the digit tiles in a row.
    Row(modifier = modifier) {
        AnimatedDigitTile(d0, direction, Modifier.fillMaxHeight())
        Spacer(modifier = Modifier.width(digitSpacing))
        AnimatedDigitTile(d1, direction, Modifier.fillMaxHeight())
        Spacer(modifier = Modifier.width(digitSpacing))
        AnimatedDigitTile(d2, direction, Modifier.fillMaxHeight())
    }
}

/**
 * A simple composable to display a single digit tile image.
 */
@Composable
private fun DigitTile(
    digit: Int,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = digitResId(digit)),
        contentDescription = null, // The digits are decorative, so no content description is needed.
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

/**
 * A composable that animates a single digit tile as it changes.
 */
@OptIn(ExperimentalAnimationApi::class) // This API is experimental and might change.
@Composable
private fun AnimatedDigitTile(
    digit: Int,
    direction: Int,
    modifier: Modifier = Modifier
) {
    val directionSign = if (direction >= 0) 1 else -1
    // 'AnimatedContent' animates the transition between different content.
    AnimatedContent(
        targetState = digit, // The target digit to animate to.
        // The transition specification defines the animation.
        transitionSpec = {
            val slideIn = slideInVertically(
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                initialOffsetY = { it * directionSign }
            ) + fadeIn(animationSpec = tween(durationMillis = 120))
            val slideOut = slideOutVertically(
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                targetOffsetY = { -it * directionSign }
            ) + fadeOut(animationSpec = tween(durationMillis = 120))
            slideIn togetherWith slideOut // Run the slide-in and slide-out animations together.
        },
        contentAlignment = Alignment.Center,
        label = "lunationDigit", // A label for debugging.
        modifier = modifier
    ) { targetDigit ->
        DigitTile(digit = targetDigit, modifier = Modifier.fillMaxHeight())
    }
}

/**
 * A helper function to get the drawable resource ID for a given digit.
 */
@DrawableRes
private fun digitResId(digit: Int): Int {
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
        else -> R.drawable.zero_tile // Default to zero if the digit is invalid.
    }
}

/**
 * A composable that displays either the illumination percentage or a placeholder image.
 */
@Composable
private fun IlluminationLayer(
    percent: Int?,
    @DrawableRes resId: Int,
    contentScale: ContentScale,
    modifier: Modifier = Modifier
) {
    if (percent != null) {
        // If the percentage is available, show the animated label.
        IlluminationPercentLabel(percent = percent, modifier = modifier)
    } else {
        // Otherwise, show the placeholder image.
        LayerImage(resId = resId, contentScale = contentScale, modifier = modifier)
    }
}

/**
 * A composable that displays the illumination percentage with a glowing and flickering animation.
 */
@Composable
private fun IlluminationPercentLabel(
    percent: Int,
    modifier: Modifier = Modifier
) {
    // 'rememberInfiniteTransition' creates a transition that runs indefinitely.
    val transition = rememberInfiniteTransition(label = "illumGlow")
    // 'animateFloat' creates an animated float value that changes over time.
    val glow by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse // The animation will reverse direction at the end.
        ),
        label = "illumGlowValue"
    )
    val flicker by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "illumFlicker"
    )
    val clamped = percent.coerceIn(0, 100) // Ensure the percentage is between 0 and 100.
    val text = clamped.toString()
    // A 'Box' is used to layer the glowing background and the text.
    Box(
        modifier = modifier.drawBehind { // 'drawBehind' allows drawing under the content of the composable.
            val glowColor = Color(0xFFFFC766).copy(alpha = 0.15f + (0.2f * glow))
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor, Color.Transparent),
                    center = center,
                    radius = size.minDimension * (0.7f + (0.2f * glow))
                ),
                blendMode = BlendMode.Screen // 'Screen' blend mode creates a nice glowing effect.
            )
        },
        contentAlignment = Alignment.Center
    ) {
        // Two text layers are used to create a more complex glowing and flickering effect.
        Text(
            text = text,
            color = Color(0xFFFFD37D).copy(alpha = 0.5f + (0.2f * glow)),
            fontSize = 22.sp,
            fontFamily = FontFamily.Serif,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                shadow = Shadow(
                    color = Color(0xFFFFB347).copy(alpha = 0.7f * glow),
                    blurRadius = 18f * glow
                )
            )
        )
        Text(
            text = text,
            color = Color(0xFFFFF3C1).copy(alpha = flicker),
            fontSize = 22.sp,
            fontFamily = FontFamily.Serif,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                shadow = Shadow(
                    color = Color(0xFFFFD27A).copy(alpha = 0.9f * glow),
                    blurRadius = 6f
                )
            )
        )
    }
}

/**
 * A composable to display the moon phase label.
 */
@Composable
private fun MoonPhaseLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(0xFF0F0F0F),
            fontSize = 16.sp,
            fontFamily = FontFamily.Serif,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * A composable that displays the moon-in layer with a glowing animation when it changes.
 */
@Composable
private fun AnimatedMoonInLayer(
    @DrawableRes resId: Int,
    contentScale: ContentScale,
    modifier: Modifier = Modifier
) {
    if (resId == 0) return // Don't draw anything if the resource ID is 0.
    val glow = remember { Animatable(0f) }
    // 'LaunchedEffect' is used to trigger the animation when the resource ID changes.
    LaunchedEffect(resId) {
        glow.snapTo(0f)
        glow.animateTo(1f, animationSpec = tween(260, easing = FastOutSlowInEasing))
        glow.animateTo(0f, animationSpec = tween(740, easing = FastOutSlowInEasing))
    }
    val glowAlpha = glow.value
    // 'Crossfade' animates the transition between the old and new moon-in images.
    Crossfade(targetState = resId, animationSpec = tween(320)) { targetResId ->
        Image(
            painter = painterResource(id = targetResId),
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier.drawWithContent { // 'drawWithContent' allows drawing on top of the content.
                drawContent()
                if (glowAlpha > 0f) {
                    val glowColor = Color(0xFFFFE39A).copy(alpha = 0.75f * glowAlpha)
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(glowColor, Color.Transparent),
                            center = center,
                            radius = size.minDimension * (0.55f + 0.45f * glowAlpha)
                        ),
                        blendMode = BlendMode.Screen
                    )
                }
            }
        )
    }
}

/**
 * A simple composable to display a layer image.
 */
@Composable
private fun LayerImage(
    @DrawableRes resId: Int,
    contentScale: ContentScale,
    modifier: Modifier = Modifier
) {
    if (resId == 0) return // Don't draw anything if the resource ID is 0.
    Image(
        painter = painterResource(id = resId),
        contentDescription = null, // These images are decorative.
        contentScale = contentScale,
        modifier = modifier
    )
}

/**
 * An extension function for the 'Modifier' class to apply a 'HudLayerTransform'.
 * This makes it easy to apply the offset and scale transformations to any composable.
 */
private fun Modifier.applyTransform(transform: HudLayerTransform): Modifier {
    return this
        .offset(x = transform.offset.x, y = transform.offset.y)
        .scale(transform.scale)
}
