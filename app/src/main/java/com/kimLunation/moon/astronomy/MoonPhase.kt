package com.kimLunation.moon.astronomy

import java.time.Instant
import kotlin.math.*

/**
 * MoonPhase
 *
 * This utility object calculates the current phase of the Moon.
 *
 * It uses a low-precision "Mean Phase" calculation suitable for UI labels and approximate illumination.
 * For simpler applications, we don't need the full VSOP87 planetary theory;
 * we can rely on standard mean orbital elements of the Moon and Sun.
 */
object MoonPhase {

    /**
     * Data class to hold the result of a phase calculation.
     * @param fraction The illuminated fraction of the moon's disk (0.0 = New, 1.0 = Full).
     * @param phaseName A human-readable string describing the phase (e.g., "Waxing Gibbous").
     * @param ageDays The approximate age of the moon in days since the last New Moon (0..29.5).
     */
    data class PhaseResult(
        val fraction: Double, 
        val phaseName: String,
        val ageDays: Double
    )

    /**
     * Computes the moon phase for a given instant in time.
     *
     * @param now The exact time for which to calculate the phase.
     * @return PhaseResult containing illumination and name.
     */
    fun compute(now: Instant): PhaseResult {
        // Convert the current time to Julian Day (JD).
        // The Julian Day is a continuous count of days since the beginning of the Julian Period (4713 BC).
        // It is the standard time unit for astronomical algorithms.
        // 2440587.5 is the JD for the Unix Epoch (1970-01-01 00:00:00 UTC).
        val jd = (now.toEpochMilli() / 86400000.0) + 2440587.5
        
        // Calculate 'T', the number of Julian Centuries since the epoch J2000.0.
        // This scales the time variable for the polynomial formulas below.
        val t = (jd - 2451545.0) / 36525.0

        // 1. Calculate the approximate Age of the Moon.
        // We use a known reference New Moon (Jan 6, 2000) and the mean length of a synodic month.
        // Synodic Month = 29.53059 days (New Moon to New Moon).
        val synodicMonth = 29.530588853
        val daysSinceRef = jd - 2451550.26 // JD of reference New Moon
        // Modulo operator gives us the days into the current cycle.
        val age = ((daysSinceRef % synodicMonth) + synodicMonth) % synodicMonth

        // 2. Calculate Astronomical Angles (in Radians) to determine Illumination.
        // These formulas come from "Astronomical Algorithms" by Jean Meeus.
        
        // D: Mean Elongation of the Moon (angular distance from Sun).
        val d = Math.toRadians(297.8501921 + 445267.1114034 * t - 0.0018819 * t * t)
        
        // M: Mean Anomaly of the Sun.
        val m = Math.toRadians(357.5291092 + 35999.0502909 * t - 0.0001536 * t * t)
        
        // Mp: Mean Anomaly of the Moon.
        val mp = Math.toRadians(134.9633964 + 477198.8675055 * t + 0.0087414 * t * t)

        // i: Phase Angle (angle Sun-Moon-Earth).
        // The illuminated fraction k depends on this angle.
        // This formula includes several periodic terms to correct for the Moon's and Earth's elliptical orbits.
        val i = Math.PI - d - Math.toRadians(6.289) * sin(mp) +
                Math.toRadians(2.100) * sin(m) - Math.toRadians(1.274) * sin(2 * d - mp) -
                Math.toRadians(0.658) * sin(2 * d) - Math.toRadians(0.214) * sin(2 * mp) -
                Math.toRadians(0.110) * sin(d)

        // k: Illuminated Fraction (0.0 to 1.0).
        // Formula: k = (1 + cos(i)) / 2
        val k = (1 + cos(i)) / 2.0

        // 3. Determine the Phase Name based on the Age.
        // While phase is strictly an instant, we assign names to ranges of days.
        val phaseName = when {
            age < 1.0 -> "New Moon"
            age < 6.4 -> "Waxing Crescent"
            age < 8.4 -> "First Quarter" // Approx 7.4 days
            age < 13.8 -> "Waxing Gibbous"
            age < 15.8 -> "Full Moon"    // Approx 14.8 days
            age < 21.1 -> "Waning Gibbous"
            age < 23.1 -> "Last Quarter" // Approx 22.1 days
            age < 28.5 -> "Waning Crescent"
            else -> "New Moon"
        }

        return PhaseResult(k, phaseName, age)
    }
}
