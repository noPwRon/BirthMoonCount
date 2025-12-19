// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon.astronomy

// Imports are used to bring in code from other parts of the project or from external libraries.
import java.time.Instant // Represents a specific moment in time, stored in UTC.
import kotlin.math.* // Imports all standard mathematical functions like sin, cos, floor, etc.

/**
 * This is a Kotlin 'object', which means there is only one instance of this class in the entire application (a Singleton).
 * 'MoonFullMoonsMeeus' is responsible for accurately counting the number of full moons between two dates.
 * It uses the algorithms described by the famous astronomer Jean Meeus in his book "Astronomical Algorithms" (Chapter 49).
 *
 * Why use a complex algorithm like Meeus's?
 * A simple approach, like assuming a full moon occurs every 29.53 days, is not very accurate over long periods.
 * The actual time between full moons (the synodic month) varies because the Moon's orbit is not a perfect circle
 * and is influenced by the gravity of the Sun and other planets.
 * Meeus provides a formula to calculate the exact time of any given full moon, which is much more accurate.
 */
object MoonFullMoonsMeeus {

    /**
     * Counts how many full moons have occurred between a start date (exclusive) and an end date (inclusive).
     *
     * @param start The starting 'Instant' (for example, a birth date). The count does not include a full moon that happens at this exact instant.
     * @param end The ending 'Instant' (for example, the current time). The count *does* include a full moon that happens at this exact instant.
     * @return The integer count of full moons.
     */
    fun countFullMoons(start: Instant, end: Instant): Int {
        // If the start date is after the end date, no full moons could have occurred.
        if (start.isAfter(end)) return 0

        var count = 0
        
        // 'k' is a number that represents a specific lunation (lunar cycle). k=0 is the first new moon of the year 2000.
        // We first estimate a starting value for 'k' that is close to our start date.
        var k = estimateK(start)

        // We loop, calculating the exact time of each full moon, one by one.
        while (true) {
            // Calculate the Julian Day of the full moon for the current 'k'.
            val jd = computeFullMoonJD(k)
            // Convert the Julian Day to a more standard 'Instant' object.
            val fullMoonTime = jdToInstant(jd)

            // If the calculated full moon time is after our end date, we have gone too far and can stop.
            if (fullMoonTime.isAfter(end)) {
                break
            }
            // If the full moon happened after our start date (and we already know it's before our end date),
            // then it falls within our desired time window, and we should count it.
            if (fullMoonTime.isAfter(start)) {
                count++
            }
            // Move to the next lunation cycle.
            k++
        }
        
        return count
    }

    /**
     * Finds the 'Instant' of the next full moon that will occur after the given 'now' time.
     *
     * @param now The 'Instant' from which to start searching.
     * @return The 'Instant' of the next full moon.
     */
    fun nextFullMoon(now: Instant): Instant {
        // Estimate the lunation number 'k' for the current time.
        var k = estimateK(now)
        // Loop forward, calculating the time of each full moon.
        while (true) {
            val jd = computeFullMoonJD(k)
            val fullMoonTime = jdToInstant(jd)
            // The first full moon we find that is after 'now' is the one we're looking for.
            if (fullMoonTime.isAfter(now)) {
                return fullMoonTime
            }
            // Otherwise, keep checking the next one.
            k++
        }
    }

    /**
     * Estimates the lunation number 'k' for a given time.
     * This gives us a good starting point for our search, so we don't have to start from k=0 every time.
     * There are approximately 12.3685 lunations (lunar cycles) in a year.
     */
    private fun estimateK(time: Instant): Double {
        // Get the year from the 'Instant'.
        val year = time.atZone(java.time.ZoneOffset.UTC).year
        // Calculate an approximate 'k' value.
        val kApprox = (year - 2000) * 12.3685
        // We use 'floor' to get the integer part and then subtract 2. This gives us a starting point
        // that is safely before the actual 'k' for the given date, ensuring we don't miss any full moons.
        return floor(kApprox) - 2 
    }

    /**
     * A helper function to convert a Julian Day (JD) number to a standard Java 'Instant'.
     * @param jd The Julian Day.
     * @return The corresponding 'Instant'.
     */
    private fun jdToInstant(jd: Double): Instant {
        // This formula converts the Julian Day to milliseconds since the Unix epoch (Jan 1, 1970).
        val millis = (jd - 2440587.5) * 86400000.0
        return Instant.ofEpochMilli(millis.toLong())
    }

    /**
     * This is the core of the algorithm. It computes the Julian Day (JD) of the full moon for a given lunation number 'k'.
     * The formulas are taken directly from Jean Meeus's "Astronomical Algorithms", Chapter 49.
     *
     * @param k The lunation number.
     * @return The Julian Day of the corresponding full moon.
     */
    private fun computeFullMoonJD(k: Double): Double {
        // In Meeus's model, 'k' is an integer number of *new* moons since the year 2000.
        // A full moon happens about half a cycle after a new moon, so we use k + 0.5 for our calculations.
        val kFull = k + 0.5 
        
        // 'T' is the time in Julian centuries since the epoch J2000.0 (January 1, 2000).
        val T = kFull / 1236.85

        // --- Mean Time of Phase (JDE) ---
        // This is a polynomial formula that gives a very good first approximation of the time of the full moon.
        // JDE stands for Julian Day, Ephemeris (a uniform timescale used by astronomers).
        var JDE = 2451550.09766 + 29.530588861 * kFull +
                0.00015437 * T * T - 0.000000150 * T * T * T +
                0.00000000073 * T * T * T * T

        // --- Calculate Mean Anomalies and Argument of Latitude ---
        // These values describe the positions of the Sun and Moon in their orbits.
        // They are needed for the correction terms below.
        
        // Sun's mean anomaly (M)
        val M = Math.toRadians(2.5534 + 29.10535670 * kFull - 0.0000014 * T * T - 0.00000011 * T * T * T)
        
        // Moon's mean anomaly (Mp)
        val Mp = Math.toRadians(201.5643 + 385.81693528 * kFull + 0.0107588 * T * T + 0.00001238 * T * T * T - 0.000000058 * T * T * T * T)

        // Moon's argument of latitude (F)
        val F = Math.toRadians(160.7108 + 390.67050284 * kFull - 0.0016118 * T * T - 0.00000227 * T * T * T + 0.000000041 * T * T * T * T)

        // Longitude of the Moon's ascending node (Omega) - needed for a small correction.
        val Omega = Math.toRadians(124.7746 - 1.56375588 * kFull + 0.0020672 * T * T + 0.00000215 * T * T * T)

        // --- Apply Periodic Corrections ---
        // The simple formula for JDE isn't perfect. These correction terms are added to account for the
        // gravitational pulls of the Sun and other planets, which cause small variations in the Moon's orbit.
        // Each term is a sine function based on the anomalies calculated above.
        // The numbers (coefficients) are from Table 49.A in Meeus's book.
        // We are only using the largest and most important terms for reasonable accuracy (down to a few minutes).
        var correction = 0.0

        correction += -0.40720 * sin(Mp)      // Evection (a major perturbation)
        correction +=  0.17241 * sin(M)       // Annual Equation (due to Earth's elliptical orbit)
        correction +=  0.01608 * sin(2 * Mp)  // Variation (another major perturbation)
        correction +=  0.01039 * sin(2 * F)
        correction +=  0.00739 * sin(Mp - M)
        correction += -0.00514 * sin(Mp + M)
        correction +=  0.00208 * sin(2 * M)
        correction += -0.00111 * sin(Mp - 2 * F)
        correction += -0.00057 * sin(Mp + 2 * F)
        correction +=  0.00056 * sin(2 * Mp + M)
        correction += -0.00042 * sin(3 * Mp)
        correction +=  0.00042 * sin(M + 2 * F)
        correction +=  0.00038 * sin(M - 2 * F)
        correction += -0.00024 * sin(2 * Mp - M)
        correction += -0.00017 * sin(Omega) 

        // Add the total correction to our initial estimate.
        JDE += correction

        // Return the final, accurate Julian Day of the full moon.
        return JDE
    }
}
