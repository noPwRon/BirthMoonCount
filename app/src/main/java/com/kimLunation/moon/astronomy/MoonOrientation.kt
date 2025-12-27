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

    private data class EquatorialCoords(val ra: Double, val dec: Double)

    private data class MoonSunEquatorial(
        val jd: Double,
        val t: Double,
        val moon: EquatorialCoords,
        val sun: EquatorialCoords
    )

    /**
     * Calculates the rotation angle (in degrees) needed to orient a standard Moon texture (which is typically North-up)
     * so that its terminator (the light/dark line) matches the local sky view.
     *
     * This accounts for the observer's latitude/longitude by applying the parallactic angle, so the bright limb
     * matches what you see on the horizon (with south at the bottom of a portrait phone).
     *
     * @param now The 'Instant' of observation.
     * @param obsLatDeg The observer's latitude in degrees.
     * @param obsLonDeg The observer's longitude in degrees (east-positive).
     * @return The rotation angle in degrees. This value can be used to rotate the Moon's image.
     */
    fun terminatorRotationDegSkyMode(
        now: Instant,
        obsLatDeg: Double,
        obsLonDeg: Double
    ): Double {
        val equatorial = computeMoonSunEquatorial(now)
        val chi = positionAngleRad(
            equatorial.sun.ra,
            equatorial.sun.dec,
            equatorial.moon.ra,
            equatorial.moon.dec
        )
        val parallactic = parallacticAngleRad(
            equatorial.jd,
            obsLatDeg,
            obsLonDeg,
            equatorial.moon.ra,
            equatorial.moon.dec
        )
        val localChi = chi - parallactic

        // Step 7: Return the result.
        // The calculated angle is measured counter-clockwise from local vertical.
        return normalizeDegrees(Math.toDegrees(localChi) + 90.0)
    }

    /**
     * Returns the Moon's axis position angle (north pole) for a sky view (north up, east left).
     * The angle is measured from north toward east, with local horizon correction applied.
     */
    fun axisRotationDegSkyMode(
        now: Instant,
        obsLatDeg: Double,
        obsLonDeg: Double
    ): Double {
        val equatorial = computeMoonSunEquatorial(now)
        val pole = lunarPoleEquatorialRad(equatorial.t)
        val positionAngle = positionAngleRad(
            pole.ra,
            pole.dec,
            equatorial.moon.ra,
            equatorial.moon.dec
        )
        val parallactic = parallacticAngleRad(
            equatorial.jd,
            obsLatDeg,
            obsLonDeg,
            equatorial.moon.ra,
            equatorial.moon.dec
        )
        val localP = positionAngle - parallactic
        return normalizeDegrees(Math.toDegrees(localP))
    }

    /**
     * A private helper function to convert an 'Instant' to a Julian Day.
     * @param now The 'Instant' to convert.
     * @return The Julian Day as a Double.
     */
    private fun julianDay(now: Instant): Double {
        return (now.toEpochMilli() / 86400000.0) + 2440587.5
    }

    private fun computeMoonSunEquatorial(now: Instant): MoonSunEquatorial {
        // Step 1: Convert the time to a Julian Day (JD) and then to Julian Centuries (t).
        val jd = julianDay(now)
        val t = (jd - 2451545.0) / 36525.0

        // Step 2: Compute the Mean Orbital Elements of the Moon and Sun.
        val d = Math.toRadians(297.8501921 + 445267.1114034 * t - 0.0018819 * t * t)
        val m = Math.toRadians(357.5291092 + 35999.0502909 * t - 0.0001536 * t * t)
        val mp = Math.toRadians(134.9633964 + 477198.8675055 * t + 0.0087414 * t * t)
        val f = Math.toRadians(93.2720950 + 483202.0175233 * t - 0.0036539 * t * t)

        // Step 3: Compute Ecliptic Coordinates.
        val l0 = Math.toRadians(280.46646 + 36000.76983 * t)
        val lMoon = Math.toRadians(218.3164477 + 481267.88123421 * t)

        val lambdaSun = l0 + Math.toRadians(1.914602) * sin(m) + Math.toRadians(0.019993) * sin(2 * m)

        var lambdaMoon = lMoon + Math.toRadians(6.289) * sin(mp)
        lambdaMoon -= Math.toRadians(1.274) * sin(mp - 2 * d)
        lambdaMoon += Math.toRadians(0.658) * sin(2 * d)
        lambdaMoon += Math.toRadians(0.214) * sin(2 * mp)
        lambdaMoon -= Math.toRadians(0.186) * sin(m)

        var betaMoon = Math.toRadians(5.128) * sin(f)
        betaMoon += Math.toRadians(0.280) * sin(mp + f)
        betaMoon += Math.toRadians(0.278) * sin(mp - f)

        // Step 4: Convert to Equatorial Coordinates.
        val epsilon = Math.toRadians(23.439 - 0.0000004 * (jd - 2451545.0))
        val sunRA = atan2(cos(epsilon) * sin(lambdaSun), cos(lambdaSun))
        val sunDec = asin(sin(epsilon) * sin(lambdaSun))
        val moonRA = atan2(
            sin(lambdaMoon) * cos(epsilon) - tan(betaMoon) * sin(epsilon),
            cos(lambdaMoon)
        )
        val moonDec = asin(
            sin(betaMoon) * cos(epsilon) +
                    cos(betaMoon) * sin(epsilon) * sin(lambdaMoon)
        )

        return MoonSunEquatorial(
            jd = jd,
            t = t,
            moon = EquatorialCoords(moonRA, moonDec),
            sun = EquatorialCoords(sunRA, sunDec)
        )
    }

    private fun lunarPoleEquatorialRad(t: Double): EquatorialCoords {
        // IAU lunar pole approximation (sufficient for orientation/texture rotation).
        val e1 = Math.toRadians(125.045 - 0.0529921 * t)
        val e2 = Math.toRadians(250.089 - 0.1059842 * t)
        val e3 = Math.toRadians(260.008 + 13.0120009 * t)
        val e4 = Math.toRadians(176.625 + 13.3407154 * t)

        val raDeg = 269.9949 + 0.0031 * t -
                3.8787 * sin(e1) -
                0.1204 * sin(e2) +
                0.0700 * sin(e3) -
                0.0172 * sin(e4)
        val decDeg = 66.5392 + 0.0130 * t +
                1.5419 * cos(e1) +
                0.0239 * cos(e2) -
                0.0278 * cos(e3) +
                0.0068 * cos(e4)

        return EquatorialCoords(Math.toRadians(raDeg), Math.toRadians(decDeg))
    }

    private fun positionAngleRad(
        targetRa: Double,
        targetDec: Double,
        originRa: Double,
        originDec: Double
    ): Double {
        val numer = cos(targetDec) * sin(targetRa - originRa)
        val denom = cos(originDec) * sin(targetDec) -
                sin(originDec) * cos(targetDec) * cos(targetRa - originRa)
        return atan2(numer, denom)
    }

    private fun parallacticAngleRad(
        jd: Double,
        obsLatDeg: Double,
        obsLonDeg: Double,
        ra: Double,
        dec: Double
    ): Double {
        val lat = Math.toRadians(obsLatDeg)
        val lst = localSiderealTimeRad(jd, obsLonDeg)
        val hourAngle = wrapRadians(lst - ra)
        return atan2(
            sin(hourAngle),
            tan(lat) * cos(dec) - sin(dec) * cos(hourAngle)
        )
    }

    private fun localSiderealTimeRad(jd: Double, lonDeg: Double): Double {
        val t = (jd - 2451545.0) / 36525.0
        val gmstDeg = 280.46061837 +
                360.98564736629 * (jd - 2451545.0) +
                0.000387933 * t * t -
                (t * t * t) / 38710000.0
        val lstDeg = (gmstDeg + lonDeg) % 360.0
        val normalized = ((lstDeg % 360.0) + 360.0) % 360.0
        return Math.toRadians(normalized)
    }

    private fun wrapRadians(angle: Double): Double {
        val twoPi = 2.0 * Math.PI
        var wrapped = angle % twoPi
        if (wrapped <= -Math.PI) wrapped += twoPi
        if (wrapped > Math.PI) wrapped -= twoPi
        return wrapped
    }

    private fun normalizeDegrees(angle: Double): Double {
        var normalized = angle % 360.0
        if (normalized < 0.0) normalized += 360.0
        return normalized
    }
}
