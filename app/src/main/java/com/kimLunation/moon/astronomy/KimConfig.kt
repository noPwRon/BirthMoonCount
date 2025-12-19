// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon.astronomy

// Imports are used to bring in code from other parts of the project or from external libraries.
import java.time.Instant // Represents a specific moment in time, stored in UTC.
import java.time.ZoneId // Represents a time zone, like "America/Vancouver".

/**
 * This is a Kotlin 'object', which is a special kind of class that only has one instance.
 * This is known as the Singleton pattern. It's a great place to store configuration values
 * that are used throughout the application, because they are globally accessible and only created once.
 *
 * This object, 'KimConfig', holds centralized configuration details for the app, such as
 * the observer's location and a specific birth date for calculations.
 */
object KimConfig {

    // --- Observer Coordinates: Victoria, BC ---
    // These latitude and longitude values are needed for astronomical calculations that depend on the
    // observer's position on Earth. While simple phase calculations can be done without a specific location,
    // having this data allows for more advanced features in the future, like calculating the moon's
    // exact position in the sky (altitude and azimuth).

    // 'const val' is used for values that are known at compile time. This is more efficient.
    const val OBS_LAT = 48.4284  // Latitude of Victoria, BC, Canada.
    const val OBS_LON = -123.3656 // Longitude of Victoria, BC, Canada.

    // --- Display TimeZone ---
    // It's important to show dates and times in the user's local time zone.
    // We use "America/Vancouver" to ensure that all times are displayed correctly for the user.
    // 'java.time.ZoneId' is part of the modern and recommended Java Date/Time API.
    val TIMEZONE_DISPLAY: ZoneId = ZoneId.of("America/Vancouver")

    // --- Birth Instant: March 6, 1987, 3:50 PM in Madrid, Spain ---
    // This is a key piece of data for a personalized feature of the app: calculating the number of full moons
    // that have occurred since a specific birth date.
    // We store this as an 'Instant', which is a point in time in UTC (Coordinated Universal Time).
    // This is the best practice to avoid any confusion or errors related to time zones.
    //
    // The original time was 3:50 PM in Madrid. In March 1987, Madrid was on Central European Time (CET),
    // which is UTC+1. So, 3:50 PM in Madrid was 2:50 PM in UTC.
    //
    // 'Instant.parse' is used to create an 'Instant' from a standard ISO-8601 formatted string.
    // The 'Z' at the end signifies Zulu time, which is another name for UTC.
    val BIRTH_INSTANT: Instant = Instant.parse("1987-03-06T14:50:00Z")
}
