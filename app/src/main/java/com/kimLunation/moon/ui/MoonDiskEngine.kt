// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon.ui

// Imports are used to bring in code from other parts of the project or from external libraries.
import android.graphics.BitmapShader // A shader that tiles a bitmap.
import android.graphics.RuntimeShader // A class for creating a shader from AGSL (Android Graphics Shading Language) code.
import android.graphics.Shader // The base class for objects that draw horizontally.
import android.os.Build // Provides information about the current device's Android version.
import androidx.compose.foundation.Canvas // A composable that provides a drawing scope for 2D graphics.
import androidx.compose.foundation.Image // A composable for displaying images.
import androidx.compose.foundation.layout.size // A modifier to set the size of a composable.
import androidx.compose.foundation.shape.CircleShape // A shape that is a circle.
import androidx.compose.runtime.* // Imports all core runtime functions for Compose.
import androidx.compose.ui.Modifier // An object that can be used to add behavior or decoration to a composable.
import androidx.compose.ui.draw.clip // A modifier to clip the content of a composable to a shape.
import androidx.compose.ui.graphics.ImageBitmap // A version of Bitmap that is optimized for use with Jetpack Compose.
import androidx.compose.ui.graphics.ShaderBrush // A brush that paints with a shader.
import androidx.compose.ui.graphics.asAndroidBitmap // An extension function to convert a Compose ImageBitmap to a regular Android Bitmap.
import androidx.compose.ui.graphics.graphicsLayer // A modifier for applying graphical effects.
import androidx.compose.ui.layout.ContentScale // Defines how to scale content within a composable.
import androidx.compose.ui.res.imageResource // A function to load a drawable resource as a Compose ImageBitmap.
import androidx.compose.ui.unit.dp // A unit of measurement for density-independent pixels.
import com.kimLunation.moon.R // A class that contains all the resource IDs for the project.
import com.kimLunation.moon.astronomy.KimConfig // A configuration file for astronomy calculations.
import com.kimLunation.moon.astronomy.MoonOrientation // A utility for calculating the moon's orientation.
import com.kimLunation.moon.astronomy.MoonPhase // A utility for calculating the moon's phase.
import kotlinx.coroutines.delay // A function to pause a coroutine for a specified time.
import kotlinx.coroutines.isActive // A property to check if a coroutine is still active.
import java.time.Instant // Represents a specific moment in time, stored in UTC.
import kotlin.math.* // Imports all standard mathematical functions like sin, cos, acos, etc.

/**
 * This is a string containing AGSL (Android Graphics Shading Language) code.
 * This code runs on the GPU and is responsible for drawing a realistic, 3D-looking Moon.
 * It performs two main tasks:
 * 1. Spherical Projection: It takes a flat 2D map of the Moon and wraps it around a sphere.
 * 2. Phase Lighting: It calculates how the sphere should be lit by the Sun to create the correct phase (e.g., crescent, gibbous, full).
 */
private const val MOON_SHADER_SRC = """// These are "uniforms" - variables that we pass from our Kotlin code into the shader.
    uniform shader uTexture;   // The 2D texture map of the Moon's surface.
    uniform float2 resolution; // The width and height of the canvas we are drawing on.
    uniform float3 sunDir;     // A 3D vector pointing from the Moon towards the Sun.
    uniform float2 texSize;    // The width and height of the texture map.

    const float PI = 3.14159265359; // The mathematical constant PI.

    // This is the main function of the shader. It runs for every single pixel on the canvas.
    half4 main(float2 coord) {
        // Step 1: Normalize the coordinates.
        // We convert the pixel coordinates from (0, 0)-(width, height) to a -1 to 1 range, with (0,0) at the center.
        float2 uv = (coord - 0.5 * resolution) / (0.5 * resolution.y);
        
        // Step 2: Check if the pixel is inside the circle.
        // We are drawing a sphere, which looks like a circle. Anything outside this circle should be transparent.
        float rSq = dot(uv, uv); // 'dot(uv, uv)' is a fast way to calculate the squared distance from the center.
        if (rSq > 1.0) {
            return half4(0.0, 0.0, 0.0, 0.0); // Return a fully transparent color.
        }
        
        // Step 3: Calculate the 3D "Normal" vector for this pixel.
        // For a sphere, the normal at any point on the surface points directly away from the center.
        // We calculate the z-component using the Pythagorean theorem (z = sqrt(1 - x^2 - y^2)).
        float z = sqrt(1.0 - rSq);
        float3 normal = float3(uv.x, uv.y, z);
        
        // Step 4: Map the 3D point on the sphere to a 2D point on our flat texture map.
        // This is called Spherical Mapping or an Inverse Equirectangular Projection.
        float lon = atan(normal.x, normal.z); // Longitude
        float lat = asin(normal.y);           // Latitude
        
        // Convert the longitude (-PI to PI) and latitude (PI/2 to -PI/2) to texture coordinates (0 to 1).
        float u = 0.5 + lon / (2.0 * PI);
        float v = 0.5 - lat / PI;
        
        // Sample the color from the Moon texture at the calculated coordinates.
        float2 texCoord = float2(u, v) * texSize;
        half4 color = uTexture.eval(texCoord);
        
        // Step 5: Apply lighting to create the phase effect.
        // We use a simple Lambertian diffuse lighting model.
        // The 'dot' product between the surface normal and the sun direction tells us how much light the point receives.
        float diffuse = max(dot(normal, sunDir), 0.0); // 'max' with 0.0 ensures we don't have negative light.
        
        // We add a little bit of "ambient" light so that the dark side of the moon isn't completely black.
        float ambient = 0.05;
        float intensity = ambient + (1.0 - ambient) * diffuse;
        
        // The final color is the texture color multiplied by the light intensity.
        return half4(color.rgb * intensity, 1.0); // We return a fully opaque color.
    }
"""

/**
 * This is the main composable function for rendering the Moon disk.
 * It uses the powerful AGSL shader on newer Android versions (API 33+) for a realistic, 3D-lit effect.
 * For older versions, it falls back to a simpler method of displaying a rotated image.
 */
@Composable
fun MoonDiskEngine(
    modifier: Modifier = Modifier.size(260.dp),
    phaseOverrideFraction: Double? = null // An optional value to manually set the moon's phase for debugging.
) {
    // --- Time State ---
    // We keep track of the current time and update it every minute to keep the astronomy calculations fresh.
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) {
        while (isActive) {
            now = Instant.now()
            delay(60000) // Pause for one minute.
        }
    }

    // --- Astronomical Calculations ---
    // 'remember' is used here so that these expensive calculations are only redone when 'now' changes.
    val phaseResult = remember(now) { MoonPhase.compute(now) }
    // Use the override value if it's provided, otherwise use the live calculated value.
    val phaseFraction = phaseOverrideFraction?.coerceIn(0.0, 1.0) ?: phaseResult.fraction

    // Calculate the orientation of the moon's terminator.
    val orientationDeg = remember(now) {
        MoonOrientation.terminatorRotationDegSkyMode(
            now,
            KimConfig.OBS_LAT,
            KimConfig.OBS_LON
        )
    }

    // --- Shader Preparation ---
    // Load the 2K texture map of the Moon's surface.
    val moonBitmap = ImageBitmap.imageResource(id = R.drawable.moon_color_2k)

    // This 'remember' block prepares the RuntimeShader. It will only rerun if the inputs (bitmap, phase, or orientation) change.
    val runtimeShader = remember(moonBitmap, phaseFraction, orientationDeg) {
        // RuntimeShaders are only available on Android 13 (API 33) and newer.
        if (Build.VERSION.SDK_INT >= 33) {
            // Step 1: Convert the orientation angle (Chi) from degrees to radians.
            val chi = Math.toRadians(-orientationDeg)

            // Step 2: Calculate the Phase Angle (Psi) from the illumination fraction (k).
            // This is the reverse of the formula k = (1 + cos(Psi))/2.
            val k = phaseFraction.coerceIn(0.0, 1.0)
            val psi = acos(2.0 * k - 1.0)

            // Step 3: Compute the 3D direction vector to the Sun.
            // This vector is what the shader will use to determine the lighting.
            val sunX = sin(psi) * sin(chi)
            val sunY = sin(psi) * cos(chi)
            val sunZ = cos(psi)

            // Step 4: Set up the RuntimeShader.
            val shader = RuntimeShader(MOON_SHADER_SRC)
            val androidBitmap = moonBitmap.asAndroidBitmap()
            val bitmapShader = BitmapShader(androidBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

            // Pass the uniforms (our input variables) to the shader.
            shader.setInputShader("uTexture", bitmapShader)
            shader.setFloatUniform("resolution", androidBitmap.width.toFloat(), androidBitmap.height.toFloat()) // Initial guess for size.
            shader.setFloatUniform("texSize", androidBitmap.width.toFloat(), androidBitmap.height.toFloat())
            shader.setFloatUniform("sunDir", sunX.toFloat(), sunY.toFloat(), sunZ.toFloat())

            shader // Return the configured shader.
        } else {
            null // Return null on older Android versions.
        }
    }

    // --- Rendering ---
    // Check again if we can use the shader.
    if (Build.VERSION.SDK_INT >= 33 && runtimeShader != null) {
        // If so, we use a 'Canvas' to draw a circle with our shader.
        Canvas(modifier = modifier) {
            // Before drawing, we update the 'resolution' uniform to the actual size of our canvas.
            runtimeShader.setFloatUniform(
                "resolution", size.width, size.height
            )
            drawCircle(brush = ShaderBrush(runtimeShader))
        }
    } else {
        // Fallback for older Android versions (API < 33).
        // We just show the image, clipped to a circle, and rotate it.
        // This doesn't have the dynamic phase lighting, so it will always look like a full moon.
        Image(
            bitmap = moonBitmap,
            contentDescription = "Moon",
            modifier = modifier
                .clip(CircleShape) // Clip the square image to a circle.
                // We can still apply the correct orientation.
                .graphicsLayer {
                    rotationZ = orientationDeg.toFloat()
                },
            contentScale = ContentScale.Crop
        )
    }
}
