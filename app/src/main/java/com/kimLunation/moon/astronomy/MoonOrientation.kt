package com.kimLunation.moon.astronomy

import java.time.Instant
import kotlin.math.*

/**
 * MoonOrientation
 *
 * This object is responsible for calculating the orientation of the Moon's disk in the sky.
 * Specifically, it computes the angle of the "Terminator" (the line dividing day and night on the Moon).
 *
 * The orientation is crucial for realistic rendering. In a "Sky View" (where Up is North, usually),
 * the lit side of the Moon points towards the Sun. The angle of this direction is called the
 * Position Angle of the Bright Limb (Chi).
 */
object MoonOrientation {

    /**
     * Calculates the rotation angle (in degrees) required to orient a standard Moon texture
     * (which is typically North-Up) so that its terminator aligns with the Sun's position in the sky.
     *
     * @param now The time of observation.
     * @param obsLatDeg Observer's latitude (unused in this geocentric approximation).
     * @param obsLonDeg Observer's longitude (unused in this geocentric approximation).
     * @return The rotation angle in degrees. Ideally, this angle is passed to a shader or rotation transform.
     */
    fun terminatorRotationDegSkyMode(
        now: Instant,
        @Suppress("UNUSED_PARAMETER") obsLatDeg: Double,
        @Suppress("UNUSED_PARAMETER") obsLonDeg: Double
    ): Double {

        // 1. Convert time to Julian Day (JD) and Centuries (t).
        // This is the standard time variable for ephemeris calculations.
        val jd = julianDay(now)
        val t = (jd - 2451545.0) / 36525.0

        // 2. Compute Mean Orbital Elements of Moon and Sun.
        // These are polynomial approximations for the position of the bodies.
        
        // Mean elongation of the Moon
        val d = Math.toRadians(297.8501921 + 445267.1114034 * t - 0.0018819 * t * t)

        // Mean anomaly of the Sun
        val m = Math.toRadians(357.5291092 + 35999.0502909 * t - 0.0001536 * t * t)

        // Mean anomaly of the Moon
        val mp = Math.toRadians(134.9633964 + 477198.8675055 * t + 0.0087414 * t * t)

        // Argument of latitude (Moon's distance from ascending node)
        val f = Math.toRadians(93.2720950 + 483202.0175233 * t - 0.0036539 * t * t)

        // 3. Compute Ecliptic Coordinates.
        // We first find the Mean Longitude of Sun (L0) and Moon (L_moon).
        val l0 = Math.toRadians(280.46646 + 36000.76983 * t)
        val lMoon = Math.toRadians(218.3164477 + 481267.88123421 * t)

        // Ecliptic Longitude of Sun (lambdaSun)
        // Includes equation of center corrections.
        val lambdaSun = l0 + Math.toRadians(1.914602) * sin(m) + Math.toRadians(0.019993) * sin(2 * m)

        // Ecliptic Longitude of Moon (lambdaMoon)
        // Includes major periodic terms (Evection, Variation, Annual Equation).
        var lambdaMoon = lMoon + Math.toRadians(6.289) * sin(mp)
        lambdaMoon -= Math.toRadians(1.274) * sin(mp - 2 * d)
        lambdaMoon += Math.toRadians(0.658) * sin(2 * d)
        lambdaMoon += Math.toRadians(0.214) * sin(2 * mp)
        lambdaMoon -= Math.toRadians(0.186) * sin(m)

        // Ecliptic Latitude of Moon (betaMoon)
        // Moon's orbit is inclined ~5 degrees to the ecliptic.
        var betaMoon = Math.toRadians(5.128) * sin(f)
        betaMoon += Math.toRadians(0.280) * sin(mp + f)
        betaMoon += Math.toRadians(0.278) * sin(mp - f)

        // 4. Convert Ecliptic to Equatorial Coordinates (RA, Dec).
        // The "Obliquity of the Ecliptic" (epsilon) is the tilt of Earth's axis (~23.4 degrees).
        val epsilon = Math.toRadians(23.439 - 0.0000004 * (jd - 2451545.0))

        // Sun's Right Ascension (RA) and Declination (Dec)
        val sunRA = atan2(cos(epsilon) * sin(lambdaSun), cos(lambdaSun))
        val sunDec = asin(sin(epsilon) * sin(lambdaSun))

        // Moon's Right Ascension (RA) and Declination (Dec)
        val moonRA = atan2(
            sin(lambdaMoon) * cos(epsilon) - tan(betaMoon) * sin(epsilon),
            cos(lambdaMoon)
        )
        val moonDec = asin(
            sin(betaMoon) * cos(epsilon) +
                    cos(betaMoon) * sin(epsilon) * sin(lambdaMoon)
        )

        // 5. Calculate Position Angle of the Bright Limb (Chi).
        // This is the direction of the Sun relative to the Moon in the sky.
        // It forms a spherical triangle with the North Pole.
        // Formula is standard spherical trigonometry.
        val numer = cos(sunDec) * sin(sunRA - moonRA)
        val denom = cos(moonDec) * sin(sunDec) -
                sin(moonDec) * cos(sunDec) * cos(sunRA - moonRA)
        val chi = atan2(numer, denom)

        // 6. Return the result.
        // Chi is measured counter-clockwise (East) from North.
        // If we want to rotate a texture on screen (where +rotation is usually Clockwise),
        // we might return -Chi.
        // However, this value depends on how the consumer uses it.
        // Our shader expects Chi for the light vector calculation, or we invert it later.
        // Here we return -Chi in degrees.
        return Math.toDegrees(-chi)
    }

    private fun julianDay(now: Instant): Double {
        return (now.toEpochMilli() / 86400000.0) + 2440587.5
    }
}
