package com.kimLunation.moon.astronomy

import java.time.Instant
import kotlin.math.floor
import kotlin.math.sin

enum class ZodiacSign {
    ARIES,
    TAURUS,
    GEMINI,
    CANCER,
    LEO,
    VIRGO,
    LIBRA,
    SCORPIO,
    SAGITTARIUS,
    CAPRICORN,
    AQUARIUS,
    PISCES
}

object MoonZodiac {
    fun sign(now: Instant): ZodiacSign {
        val lambdaDeg = moonEclipticLongitudeDeg(now)
        val index = floor(lambdaDeg / 30.0).toInt().coerceIn(0, 11)
        return ZodiacSign.values()[index]
    }

    private fun moonEclipticLongitudeDeg(now: Instant): Double {
        val jd = julianDay(now)
        val t = (jd - 2451545.0) / 36525.0

        val d = Math.toRadians(297.8501921 + 445267.1114034 * t - 0.0018819 * t * t)
        val m = Math.toRadians(357.5291092 + 35999.0502909 * t - 0.0001536 * t * t)
        val mp = Math.toRadians(134.9633964 + 477198.8675055 * t + 0.0087414 * t * t)

        val lMoon = Math.toRadians(218.3164477 + 481267.88123421 * t)

        var lambdaMoon = lMoon + Math.toRadians(6.289) * sin(mp)
        lambdaMoon -= Math.toRadians(1.274) * sin(mp - 2 * d)
        lambdaMoon += Math.toRadians(0.658) * sin(2 * d)
        lambdaMoon += Math.toRadians(0.214) * sin(2 * mp)
        lambdaMoon -= Math.toRadians(0.186) * sin(m)

        val deg = Math.toDegrees(lambdaMoon)
        return ((deg % 360.0) + 360.0) % 360.0
    }

    private fun julianDay(now: Instant): Double {
        return (now.toEpochMilli() / 86400000.0) + 2440587.5
    }
}
