// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon.astronomy

// Imports are used to bring in code from other parts of the project or from external libraries.
import java.time.Instant // Represents a specific moment in time, stored in UTC.
import kotlin.math.asin // A mathematical function to calculate the arcsine.
import kotlin.math.atan2 // A mathematical function to calculate the angle from the conversion of rectangular coordinates to polar coordinates.
import kotlin.math.cos // A mathematical function to calculate the cosine of an angle.
import kotlin.math.sin // A mathematical function to calculate the sine of an angle.
import kotlin.math.tan // A mathematical function to calculate the tangent of an angle.

/**
 * This is a Kotlin 'object', which means there is only one instance of this class in the entire application (a Singleton).
 * 'MoonAzimuth' is responsible for calculating the azimuth of the moon for a given time and location.
 * Azimuth is the direction of a celestial object from the observer, expressed as the angular distance from the north or south point
 * of the horizon to the point at which a vertical circle passing through the object intersects the horizon.
 * In simpler terms, it tells you which direction to look (like North, East, South, West) to see the moon.
 */
object MoonAzimuth {

    /**
     * Calculates the moon's azimuth in degrees.
     * This is a complex astronomical calculation that determines the moon's position in the sky
     * for a specific observer on Earth at a specific time.
     *
     * @param now The 'Instant' in time for which to calculate the azimuth.
     * @param latDeg The latitude of the observer in degrees.
     * @param lonDeg The longitude of the observer in degrees.
     * @return The azimuth of the moon in degrees, where 0 is North, 90 is East, 180 is South, and 270 is West.
     */
    fun azimuthDeg(now: Instant, latDeg: Double, lonDeg: Double): Double {
        // Step 1: Calculate the Julian Day. This is a continuous count of days since a remote epoch,
        // and it's a standard way to represent time in astronomy.
        val jd = julianDay(now)
        // 't' is the number of Julian centuries since the standard epoch J2000.0.
        val t = (jd - 2451545.0) / 36525.0

        // Step 2: Calculate the orbital elements of the Moon and Sun. These are parameters that define their orbits.
        // These formulas are based on astronomical models, like the one by Jean Meeus.
        val d = Math.toRadians(297.8501921 + 445267.1114034 * t - 0.0018819 * t * t) // Mean elongation of the Moon
        val m = Math.toRadians(357.5291092 + 35999.0502909 * t - 0.0001536 * t * t)  // Mean anomaly of the Sun
        val mp = Math.toRadians(134.9633964 + 477198.8675055 * t + 0.0087414 * t * t) // Mean anomaly of the Moon
        val f = Math.toRadians(93.2720950 + 483202.0175233 * t - 0.0036539 * t * t)   // Mean argument of latitude of the Moon

        // Step 3: Calculate the Moon's position in ecliptic coordinates (longitude and latitude).
        // The ecliptic is the plane of Earth's orbit around the Sun.
        val lMoon = Math.toRadians(218.3164477 + 481267.88123421 * t) // Moon's mean longitude

        // Apply corrections to the Moon's longitude to get a more accurate position.
        var lambdaMoon = lMoon + Math.toRadians(6.289) * sin(mp)
        lambdaMoon -= Math.toRadians(1.274) * sin(mp - 2 * d)
        lambdaMoon += Math.toRadians(0.658) * sin(2 * d)
        lambdaMoon += Math.toRadians(0.214) * sin(2 * mp)
        lambdaMoon -= Math.toRadians(0.186) * sin(m)

        // Calculate the Moon's latitude (its position above or below the ecliptic plane).
        var betaMoon = Math.toRadians(5.128) * sin(f)
        betaMoon += Math.toRadians(0.280) * sin(mp + f)
        betaMoon += Math.toRadians(0.278) * sin(mp - f)

        // Step 4: Convert the Moon's ecliptic coordinates to equatorial coordinates (Right Ascension and Declination).
        // Equatorial coordinates are fixed with respect to the stars.
        val epsilon = Math.toRadians(23.439 - 0.0000004 * (jd - 2451545.0)) // Obliquity of the ecliptic

        // Calculate Right Ascension (RA) and Declination (Dec).
        val ra = atan2(
            sin(lambdaMoon) * cos(epsilon) - tan(betaMoon) * sin(epsilon),
            cos(lambdaMoon)
        )
        val dec = asin(
            sin(betaMoon) * cos(epsilon) +
                cos(betaMoon) * sin(epsilon) * sin(lambdaMoon)
        )

        // Step 5: Calculate Sidereal Time. This is a timekeeping system that astronomers use to locate celestial objects.
        // Greenwich Mean Sidereal Time (GMST)
        val gmst = 280.46061837 +
            360.98564736629 * (jd - 2451545.0) +
            0.000387933 * t * t -
            (t * t * t) / 38710000.0
        // Local Sidereal Time (LST) is GMST adjusted for the observer's longitude.
        val lstDeg = (gmst + lonDeg) % 360.0
        val lst = Math.toRadians((lstDeg + 360.0) % 360.0)

        // Step 6: Convert from equatorial coordinates to horizontal coordinates (Azimuth and Altitude).
        // The Hour Angle (HA) is the angular distance of the object from the meridian.
        val ha = lst - ra
        val lat = Math.toRadians(latDeg)

        // Calculate the Azimuth (az). This is the final direction we are looking for.
        val az = atan2(
            -sin(ha),
            tan(dec) * cos(lat) - sin(lat) * cos(ha)
        )
        // Convert the azimuth from radians to degrees and normalize it to the range [0, 360).
        return (Math.toDegrees(az) + 360.0) % 360.0
    }

    /**
     * A private helper function to convert an 'Instant' to a Julian Day.
     * The Julian Day is the number of days that have elapsed since noon on January 1, 4713 BC.
     *
     * @param now The 'Instant' to convert.
     * @return The Julian Day as a Double.
     */
    private fun julianDay(now: Instant): Double {
        // The formula is: (milliseconds since epoch / milliseconds in a day) + Julian Day of the epoch.
        return (now.toEpochMilli() / 86400000.0) + 2440587.5
    }
}
