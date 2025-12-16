package com.kimLunation.moon.astronomy

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs

object MoonStats {

    // --- Configuration ---
    // Birth date needs to be parsed correctly.
    // Malaga is in CET/CEST. In March, it's CET (UTC+1).
    private val BIRTH_INSTANT: Instant = ZonedDateTime.of(1987, 3, 6, 15, 50, 0, 0, ZoneId.of("Europe/Madrid")).toInstant()
    private val VICTORIA_ZONE: ZoneId = ZoneId.of("America/Vancouver")

    // --- Public API ---

    /**
     * Calculates the percentage of the moon's disk that is illuminated.
     * Uses a simple linear approximation based on the synodic month.
     */
    fun illuminationPercent(now: Instant): Double {
        val phase = phaseFraction(now)
        // Approximate illumination based on phase fraction (0.0=new, 0.5=full, 1.0=new)
        val illumination = 1.0 - (2.0 * abs(phase - 0.5))
        return illumination * 100.0
    }

    /**
     * Counts full moons since a given birth instant.
     * This is an approximation. A high-precision implementation would require a Meeus algorithm.
     */
    fun fullMoonsSinceBirth(now: Instant): Int {
        val years = java.time.Duration.between(BIRTH_INSTANT, now).toDays() / 365.25
        return (years * 12.3685).toInt()
    }

    /**
     * Provides a traditional name for the full moon of the current month.
     * This is a simplified, Northern Hemisphere-centric naming convention.
     */
    fun moonName(now: Instant): String {
        val zdt = ZonedDateTime.ofInstant(now, VICTORIA_ZONE)
        val phase = phaseFraction(now)

        // Define a small window around the full moon (phase = 0.5)
        if (phase > 0.47 && phase < 0.53) {
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
                else -> "Full Moon"
            }
        } else {
            // Use standard phase names for other times
            return when {
                phase < 0.03 || phase > 0.97 -> "New Moon"
                phase < 0.25 -> "Waxing Crescent"
                phase < 0.47 -> "Waxing Gibbous"
                phase < 0.53 -> "Full Moon" // Already covered, but for completeness
                phase < 0.75 -> "Waning Gibbous"
                phase < 0.97 -> "Waning Crescent"
                else -> "New Moon"
            }
        }
    }

    // --- Private Helpers ---

    /**
     * Calculates the phase fraction of the moon (0.0 to 1.0).
     */
    private fun phaseFraction(now: Instant): Double {
        // Reference New Moon: January 6, 2000, 18:14 UTC
        val refNewMoon = ZonedDateTime.of(2000, 1, 6, 18, 14, 0, 0, ZoneId.of("UTC")).toInstant()
        val synodicMonthMillis = 29.530588853 * 24 * 60 * 60 * 1000
        
        val diff = now.toEpochMilli() - refNewMoon.toEpochMilli()
        val fraction = (diff.toDouble() / synodicMonthMillis) % 1.0
        return if (fraction < 0) fraction + 1.0 else fraction
    }
}
