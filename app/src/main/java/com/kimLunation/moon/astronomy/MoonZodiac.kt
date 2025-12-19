// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon.astronomy

// Imports are used to bring in code from other parts of the project or from external libraries.
import java.time.Instant // Represents a specific moment in time, stored in UTC.
import kotlin.math.floor // A mathematical function to find the largest integer less than or equal to a number.
import kotlin.math.sin   // A mathematical function to calculate the sine of an angle.

/**
 * An 'enum' (enumeration) is a special type that represents a fixed set of constants.
 * This enum, 'ZodiacSign', defines the twelve signs of the tropical zodiac.
 */
enum class ZodiacSign {
    ARIES, TAURUS, GEMINI, CANCER, LEO, VIRGO, LIBRA, SCORPIO, SAGITTARIUS, CAPRICORN, AQUARIUS, PISCES
}

/**
 * This is a Kotlin 'object', which means there is only one instance of this class in the entire application (a Singleton).
 * 'MoonZodiac' is responsible for calculating the astrological zodiac sign that the Moon is currently in.
 * This is based on the Moon's position along the ecliptic, the apparent path of the Sun across the sky over the course of a year.
 */
object MoonZodiac {

    /**
     * Determines the zodiac sign for the Moon at a given moment.
     *
     * @param now The 'Instant' for which to calculate the sign.
     * @return The 'ZodiacSign' the Moon is currently in.
     */
    fun sign(now: Instant): ZodiacSign {
        // First, we calculate the Moon's ecliptic longitude in degrees.
        // The ecliptic is a 360-degree circle, and it's divided into 12 zodiac signs of 30 degrees each.
        val lambdaDeg = moonEclipticLongitudeDeg(now)
        
        // To find the sign, we simply divide the longitude by 30. The integer part of the result gives us an index from 0 to 11.
        // For example, if the longitude is 35 degrees, 35 / 30 = 1.16, and the integer part is 1. This corresponds to Taurus.
        val index = floor(lambdaDeg / 30.0).toInt().coerceIn(0, 11) // 'coerceIn' ensures the index is safely within the valid range.
        
        // We then use this index to get the correct sign from our 'ZodiacSign' enum.
        return ZodiacSign.values()[index]
    }

    /**
     * A private helper function to calculate the ecliptic longitude of the Moon in degrees.
     * This uses the same set of simplified astronomical formulas as other parts of the app.
     *
     * @param now The 'Instant' to calculate for.
     * @return The Moon's ecliptic longitude in degrees (from 0 to 360).
     */
    private fun moonEclipticLongitudeDeg(now: Instant): Double {
        // Convert the time to Julian Days (jd) and Julian Centuries (t).
        val jd = julianDay(now)
        val t = (jd - 2451545.0) / 36525.0

        // Calculate the mean orbital elements.
        val d = Math.toRadians(297.8501921 + 445267.1114034 * t - 0.0018819 * t * t) // Mean elongation of the Moon
        val m = Math.toRadians(357.5291092 + 35999.0502909 * t - 0.0001536 * t * t)  // Mean anomaly of the Sun
        val mp = Math.toRadians(134.9633964 + 477198.8675055 * t + 0.0087414 * t * t) // Mean anomaly of the Moon

        // Calculate the Moon's mean longitude.
        val lMoon = Math.toRadians(218.3164477 + 481267.88123421 * t)

        // Apply the main correction terms to get a more accurate longitude (lambdaMoon).
        var lambdaMoon = lMoon + Math.toRadians(6.289) * sin(mp)      // Correction for Moon's elliptical orbit
        lambdaMoon -= Math.toRadians(1.274) * sin(mp - 2 * d) // Evection
        lambdaMoon += Math.toRadians(0.658) * sin(2 * d)      // Variation
        lambdaMoon += Math.toRadians(0.214) * sin(2 * mp)
        lambdaMoon -= Math.toRadians(0.186) * sin(m)      // Annual Equation

        // Convert the final longitude from radians to degrees.
        val deg = Math.toDegrees(lambdaMoon)
        // Normalize the result to be within the range [0, 360).
        return ((deg % 360.0) + 360.0) % 360.0
    }

    /**
     * A private helper function to convert an 'Instant' to a Julian Day.
     * @param now The 'Instant' to convert.
     * @return The Julian Day as a Double.
     */
    private fun julianDay(now: Instant): Double {
        return (now.toEpochMilli() / 86400000.0) + 2440587.5
    }
}
