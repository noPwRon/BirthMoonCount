// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon.astronomy

// Imports are used to bring in code from other parts of the project or from external libraries.
import java.time.Instant // Represents a specific moment in time, stored in UTC.
import kotlin.math.* // Imports all standard mathematical functions like sin, cos, etc.

/**
 * This is a Kotlin 'object', which means there is only one instance of this class in the entire application (a Singleton).
 * 'MoonPhase' is a utility object that calculates the current phase of the Moon.
 *
 * It uses a relatively simple and fast "Mean Phase" calculation, which is suitable for UI elements like labels and
 * for getting an approximate illumination percentage. For a simple app, we don't need the extreme precision of
 * complex planetary theories like VSOP87. We can get good-enough results by using the standard mean (average)
 * orbital elements of the Moon and the Sun.
 */
object MoonPhase {

    /**
     * A 'data class' is a special type of class in Kotlin that is primarily used to hold data.
     * This 'PhaseResult' data class holds the result of a phase calculation.
     *
     * @param fraction The illuminated fraction of the moon's disk, where 0.0 is a New Moon and 1.0 is a Full Moon.
     * @param phaseName A human-readable string describing the current phase (e.g., "Waxing Gibbous").
     * @param ageDays The approximate age of the moon in days since the last New Moon (from 0 to about 29.5).
     */
    data class PhaseResult(
        val fraction: Double, 
        val phaseName: String,
        val ageDays: Double
    )

    /**
     * Computes the moon phase for a given instant in time.
     *
     * @param now The exact 'Instant' for which to calculate the phase.
     * @return A 'PhaseResult' object containing the illumination fraction, phase name, and age.
     */
    fun compute(now: Instant): PhaseResult {
        // Step 1: Convert the current time to a Julian Day (JD).
        // The Julian Day is a continuous count of days since a very remote date (January 1, 4713 BC).
        // It is the standard way of representing time in astronomical algorithms because it avoids the complexity of calendar systems.
        // The number 2440587.5 is the Julian Day for the standard computer time epoch (January 1, 1970, 00:00:00 UTC).
        val jd = (now.toEpochMilli() / 86400000.0) + 2440587.5
        
        // Calculate 'T', which is the time in Julian Centuries (periods of 36525 days) since the J2000.0 epoch (January 1, 2000).
        // This is a standard time variable used in many astronomical formulas.
        val t = (jd - 2451545.0) / 36525.0

        // Step 2: Calculate the approximate Age of the Moon.
        // The age is the number of days that have passed since the last New Moon.
        // We use a known reference New Moon (January 6, 2000) and the mean (average) length of a synodic month.
        val synodicMonth = 29.530588853 // The average time from one New Moon to the next.
        val daysSinceRef = jd - 2451550.26 // The Julian Day of the reference New Moon.
        // The modulo operator (%) gives us the remainder of a division. This effectively tells us how many days we are into the current lunar cycle.
        val age = ((daysSinceRef % synodicMonth) + synodicMonth) % synodicMonth

        // Step 3: Calculate Astronomical Angles (in Radians) to determine the Illumination.
        // These formulas are simplified versions from Jean Meeus's "Astronomical Algorithms".
        
        // D: Mean Elongation of the Moon. This is the mean angular distance between the Moon and the Sun in the sky.
        val d = Math.toRadians(297.8501921 + 445267.1114034 * t - 0.0018819 * t * t)
        
        // M: Mean Anomaly of the Sun. This relates to the position of the Earth in its elliptical orbit around the Sun.
        val m = Math.toRadians(357.5291092 + 35999.0502909 * t - 0.0001536 * t * t)
        
        // Mp: Mean Anomaly of the Moon. This relates to the position of the Moon in its elliptical orbit around the Earth.
        val mp = Math.toRadians(134.9633964 + 477198.8675055 * t + 0.0087414 * t * t)

        // i: The Phase Angle. This is the angle between the Sun and the Earth as seen from the Moon (Sun-Moon-Earth).
        // The illuminated fraction of the Moon that we see depends directly on this angle.
        // The formula starts with (PI - d) and then adds several correction terms to account for the elliptical orbits.
        val i = Math.PI - d - Math.toRadians(6.289) * sin(mp) +
                Math.toRadians(2.100) * sin(m) - Math.toRadians(1.274) * sin(2 * d - mp) -
                Math.toRadians(0.658) * sin(2 * d) - Math.toRadians(0.214) * sin(2 * mp) -
                Math.toRadians(0.110) * sin(d)

        // k: The Illuminated Fraction of the Moon's disk (from 0.0 to 1.0).
        // This is calculated with the simple formula: k = (1 + cos(i)) / 2.
        // If the phase angle 'i' is 0 (Full Moon), cos(i) is 1, so k = (1+1)/2 = 1.
        // If the phase angle 'i' is PI (180 degrees, New Moon), cos(i) is -1, so k = (1-1)/2 = 0.
        val k = (1 + cos(i)) / 2.0

        // Step 4: Determine the human-readable Phase Name based on the age of the Moon.
        // While the phase itself is a continuous value, we commonly use names for specific ranges of the lunar cycle.
        val phaseName = when {
            age < 1.0 -> "New Moon"
            age < 6.4 -> "Waxing Crescent" // Getting bigger
            age < 8.4 -> "First Quarter"    // Half lit, at about 7.4 days
            age < 13.8 -> "Waxing Gibbous"
            age < 15.8 -> "Full Moon"        // Fully lit, at about 14.8 days
            age < 21.1 -> "Waning Gibbous" // Getting smaller
            age < 23.1 -> "Last Quarter"     // Half lit, at about 22.1 days
            age < 28.5 -> "Waning Crescent"
            else -> "New Moon"
        }

        // Return all the calculated results in our data class.
        return PhaseResult(k, phaseName, age)
    }
}
