// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon

// Imports are used to bring in code from other parts of the project or from external libraries.
import androidx.compose.ui.unit.Dp // A unit of measurement for density-independent pixels.

/**
 * A 'data class' is a special type of class in Kotlin that is primarily used to hold data.
 * This 'DpOffset' data class represents a 2D offset (an x and y position) using Dp units.
 * Dp (Density-independent pixels) is a flexible unit that scales with screen density,
 * making UI elements appear similarly sized on different devices.
 *
 * @param x The horizontal offset in Dp.
 * @param y The vertical offset in Dp.
 */
data class DpOffset(val x: Dp, val y: Dp)
