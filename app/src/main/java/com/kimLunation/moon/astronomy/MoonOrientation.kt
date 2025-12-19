// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon.astronomy

// Imports are used to bring in code from other parts of the project or from external libraries.
import java.time.Instant // Represents a specific moment in time, stored in UTC.
import kotlin.math.* // Imports all standard mathematical functions like sin, cos, atan2, etc.

/**
 * This is a Kotlin 'object', which means there is only one instance of this class in the entire application (a Singleton).
 * 'MoonOrientation' is responsible for calculating the orientation of the Moon's disk as it appears in the sky.
 * Specifically, it computes the angle of the "terminator" – the line that separates the sunlit part of the Moon from the dark part.
 *
 * This orientation is crucial for rendering a realistic image of the Moon. In what's often called a "Sky View"
 * (where the top of the view is North), the lit side of the Moon always points towards the Sun.
 * The angle of this direction is called the Position Angle of the Bright Limb, often represented by the Greek letter Chi (χ).
 */
object MoonOrientation {

    /**
     * Calculates the rotation angle (in degrees) needed to orient a standard Moon texture (which is typically North-up)
     * so that its terminator (the light/dark line) correctly aligns with the Sun's position in the sky.
     *
     * @param now The 'Instant' of observation.
     * @param obsLatDeg The observer's latitude in degrees. (This is currently unused because we are using a geocentric approximation,
     *                  which assumes the observer is at the center of the Earth. This is accurate enough for this purpose).
     * @param obsLonDeg The observer's longitude in degrees. (Also unused for the same reason).
     * @return The rotation angle in degrees. This value can be used to rotate the Moon's image.
     */
    fun terminatorRotationDegSkyMode(
        now: Instant,
        @Suppress("UNUSED_PARAMETER") obsLatDeg: Double,
        @Suppress("UNUSED_PARAMETER") obsLonDeg: Double
    ): Double {

        // Step 1: Convert the time to a Julian Day (JD) and then to Julian Centuries (t).
        // This is a standard way to represent time in astronomical calculations.
        val jd = julianDay(now)
        val t = (jd - 2451545.0) / 36525.0

        // Step 2: Compute the Mean Orbital Elements of the Moon and Sun.
        // These are polynomial formulas that give an approximation of the positions of the Moon and Sun.
        // They are based on astronomical models.
        
        // Mean elongation of the Moon (difference in longitude between Moon and Sun)
        val d = Math.toRadians(297.8501921 + 445267.1114034 * t - 0.0018819 * t * t)

        // Mean anomaly of the Sun (position in its elliptical orbit)
        val m = Math.toRadians(357.5291092 + 35999.0502909 * t - 0.0001536 * t * t)

        // Mean anomaly of the Moon (position in its elliptical orbit)
        val mp = Math.toRadians(134.9633964 + 477198.8675055 * t + 0.0087414 * t * t)

        // Moon's argument of latitude (distance from its ascending node - where it crosses the ecliptic plane going North)
        val f = Math.toRadians(93.2720950 + 483202.0175233 * t - 0.0036539 * t * t)

        // Step 3: Compute Ecliptic Coordinates.
        // Ecliptic coordinates describe a position on the celestial sphere relative to the ecliptic plane (the plane of Earth's orbit).
        // First, we find the Mean Longitude of the Sun (l0) and the Moon (lMoon).
        val l0 = Math.toRadians(280.46646 + 36000.76983 * t) // Sun's mean longitude
        val lMoon = Math.toRadians(218.3164477 + 481267.88123421 * t) // Moon's mean longitude

        // Calculate the Ecliptic Longitude of the Sun (lambdaSun).
        // This includes corrections for the Earth's elliptical orbit (the "equation of center").
        val lambdaSun = l0 + Math.toRadians(1.914602) * sin(m) + Math.toRadians(0.019993) * sin(2 * m)

        // Calculate the Ecliptic Longitude of the Moon (lambdaMoon).
        // This includes major correction terms for gravitational perturbations, like Evection, Variation, and the Annual Equation.
        var lambdaMoon = lMoon + Math.toRadians(6.289) * sin(mp)
        lambdaMoon -= Math.toRadians(1.274) * sin(mp - 2 * d)
        lambdaMoon += Math.toRadians(0.658) * sin(2 * d)
        lambdaMoon += Math.toRadians(0.214) * sin(2 * mp)
        lambdaMoon -= Math.toRadians(0.186) * sin(m)

        // Calculate the Ecliptic Latitude of the Moon (betaMoon).
        // The Moon's orbit is tilted about 5.1 degrees relative to the ecliptic plane.
        var betaMoon = Math.toRadians(5.128) * sin(f)
        betaMoon += Math.toRadians(0.280) * sin(mp + f)
        betaMoon += Math.toRadians(0.278) * sin(mp - f)

        // Step 4: Convert from Ecliptic Coordinates to Equatorial Coordinates (Right Ascension and Declination).
        // Equatorial coordinates are fixed relative to the stars.
        // The "Obliquity of the Ecliptic" (epsilon) is the tilt of Earth's axis, about 23.4 degrees.
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

        // Step 5: Calculate the Position Angle of the Bright Limb (Chi).
        // This angle represents the direction of the Sun relative to the Moon as seen from Earth.
        // The calculation involves solving a spherical triangle formed by the celestial North Pole, the Sun, and the Moon.
        // The formula is a standard application of spherical trigonometry.
        val numer = cos(sunDec) * sin(sunRA - moonRA)
        val denom = cos(moonDec) * sin(sunDec) -
                sin(moonDec) * cos(sunDec) * cos(sunRA - moonRA)
        val chi = atan2(numer, denom)

        // Step 6: Return the result.
        // The calculated angle 'chi' is measured counter-clockwise from North.
        // On-screen rotations are often clockwise, so we might need to negate the angle.
        // This function returns -chi in degrees, which can be directly used for rotation.
        return Math.toDegrees(-chi)
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
