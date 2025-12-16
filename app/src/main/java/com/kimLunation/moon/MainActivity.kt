package com.kimLunation.moon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(gestureModifier)
    ) {
        // 1. Starfield Background (Bottom)
        Image(
            painter = painterResource(id = R.drawable.starfield_birth_malaga),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Moon Disk (Middle)
        Box(modifier = Modifier.align(Alignment.Center)) {
            MoonDiskEngine(
                modifier = Modifier
                    .size(moonSizeDp.dp)
                    .rotate(-rollDeg) // Compensate for phone roll
            )
        }

        // 3. Astrolabe Ring (Top)
        Image(
            painter = painterResource(id = R.drawable.astrolabe_ring),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(astrolabeSizeDp.dp)
                .alpha(astrolabeAlpha),
            contentScale = ContentScale.Fit
        )

        // 4. Debug Readout (Top Left)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .clickable { isDebugMode = !isDebugMode }
        ) {
            Text(
                text = "DEBUG: ${if (isDebugMode) "ON" else "OFF"}",
                color = if (isDebugMode) Color.Green else Color.Red,
                fontSize = 16.sp
            )
            if (isDebugMode) {
                Text(
                    text = "Astrolabe Size: ${astrolabeSizeDp.roundToInt()} dp",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Text(
                    text = "Moon Size: ${moonSizeDp.roundToInt()} dp",
                    color = Color.White,
                    fontSize = 12.sp
                )
                 Text(
                    text = "Fit Ratio: %.2f".format(moonSizeDp / astrolabeSizeDp),
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
