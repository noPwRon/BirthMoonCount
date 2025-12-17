package com.kimLunation.moon.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

object AstrolabeImageEngine {

    /**
     * Loads the astrolabe resource and processes it to remove the background.
     * It uses a "chroma key" approach based on the corner pixels, combined with a brightness threshold
     * to handle baked-in checkerboard patterns often found in opaque screenshots of transparent assets.
     */
    suspend fun loadProcessedAstrolabe(
        context: Context,
        @DrawableRes resId: Int,
        tolerance: Int = 30,
        brightnessMin: Int = 200
    ): ImageBitmap? {
        return withContext(Dispatchers.IO) {
            val opts = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inMutable = true
            }
            // Decode the resource
            val bitmap = BitmapFactory.decodeResource(context.resources, resId, opts) 
                ?: return@withContext null

            val w = bitmap.width
            val h = bitmap.height
            val pixels = IntArray(w * h)
            bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

            // Sample 4 corners to establish the "background key"
            // We assume the corners represent the background to be removed.
            val c1 = pixels[0]
            pixels[w - 1]
            pixels[(h - 1) * w]
            pixels[(h - 1) * w + (w - 1)]

            // Use Top-Left corner as the primary key reference
            val keyR = Color.red(c1)
            val keyG = Color.green(c1)
            val keyB = Color.blue(c1)

            // Verify if corners are consistent (optional, but good for debugging)
            // Here we just proceed to iterate all pixels.

            for (i in pixels.indices) {
                val p = pixels[i]
                val r = Color.red(p)
                val g = Color.green(p)
                val b = Color.blue(p)

                // 1. Distance check from Key Color
                val diff = abs(r - keyR) + abs(g - keyG) + abs(b - keyB)
                val isCloseToKey = diff < tolerance * 3 // *3 for sum of RGB diffs approx

                // 2. Brightness check (for checkerboard squares that might differ from key)
                // If the pixel is very bright (near white/light gray), treat as background
                val brightness = (r + g + b) / 3
                val isBrightBackground = brightness > brightnessMin

                // If either condition matches, make transparent
                if (isCloseToKey || isBrightBackground) {
                    pixels[i] = 0 // Set Alpha to 0 (Transparent)
                } else {
                    // Keep original pixel (Brass fragment)
                    // We could optionally ensure alpha is 255, but it should be already if opaque source.
                }
            }

            // Update bitmap
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
            bitmap.asImageBitmap()
        }
    }
}
