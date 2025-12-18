package com.kimLunation.moon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kimLunation.moon.ui.MoonDiskEngine
import kotlin.math.roundToInt

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

    // UI State for Debug Mode
    var isDebugMode by remember { mutableStateOf(false) }

    // Dynamic Sizing State - default values are now finalized.
    var astrolabeSizeDp by remember { mutableStateOf(505f) }
    var moonSizeDp by remember { mutableStateOf(281f) }
    
    // Alpha is now 1.0f always (removed transparency reduction)
    val astrolabeAlpha = 1.0f

    // Phone Tilt for Moon Orientation
    val rollDeg by rememberPhoneRollDegrees(context)
    
    // Gestures for sizing (Only in Debug Mode)
    val gestureModifier = if (isDebugMode) {
        Modifier.pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                // Vertical drag -> Adjust Astrolabe Size
                val dy = -dragAmount.y // Drag up to increase
                astrolabeSizeDp = (astrolabeSizeDp + dy).coerceIn(400f, 1000f)

                // Horizontal drag -> Adjust Moon Size
                val dx = dragAmount.x
                moonSizeDp = (moonSizeDp + dx).coerceIn(100f, 500f)
            }
        }
    } else {
        Modifier
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .then(gestureModifier)
    ) {
        val minDim = min(maxWidth, maxHeight)

        // Use debug-controlled pixel sizes when debugging, otherwise responsive sizing.
        val astrolabeSize = if (isDebugMode) astrolabeSizeDp.dp else minDim * 0.9f
        val moonSize = if (isDebugMode) moonSizeDp.dp else minDim * 0.44f

        // 1. Starfield Background (Bottom)
        Image(
            painter = painterResource(id = R.drawable.starfield_birth_malaga),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Moon Disk (Middle)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            MoonDiskEngine(
                modifier = Modifier
                    .size(moonSize)
                    .rotate(-rollDeg) // Compensate for phone roll
            )
        }

        // 3. Astrolabe Ring (Top)
        Image(
            painter = painterResource(id = R.drawable.astrolabe_ring),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(astrolabeSize)
                .alpha(astrolabeAlpha),
            contentScale = ContentScale.Fit
        )

        // 4. Interactive HUD Plaque (Top End) - toggles debug
        Image(
            painter = painterResource(id = R.drawable.hud_plaque),
            contentDescription = "HUD Plaque",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(minDim * 0.28f)
                .clickable { isDebugMode = !isDebugMode },
            contentScale = ContentScale.Fit
        )

        // 5. Debug Readout (Top Left)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            // Small hit target to toggle debug
            Text(
                text = "DEBUG: ${if (isDebugMode) "ON" else "OFF"}",
                color = if (isDebugMode) Color.Green else Color.Red,
                fontSize = 16.sp,
                modifier = Modifier.clickable { isDebugMode = !isDebugMode }
            )
            if (isDebugMode) {
                Text(
                    text = "Astrolabe Size: ${astrolabeSize.toString()}",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Text(
                    text = "Moon Size: ${moonSize.toString()}",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Text(
                    text = "Fit Ratio: %.2f".format(
                        if (isDebugMode) (moonSizeDp / astrolabeSizeDp) else (moonSize.value / astrolabeSize.value)
                    ),
                    color = Color.Yellow,
                    fontSize = 12.sp
                )
                Text(
                    text = "Roll: ${rollDeg.toInt()}°",
                    color = Color.Yellow,
                    fontSize = 12.sp
                )
            }
        }
    }
}
