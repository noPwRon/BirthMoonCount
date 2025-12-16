package com.kimLunation.moon.astronomy

import java.time.Instant
import kotlin.math.*

/**
 * MoonFullMoonsMeeus
 *
 * This object is responsible for accurately counting the number of Full Moons between two dates.
 * It uses the algorithms described by Jean Meeus in "Astronomical Algorithms" (Chapter 49).
 *
 * Why Meeus?
 * A simple "every 29.5 days" loop drifts significantly over decades because the lunar month varies.
 * Meeus provides a formula to calculate the exact time of a specific lunation 'k' (where k is an integer counter).
 * By finding the exact time, we can be sure if a Full Moon fell strictly within the time window.
 */
object MoonFullMoonsMeeus {

    /**
     * Counts how many full moons have occurred between start (exclusive) and end (inclusive).
     *
     * @param start The starting Instant (e.g., birth date).
     * @param end The ending Instant (e.g., now).
     * @return The integer count of full moons.
     */
    fun countFullMoons(start: Instant, end: Instant): Int {
        if (start.isAfter(end)) return 0

        var count = 0
        
        // estimateK finds an approximate lunation number k for the start date.
        var k = estimateK(start)

        // Iterate forward from the estimated k.
        // We calculate the exact time of Full Moon #k.
        // If it's after 'end', we stop.
        // If it's after 'start' (and before 'end'), we count it.
        while (true) {
            val jd = computeFullMoonJD(k)
            val fullMoonTime = jdToInstant(jd)

            if (fullMoonTime.isAfter(end)) {
                break
            }
            if (fullMoonTime.isAfter(start)) {
                count++
            }
            k++
        }
        
        return count
    }

    /**
     * Returns the Instant of the next full moon after 'now'.
     */
    fun nextFullMoon(now: Instant): Instant {
        var k = estimateK(now)
        while (true) {
            val jd = computeFullMoonJD(k)
            val fullMoonTime = jdToInstant(jd)
            if (fullMoonTime.isAfter(now)) {
                return fullMoonTime
            }
            k++
        }
    }

    /**
     * Estimates the lunation number K based on the year.
     * k=0 corresponds roughly to the first New Moon of 2000.
     * 12.3685 is the average number of lunations per year.
     */
    private fun estimateK(time: Instant): Double {
        val year = time.atZone(java.time.ZoneOffset.UTC).year
        val kApprox = (year - 2000) * 12.3685
        // Start a bit early (minus 2 lunations) to ensure we don't miss one due to approximation error.
        return floor(kApprox) - 2 
    }

    private fun jdToInstant(jd: Double): Instant {
        val millis = (jd - 2440587.5) * 86400000.0
        return Instant.ofEpochMilli(millis.toLong())
    }

    /**
     * Computes the Julian Day (JD) of the Full Moon for a given lunation number k.
     * Based on Meeus Chapter 49.
     */
    private fun computeFullMoonJD(k: Double): Double {
        // k is the integer number of New Moons since 2000.0.
        // Full Moon occurs half a cycle later, so we use k + 0.5.
        val kFull = k + 0.5 
        
        // T is time in centuries since 2000.0
        val T = kFull / 1236.85

        // Mean time of phase (JDE). Base polynomial.
        var JDE = 2451550.09766 + 29.530588861 * kFull +
                0.00015437 * T * T - 0.000000150 * T * T * T +
                0.00000000073 * T * T * T * T

        // Calculate Solar and Lunar Anomalies (M, Mp) and Argument of Latitude (F).
        // These are needed for the periodic corrections below.
        
        // Sun's mean anomaly
        val M = Math.toRadians(2.5534 + 29.10535670 * kFull - 0.0000014 * T * T - 0.00000011 * T * T * T)
        
        // Moon's mean anomaly
        val Mp = Math.toRadians(201.5643 + 385.81693528 * kFull + 0.0107588 * T * T + 0.00001238 * T * T * T - 0.000000058 * T * T * T * T)

        // Argument of latitude
        val F = Math.toRadians(160.7108 + 390.67050284 * kFull - 0.0016118 * T * T - 0.00000227 * T * T * T + 0.000000041 * T * T * T * T)

        // Longitude of ascending node (Omega) - small correction term
        val Omega = Math.toRadians(124.7746 - 1.56375588 * kFull + 0.0020672 * T * T + 0.00000215 * T * T * T)

        // Apply Periodic Corrections (Planetary arguments).
        // These terms account for the complex gravitational interactions (Evection, Variation, etc.).
        // Coefficients are from Meeus Table 49.A.
        var correction = 0.0

        // Selected largest terms for reasonable accuracy (~minutes):
        correction += -0.40720 * sin(Mp)      // Evection
        correction +=  0.17241 * sin(M)       // Annual Equation
        correction +=  0.01608 * sin(2 * Mp)  // Variation
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

        JDE += correction

        return JDE
    }
}
