package com.kimLunation.moon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kimLunation.moon.ui.HudPlaque
import com.kimLunation.moon.ui.MoonDiskEngine
import kotlin.math.roundToInt

import com.kimLunation.moon.ui.HudLayerRes

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.Instant

import com.kimLunation.moon.astronomy.KimConfig
import com.kimLunation.moon.astronomy.MoonFullMoonsMeeus



enum class DebugHudElement {
    DIGITS, ILLUMINATION, MOON_NAME, LUNATION_BORDER
}


class MainActivity : ComponentActivity() {
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

@Composable
fun MoonScene() {
    val context = LocalContext.current
    val density = LocalDensity.current

    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) {
        while (isActive) {
            now = Instant.now()
            delay(60000)
        }
    }

    val fullMoonCount = remember(now) {
        MoonFullMoonsMeeus.countFullMoons(KimConfig.BIRTH_INSTANT, now)
    }


    // --- Debug State ---
    var isDebugMode by remember { mutableStateOf(false) }
    var selectedHudElement by remember { mutableStateOf(DebugHudElement.DIGITS) }

    // --- HUD Object Offsets (for debug tuning) ---
    var digitsOffset by remember { mutableStateOf(DpOffset(0.dp, 0.dp)) }
    var illumOffset by remember { mutableStateOf(DpOffset(0.dp, 0.dp)) }
    var nameOffset by remember { mutableStateOf(DpOffset(0.dp, 0.dp)) }
    var lunationBorderOffset by remember { mutableStateOf(DpOffset(256.dp, 66.dp)) }
    var lunationBorderScale by remember { mutableStateOf(1.0f) }
    var lunationBorderHeight by remember { mutableStateOf(42.dp) } // set to match digitHeight you use



    // --- Finalized Scene Sizes ---
    val astrolabeSizeDp = 505f
    val moonSizeDp = 281f
    val hudScale = 1.0f
    val hudOffsetY = 35.dp
    val astrolabeAlpha = 1.0f
    val digitHeight = (69/2).dp
    val digitSpacing = 0.dp



    // --- Phone & Animation State ---
    val rollDeg by rememberPhoneRollDegrees(context)

    // --- Gestures for Debug Mode ---
    val gestureModifier = if (isDebugMode) {
        Modifier.pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                val dx = with(density) { dragAmount.x.toDp() }
                val dy = with(density) { dragAmount.y.toDp() }

                when (selectedHudElement) {
                    DebugHudElement.DIGITS -> digitsOffset = DpOffset(digitsOffset.x + dx, digitsOffset.y + dy)
                    DebugHudElement.ILLUMINATION -> illumOffset = DpOffset(illumOffset.x + dx, illumOffset.y + dy)
                    DebugHudElement.MOON_NAME -> nameOffset = DpOffset(nameOffset.x + dx, nameOffset.y + dy)
                    DebugHudElement.LUNATION_BORDER -> lunationBorderOffset = DpOffset(lunationBorderOffset.x + dx, lunationBorderOffset.y + dy)
                }

            }
        }
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(gestureModifier)
    ) {
        // --- SCENE LAYERS ---
        Image(painter = painterResource(id = R.drawable.starfield_birth_malaga), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

        Box(modifier = Modifier.align(Alignment.Center)) {
            MoonDiskEngine(modifier = Modifier.size(moonSizeDp.dp).rotate(-rollDeg))
        }

        Image(painter = painterResource(id = R.drawable.astrolabe_ring), contentDescription = null, modifier = Modifier.align(Alignment.Center).size(astrolabeSizeDp.dp).alpha(astrolabeAlpha), contentScale = ContentScale.Fit)

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(200.dp)
                .offset(y = hudOffsetY)
                .scale(hudScale)
        ) {
            HudPlaque(
                modifier = Modifier.fillMaxSize(),
                digitsOffset = digitsOffset,
                illumOffset = illumOffset,
                nameOffset = nameOffset,
                lunationCount = fullMoonCount,
                layers = HudLayerRes.fromProjectAssets(),
                contentScale = ContentScale.Fit,
                digitHeight =  digitHeight, // Default height for digit tiles
                digitSpacing = digitSpacing,   // Spacing between digit ti
                lunationBorderOffset = lunationBorderOffset,
                lunationBorderScale = lunationBorderScale,
                lunationBorderHeight = lunationBorderHeight,


                )
        }

        // --- DEBUG UI ---
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .clickable {
                    if (!isDebugMode) {
                        isDebugMode = true
                    } else {
                        // Cycle through elements to adjust
                        selectedHudElement = when (selectedHudElement) {
                            DebugHudElement.DIGITS -> DebugHudElement.ILLUMINATION
                            DebugHudElement.ILLUMINATION -> DebugHudElement.MOON_NAME
                            DebugHudElement.MOON_NAME -> DebugHudElement.LUNATION_BORDER
                            DebugHudElement.LUNATION_BORDER -> DebugHudElement.DIGITS
                        }
                    }
                }
        ) {
            Text(
                text = "DEBUG: ${if (isDebugMode) "ON" else "OFF"}",
                color = if (isDebugMode) Color.Green else Color.Red,
                fontSize = 16.sp
            )
            if (isDebugMode) {
                Text(text = "Selected: $selectedHudElement", color = Color.Yellow, fontSize = 12.sp)
                Text(text = "Digits Offset: (${digitsOffset.x.value.roundToInt()}, ${digitsOffset.y.value.roundToInt()})", color = Color.White, fontSize = 12.sp)
                Text(text = "Illum Offset: (${illumOffset.x.value.roundToInt()}, ${illumOffset.y.value.roundToInt()})", color = Color.White, fontSize = 12.sp)
                Text(text = "Name Offset: (${nameOffset.x.value.roundToInt()}, ${nameOffset.y.value.roundToInt()})", color = Color.White, fontSize = 12.sp)
                Text(
                    text = "DIGITS_RENDER: offset=(${digitsOffset.x.value}, ${digitsOffset.y.value}) dp",
                    color = Color.Cyan,
                    fontSize = 12.sp
                )
                Text(
                    text = "DIGITS_RENDER: height=${50} dp spacing=${2} dp scale=${1.0f}",
                    color = Color.Cyan,
                    fontSize = 12.sp
                )
                Text(
                    text = "Lunation Border Offset: (${lunationBorderOffset.x.value.roundToInt()}, ${lunationBorderOffset.y.value.roundToInt()})",
                    color = Color.White,
                    fontSize = 12.sp
                )


            }
        }
    }
}
