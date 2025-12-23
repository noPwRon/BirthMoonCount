// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon

// Imports are used to bring in code from other parts of the project or from external libraries.
import android.Manifest // Manifest constants for permissions.
import android.annotation.SuppressLint // Suppresses lint warnings when permissions are guarded.
import android.content.pm.ApplicationInfo // Provides access to application-specific information, like whether the app is debuggable.
import android.content.pm.PackageManager // Provides permission constants and checks.
import android.os.Bundle // Used to save and restore an activity's state.
import androidx.activity.ComponentActivity // The base class for activities that use Jetpack Compose.
import androidx.activity.compose.rememberLauncherForActivityResult // Launches permission requests from Compose.
import androidx.activity.compose.setContent // A function to set the Jetpack Compose content for an activity.
import androidx.activity.result.contract.ActivityResultContracts // Activity result contracts (permissions).
import androidx.compose.animation.core.animateDpAsState // Animates a Dp (density-independent pixel) value.
import androidx.compose.animation.core.animateFloatAsState // Animates a float (decimal number) value.
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image // A composable for displaying images.
import androidx.compose.foundation.clickable // A modifier to make a composable clickable.
import androidx.compose.foundation.gestures.detectTapGestures // A gesture detector for taps.
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource // Represents a stream of interactions for a component.
import androidx.compose.foundation.layout.Arrangement // Used to specify the arrangement of children in a Row or Column.
import androidx.compose.foundation.layout.Box // A composable that stacks its children on top of each other.
import androidx.compose.foundation.layout.BoxWithConstraints // A Box that provides the size constraints of its parent.
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf // Creates a mutable state object that is observable by Compose.
import androidx.compose.runtime.remember // Remembers a value across recompositions.
import androidx.compose.runtime.rememberCoroutineScope // Remembers a coroutine scope across recompositions.
import androidx.compose.runtime.setValue // A delegate to set the value of a State object.
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment // Used to specify the alignment of a composable within its parent.
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier // An object that can be used to add behavior or decoration to a composable.
import androidx.compose.ui.draw.alpha // A modifier to change the transparency of a composable.
import androidx.compose.ui.draw.scale // A modifier to scale a composable up or down.
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color // Represents a color.
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TransformOrigin // The point from which a transformation (like scaling) originates.
import androidx.compose.ui.graphics.graphicsLayer // A modifier for applying graphical effects.
import androidx.compose.ui.input.pointer.pointerInput // A modifier to handle pointer input (like taps and drags).
import androidx.compose.ui.layout.ContentScale // Defines how to scale content within a composable.
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext // Provides the current Android Context.
import androidx.compose.ui.res.painterResource // A function to load a drawable resource as a Painter.
import androidx.compose.ui.unit.dp // A unit of measurement for density-independent pixels.
import androidx.compose.ui.unit.sp // A unit of measurement for scalable pixels (for text).
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat // Compatibility helpers for permission checks.
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.GsonBuilder
import com.google.android.gms.location.LocationServices // Fused location provider.
import com.google.android.gms.location.Priority // Location request priorities.
import com.kimLunation.moon.astronomy.KimConfig // A configuration file for astronomy calculations.
import com.kimLunation.moon.astronomy.MoonFullMoonsMeeus // A utility for calculating full moons.
import com.kimLunation.moon.astronomy.MoonPhase // A utility for calculating the moon phase.
import com.kimLunation.moon.astronomy.MoonStats // A utility for getting moon statistics.
import com.kimLunation.moon.astronomy.MoonZodiac // A utility for calculating the moon's zodiac sign.
import com.kimLunation.moon.astronomy.ZodiacSign // An enum representing the zodiac signs.
import com.kimLunation.moon.journal.JournalEntry
import com.kimLunation.moon.journal.JournalGlyphButton
import com.kimLunation.moon.journal.JournalRepository
import com.kimLunation.moon.journal.JournalReviewScreen
import com.kimLunation.moon.journal.JournalReviewViewModel
import com.kimLunation.moon.journal.JournalReviewViewModelFactory
import com.kimLunation.moon.journal.JournalScreen
import com.kimLunation.moon.journal.JournalSkyStamp
import com.kimLunation.moon.quotes.DailyQuoteRepository // The repository for daily quotes.
import com.kimLunation.moon.quotes.DailyQuoteScroll // A composable for the daily quote scroll.
import com.kimLunation.moon.quotes.Quote // The data class for a quote.
import com.kimLunation.moon.ui.HudLayerRes // A data class for the HUD layer resources.
import com.kimLunation.moon.ui.HudLayerTransform // A data class for HUD layer transformations.
import com.kimLunation.moon.ui.HudPlaque // A composable for the main HUD plaque.
import com.kimLunation.moon.ui.HudPlaqueTransforms // A data class for all HUD plaque transformations.
import com.kimLunation.moon.ui.MoonDiskEngine // A composable for the moon disk.
import java.time.Instant // Represents a point in time.
import java.time.LocalDate // Represents a date without time.
import java.time.LocalDateTime // Represents a date-time without a time-zone.
import java.time.ZoneId
import java.util.Locale // Represents a specific geographical, political, or cultural region.
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay // A function to pause a coroutine for a specified time.
import kotlinx.coroutines.isActive // A property to check if a coroutine is still active.
import kotlinx.coroutines.launch // A function to start a new coroutine.
import kotlin.math.PI
import kotlin.math.roundToInt // A function to round a number to the nearest integer.
import kotlin.math.sin
import kotlin.random.Random

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
    LUNATION_BORDER,
    ILLUMINATION_BORDER,
    MOON_IN_BORDER,
    COMPASS_CIRCLE,
    COMPASS_RIDGE,
    COMPASS_ARROW,
    COMPASS_DETAIL_LOWER,
    DIGITS
}

/**
 * This function returns a human-readable label for a given 'ZodiacSign'.
 * It takes the enum name, converts it to lowercase, and then capitalizes the first letter.
 */
private fun zodiacLabel(sign: ZodiacSign): String {
    val lower = sign.name.lowercase(Locale.US)
    return lower.replaceFirstChar { it.uppercase(Locale.US) }
}

private fun phaseLabelForIllumination(fraction: Double, waxing: Boolean): String {
    val f = fraction.coerceIn(0.0, 1.0)
    return when {
        f <= 0.03 -> "New Moon"
        f >= 0.97 -> "Full Moon"
        f < 0.47 -> if (waxing) "Waxing Crescent" else "Waning Crescent"
        f <= 0.53 -> if (waxing) "First Quarter" else "Last Quarter"
        else -> if (waxing) "Waxing Gibbous" else "Waning Gibbous"
    }
}

private data class NebulaPulse(
    val centerX: Float,
    val centerY: Float,
    val minRadius: Float,
    val maxRadius: Float,
    val periodSeconds: Float,
    val phase: Float
)

@Composable
private fun NebulaBreathingOverlay(
    modifier: Modifier = Modifier,
    circleCount: Int = 9,
    maxAlpha: Float = 0.5f
) {
    val centers = remember {
        listOf(
            Offset(0.16f, 0.12f),
            Offset(0.82f, 0.18f),
            Offset(0.10f, 0.56f),
            Offset(0.88f, 0.48f),
            Offset(0.22f, 0.72f),
            Offset(0.44f, 0.78f),
            Offset(0.60f, 0.80f),
            Offset(0.74f, 0.86f),
            Offset(0.12f, 0.88f)
        )
    }
    val pulses = remember(circleCount) {
        val random = Random(9317)
        List(circleCount) {
            val center = centers[it % centers.size]
            val minRadius = 0.32f + random.nextFloat() * 0.18f
            val maxRadius = minRadius + 0.12f + random.nextFloat() * 0.20f
            NebulaPulse(
                centerX = center.x,
                centerY = center.y,
                minRadius = minRadius,
                maxRadius = maxRadius,
                periodSeconds = 22f + random.nextFloat() * 26f,
                phase = random.nextFloat() * (2f * PI.toFloat())
            )
        }
    }
    var timeSeconds by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            timeSeconds = (now - start) / 1_000_000_000f
        }
    }
    Canvas(
        modifier = modifier.graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
        }
    ) {
        val minDim = minOf(size.width, size.height)
        pulses.forEach { pulse ->
            val angle = (timeSeconds / pulse.periodSeconds) * (2f * PI.toFloat()) + pulse.phase
            val intensity = 0.5f + 0.5f * sin(angle)
            val radius = minDim * (pulse.minRadius + intensity * (pulse.maxRadius - pulse.minRadius))
            val alpha = maxAlpha * (0.20f + 0.80f * intensity)
            val center = Offset(size.width * pulse.centerX, size.height * pulse.centerY)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = alpha),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center,
                blendMode = BlendMode.Screen
            )
        }
    }
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
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MoonScene() {
    // This is the central host for the UI. It fetches moon and quote data, provides debug controls,
    // and combines all the different visual layers.

    // 'LocalContext.current' gives us the application's context, which is needed for things like accessing resources.
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var observerLat by remember { mutableDoubleStateOf(KimConfig.OBS_LAT) }
    var observerLon by remember { mutableDoubleStateOf(KimConfig.OBS_LON) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    @SuppressLint("MissingPermission")
    fun refreshObserverLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                observerLat = location.latitude
                observerLon = location.longitude
            }
        }
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    observerLat = location.latitude
                    observerLon = location.longitude
                }
            }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
        if (granted) {
            refreshObserverLocation()
        }
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        hasLocationPermission = granted
        if (granted) {
            refreshObserverLocation()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }
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
    val currentLocalDay = remember(now) { LocalDateTime.ofInstant(now, ZoneId.systemDefault()).toLocalDate() }
    val quoteRepository = remember { DailyQuoteRepository(context) } // The repository for fetching quotes.
    val journalRepository = remember { JournalRepository(context) }
    val journalReviewViewModel: JournalReviewViewModel =
        viewModel(factory = JournalReviewViewModelFactory(journalRepository))
    val journalExportGson = remember { GsonBuilder().setPrettyPrinting().create() }
    var todayQuote by remember { mutableStateOf<Quote?>(null) } // The quote for the current day.
    var quoteVisible by remember { mutableStateOf(false) } // Whether the quote scroll is visible or not.
    var quoteDebugMode by remember { mutableStateOf(false) } // A flag for debugging the quote display.
    var quoteCycleEnabled by remember { mutableStateOf(false) } // Automatically cycle quotes in debug mode.
    var quoteDay by remember { mutableStateOf<LocalDate?>(null) } // The day associated with the current quote.
    var debugMenuEnabled by remember { mutableStateOf(false) } // Whether the debug menu is enabled.
    var journalVisible by remember { mutableStateOf(false) }
    var journalReviewVisible by remember { mutableStateOf(false) }
    var journalReviewSeedEnabled by remember { mutableStateOf(false) }
    var journalRecencyPreviewEnabled by remember { mutableStateOf(false) }
    var journalEntry by remember { mutableStateOf<JournalEntry?>(null) }
    var journalEngraveNonce by remember { mutableIntStateOf(0) }
    var pendingExportJson by remember { mutableStateOf<String?>(null) }
    var journalBody by remember { mutableStateOf("") }
    var journalMoodX by remember { mutableFloatStateOf(0f) }
    var journalMoodY by remember { mutableFloatStateOf(0f) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val json = pendingExportJson ?: return@rememberLauncherForActivityResult
        pendingExportJson = null
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(json.toByteArray(Charsets.UTF_8))
            }
        }
    }

    // 'LaunchedEffect' is used to run a coroutine when the composable first appears.
    // Here, we load the daily quote when the app starts.
    LaunchedEffect(Unit) {
        todayQuote = quoteRepository.getQuoteForToday()
        quoteVisible = todayQuote != null
        quoteDay = currentLocalDay
    }

    // This 'LaunchedEffect' runs a loop that updates the 'now' state every minute.
    // This keeps the astronomical data live.
    LaunchedEffect(Unit) {
        while (isActive) { // 'isActive' is true as long as the coroutine is active.
            now = Instant.now()
            delay(60000) // Pause the coroutine for 60,000 milliseconds (1 minute).
        }
    }

    // When the local day flips, fetch the new daily quote and let the scroll animate it in.
    LaunchedEffect(currentLocalDay) {
        if (quoteDay != null && quoteDay != currentLocalDay) {
            todayQuote = quoteRepository.getQuoteForToday()
            quoteVisible = todayQuote != null
            quoteDay = currentLocalDay
        }
    }

    LaunchedEffect(journalVisible, currentLocalDay) {
        if (!journalVisible) return@LaunchedEffect
        val entry = journalRepository.getEntry(currentLocalDay.toString())
        journalEntry = entry
        if (entry != null) {
            journalBody = entry.body
            journalMoodX = entry.moodX
            journalMoodY = entry.moodY
        } else {
            journalBody = ""
            journalMoodX = 0f
            journalMoodY = 0f
        }
    }

    LaunchedEffect(journalVisible) {
        if (!journalVisible) {
            journalReviewVisible = false
        }
    }

    LaunchedEffect(journalReviewVisible, journalReviewSeedEnabled) {
        if (!journalReviewVisible) return@LaunchedEffect
        journalReviewViewModel.load(
            seedWhenEmpty = isDebuggable,
            forceSeed = journalReviewSeedEnabled
        )
    }

    // 'remember' with a key. The code inside the block will only be re-executed if the key changes.
    // Here, we recalculate the full moon count whenever 'now' changes.
    val fullMoonCount = remember(now) {
        MoonFullMoonsMeeus.countFullMoons(KimConfig.BIRTH_INSTANT, now)
    }
    val moonPhase = remember(now) { MoonPhase.compute(now) }
    val liveIlluminationPercent = remember(moonPhase) {
        (moonPhase.fraction * 100.0).roundToInt().coerceIn(0, 100)
    }
    val liveWaxing = remember(moonPhase) {
        moonPhase.ageDays <= 14.765
    }
    val moonNameLive = remember(now) { MoonStats.moonName(now) }
    val moonSign = remember(now, observerLat, observerLon, hasLocationPermission) {
        MoonZodiac.sign(now, observerLat, observerLon, useTopocentric = hasLocationPermission)
    }


    // --- Debug State ---
    var phaseOverrideEnabled by remember { mutableStateOf(false) }
    var phaseOverridePercent by remember { mutableIntStateOf(liveIlluminationPercent) }
    var phaseOverrideWaxing by remember { mutableStateOf(liveWaxing) }
    val phaseOverrideFraction = if (phaseOverrideEnabled) phaseOverridePercent / 100.0 else null
    val moonPhaseLabel = if (phaseOverrideEnabled) {
        phaseLabelForIllumination(phaseOverridePercent / 100.0, phaseOverrideWaxing)
    } else {
        moonNameLive
    }
    val illuminationPercent = if (phaseOverrideEnabled) phaseOverridePercent else liveIlluminationPercent

    var moonInOverrideEnabled by remember { mutableStateOf(false) }
    var selectedMoonInSign by remember { mutableStateOf(moonSign) }
    var lunationOverrideEnabled by remember { mutableStateOf(false) }
    var lunationOverrideCount by remember { mutableIntStateOf(fullMoonCount) }

    LaunchedEffect(liveIlluminationPercent, liveWaxing, phaseOverrideEnabled) {
        if (!phaseOverrideEnabled) {
            phaseOverridePercent = liveIlluminationPercent
            phaseOverrideWaxing = liveWaxing
        }
    }

    LaunchedEffect(moonSign, moonInOverrideEnabled) {
        if (!moonInOverrideEnabled) {
            selectedMoonInSign = moonSign
        }
    }

    LaunchedEffect(fullMoonCount, lunationOverrideEnabled) {
        if (!lunationOverrideEnabled) {
            lunationOverrideCount = fullMoonCount
        }
    }

    // 'defaultTransforms' holds the original, hard-coded positions and scales for each HUD element.
    // These are the reference values that we can reset to.
    val defaultTransforms = remember {
        mapOf(
            DebugHudElement.PLAQUE to HudLayerTransform(offset = DpOffset(0.dp, 36.dp)),
            DebugHudElement.ILLUMINATION to HudLayerTransform(offset = DpOffset((-89).dp, 113.dp), scale = 0.7f),
            DebugHudElement.MOON_PHASE to HudLayerTransform(offset = DpOffset(0.dp, 113.dp), scale = 0.65f),
            DebugHudElement.MOON_IN to HudLayerTransform(offset = DpOffset((-40).dp, 40.dp), scale = 0.2f),
            DebugHudElement.ILLUMINATION_BORDER to HudLayerTransform(offset = DpOffset(80.dp, 90.dp),scale = 0.55f),
            DebugHudElement.MOON_IN_BORDER to HudLayerTransform(offset = DpOffset(10.dp, 90.dp),scale = 0.55f),
            DebugHudElement.COMPASS_CIRCLE to HudLayerTransform(offset = DpOffset(175.dp, 44.dp), scale = 0.45f),
            DebugHudElement.COMPASS_RIDGE to HudLayerTransform(offset = DpOffset(0.dp, (-25).dp), scale = 0.35f),
            DebugHudElement.COMPASS_ARROW to HudLayerTransform(offset = DpOffset(186.dp, (-50).dp), scale = 0.3f),
            DebugHudElement.COMPASS_DETAIL_LOWER to HudLayerTransform(offset = DpOffset(0.dp, 35.dp), scale = 0.4f),
            DebugHudElement.DIGITS to HudLayerTransform(offset = DpOffset(278.dp, 110.dp), scale = 1.05f),
            DebugHudElement.LUNATION_BORDER to HudLayerTransform(offset = DpOffset(270.dp, 105.dp),scale = 0.95f)
        )
    }

    val hudTransforms = remember { defaultTransforms }
    val moonInSignTransforms = remember { ZodiacSign.entries.associateWith { HudLayerTransform() } }


    // A helper function to get the current transform for a HUD element.
    fun hudTransformFor(element: DebugHudElement): HudLayerTransform {
        return hudTransforms[element] ?: HudLayerTransform()
    }

    // A helper function to get the current transform for a zodiac sign tile.
    fun signTransformFor(sign: ZodiacSign): HudLayerTransform {
        return moonInSignTransforms[sign] ?: HudLayerTransform()
    }

    // A helper function to combine a base transform with a detail transform.
    // This is used to layer the per-sign transform on top of the shared "moon in" transform.
    fun combineTransforms(base: HudLayerTransform, detail: HudLayerTransform): HudLayerTransform {
        return HudLayerTransform(
            offset = DpOffset(base.offset.x + detail.offset.x, base.offset.y + detail.offset.y),
            scale = base.scale * detail.scale
        )
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
    val displayedMoonInSign = if (moonInOverrideEnabled) selectedMoonInSign else moonSign
    val moonInDrawable = remember(displayedMoonInSign) { zodiacTileResId(displayedMoonInSign) }
    val moonInTransform = combineTransforms(
        base = hudTransformFor(DebugHudElement.MOON_IN),
        detail = signTransformFor(displayedMoonInSign)
    )
    val lunationDisplayCount = if (lunationOverrideEnabled) lunationOverrideCount else fullMoonCount
    val currentJournalStamp = JournalSkyStamp(
        localDate = currentLocalDay.toString(),
        lunationCount = lunationDisplayCount,
        lunationDay = moonPhase.ageDays,
        phaseLabel = moonPhase.phaseName,
        illuminationPercent = illuminationPercent,
        moonSign = zodiacLabel(displayedMoonInSign)
    )
    val displayJournalStamp = journalEntry?.let { entry ->
        JournalSkyStamp(
            localDate = entry.localDate,
            lunationCount = entry.lunationCount,
            lunationDay = entry.lunationDay,
            phaseLabel = entry.phaseLabel,
            illuminationPercent = entry.illuminationPercent,
            moonSign = entry.moonSign
        )
    } ?: currentJournalStamp

    // A function to show the next debug quote.
    fun showNextDebugQuote() {
        coroutineScope.launch {
            // This bypasses the "used" tracking to quickly preview all quotes.
            todayQuote = quoteRepository.nextDebugQuote()
            quoteVisible = todayQuote != null
            quoteDay = currentLocalDay
        }
    }

    fun showLongestQuote() {
        coroutineScope.launch {
            todayQuote = quoteRepository.findLongestQuote()
            quoteVisible = todayQuote != null
        }
    }

    fun showLongestAuthorQuote() {
        coroutineScope.launch {
            todayQuote = quoteRepository.findLongestAuthor()
            quoteVisible = todayQuote != null
        }
    }

    fun adjustIllumination(delta: Int) {
        val range = 101
        val next = ((phaseOverridePercent + delta) % range + range) % range
        phaseOverridePercent = next
    }

    fun stepMoonIn(delta: Int) {
        val signs = ZodiacSign.entries
        val idx = signs.indexOf(selectedMoonInSign).coerceAtLeast(0)
        val nextIdx = (idx + delta + signs.size) % signs.size
        selectedMoonInSign = signs[nextIdx]
    }

    fun adjustLunation(delta: Int) {
        val next = (lunationOverrideCount + delta).coerceIn(0, 999)
        lunationOverrideCount = next
    }

    fun exportJournalEntries(entries: List<JournalEntry>) {
        if (entries.isEmpty()) return
        pendingExportJson = journalExportGson.toJson(entries)
        val fileName = "journal_entries_${currentLocalDay}.json"
        exportLauncher.launch(fileName)
    }

    fun buildRecencyPreview(density: FloatArray, gridSize: Int): FloatArray {
        val preview = FloatArray(density.size)
        if (gridSize <= 0) return preview
        for (i in density.indices) {
            if (density[i] <= 0f) continue
            val row = i / gridSize
            val col = i % gridSize
            preview[i] = if ((row + col) % 2 == 0) 1f else 0f
        }
        return preview
    }

    // Automatically cycle through quotes when enabled in debug UI.
    LaunchedEffect(quoteCycleEnabled, quoteDebugMode) {
        if (!quoteCycleEnabled || !quoteDebugMode) return@LaunchedEffect
        while (isActive && quoteCycleEnabled && quoteDebugMode) {
            showNextDebugQuote()
            delay(1500)
        }
    }

    // --- Phone & Animation State ---

    // The root composable of the scene.
    Box(
        modifier = Modifier
            .fillMaxSize() // Fill the whole screen.
    ) {
        // --- SCENE LAYERS ---
        // The layers are drawn in order, so the ones at the bottom of the code appear on top.
        Image(painter = painterResource(id = R.drawable.starfield_birth_malaga), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Image(
            painter = painterResource(id = R.drawable.splash_nebula),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.75f),
            contentScale = ContentScale.Crop
        )
        NebulaBreathingOverlay(
            modifier = Modifier.fillMaxSize(),
            maxAlpha = 0.15f
        )
        Box(modifier = Modifier.align(Alignment.Center)) {
            MoonDiskEngine(
                diskSize = moonSizeDp.dp,
                phaseOverrideFraction = phaseOverrideFraction
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
                    illumBorder = hudTransformFor(DebugHudElement.ILLUMINATION_BORDER),
                    moonInBorder = hudTransformFor(DebugHudElement.MOON_IN_BORDER),
                    compassCircle = hudTransformFor(DebugHudElement.COMPASS_CIRCLE),
                    compassRidge = hudTransformFor(DebugHudElement.COMPASS_RIDGE),
                    compassArrow = hudTransformFor(DebugHudElement.COMPASS_ARROW),
                    compassDetailLower = hudTransformFor(DebugHudElement.COMPASS_DETAIL_LOWER),
                    digits = hudTransformFor(DebugHudElement.DIGITS)
                )
            )
        }

        // The daily quote scroll.
        todayQuote?.let { quote ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
            )@Suppress("UnusedBoxWithConstraintsScope") {
                // Precompute the coordinates for the small, minimized scroll and the large, expanded scroll.
                val scrollMaxWidth = maxWidth.coerceAtMost(360.dp)
                val journalButtonSize = 108.dp
                val journalButtonSpacing = 8.dp
                val density = LocalDensity.current
                var scrollSizePx by remember { mutableStateOf(IntSize.Zero) }
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
                val scaledScrollHeight = with(density) { (scrollSizePx.height * animatedScale).toDp() }
                // val journalButtonX = animatedX + scrollMaxWidth - journalButtonSize + 37.dp
                val journalButtonX =  321.dp
                // val journalButtonY = animatedY + scaledScrollHeight + journalButtonSpacing - 10.dp
                val journalButtonY =  65.dp

                // When the scroll is open, a tap or upward swipe anywhere closes it.
                if (quoteVisible) {
                    val swipeCloseThreshold = with(density) { 24.dp.toPx() }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(4f)
                            .pointerInput(Unit) {
                                detectTapGestures { quoteVisible = false }
                            }
                            .pointerInput(Unit) {
                                var totalDrag = 0f
                                detectVerticalDragGestures(
                                    onDragStart = { totalDrag = 0f },
                                    onVerticalDrag = { _, dragAmount ->
                                        totalDrag += dragAmount
                                        if (totalDrag < -swipeCloseThreshold) {
                                            quoteVisible = false
                                        }
                                    }
                                )
                            }
                    )
                }

                // The Box containing the quote scroll.
                Box(
                    modifier = Modifier
                        .offset {
                            with(density) {
                                androidx.compose.ui.unit.IntOffset(
                                    animatedX.roundToPx(),
                                    animatedY.roundToPx()
                                )
                            }
                        }
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
                        ) { if (!quoteVisible) quoteVisible = true }
                ) {
                    DailyQuoteScroll(
                        quote = quote,
                        modifier = Modifier
                            .width(scrollMaxWidth)
                            .onSizeChanged { scrollSizePx = it },
                        showText = quoteVisible,
                        showCloseButton = false,
                        showDebugBounds = quoteDebugMode,
                        onClose = { quoteVisible = false }
                    )
                }
                JournalGlyphButton(
                    iconResId = R.drawable.journal_quill,
                    contentDescription = "Open journal",
                    onClick = { journalVisible = true },
                    modifier = Modifier
                        .offset(x = journalButtonX, y = journalButtonY)
                        .zIndex(2f),
                    size = journalButtonSize
                )
            }
        }

        // The debug UI for the quote feature.
        if (isDebuggable && debugMenuEnabled) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .zIndex(2f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = debugMenuEnabled,
                        onCheckedChange = { debugMenuEnabled = it }
                    )
                    Text(
                        text = "Debug Menu",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = journalReviewSeedEnabled,
                        onCheckedChange = { journalReviewSeedEnabled = it }
                    )
                    Text(
                        text = "Seed journal map",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = journalRecencyPreviewEnabled,
                        onCheckedChange = { journalRecencyPreviewEnabled = it }
                    )
                    Text(
                        text = "Preview recency contrast",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                }
                Row(
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
                    Checkbox(
                        checked = quoteCycleEnabled,
                        onCheckedChange = { quoteCycleEnabled = it },
                        enabled = quoteDebugMode
                    )
                    Text(
                        text = "Cycle",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { showLongestQuote() }) {
                        Text("Longest Quote")
                    }
                    Button(onClick = { showLongestAuthorQuote() }) {
                        Text("Longest Author")
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = phaseOverrideEnabled,
                        onCheckedChange = { enabled ->
                            phaseOverrideEnabled = enabled
                            if (enabled) {
                                phaseOverridePercent = liveIlluminationPercent
                                phaseOverrideWaxing = liveWaxing
                            }
                        }
                    )
                    Text(
                        text = "Illumination",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                    Button(
                        onClick = { adjustIllumination(-1) },
                        enabled = phaseOverrideEnabled
                    ) {
                        Text(text = "-")
                    }
                    Text(
                        text = "${phaseOverridePercent}%",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                    Button(
                        onClick = { adjustIllumination(1) },
                        enabled = phaseOverrideEnabled
                    ) {
                        Text(text = "+")
                    }
                    Button(
                        onClick = { phaseOverrideWaxing = !phaseOverrideWaxing },
                        enabled = phaseOverrideEnabled
                    ) {
                        Text(text = if (phaseOverrideWaxing) "Waxing" else "Waning")
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = moonInOverrideEnabled,
                        onCheckedChange = { enabled ->
                            moonInOverrideEnabled = enabled
                            if (enabled) {
                                selectedMoonInSign = moonSign
                            }
                        }
                    )
                    Text(
                        text = "Moon in",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                    Button(
                        onClick = { stepMoonIn(-1) },
                        enabled = moonInOverrideEnabled
                    ) {
                        Text(text = "-")
                    }
                    Text(
                        text = zodiacLabel(selectedMoonInSign),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                    Button(
                        onClick = { stepMoonIn(1) },
                        enabled = moonInOverrideEnabled
                    ) {
                        Text(text = "+")
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = lunationOverrideEnabled,
                        onCheckedChange = { enabled ->
                            lunationOverrideEnabled = enabled
                            if (enabled) {
                                lunationOverrideCount = fullMoonCount
                            }
                        }
                    )
                    Text(
                        text = "Lunation",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                    Button(
                        onClick = { adjustLunation(-1) },
                        enabled = lunationOverrideEnabled
                    ) {
                        Text(text = "-")
                    }
                    Text(
                        text = lunationOverrideCount.toString(),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                    Button(
                        onClick = { adjustLunation(1) },
                        enabled = lunationOverrideEnabled
                    ) {
                        Text(text = "+")
                    }
                }
            }
        }
        if (journalVisible && journalReviewVisible) {
            JournalReviewScreen(
                entries = journalReviewViewModel.entries,
                density = journalReviewViewModel.density,
                recency = if (journalRecencyPreviewEnabled) {
                    buildRecencyPreview(
                        journalReviewViewModel.density,
                        journalReviewViewModel.gridSize
                    )
                } else {
                    journalReviewViewModel.recency
                },
                gridSize = journalReviewViewModel.gridSize,
                latestPoint = journalReviewViewModel.latestPoint,
                onExport = { exportJournalEntries(it) },
                onClose = { journalReviewVisible = false },
                modifier = Modifier.zIndex(3f)
            )
        } else if (journalVisible) {
            JournalScreen(
                stamp = displayJournalStamp,
                body = journalBody,
                onBodyChange = { journalBody = it },
                moodX = journalMoodX,
                moodY = journalMoodY,
                onMoodChange = { x, y ->
                    journalMoodX = x
                    journalMoodY = y
                },
                onSave = {
                    val nowMillis = System.currentTimeMillis()
                    val existing = journalEntry
                    val stampForSave = existing?.let { entry ->
                        JournalSkyStamp(
                            localDate = entry.localDate,
                            lunationCount = entry.lunationCount,
                            lunationDay = entry.lunationDay,
                            phaseLabel = entry.phaseLabel,
                            illuminationPercent = entry.illuminationPercent,
                            moonSign = entry.moonSign
                        )
                    } ?: currentJournalStamp
                    val entry = JournalEntry(
                        id = existing?.id ?: "journal_${currentJournalStamp.localDate}",
                        localDate = currentJournalStamp.localDate,
                        createdAtMillis = existing?.createdAtMillis ?: nowMillis,
                        updatedAtMillis = nowMillis,
                        body = journalBody,
                        moodX = journalMoodX,
                        moodY = journalMoodY,
                        lunationCount = stampForSave.lunationCount,
                        lunationDay = stampForSave.lunationDay,
                        phaseLabel = stampForSave.phaseLabel,
                        illuminationPercent = stampForSave.illuminationPercent,
                        moonSign = stampForSave.moonSign
                    )
                    coroutineScope.launch {
                        journalRepository.upsertEntry(entry)
                        journalEntry = entry
                        journalEngraveNonce += 1
                    }
                },
                onReview = { journalReviewVisible = true },
                onClose = { journalVisible = false },
                saveEnabled = journalBody.isNotBlank(),
                engraveNonce = journalEngraveNonce,
                modifier = Modifier.zIndex(3f)
            )
        }
    }
}
