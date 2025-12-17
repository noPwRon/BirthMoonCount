package com.kimLunation.moon.ui

import android.graphics.BitmapShader
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import com.kimLunation.moon.R
import com.kimLunation.moon.astronomy.KimConfig
import com.kimLunation.moon.astronomy.MoonOrientation
import com.kimLunation.moon.astronomy.MoonPhase
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.Instant
import kotlin.math.*

/**
 * AGSL Shader Source Code
 * Spherical Projection and Phase Lighting.
 */
private const val MOON_SHADER_SRC = """
    uniform shader uTexture;
    uniform float2 resolution;
    uniform float3 sunDir;
    uniform float2 texSize;

    const float PI = 3.14159265359;

    half4 main(float2 coord) {
        // 1. Normalize coordinates to -1..1 relative to the circle center.
        float2 uv = (coord - 0.5 * resolution) / (0.5 * resolution.y);
        
        // 2. Check if pixel is inside the sphere disk (radius 1.0).
        float rSq = dot(uv, uv);
        if (rSq > 1.0) {
            return half4(0.0, 0.0, 0.0, 0.0); // Transparent outside
        }
        
        // 3. Calculate 3D Normal.
        float z = sqrt(1.0 - rSq);
        float3 normal = float3(uv.x, uv.y, z);
        
        // 4. Spherical Mapping (Inverse Equirectangular).
        float lon = atan(normal.x, normal.z); // Longitude
        float lat = asin(normal.y);           // Latitude
        
        // Map Longitude (-PI..PI) to U (0..1)
        float u = 0.5 + lon / (2.0 * PI);
        // Map Latitude (PI/2..-PI/2) to V (0..1)
        float v = 0.5 - lat / PI;
        
        // Sample the texture
        float2 texCoord = float2(u, v) * texSize;
        half4 color = uTexture.eval(texCoord);
        
        // 5. Lighting (Phase).
        float diffuse = max(dot(normal, sunDir), 0.0);
        
        // Ambient term
        float ambient = 0.05;
        float intensity = ambient + (1.0 - ambient) * diffuse;
        
        return half4(color.rgb * intensity, 1.0);
    }
"""

/**
 * MoonDiskEngine
 *
 * Renders the Moon Disk using a shader (if supported) or fallback image.
 * This component is now pure: it only renders the moon, not the surrounding scene.
 */
@Composable
fun MoonDiskEngine(
    modifier: Modifier = Modifier.size(260.dp)
) {
    // Time State: Updates every minute to keep astronomy fresh.
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) {
        while (isActive) {
            now = Instant.now()
            delay(60000)
        }
    }

    // --- Astronomical Calculations ---
    val phaseResult = remember(now) { MoonPhase.compute(now) }

    val orientationDeg = remember(now) {
        MoonOrientation.terminatorRotationDegSkyMode(
            now,
            KimConfig.OBS_LAT,
            KimConfig.OBS_LON
        )
    }

    // --- Shader Preparation ---
    val moonBitmap = ImageBitmap.imageResource(id = R.drawable.moon_color_2k)

    val runtimeShader = remember(moonBitmap, phaseResult.fraction, orientationDeg) {
        if (Build.VERSION.SDK_INT >= 33) {
            // 1. Convert Orientation Angle (Degrees) to Radians (Chi).
            val chi = Math.toRadians(-orientationDeg)

            // 2. Calculate Phase Angle (Psi).
            val k = phaseResult.fraction.coerceIn(0.0, 1.0)
            val psi = acos(2.0 * k - 1.0)

            // 3. Compute Sun Direction Vector.
            val sunX = sin(psi) * sin(chi)
            val sunY = sin(psi) * cos(chi)
            val sunZ = cos(psi)

            // 4. Setup RuntimeShader
            val shader = RuntimeShader(MOON_SHADER_SRC)
            val androidBitmap = moonBitmap.asAndroidBitmap()
            val bitmapShader = BitmapShader(androidBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

            shader.setInputShader("uTexture", bitmapShader)
            shader.setFloatUniform("resolution", androidBitmap.width.toFloat(), androidBitmap.height.toFloat()) // Initial guess
            shader.setFloatUniform("texSize", androidBitmap.width.toFloat(), androidBitmap.height.toFloat())
            shader.setFloatUniform("sunDir", sunX.toFloat(), sunY.toFloat(), sunZ.toFloat())

            shader
        } else {
            null
        }
    }

    // --- Rendering ---
    if (Build.VERSION.SDK_INT >= 33 && runtimeShader != null) {
        Canvas(modifier = modifier) {
            // Update resolution uniform to match current draw size
            runtimeShader.setFloatUniform(
                "resolution", size.width, size.height
            )
            drawCircle(brush = ShaderBrush(runtimeShader))
        }
    } else {
        // Fallback (API < 33)
        Image(
            bitmap = moonBitmap,
            contentDescription = "Moon",
            modifier = modifier
                .clip(CircleShape)
                .graphicsLayer {
                    rotationZ = orientationDeg.toFloat()
                },
            contentScale = ContentScale.Crop
        )
    }
}
