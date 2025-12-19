// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon

// Imports are used to bring in code from other parts of the project or from external libraries.
import android.content.pm.ApplicationInfo // Provides access to application-specific information, like whether the app is debuggable.
import android.os.Bundle // Used to save and restore an activity's state.
import androidx.activity.ComponentActivity // The base class for activities that use Jetpack Compose.
import androidx.activity.compose.setContent // A function to set the Jetpack Compose content for an activity.
import androidx.compose.animation.AnimatedVisibility // A composable that animates the appearance and disappearance of its content.
import androidx.compose.animation.fadeIn // An animation that fades in content.
import androidx.compose.animation.fadeOut // An animation that fades out content.
import androidx.compose.animation.core.animateDpAsState // Animates a Dp (density-independent pixel) value.
import androidx.compose.animation.core.animateFloatAsState // Animates a float (decimal number) value.
import androidx.compose.foundation.Image // A composable for displaying images.
import androidx.compose.foundation.clickable // A modifier to make a composable clickable.
import androidx.compose.foundation.gestures.detectTapGestures // A gesture detector for taps.
import androidx.compose.foundation.gestures.detectTransformGestures // A gesture detector for transformations like pinch-to-zoom and drag.
import androidx.compose.foundation.interaction.MutableInteractionSource // Represents a stream of interactions for a component.
import androidx.compose.foundation.layout.Arrangement // Used to specify the arrangement of children in a Row or Column.
import androidx.compose.foundation.layout.Box // A composable that stacks its children on top of each other.
import androidx.compose.foundation.layout.BoxWithConstraints // A Box that provides the size constraints of its parent.
import androidx.compose.foundation.layout.Row // A composable that arranges its children in a horizontal sequence.
import androidx.compose.foundation.layout.fillMaxSize // A modifier to make a composable fill its entire available space.
import androidx.compose.foundation.layout.fillMaxWidth // A modifier to make a composable fill its available width.
import androidx.compose.foundation.layout.height // A modifier to set the height of a composable.
import androidx.compose.foundation.layout.offset // A modifier to offset a composable from its original position.
import androidx.compose.foundation.layout.padding // A modifier to add padding around a composable.
import androidx.compose.foundation.layout.size // A modifier to set the size of a composable.
import androidx.compose.foundation.layout.width // A modifier to set the width of a composable.
import androidx.compose.material3.Button // A composable for a Material Design button.
import androidx.compose.material3.Checkbox // A composable for a Material Design checkbox.
import androidx.compose.material3.MaterialTheme // Provides styling for Material Design components.
import androidx.compose.material3.Surface // A container that can have a background color and elevation.
import androidx.compose.material3.Text // A composable for displaying text.
import androidx.compose.runtime.Composable // An annotation that marks a function as a Jetpack Compose UI component.
import androidx.compose.runtime.LaunchedEffect // A coroutine scope that is tied to the lifecycle of a composable.
import androidx.compose.runtime.getValue // A delegate to get the value of a State object.
import androidx.compose.runtime.mutableStateMapOf // Creates a mutable (changeable) map that is observable by Compose.
import androidx.compose.runtime.mutableStateOf // Creates a mutable state object that is observable by Compose.
import androidx.compose.runtime.remember // Remembers a value across recompositions.
import androidx.compose.runtime.rememberCoroutineScope // Remembers a coroutine scope across recompositions.
import androidx.compose.runtime.setValue // A delegate to set the value of a State object.
import androidx.compose.ui.Alignment // Used to specify the alignment of a composable within its parent.
import androidx.compose.ui.Modifier // An object that can be used to add behavior or decoration to a composable.
import androidx.compose.ui.draw.alpha // A modifier to change the transparency of a composable.
import androidx.compose.ui.draw.scale // A modifier to scale a composable up or down.
import androidx.compose.ui.graphics.Color // Represents a color.
import androidx.compose.ui.graphics.TransformOrigin // The point from which a transformation (like scaling) originates.
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed // Utility to detect up events without considering consumption.
import androidx.compose.ui.input.pointer.awaitPointerEventScope // Scope for awaiting low-level pointer events.
import androidx.compose.ui.input.pointer.pointerInput // A modifier to handle pointer input (like taps and drags).
import androidx.compose.ui.layout.ContentScale // Defines how to scale content within a composable.
import androidx.compose.ui.platform.LocalContext // Provides the current Android Context.
import androidx.compose.ui.platform.LocalDensity // Provides the current screen density.
import androidx.compose.ui.res.painterResource // A function to load a drawable resource as a Painter.
import androidx.compose.ui.graphics.graphicsLayer // A modifier for applying graphical effects.
import androidx.compose.ui.unit.dp // A unit of measurement for density-independent pixels.
import androidx.compose.ui.unit.sp // A unit of measurement for scalable pixels (for text).
import com.kimLunation.moon.astronomy.KimConfig // A configuration file for astronomy calculations.
import com.kimLunation.moon.astronomy.MoonFullMoonsMeeus // A utility for calculating full moons.
import com.kimLunation.moon.astronomy.MoonPhase // A utility for calculating the moon phase.
import com.kimLunation.moon.astronomy.MoonStats // A utility for getting moon statistics.
import com.kimLunation.moon.astronomy.MoonZodiac // A utility for calculating the moon's zodiac sign.
import com.kimLunation.moon.astronomy.ZodiacSign // An enum representing the zodiac signs.
import com.kimLunation.moon.ui.HudLayerRes // A data class for the HUD layer resources.
import com.kimLunation.moon.ui.HudLayerTransform // A data class for HUD layer transformations.
import com.kimLunation.moon.ui.HudPlaque // A composable for the main HUD plaque.
import com.kimLunation.moon.ui.HudPlaqueTransforms // A data class for all HUD plaque transformations.
import com.kimLunation.moon.ui.MoonDiskEngine // A composable for the moon disk.
import com.kimLunation.moon.quotes.DailyQuoteRepository // The repository for daily quotes.
import com.kimLunation.moon.quotes.DailyQuoteScroll // A composable for the daily quote scroll.
import com.kimLunation.moon.quotes.Quote // The data class for a quote.
import java.time.Instant // Represents a point in time.
import java.time.LocalDate // Represents a date without time.
import java.time.LocalDateTime // Represents a date-time without a time-zone.
import java.time.ZoneOffset // A time-zone offset from UTC.
import java.util.Locale // Represents a specific geographical, political, or cultural region.
import kotlinx.coroutines.delay // A function to pause a coroutine for a specified time.
import kotlinx.coroutines.isActive // A property to check if a coroutine is still active.
import kotlinx.coroutines.launch // A function to start a new coroutine.
import kotlin.math.roundToInt // A function to round a number to the nearest integer.

/**
 * An 'enum' (enumeration) is a special type that represents a fixed set of constants.
 * This enum, 'DebugHudElement', defines all the individual elements of the Heads-Up Display (HUD)
 * that can be manipulated in debug mode.
 */
enum class DebugHudElement {
    PLAQUE,
    ILLUMINATION,
    MOON_PHASE,
    MOON_IN,
    MOON_IN_SIGN,
    LUNATION_BORDER,
    ILLUM_BORDER,
    MOON_IN_BORDER,
    COMPASS_CIRCLE,
    COMPASS_RIDGE,
    COMPASS_ARROW,
    COMPASS_DETAIL_LOWER,
    DIGITS
}

/**
 * This is an extension property for the 'DebugHudElement' enum.
 * It provides a human-readable label for each element, which is useful for UI in debug mode.
 */
private val DebugHudElement.label: String
    get() = when (this) { // 'when' is like a 'switch' statement in other languages.
        DebugHudElement.PLAQUE -> "Plaque"
        DebugHudElement.ILLUMINATION -> "Illumination"
        DebugHudElement.MOON_PHASE -> "Moon Phase"
        DebugHudElement.MOON_IN -> "Moon In (Base)"
        DebugHudElement.MOON_IN_SIGN -> "Moon In (Sign)"
        DebugHudElement.LUNATION_BORDER -> "Lunation Border"
        DebugHudElement.ILLUM_BORDER -> "Illum Border"
        DebugHudElement.MOON_IN_BORDER -> "Moon In Border"
        DebugHudElement.COMPASS_CIRCLE -> "Compass Circle"
        DebugHudElement.COMPASS_RIDGE -> "Compass Ridge"
        DebugHudElement.COMPASS_ARROW -> "Compass Arrow"
        DebugHudElement.COMPASS_DETAIL_LOWER -> "Compass Detail Lower"
        DebugHudElement.DIGITS -> "Digits"
    }

/**
 * This function returns a human-readable label for a given 'ZodiacSign'.
 * It takes the enum name, converts it to lowercase, and then capitalizes the first letter.
 */
private fun zodiacLabel(sign: ZodiacSign): String {
    val lower = sign.name.lowercase(Locale.US)
    return lower.replaceFirstChar { it.uppercase(Locale.US) }
}

/**
 * This function returns the drawable resource ID for the tile of a given 'ZodiacSign'.
 * This is used to display the correct image for each zodiac sign in the HUD.
 */
private fun zodiacTileResId(sign: ZodiacSign): Int {
    return when (sign) {
        ZodiacSign.ARIES -> R.drawable.aries_tile
        ZodiacSign.TAURUS -> R.drawable.taurus_tile
        ZodiacSign.GEMINI -> R.drawable.gemini_tile
        ZodiacSign.CANCER -> R.drawable.cancer_tile
        ZodiacSign.LEO -> R.drawable.leo_tile
        ZodiacSign.VIRGO -> R.drawable.virgo_tile
        ZodiacSign.LIBRA -> R.drawable.libra_tile
        ZodiacSign.SCORPIO -> R.drawable.scorpio_tile
        ZodiacSign.SAGITTARIUS -> R.drawable.sagittarius_tile
        ZodiacSign.CAPRICORN -> R.drawable.capricorn_tile
        ZodiacSign.AQUARIUS -> R.drawable.aquarius_tile
        ZodiacSign.PISCES -> R.drawable.pisces_tile
    }
}

/**
 * This is the main entry point of the application. It's an 'Activity', which is a single, focused thing that the user can do.
 */
class MainActivity : ComponentActivity() {
    /**
     * This method is called when the activity is first created. It's where you do all of your normal static set up:
     * create views, bind data to lists, etc.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Always call the superclass's method first.
        // 'setContent' is where you define your UI using Jetpack Compose.
        setContent {
            // 'MaterialTheme' provides the default styling for the app.
            MaterialTheme {
                // 'Surface' is a container that provides a background color.
                Surface(color = Color.Black) {
                    // 'MoonScene' is the main composable function that contains the entire UI of the app.
                    MoonScene()
                }
            }
        }
    }
}

/**
 * This is the main composable function that builds the entire user interface of the app.
 * It's marked with '@Composable', which means it's a UI component that can be used in Jetpack Compose.
 */
@Composable
fun MoonScene() {
    // This is the central host for the UI. It fetches moon and quote data, provides debug controls,
    // and combines all the different visual layers.

    // 'LocalContext.current' gives us the application's context, which is needed for things like accessing resources.
    val context = LocalContext.current
    // 'LocalDensity.current' gives us the screen density, used for converting between pixels and Dp.
    val density = LocalDensity.current
    // 'rememberCoroutineScope' gives us a coroutine scope that is tied to the lifecycle of this composable.
    val coroutineScope = rememberCoroutineScope()
    // 'remember' is a key function in Compose. It stores a value that will survive recompositions (UI updates).
    // Here, we check if the app is in a debuggable mode.
    val isDebuggable = remember {
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    // 'mutableStateOf' creates a piece of state that Compose can observe. When this state changes, any composable
    // that uses it will be recomposed (redrawn).
    var now by remember { mutableStateOf(Instant.now()) } // The current time, updated periodically.
    val currentUtcDay = remember(now) { LocalDateTime.ofInstant(now, ZoneOffset.UTC).toLocalDate() }
    val quoteRepository = remember { DailyQuoteRepository(context) } // The repository for fetching quotes.
    var todayQuote by remember { mutableStateOf<Quote?>(null) } // The quote for the current day.
    var quoteVisible by remember { mutableStateOf(false) } // Whether the quote scroll is visible or not.
    var quoteDebugMode by remember { mutableStateOf(false) } // A flag for debugging the quote display.
    var quoteDay by remember { mutableStateOf<LocalDate?>(null) } // The day associated with the current quote.
    var debugUnlocked by remember { mutableStateOf(false) } // Whether the debug mode is unlocked.
    var secretTapCount by remember { mutableStateOf(0) } // A counter for the secret tap gesture to unlock debug mode.
    var secretFirstTapTime by remember { mutableStateOf(0L) } // The time of the first tap in the secret gesture sequence.

    // 'LaunchedEffect' is used to run a coroutine when the composable first appears.
    // Here, we load the daily quote when the app starts.
    LaunchedEffect(Unit) {
        todayQuote = quoteRepository.getQuoteForToday()
        quoteVisible = todayQuote != null
        quoteDay = currentUtcDay
    }

    // This 'LaunchedEffect' runs a loop that updates the 'now' state every minute.
    // This keeps the astronomical data live.
    LaunchedEffect(Unit) {
        while (isActive) { // 'isActive' is true as long as the coroutine is active.
            now = Instant.now()
            delay(60000) // Pause the coroutine for 60,000 milliseconds (1 minute).
        }
    }

    // When the UTC day flips, fetch the new daily quote and let the scroll animate it in.
    LaunchedEffect(currentUtcDay) {
        if (quoteDay != null && quoteDay != currentUtcDay) {
            todayQuote = quoteRepository.getQuoteForToday()
            quoteVisible = todayQuote != null
            quoteDay = currentUtcDay
        }
    }

    // 'remember' with a key. The code inside the block will only be re-executed if the key changes.
    // Here, we recalculate the full moon count whenever 'now' changes.
    val fullMoonCount = remember(now) {
        MoonFullMoonsMeeus.countFullMoons(KimConfig.BIRTH_INSTANT, now)
    }
    val moonPhase = remember(now) { MoonPhase.compute(now) }
    val illuminationPercent = remember(moonPhase) {
        (moonPhase.fraction * 100.0).roundToInt().coerceIn(0, 100)
    }
    val moonNameLive = remember(now) { MoonStats.moonName(now) }
    val moonSign = remember(now) { MoonZodiac.sign(now) }


    // --- Debug State ---
    // State variables for controlling the debug mode.
    var isDebugMode by remember { mutableStateOf(false) }
    var selectedHudElement by remember { mutableStateOf(DebugHudElement.DIGITS) }
    var moonPhaseOverride by remember { mutableStateOf<String?>(null) }

    val moonPhaseLabel = moonPhaseOverride ?: moonNameLive // Use the override if it exists, otherwise use the live name.
    val moonPhaseOptions = remember {
        // A hard-coded list of moon phase names for the debug dropdown.
        listOf(
            "Live", "Wolf Moon", "Snow Moon", "Worm Moon", "Pink Moon", "Flower Moon",
            "Strawberry Moon", "Buck Moon", "Sturgeon Moon", "Harvest Moon", "Hunter's Moon",
            "Beaver Moon", "Cold Moon", "New Moon", "Waxing Crescent", "Waxing Gibbous",
            "Full Moon", "Waning Gibbous", "Waning Crescent"
        )
    }

    // Minimum and maximum scale values for pinch-to-zoom in debug mode.
    val scaleMin = 0.2f
    val scaleMax = 4.0f

    // 'defaultTransforms' holds the original, hard-coded positions and scales for each HUD element.
    // These are the reference values that we can reset to.
    val defaultTransforms = remember {
        mapOf(
            DebugHudElement.PLAQUE to HudLayerTransform(offset = DpOffset(0.dp, 36.dp)),
            DebugHudElement.ILLUMINATION to HudLayerTransform(offset = DpOffset(-90.dp, 115.dp), scale = 0.7f),
            DebugHudElement.MOON_PHASE to HudLayerTransform(offset = DpOffset(0.dp, 113.dp), scale = 0.65f),
            DebugHudElement.MOON_IN to HudLayerTransform(offset = DpOffset(-40.dp, 40.dp), scale = 0.2f),
            DebugHudElement.ILLUM_BORDER to HudLayerTransform(offset = DpOffset(80.dp, 90.dp),scale = 0.55f),
            DebugHudElement.MOON_IN_BORDER to HudLayerTransform(offset = DpOffset(10.dp, 90.dp),scale = 0.55f),
            DebugHudElement.COMPASS_CIRCLE to HudLayerTransform(offset = DpOffset(175.dp, 44.dp), scale = 0.45f),
            DebugHudElement.COMPASS_RIDGE to HudLayerTransform(offset = DpOffset(0.dp, -25.dp), scale = 0.35f),
            DebugHudElement.COMPASS_ARROW to HudLayerTransform(offset = DpOffset(186.dp, -50.dp), scale = 0.3f),
            DebugHudElement.COMPASS_DETAIL_LOWER to HudLayerTransform(offset = DpOffset(0.dp, 35.dp), scale = 0.4f),
            DebugHudElement.DIGITS to HudLayerTransform(offset = DpOffset(278.dp, 110.dp), scale = 1.05f),
            DebugHudElement.LUNATION_BORDER to HudLayerTransform(offset = DpOffset(270.dp, 105.dp),scale = 0.95f)
        )
    }
    // 'hudTransforms' is a mutable map that holds the current transformations for each HUD element.
    // These values are changed by the debug gestures.
    val hudTransforms = remember {
        mutableStateMapOf<DebugHudElement, HudLayerTransform>().apply {
            putAll(defaultTransforms)
        }
    }

    // Default transforms for the zodiac sign tiles.
    val defaultMoonInSignTransforms = remember {
        ZodiacSign.values().associateWith { HudLayerTransform() }
    }
    // Fine-grained offsets for each zodiac tile, layered on top of the shared "Moon In" transform.
    val moonInSignTransforms = remember {
        mutableStateMapOf<ZodiacSign, HudLayerTransform>().apply {
            putAll(defaultMoonInSignTransforms)
        }
    }

    // State for the debug controls.
    var selectedMoonInSign by remember { mutableStateOf(moonSign) }
    var moonInCycleEnabled by remember { mutableStateOf(false) }
    var moonInCycleSign by remember { mutableStateOf(moonSign) }
    var lunationCycleEnabled by remember { mutableStateOf(false) }
    var lunationCycleCount by remember { mutableStateOf(fullMoonCount) }

    // This 'LaunchedEffect' ensures that the selected moon sign in the debug UI
    // stays in sync with the actual moon sign, unless debug mode is active.
    LaunchedEffect(moonSign, isDebugMode) {
        if (!isDebugMode) {
            selectedMoonInSign = moonSign
        }
    }

    // This 'LaunchedEffect' cycles through all the zodiac signs when 'moonInCycleEnabled' is true.
    // This is useful for visually tuning the position of each sign's tile.
    LaunchedEffect(moonInCycleEnabled) {
        if (!moonInCycleEnabled) return@LaunchedEffect
        moonInCycleSign = moonSign
        var idx = ZodiacSign.values().indexOf(moonInCycleSign).coerceAtLeast(0)
        while (isActive && moonInCycleEnabled) {
            delay(1200)
            idx = (idx + 1) % ZodiacSign.values().size
            moonInCycleSign = ZodiacSign.values()[idx]
        }
    }

    // This 'LaunchedEffect' cycles the lunation counter when 'lunationCycleEnabled' is true.
    // This helps with aligning the digit artwork in debug mode.
    LaunchedEffect(lunationCycleEnabled) {
        if (!lunationCycleEnabled) return@LaunchedEffect
        lunationCycleCount = fullMoonCount
        while (isActive && lunationCycleEnabled) {
            delay(800)
            lunationCycleCount = (lunationCycleCount + 1) % 1000
        }
    }

    // A helper function to get the current transform for a HUD element.
    fun hudTransformFor(element: DebugHudElement): HudLayerTransform {
        return hudTransforms[element] ?: HudLayerTransform()
    }

    // A helper function to update the transform for a HUD element.
    fun updateHudTransform(element: DebugHudElement, update: (HudLayerTransform) -> HudLayerTransform) {
        hudTransforms[element] = update(hudTransformFor(element))
    }

    // A helper function to get the current transform for a zodiac sign tile.
    fun signTransformFor(sign: ZodiacSign): HudLayerTransform {
        return moonInSignTransforms[sign] ?: HudLayerTransform()
    }

    // A helper function to update the transform for a zodiac sign tile.
    fun updateSignTransform(sign: ZodiacSign, update: (HudLayerTransform) -> HudLayerTransform) {
        moonInSignTransforms[sign] = update(signTransformFor(sign))
    }

    // A helper function to get the transform for the currently selected element in the debug UI.
    fun activeTransformFor(element: DebugHudElement): HudLayerTransform {
        return when (element) {
            DebugHudElement.MOON_IN_SIGN -> signTransformFor(selectedMoonInSign)
            else -> hudTransformFor(element)
        }
    }

    // A helper function to update the transform for the currently selected element.
    fun updateActiveTransform(element: DebugHudElement, update: (HudLayerTransform) -> HudLayerTransform) {
        when (element) {
            DebugHudElement.MOON_IN_SIGN -> updateSignTransform(selectedMoonInSign, update)
            else -> updateHudTransform(element, update)
        }
    }

    // A helper function to combine a base transform with a detail transform.
    // This is used to layer the per-sign transform on top of the shared "moon in" transform.
    fun combineTransforms(base: HudLayerTransform, detail: HudLayerTransform): HudLayerTransform {
        return HudLayerTransform(
            offset = DpOffset(base.offset.x + detail.offset.x, base.offset.y + detail.offset.y),
            scale = base.scale * detail.scale
        )
    }

    // The transform of the currently selected HUD element.
    val selectedTransform = activeTransformFor(selectedHudElement)

    // A helper function to adjust the scale of the selected element.
    fun adjustSelectedScale(delta: Float) {
        updateActiveTransform(selectedHudElement) { current ->
            val nextScale = (current.scale + delta).coerceIn(scaleMin, scaleMax)
            current.copy(scale = nextScale)
        }
    }

    // A helper function to reset the transform of the selected element to its default.
    fun resetSelectedTransform() {
        when (selectedHudElement) {
            DebugHudElement.MOON_IN_SIGN -> {
                moonInSignTransforms[selectedMoonInSign] =
                    defaultMoonInSignTransforms[selectedMoonInSign] ?: HudLayerTransform()
            }
            else -> {
                hudTransforms[selectedHudElement] = defaultTransforms[selectedHudElement] ?: HudLayerTransform()
            }
        }
    }

    // The height of the lunation border, which can be adjusted in debug mode.
    var lunationBorderHeight by remember { mutableStateOf(42.dp) }

    // --- Finalized Scene Sizes ---
    // These are the final sizes and positions for the main scene elements.
    val astrolabeSizeDp = 505f
    val moonSizeDp = 281f
    val hudScale = 1.0f
    val hudOffsetY = 35.dp
    val astrolabeAlpha = 1.0f
    val digitHeight = (69/2).dp
    val digitSpacing = 0.dp

    // Decide which moon-in tile and lunation count to display based on debug settings.
    val displayedMoonInSign = when {
        moonInCycleEnabled -> moonInCycleSign
        isDebugMode && selectedHudElement == DebugHudElement.MOON_IN_SIGN -> selectedMoonInSign
        else -> moonSign
    }
    val moonInDrawable = remember(displayedMoonInSign) { zodiacTileResId(displayedMoonInSign) }
    val moonInTransform = combineTransforms(
        base = hudTransformFor(DebugHudElement.MOON_IN),
        detail = signTransformFor(displayedMoonInSign)
    )
    val lunationDisplayCount = if (lunationCycleEnabled) lunationCycleCount else fullMoonCount

    // A function to show the next debug quote.
    fun showNextDebugQuote() {
        coroutineScope.launch {
            // This bypasses the "used" tracking to quickly preview all quotes.
            todayQuote = quoteRepository.nextDebugQuote()
            quoteVisible = todayQuote != null
            quoteDay = currentUtcDay
        }
    }

    // A function to register a tap for the secret debug unlock gesture.
    fun registerSecretTap() {
        val nowMs = System.currentTimeMillis()
        // If it's the first tap or too much time has passed since the last tap, reset the counter.
        if (secretFirstTapTime == 0L || nowMs - secretFirstTapTime > 1500) {
            secretFirstTapTime = nowMs
            secretTapCount = 1
            return
        }
        secretTapCount += 1
        // If the user has tapped 10 times in a row, unlock or lock the debug mode.
        if (secretTapCount >= 10) {
            debugUnlocked = !debugUnlocked
            // When locking, reset any debug-only flags.
            if (!debugUnlocked) {
                quoteDebugMode = false
                isDebugMode = false
            }
            secretTapCount = 0
            secretFirstTapTime = 0L
        }
    }



    // --- Phone & Animation State ---

    // --- Gestures for Debug Mode ---
    // This modifier is only applied when debug mode is active. It detects pinch-to-zoom and drag gestures
    // to allow for live adjustment of the HUD elements.
    val gestureModifier = if (isDebugMode) {
        Modifier.pointerInput(selectedHudElement, selectedMoonInSign) {
            detectTransformGestures { _, pan, zoom, _ ->
                val dx = with(density) { pan.x.toDp() }
                val dy = with(density) { pan.y.toDp() }
                updateActiveTransform(selectedHudElement) { current ->
                    val nextOffset = DpOffset(current.offset.x + dx, current.offset.y + dy)
                    val nextScale = (current.scale * zoom).coerceIn(scaleMin, scaleMax)
                    current.copy(offset = nextOffset, scale = nextScale)
                }
            }
        }
    } else {
        Modifier // An empty modifier if not in debug mode.
    }

    // The root composable of the scene.
    Box(
        modifier = Modifier
            .fillMaxSize() // Fill the whole screen.
            // Lightweight tap listener for the 10-tap unlock; does not consume so children still get events.
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { change ->
                            if (change.changedToUpIgnoreConsumed()) {
                                registerSecretTap()
                            }
                        }
                    }
                }
            }
    ) {
        // --- SCENE LAYERS ---
        // The layers are drawn in order, so the ones at the bottom of the code appear on top.
        NebulaBackground(modifier = Modifier.fillMaxSize())
        Image(painter = painterResource(id = R.drawable.starfield_birth_malaga), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(modifier = Modifier.align(Alignment.Center)) {
            MoonDiskEngine(
                modifier = Modifier
                    .size(moonSizeDp.dp)
            )
        }

        // The astrolabe ring sits on top of the moon disk.
        Image(painter = painterResource(id = R.drawable.astrolabe_ring), contentDescription = null, modifier = Modifier.align(Alignment.Center).size(astrolabeSizeDp.dp).alpha(astrolabeAlpha), contentScale = ContentScale.Fit)

        // The HUD plaque is aligned to the bottom of the screen.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(200.dp)
                .offset(y = hudOffsetY)
                .scale(hudScale)
                .then(gestureModifier) // Apply the gesture modifier here.
        ) {
            // The 'HudPlaque' composable is composed of many positioned layers.
            // The 'transforms' parameter allows for live adjustment of these layers in debug mode.
            HudPlaque(
                modifier = Modifier.fillMaxSize(),
                lunationCount = lunationDisplayCount,
                illuminationPercent = illuminationPercent,
                moonPhaseLabel = moonPhaseLabel,
                layers = HudLayerRes.fromProjectAssets().copy(moonInDrawable = moonInDrawable),
                contentScale = ContentScale.Fit,
                digitHeight = digitHeight,
                digitSpacing = digitSpacing,
                lunationBorderHeight = lunationBorderHeight,
                transforms = HudPlaqueTransforms(
                    plaque = hudTransformFor(DebugHudElement.PLAQUE),
                    illumDrawable = hudTransformFor(DebugHudElement.ILLUMINATION),
                    moonPhaseLabel = hudTransformFor(DebugHudElement.MOON_PHASE),
                    moonInDrawable = moonInTransform,
                    lunationBorder = hudTransformFor(DebugHudElement.LUNATION_BORDER),
                    illumBorder = hudTransformFor(DebugHudElement.ILLUM_BORDER),
                    moonInBorder = hudTransformFor(DebugHudElement.MOON_IN_BORDER),
                    compassCircle = hudTransformFor(DebugHudElement.COMPASS_CIRCLE),
                    compassRidge = hudTransformFor(DebugHudElement.COMPASS_RIDGE),
                    compassArrow = hudTransformFor(DebugHudElement.COMPASS_ARROW),
                    compassDetailLower = hudTransformFor(DebugHudElement.COMPASS_DETAIL_LOWER),
                    digits = hudTransformFor(DebugHudElement.DIGITS)
                )
            )
        }

        // The debug UI for the quote feature.
        if (isDebuggable && debugUnlocked) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = quoteDebugMode,
                    onCheckedChange = { quoteDebugMode = it }
                )
                Text(
                    text = "Quote debug",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )
                Button(
                    onClick = { showNextDebugQuote() },
                    enabled = quoteDebugMode
                ) {
                    Text(text = "Next")
                }
            }
        }

        // The daily quote scroll.
        todayQuote?.let { quote ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Precompute the coordinates for the small, minimized scroll and the large, expanded scroll.
                val scrollMaxWidth = maxWidth.coerceAtMost(360.dp)
                val closedScale = 0.10f
                val openScale = 1f
                // Anchor the mini scroll to the top-right.
                val closedX = maxWidth - scrollMaxWidth - 16.dp
                val closedY = 16.dp
                // Center the expanded scroll.
                val openX = (maxWidth - scrollMaxWidth) / 2
                val openY = 16.dp

                // Animate the position and scale of the scroll when it opens and closes.
                val animatedX by animateDpAsState(if (quoteVisible) openX else closedX, label = "quoteX")
                val animatedY by animateDpAsState(if (quoteVisible) openY else closedY, label = "quoteY")
                val animatedScale by animateFloatAsState(if (quoteVisible) openScale else closedScale, label = "quoteScale")

                // When the scroll is open, a tap anywhere on the screen will close it.
                if (quoteVisible) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures { quoteVisible = false }
                            }
                    )
                }

                // The Box containing the quote scroll.
                Box(
                    modifier = Modifier
                        .offset(x = animatedX, y = animatedY)
                        // The 'graphicsLayer' modifier is used to apply transformations like scaling.
                        .graphicsLayer {
                            scaleX = animatedScale
                            scaleY = animatedScale
                            // Scale from the top-right corner so the mini scroll stays in the corner.
                            transformOrigin = TransformOrigin(1f, 0f)
                        }
                        // The mini scroll is clickable to open it.
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { quoteVisible = true }
                ) {
                    DailyQuoteScroll(
                        quote = quote,
                        modifier = Modifier.width(scrollMaxWidth),
                        showText = quoteVisible,
                        showCloseButton = false,
                        showDebugBounds = quoteDebugMode,
                        onClose = { quoteVisible = false }
                    )
                }
            }
        }
    }
}
