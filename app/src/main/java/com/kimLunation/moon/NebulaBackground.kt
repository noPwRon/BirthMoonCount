// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon

// Imports are used to bring in code from other parts of the project or from external libraries.
import android.graphics.RuntimeShader // A class for creating a shader from AGSL (Android Graphics Shading Language) code.
import android.os.Build // Provides information about the current device's Android version.
import androidx.compose.foundation.Canvas // A composable that provides a drawing scope for 2D graphics.
import androidx.compose.runtime.Composable // An annotation that marks a function as a Jetpack Compose UI component.
import androidx.compose.runtime.LaunchedEffect // A coroutine scope that is tied to the lifecycle of a composable.
import androidx.compose.runtime.State // A holder for a value that can be observed by Compose.
import androidx.compose.runtime.getValue // A delegate to get the value of a State object.
import androidx.compose.runtime.mutableStateOf // Creates a mutable state object that is observable by Compose.
import androidx.compose.runtime.remember // Remembers a value across recompositions.
import androidx.compose.runtime.withFrameNanos // A function to await the next frame and get its time in nanoseconds.
import androidx.compose.ui.Modifier // An object that can be used to add behavior or decoration to a composable.
import androidx.compose.ui.graphics.Brush // A brush to paint a shape with.
import androidx.compose.ui.graphics.Color // Represents a color.
import androidx.compose.ui.graphics.ShaderBrush // A brush that paints with a shader.
import androidx.compose.ui.graphics.graphicsLayer // A modifier for applying graphical effects.
import kotlinx.coroutines.isActive // A property to check if a coroutine is still active.
import kotlin.math.sin // A mathematical function to calculate the sine of an angle.

/**
 * This is a string containing AGSL (Android Graphics Shading Language) code.
 * This code runs on the GPU and is used to create a dynamic, animated nebula effect.
 * It uses noise functions (hash21, noise, fbm) to generate a procedural texture that looks like a nebula.
 * The 'iResolution' uniform is the size of the drawing area, and 'iTime' is a time value that is used to animate the nebula.
 */
private const val NEBULA_SHADER = """uniform float2 iResolution;
uniform float iTime;

float hash21(float2 p) {
    return fract(sin(dot(p, float2(127.1, 311.7))) * 43758.5453);
}

float noise(float2 p) {
    float2 i = floor(p);
    float2 f = fract(p);
    float a = hash21(i);
    float b = hash21(i + float2(1.0, 0.0));
    float c = hash21(i + float2(0.0, 1.0));
    float d = hash21(i + float2(1.0, 1.0));
    float2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float fbm(float2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 5; ++i) {
        v += a * noise(p);
        p = p * 2.0 + 1.7;
        a *= 0.5;
    }
    return v;
}

half4 main(float2 fragCoord) {
    float2 uv = fragCoord / iResolution.xy;
    float2 p = (uv - 0.5) * float2(iResolution.x / iResolution.y, 1.0);
    float t = iTime * 0.12;
    float breath = 0.85 + 0.15 * sin(iTime * 0.6);

    float2 warp = p + 0.15 * float2(
        sin(p.y * 2.4 + t),
        cos(p.x * 2.1 - t)
    );

    float n1 = fbm(warp * 1.4 + float2(t * 0.2, t * 0.15));
    float n2 = fbm((warp + n1) * 2.0 - t * 0.1);
    float nebula = smoothstep(0.2, 0.9, n1 * 0.8 + n2 * 0.6) * breath;

    float3 colA = float3(0.05, 0.07, 0.12);
    float3 colB = float3(0.12, 0.18, 0.32);
    float3 colC = float3(0.35, 0.18, 0.28);
    float3 col = mix(colA, colB, nebula);
    col = mix(col, colC, n2 * 0.35);

    float starNoise = noise(uv * 220.0);
    float stars = smoothstep(0.995, 1.0, starNoise) * 0.8;
    col += stars;

    col = clamp(col, 0.0, 1.0);
    return half4(col, 1.0);
}
"""

/**
 * A composable function that displays a nebula background.
 * It uses a powerful 'RuntimeShader' on newer Android versions (Tiramisu and above)
 * and falls back to a simpler, less resource-intensive gradient on older versions.
 */
@Composable
fun NebulaBackground(modifier: Modifier = Modifier) {
    // 'rememberFrameTimeSeconds' provides a continuously updating time value.
    val timeSeconds by rememberFrameTimeSeconds()
    // Check the Android version to decide which background to use.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // On newer devices, use the cool shader-based nebula.
        NebulaShaderLayer(modifier, timeSeconds)
    } else {
        // On older devices, use a simpler fallback.
        NebulaFallbackLayer(modifier, timeSeconds)
    }
}

/**
 * This composable uses the AGSL shader to draw the animated nebula.
 * It's only called on devices that support 'RuntimeShader'.
 */
@Composable
private fun NebulaShaderLayer(modifier: Modifier, timeSeconds: Float) {
    // 'remember' is used to create and keep the shader object across recompositions.
    val shader = remember { RuntimeShader(NEBULA_SHADER) }
    // A 'ShaderBrush' is used to paint with the shader.
    val brush = remember { ShaderBrush(shader) }
    // The 'Canvas' composable provides a drawing area.
    Canvas(modifier = modifier) {
        // The shader needs to know the size of the canvas and the current time.
        // These are passed in as "uniforms".
        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", timeSeconds)
        // Finally, we draw a rectangle that fills the entire canvas with our shader brush.
        drawRect(brush = brush)
    }
}

/**
 * This composable provides a fallback for older devices that don't support 'RuntimeShader'.
 * It draws a simple, animated radial gradient that gives a hint of a nebula effect.
 */
@Composable
private fun NebulaFallbackLayer(modifier: Modifier, timeSeconds: Float) {
    // A list of colors for the gradient.
    val colors = remember {
        listOf(Color(0xFF05060C), Color(0xFF02030A))
    }
    // Animate the alpha (transparency) to create a subtle pulsing effect.
    val alpha = 0.92f + 0.05f * sin(timeSeconds * 0.6f)
    Canvas(modifier = modifier.graphicsLayer { this.alpha = alpha }) {
        val radius = size.minDimension * 0.85f
        // Draw a rectangle filled with a radial gradient.
        drawRect(
            brush = Brush.radialGradient(
                colors = colors,
                center = center,
                radius = radius
            )
        )
    }
}

/**
 * A composable function that provides a continuously updating 'State' object
 * containing the time in seconds since the composable was first launched.
 * This is used to drive the animations in the nebula background.
 */
@Composable
private fun rememberFrameTimeSeconds(): State<Float> {
    val timeState = remember { mutableStateOf(0f) }
    // 'LaunchedEffect' is used to start a coroutine that updates the time on every frame.
    LaunchedEffect(Unit) {
        val startTime = withFrameNanos { it } // Get the start time.
        while (isActive) { // Loop as long as the composable is on the screen.
            val frameTime = withFrameNanos { it } // Get the time of the current frame.
            // Calculate the elapsed time in seconds and update the state.
            timeState.value = (frameTime - startTime) / 1_000_000_000f
        }
    }
    return timeState
}
