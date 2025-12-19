package com.kimLunation.moon

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.isActive
import kotlin.math.sin

private const val NEBULA_SHADER = """
uniform float2 iResolution;
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

@Composable
fun NebulaBackground(modifier: Modifier = Modifier) {
    val timeSeconds by rememberFrameTimeSeconds()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        NebulaShaderLayer(modifier, timeSeconds)
    } else {
        NebulaFallbackLayer(modifier, timeSeconds)
    }
}

@Composable
private fun NebulaShaderLayer(modifier: Modifier, timeSeconds: Float) {
    val shader = remember { RuntimeShader(NEBULA_SHADER) }
    val brush = remember { ShaderBrush(shader) }
    Canvas(modifier = modifier) {
        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", timeSeconds)
        drawRect(brush = brush)
    }
}

@Composable
private fun NebulaFallbackLayer(modifier: Modifier, timeSeconds: Float) {
    val colors = remember {
        listOf(Color(0xFF05060C), Color(0xFF02030A))
    }
    val alpha = 0.92f + 0.05f * sin(timeSeconds * 0.6f)
    Canvas(modifier = modifier.graphicsLayer { this.alpha = alpha }) {
        val radius = size.minDimension * 0.85f
        drawRect(
            brush = Brush.radialGradient(
                colors = colors,
                center = center,
                radius = radius
            )
        )
    }
}

@Composable
private fun rememberFrameTimeSeconds(): State<Float> {
    val timeState = remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        val startTime = withFrameNanos { it }
        while (isActive) {
            val frameTime = withFrameNanos { it }
            timeState.value = (frameTime - startTime) / 1_000_000_000f
        }
    }
    return timeState
}
