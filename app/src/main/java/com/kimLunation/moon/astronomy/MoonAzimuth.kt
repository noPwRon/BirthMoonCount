package com.kimLunation.moon.astronomy

import java.time.Instant
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

object MoonAzimuth {
    fun azimuthDeg(now: Instant, latDeg: Double, lonDeg: Double): Double {
        val jd = julianDay(now)
        val t = (jd - 2451545.0) / 36525.0

        val d = Math.toRadians(297.8501921 + 445267.1114034 * t - 0.0018819 * t * t)
        val m = Math.toRadians(357.5291092 + 35999.0502909 * t - 0.0001536 * t * t)
        val mp = Math.toRadians(134.9633964 + 477198.8675055 * t + 0.0087414 * t * t)
        val f = Math.toRadians(93.2720950 + 483202.0175233 * t - 0.0036539 * t * t)

        val lMoon = Math.toRadians(218.3164477 + 481267.88123421 * t)

        var lambdaMoon = lMoon + Math.toRadians(6.289) * sin(mp)
        lambdaMoon -= Math.toRadians(1.274) * sin(mp - 2 * d)
        lambdaMoon += Math.toRadians(0.658) * sin(2 * d)
        lambdaMoon += Math.toRadians(0.214) * sin(2 * mp)
        lambdaMoon -= Math.toRadians(0.186) * sin(m)

        var betaMoon = Math.toRadians(5.128) * sin(f)
        betaMoon += Math.toRadians(0.280) * sin(mp + f)
        betaMoon += Math.toRadians(0.278) * sin(mp - f)

        val epsilon = Math.toRadians(23.439 - 0.0000004 * (jd - 2451545.0))

        val ra = atan2(
            sin(lambdaMoon) * cos(epsilon) - tan(betaMoon) * sin(epsilon),
            cos(lambdaMoon)
        )
        val dec = asin(
            sin(betaMoon) * cos(epsilon) +
                cos(betaMoon) * sin(epsilon) * sin(lambdaMoon)
        )

        val gmst = 280.46061837 +
            360.98564736629 * (jd - 2451545.0) +
            0.000387933 * t * t -
            (t * t * t) / 38710000.0
        val lstDeg = (gmst + lonDeg) % 360.0
        val lst = Math.toRadians((lstDeg + 360.0) % 360.0)

        val ha = lst - ra
        val lat = Math.toRadians(latDeg)

        val az = atan2(
            -sin(ha),
            tan(dec) * cos(lat) - sin(lat) * cos(ha)
        )
        return (Math.toDegrees(az) + 360.0) % 360.0
    }

    private fun julianDay(now: Instant): Double {
        return (now.toEpochMilli() / 86400000.0) + 2440587.5
    }
}
