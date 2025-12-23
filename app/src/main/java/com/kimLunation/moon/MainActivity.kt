// Main scene composition and UI state for the app.
package com.kimLunation.moon

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.GsonBuilder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.kimLunation.moon.astronomy.KimConfig
import com.kimLunation.moon.astronomy.MoonFullMoonsMeeus
import com.kimLunation.moon.astronomy.MoonPhase
import com.kimLunation.moon.astronomy.MoonStats
import com.kimLunation.moon.astronomy.MoonZodiac
import com.kimLunation.moon.astronomy.ZodiacSign
import com.kimLunation.moon.journal.JournalEntry
import com.kimLunation.moon.journal.JournalGlyphButton
import com.kimLunation.moon.journal.JournalRepository
import com.kimLunation.moon.journal.JournalReviewScreen
import com.kimLunation.moon.journal.JournalReviewViewModel
import com.kimLunation.moon.journal.JournalReviewViewModelFactory
import com.kimLunation.moon.journal.JournalScreen
import com.kimLunation.moon.journal.JournalSkyStamp
import com.kimLunation.moon.quotes.DailyQuoteRepository
import com.kimLunation.moon.quotes.DailyQuoteScroll
import com.kimLunation.moon.quotes.Quote
import com.kimLunation.moon.ui.HudLayerRes
import com.kimLunation.moon.ui.HudLayerTransform
import com.kimLunation.moon.ui.HudPlaque
import com.kimLunation.moon.ui.HudPlaqueTransforms
import com.kimLunation.moon.ui.MoonDiskEngine
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * Keys for HUD layer transforms used by the debug layout controls.
 */
enum class DebugHudElement {
    PLAQUE,
    ILLUMINATION,
    MOON_PHASE,
    MOON_IN,
    LUNATION_BORDER,
    ILLUMINATION_BORDER,
    MOON_IN_BORDER,
    LUNATION_LABEL,
    ILLUMINATION_LABEL,
    MOON_IN_LABEL,
    COMPASS_CIRCLE,
    COMPASS_RIDGE,
    COMPASS_ARROW,
    COMPASS_DETAIL_LOWER,
    DIGITS
}

/**
 * Formats zodiac enum names into UI-facing labels.
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
 * Maps zodiac signs to their HUD tile artwork.
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
 * Activity host that installs the Compose scene.
 */
class MainActivity : ComponentActivity() {
    /**
     * Installs the Compose content and theme wrapper.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = Color.Black) {
                    MoonScene()
                }
            }
        }
    }
}

/**
 * Composes the main moon scene, HUD, journal, and quote UI layers.
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MoonScene() {
    // Central UI host that ties together data, overlays, and debug controls.
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
    // UI-scoped coroutines for save/export work.
    val coroutineScope = rememberCoroutineScope()
    // Gate debug UI on app debuggability.
    val isDebuggable = remember {
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    // Live clock used for daily quote and moon calculations.
    var now by remember { mutableStateOf(Instant.now()) }
    val currentLocalDay = remember(now) { LocalDateTime.ofInstant(now, ZoneId.systemDefault()).toLocalDate() }
    val quoteRepository = remember { DailyQuoteRepository(context) }
    val journalRepository = remember { JournalRepository(context) }
    val journalReviewViewModel: JournalReviewViewModel =
        viewModel(factory = JournalReviewViewModelFactory(journalRepository))
    val journalExportGson = remember { GsonBuilder().setPrettyPrinting().create() }
    var todayQuote by remember { mutableStateOf<Quote?>(null) }
    var quoteVisible by remember { mutableStateOf(false) }
    var quoteDebugMode by remember { mutableStateOf(false) }
    var quoteCycleEnabled by remember { mutableStateOf(false) }
    var quoteDay by remember { mutableStateOf<LocalDate?>(null) }
    var debugMenuEnabled by remember { mutableStateOf(false) }
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
    var journalIsPeriod by remember { mutableStateOf(false) }
    var journalIsGreenEvent by remember { mutableStateOf(false) }
    var journalIsYellowEvent by remember { mutableStateOf(false) }
    var journalIsBlueEvent by remember { mutableStateOf(false) }

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

    // Load the initial daily quote when the scene enters composition.
    LaunchedEffect(Unit) {
        todayQuote = quoteRepository.getQuoteForToday()
        quoteVisible = todayQuote != null
        quoteDay = currentLocalDay
    }

    // Keep time-based astronomy values up to date.
    LaunchedEffect(Unit) {
        while (isActive) {
            now = Instant.now()
            delay(60000)
        }
    }

    // Refresh the daily quote when the local day changes.
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
            journalIsPeriod = entry.isPeriod
            journalIsGreenEvent = entry.isGreenEvent
            journalIsYellowEvent = entry.isYellowEvent
            journalIsBlueEvent = entry.isBlueEvent
        } else {
            journalBody = ""
            journalMoodX = 0f
            journalMoodY = 0f
            journalIsPeriod = false
            journalIsGreenEvent = false
            journalIsYellowEvent = false
            journalIsBlueEvent = false
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

    // Recompute the full moon count whenever the time tick changes.
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


    // Debug-only overrides for HUD values.
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

    // Default HUD layer transforms used as the debug baseline.
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
            DebugHudElement.LUNATION_BORDER to HudLayerTransform(offset = DpOffset(270.dp, 105.dp),scale = 0.95f),
            DebugHudElement.LUNATION_LABEL to HudLayerTransform(offset = DpOffset(240.dp, 80.dp), scale = 0.5f),
            DebugHudElement.ILLUMINATION_LABEL to HudLayerTransform(offset = DpOffset(55.dp, 80.dp), scale = 0.5f),
            DebugHudElement.MOON_IN_LABEL to HudLayerTransform(offset = DpOffset((-10).dp, 80.dp), scale = 0.5f)
        )
    }

    val hudTransforms = remember { defaultTransforms }
    val moonInSignTransforms = remember { ZodiacSign.entries.associateWith { HudLayerTransform() } }


    // Resolve the active transform for a HUD layer.
    fun hudTransformFor(element: DebugHudElement): HudLayerTransform {
        return hudTransforms[element] ?: HudLayerTransform()
    }

    // Per-zodiac offset/scale overrides for the moon-in tile art.
    fun signTransformFor(sign: ZodiacSign): HudLayerTransform {
        return moonInSignTransforms[sign] ?: HudLayerTransform()
    }

    // Combine the shared moon-in transform with a per-sign tweak.
    fun combineTransforms(base: HudLayerTransform, detail: HudLayerTransform): HudLayerTransform {
        return HudLayerTransform(
            offset = DpOffset(base.offset.x + detail.offset.x, base.offset.y + detail.offset.y),
            scale = base.scale * detail.scale
        )
    }

    // Tunable lunation border height for HUD layout tweaks.
    var lunationBorderHeight by remember { mutableStateOf(42.dp) }

    // Fixed scene sizing constants.
    val astrolabeSizeDp = 505f
    val moonSizeDp = 281f
    val hudScale = 1.0f
    val hudOffsetY = 35.dp
    val astrolabeAlpha = 1.0f
    val digitHeight = (69/2).dp
    val digitSpacing = 0.dp

    // Select the active moon-in tile and lunation count (debug overrides when enabled).
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

    // Debug: advance to the next quote without marking it as used.
    fun showNextDebugQuote() {
        coroutineScope.launch {
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

    // Auto-cycle quotes in debug mode.
    LaunchedEffect(quoteCycleEnabled, quoteDebugMode) {
        if (!quoteCycleEnabled || !quoteDebugMode) return@LaunchedEffect
        while (isActive && quoteCycleEnabled && quoteDebugMode) {
            showNextDebugQuote()
            delay(1500)
        }
    }

    // Scene composition stack and overlays.
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Scene layers render bottom-to-top in declaration order.
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

        // Astrolabe ring sits above the moon disk.
        Image(painter = painterResource(id = R.drawable.astrolabe_ring), contentDescription = null, modifier = Modifier.align(Alignment.Center).size(astrolabeSizeDp.dp).alpha(astrolabeAlpha), contentScale = ContentScale.Fit)

        // HUD plaque anchored to the bottom of the scene.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(200.dp)
                .offset(y = hudOffsetY)
                .scale(hudScale)
        ) {
            // HUD plaque stack with debug-tunable layer transforms.
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
                    lunationLabel = hudTransformFor(DebugHudElement.LUNATION_LABEL),
                    illumLabel = hudTransformFor(DebugHudElement.ILLUMINATION_LABEL),
                    moonInLabel = hudTransformFor(DebugHudElement.MOON_IN_LABEL),
                    compassCircle = hudTransformFor(DebugHudElement.COMPASS_CIRCLE),
                    compassRidge = hudTransformFor(DebugHudElement.COMPASS_RIDGE),
                    compassArrow = hudTransformFor(DebugHudElement.COMPASS_ARROW),
                    compassDetailLower = hudTransformFor(DebugHudElement.COMPASS_DETAIL_LOWER),
                    digits = hudTransformFor(DebugHudElement.DIGITS)
                )
            )
        }

        // Daily quote scroll overlay.
        todayQuote?.let { quote ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
            )@Suppress("UnusedBoxWithConstraintsScope") {
                // Precompute scroll positions for minimized/expanded states.
                val scrollMaxWidth = maxWidth.coerceAtMost(360.dp)
                val journalButtonSize = 108.dp
                val journalButtonSpacing = 8.dp
                val density = LocalDensity.current
                var scrollSizePx by remember { mutableStateOf(IntSize.Zero) }
                val closedScale = 0.10f
                val openScale = 1f
                // Anchor the minimized scroll to the top-right.
                val closedX = maxWidth - scrollMaxWidth - 16.dp
                val closedY = 16.dp
                // Center the expanded scroll.
                val openX = (maxWidth - scrollMaxWidth) / 2
                val openY = 16.dp

                // Animate scroll position and scale between states.
                val animatedX by animateDpAsState(if (quoteVisible) openX else closedX, label = "quoteX")
                val animatedY by animateDpAsState(if (quoteVisible) openY else closedY, label = "quoteY")
                val animatedScale by animateFloatAsState(if (quoteVisible) openScale else closedScale, label = "quoteScale")
                val scaledScrollHeight = with(density) { (scrollSizePx.height * animatedScale).toDp() }
                // val journalButtonX = animatedX + scrollMaxWidth - journalButtonSize + 37.dp
                val journalButtonX =  321.dp
                // val journalButtonY = animatedY + scaledScrollHeight + journalButtonSpacing - 10.dp
                val journalButtonY =  65.dp

                // When open, allow tap or upward swipe anywhere to dismiss.
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

                // Quote scroll container.
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
                        // Apply scale animation via graphicsLayer.
                        .graphicsLayer {
                            scaleX = animatedScale
                            scaleY = animatedScale
                            // Scale from the top-right to keep the mini scroll pinned.
                            transformOrigin = TransformOrigin(1f, 0f)
                        }
                        // Tap the mini scroll to expand.
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

        // Debug UI overlay.
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
                isPeriod = journalIsPeriod,
                onTogglePeriod = { journalIsPeriod = !journalIsPeriod },
                isGreenEvent = journalIsGreenEvent,
                onToggleGreenEvent = { journalIsGreenEvent = !journalIsGreenEvent },
                isYellowEvent = journalIsYellowEvent,
                onToggleYellowEvent = { journalIsYellowEvent = !journalIsYellowEvent },
                isBlueEvent = journalIsBlueEvent,
                onToggleBlueEvent = { journalIsBlueEvent = !journalIsBlueEvent },
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
                        moonSign = stampForSave.moonSign,
                        isPeriod = journalIsPeriod,
                        isGreenEvent = journalIsGreenEvent,
                        isYellowEvent = journalIsYellowEvent,
                        isBlueEvent = journalIsBlueEvent
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
                readOnly = false,
                engraveNonce = journalEngraveNonce,
                modifier = Modifier.zIndex(3f)
            )
        }
    }
}
