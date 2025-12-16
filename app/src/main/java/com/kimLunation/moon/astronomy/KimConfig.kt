package com.kimLunation.moon.astronomy

import java.time.Instant
import java.time.ZoneId

/**
 * KimConfig
 *
 * This object serves as a centralized configuration holder for the application.
 * It contains constants related to the specific user requirements:
 * - Observer location (Victoria, BC)
 * - Birth instant for calculating the "Full Moons since birth" metric.
 *
 * Using an 'object' in Kotlin creates a Singleton, meaning these values are globally accessible
 * and initialized only once.
 */
object KimConfig {
    
    // Observer Coordinates: Victoria, BC
    // Latitude and Longitude are required for topocentric astronomical calculations,
    // although for simple phase and orientation, geocentric (earth-center) approximation is often close enough.
    // However, knowing the location allows for future expansion (e.g. altitude/azimuth of the moon).
    const val OBS_LAT = 48.4284
    const val OBS_LON = -123.3656

    // Display TimeZone
    // We use "America/Vancouver" to format dates correctly for the user's location.
    // java.time.ZoneId is part of the modern Java Date/Time API (JSR-310).
    val TIMEZONE_DISPLAY: ZoneId = ZoneId.of("America/Vancouver")

    // Birth Instant: 1987-03-06 15:50 Europe/Madrid
    // We store this as a fixed Instant in UTC to avoid timezone ambiguity during calculations.
    // Madrid was CET (UTC+1) in March 1987 (Standard Time, not DST yet).
    // So 15:50 Local = 14:50 UTC.
    // Instant.parse accepts ISO-8601 format strings.
    val BIRTH_INSTANT: Instant = Instant.parse("1987-03-06T14:50:00Z")
}
