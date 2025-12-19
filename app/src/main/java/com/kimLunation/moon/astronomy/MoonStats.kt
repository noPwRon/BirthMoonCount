// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon.astronomy

// Imports are used to bring in code from other parts of the project or from external libraries.
import java.time.Instant // Represents a specific moment in time, stored in UTC.
import java.time.ZoneId // Represents a time zone, like "America/Vancouver".
import java.time.ZonedDateTime // Represents a date and time with a time zone.
import kotlin.math.abs // A mathematical function to get the absolute value of a number.

/**
 * This is a Kotlin 'object', which means there is only one instance of this class in the entire application (a Singleton).
 * 'MoonStats' provides several simple, approximate statistics about the Moon.
 * These calculations are not as precise as the ones in other files (like 'MoonFullMoonsMeeus'),
 * but they are fast and good enough for display purposes in a user interface.
 */
object MoonStats {

    // --- Configuration ---
    // This section defines constants that are used in the calculations.

    // The birth date is defined here. It's important to get the time zone right.
    // Malaga, Spain is in the "Europe/Madrid" time zone. In March, it uses CET (Central European Time), which is UTC+1.
    private val BIRTH_INSTANT: Instant = ZonedDateTime.of(1987, 3, 6, 15, 50, 0, 0, ZoneId.of("Europe/Madrid")).toInstant()
    // The time zone for the user's location (Victoria, BC, Canada).
    private val VICTORIA_ZONE: ZoneId = ZoneId.of("America/Vancouver")

    // --- Public API ---
    // This section contains the functions that other parts of the app can call.

    /**
     * Calculates the approximate percentage of the moon's disk that is illuminated.
     * This uses a simple linear approximation based on the phase of the synodic month.
     *
     * @param now The 'Instant' for which to calculate the illumination.
     * @return The illumination percentage as a Double (from 0.0 to 100.0).
     */
    fun illuminationPercent(now: Instant): Double {
        val phase = phaseFraction(now) // Get the phase fraction (0.0 for New Moon, 0.5 for Full Moon, 1.0 for the next New Moon).
        // This formula approximates the illumination. It's 100% at phase 0.5 and 0% at phase 0.0 and 1.0.
        val illumination = 1.0 - (2.0 * abs(phase - 0.5))
        return illumination * 100.0
    }

    /**
     * Counts the number of full moons that have occurred since a given birth date.
     * **Important:** This is a rough approximation and not highly accurate over many decades.
     * For a high-precision count, an algorithm like the one in 'MoonFullMoonsMeeus.kt' should be used.
     *
     * @param now The 'Instant' to count up to.
     * @return An estimated integer count of full moons.
     */
    fun fullMoonsSinceBirth(now: Instant): Int {
        // Calculate the number of years between the birth date and now.
        val years = java.time.Duration.between(BIRTH_INSTANT, now).toDays() / 365.25
        // There are approximately 12.3685 full moons (lunations) in a year.
        return (years * 12.3685).toInt()
    }

    /**
     * Provides a traditional, folkloric name for the full moon of the current month.
     * This is a simplified naming convention based on North American traditions.
     *
     * @param now The 'Instant' for which to get the moon name.
     * @return A String with the moon's name (e.g., "Wolf Moon", "Waxing Crescent").
     */
    fun moonName(now: Instant): String {
        // Get the date and time in the user's local time zone.
        val zdt = ZonedDateTime.ofInstant(now, VICTORIA_ZONE)
        val phase = phaseFraction(now)

        // Check if the moon is currently "full" (we define this as being in a small window around the exact full moon phase).
        if (phase > 0.47 && phase < 0.53) {
            // If it is a full moon, return the traditional name for the current month.
            return when (zdt.monthValue) {
                1 -> "Wolf Moon"
                2 -> "Snow Moon"
                3 -> "Worm Moon"
                4 -> "Pink Moon"
                5 -> "Flower Moon"
                6 -> "Strawberry Moon"
                7 -> "Buck Moon"
                8 -> "Sturgeon Moon"
                9 -> "Harvest Moon"
                10 -> "Hunter's Moon"
                11 -> "Beaver Moon"
                12 -> "Cold Moon"
                else -> "Full Moon" // A fallback name.
            }
        } else {
            // If it's not a full moon, return the standard name for the current phase.
            return when {
                phase < 0.03 || phase > 0.97 -> "New Moon"
                phase < 0.25 -> "Waxing Crescent" // Getting bigger
                phase < 0.47 -> "Waxing Gibbous"
                phase < 0.53 -> "Full Moon" // This is already covered above, but included for completeness.
                phase < 0.75 -> "Waning Gibbous" // Getting smaller
                phase < 0.97 -> "Waning Crescent"
                else -> "New Moon" // Fallback.
            }
        }
    }

    // --- Private Helpers ---
    // These are functions that are only used inside this object.

    /**
     * Calculates the phase fraction of the moon, which is a number from 0.0 to 1.0 representing
     * the position of the moon in its cycle (0.0 = New Moon, 0.5 = Full Moon).
     *
     * @param now The 'Instant' to calculate for.
     * @return The phase fraction as a Double.
     */
    private fun phaseFraction(now: Instant): Double {
        // We need a known point in time of a New Moon to use as a reference.
        // Here, we use January 6, 2000, 18:14 UTC.
        val refNewMoon = ZonedDateTime.of(2000, 1, 6, 18, 14, 0, 0, ZoneId.of("UTC")).toInstant()
        // The average length of a synodic month in milliseconds.
        val synodicMonthMillis = 29.530588853 * 24 * 60 * 60 * 1000
        
        // Calculate the difference in milliseconds between the current time and the reference new moon.
        val diff = now.toEpochMilli() - refNewMoon.toEpochMilli()
        // The phase fraction is the remainder of the total time divided by the length of a lunar month.
        val fraction = (diff.toDouble() / synodicMonthMillis) % 1.0
        // The modulo operator can return a negative number, so we correct for that here.
        return if (fraction < 0) fraction + 1.0 else fraction
    }
}
