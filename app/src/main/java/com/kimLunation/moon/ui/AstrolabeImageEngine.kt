// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon.ui

// Imports are used to bring in code from other parts of the project or from external libraries.
import android.content.Context // Provides access to application-specific resources and classes, like loading drawables.
import android.graphics.Bitmap // The basic class for working with image data.
import android.graphics.BitmapFactory // A class for creating Bitmaps from various sources.
import android.graphics.Color // A utility class for working with color values.
import androidx.annotation.DrawableRes // An annotation to indicate that an integer is a drawable resource ID.
import androidx.compose.ui.graphics.ImageBitmap // A version of Bitmap that is optimized for use with Jetpack Compose.
import androidx.compose.ui.graphics.asImageBitmap // An extension function to convert a regular Bitmap to a Compose ImageBitmap.
import kotlinx.coroutines.Dispatchers // Provides different threads for running tasks. Dispatchers.IO is for disk or network operations.
import kotlinx.coroutines.withContext // A function to switch to a different thread for a block of code.
import kotlin.math.abs // A mathematical function to get the absolute value of a number.

/**
 * This is a Kotlin 'object', which means there is only one instance of this class in the entire application (a Singleton).
 * 'AstrolabeImageEngine' is a utility for processing image assets.
 * Its main job is to load an image and dynamically remove its background, making it transparent.
 */
object AstrolabeImageEngine {

    /**
     * Loads a drawable resource and processes it to remove the background, turning it into a transparent image.
     * This technique is often called "chroma keying". It works by identifying a "key" color (the background color)
     * and making all pixels of that color transparent.
     * This implementation also has a special check for bright pixels, which helps remove checkerboard patterns
     * that are sometimes used to represent transparency in image editors.
     *
     * @param context The application context, needed to access resources.
     * @param resId The resource ID of the drawable image to load.
     * @param tolerance How close a color can be to the key color to be considered background. A higher value is more lenient.
     * @param brightnessMin The brightness threshold. Any pixel brighter than this will also be considered background.
     * @return An 'ImageBitmap' with the background removed, ready to be used in Compose. Returns null if loading fails.
     */
    suspend fun loadProcessedAstrolabe(
        context: Context,
        @DrawableRes resId: Int,
        tolerance: Int = 30,
        brightnessMin: Int = 200
    ): ImageBitmap? {
        // 'withContext(Dispatchers.IO)' runs the code inside this block on a background thread.
        // This is important because image processing can be slow and we don't want to freeze the user interface.
        return withContext(Dispatchers.IO) {
            // Set up options for loading the bitmap.
            val opts = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888 // Use a high-quality color format with an alpha channel.
                inMutable = true // This is crucial! We need a mutable bitmap so we can change its pixels.
            }
            // Decode the drawable resource into a bitmap.
            val bitmap = BitmapFactory.decodeResource(context.resources, resId, opts) 
                ?: return@withContext null // If loading fails, return null.

            val w = bitmap.width
            val h = bitmap.height
            // Create an array to hold all the pixel color data from the bitmap.
            val pixels = IntArray(w * h)
            bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

            // --- Chroma Keying Logic ---
            
            // We assume that the color of the corner pixels is the background color we want to remove.
            // Let's sample the color of the top-left corner pixel.
            val c1 = pixels[0]
            // We could also sample the other corners for a more robust approach, but for this asset, one is enough.
            // pixels[w - 1] // Top-right
            // pixels[(h - 1) * w] // Bottom-left
            // pixels[(h - 1) * w + (w - 1)] // Bottom-right

            // Extract the Red, Green, and Blue components of our key color.
            val keyR = Color.red(c1)
            val keyG = Color.green(c1)
            val keyB = Color.blue(c1)

            // Now, loop through every single pixel in the image.
            for (i in pixels.indices) {
                val p = pixels[i]
                val r = Color.red(p)
                val g = Color.green(p)
                val b = Color.blue(p)

                // Condition 1: Check if the pixel's color is close to our key color.
                // We calculate the "Manhattan distance" between the colors. If it's within our tolerance, it's background.
                val diff = abs(r - keyR) + abs(g - keyG) + abs(b - keyB)
                val isCloseToKey = diff < tolerance * 3 // Multiply tolerance by 3 for the sum of R, G, and B differences.

                // Condition 2: Check if the pixel is very bright.
                // This is a special check to handle cases where the background is a checkerboard of two different colors (e.g., white and light gray).
                // By removing all very bright pixels, we can clear both.
                val brightness = (r + g + b) / 3
                val isBrightBackground = brightness > brightnessMin

                // If either of our conditions is met, we make the pixel transparent.
                if (isCloseToKey || isBrightBackground) {
                    pixels[i] = 0 // Setting the integer value of a pixel to 0 makes it fully transparent black.
                } else {
                    // If it's not a background pixel, we leave it as it is (part of the brass astrolabe).
                    // We could also ensure its alpha is set to 255 (fully opaque), but it should be already.
                }
            }

            // After modifying our pixel array, we write the changes back into the bitmap.
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
            // Finally, we convert the processed Android Bitmap into a Compose ImageBitmap and return it.
            bitmap.asImageBitmap()
        }
    }
}
