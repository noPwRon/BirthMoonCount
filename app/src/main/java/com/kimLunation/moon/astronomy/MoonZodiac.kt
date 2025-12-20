// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon.astronomy

// Imports are used to bring in code from other parts of the project or from external libraries.
import java.time.Instant // Represents a specific moment in time, stored in UTC.
import kotlin.math.abs // A mathematical function to compute absolute values.
import kotlin.math.asin // A mathematical function to calculate the arcsine.
import kotlin.math.atan2 // A mathematical function to calculate the angle from x/y coordinates.
import kotlin.math.cos // A mathematical function to calculate the cosine of an angle.
import kotlin.math.floor // A mathematical function to find the largest integer less than or equal to a number.
import kotlin.math.sin   // A mathematical function to calculate the sine of an angle.
import kotlin.math.tan // A mathematical function to calculate the tangent of an angle.

/**
 * An 'enum' (enumeration) is a special type that represents a fixed set of constants.
 * This enum, 'ZodiacSign', defines the twelve signs of the tropical zodiac.
 */
enum class ZodiacSign {
    ARIES, TAURUS, GEMINI, CANCER, LEO, VIRGO, LIBRA, SCORPIO, SAGITTARIUS, CAPRICORN, AQUARIUS, PISCES
}

/**
 * This is a Kotlin 'object', which means there is only one instance of this class in the entire application (a Singleton).
 * 'MoonZodiac' is responsible for calculating the astrological zodiac sign that the Moon is currently in.
 * This is based on the Moon's position along the ecliptic, the apparent path of the Sun across the sky over the course of a year.
 */
object MoonZodiac {

    /**
     * Determines the zodiac sign for the Moon at a given moment.
     *
     * @param now The 'Instant' for which to calculate the sign.
     * @return The 'ZodiacSign' the Moon is currently in.
     */
    fun sign(
        now: Instant,
        latDeg: Double = KimConfig.OBS_LAT,
        lonDeg: Double = KimConfig.OBS_LON,
        useTopocentric: Boolean = true
    ): ZodiacSign {
        // Calculate the Moon's apparent ecliptic longitude for the observer.
        val lambdaDeg = moonEclipticLongitudeDeg(now, latDeg, lonDeg, useTopocentric)
        
        // To find the sign, we simply divide the longitude by 30. The integer part of the result gives us an index from 0 to 11.
        // For example, if the longitude is 35 degrees, 35 / 30 = 1.16, and the integer part is 1. This corresponds to Taurus.
        val index = floor(lambdaDeg / 30.0).toInt().coerceIn(0, 11) // 'coerceIn' ensures the index is safely within the valid range.
        
        // We then use this index to get the correct sign from our 'ZodiacSign' enum.
        return ZodiacSign.values()[index]
    }

    /**
     * A private helper function to calculate the ecliptic longitude of the Moon in degrees.
     * This uses the same set of simplified astronomical formulas as other parts of the app.
     *
     * @param now The 'Instant' to calculate for.
     * @return The Moon's ecliptic longitude in degrees (from 0 to 360).
     */
    private fun moonEclipticLongitudeDeg(
        now: Instant,
        latDeg: Double,
        lonDeg: Double,
        useTopocentric: Boolean
    ): Double {
        val jd = julianDay(now)
        val t = (jd - 2451545.0) / 36525.0

        val geo = moonGeocentricEcliptic(t)
        if (!useTopocentric) {
            return normalizeDegrees(geo.lambdaDeg)
        }

        return topocentricLongitudeDeg(geo, jd, t, latDeg, lonDeg)
    }

    /**
     * A private helper function to convert an 'Instant' to a Julian Day.
     * @param now The 'Instant' to convert.
     * @return The Julian Day as a Double.
     */
    private data class MoonEcliptic(
        val lambdaDeg: Double,
        val betaDeg: Double,
        val distanceKm: Double
    )

    private data class LrTerm(
        val d: Int,
        val m: Int,
        val mp: Int,
        val f: Int,
        val l: Int,
        val r: Int
    )

    private data class BTerm(
        val d: Int,
        val m: Int,
        val mp: Int,
        val f: Int,
        val b: Int
    )

    private val LR_TERMS = arrayOf(
        LrTerm(0, 0, 1, 0, 6288774, -20905355),
        LrTerm(2, 0, -1, 0, 1274027, -3699111),
        LrTerm(2, 0, 0, 0, 658314, -2955968),
        LrTerm(0, 0, 2, 0, 213618, -569925),
        LrTerm(0, 1, 0, 0, -185116, 48888),
        LrTerm(0, 0, 0, 2, -114332, -3149),
        LrTerm(2, 0, -2, 0, 58793, 246158),
        LrTerm(2, -1, -1, 0, 57066, -152138),
        LrTerm(2, 0, 1, 0, 53322, -170733),
        LrTerm(2, -1, 0, 0, 45758, -204586),
        LrTerm(0, 1, -1, 0, -40923, -129620),
        LrTerm(1, 0, 0, 0, -34720, 108743),
        LrTerm(0, 1, 1, 0, -30383, 104755),
        LrTerm(2, 0, 0, -2, 15327, 10321),
        LrTerm(0, 0, 1, 2, -12528, 0),
        LrTerm(0, 0, 1, -2, 10980, 79661),
        LrTerm(4, 0, -1, 0, 10675, -34782),
        LrTerm(0, 0, 3, 0, 10034, -23210),
        LrTerm(4, 0, -2, 0, 8548, -21636),
        LrTerm(2, 1, -1, 0, -7888, 24208),
        LrTerm(2, 1, 0, 0, -6766, 30824),
        LrTerm(1, 0, -1, 0, -5163, -8379),
        LrTerm(1, 1, 0, 0, 4987, -16675),
        LrTerm(2, -1, 1, 0, 4036, -12831),
        LrTerm(2, 0, 2, 0, 3994, -10445),
        LrTerm(4, 0, 0, 0, 3861, -11650),
        LrTerm(2, 0, -3, 0, 3665, 14403),
        LrTerm(0, 1, -2, 0, -2689, -7003),
        LrTerm(2, 0, -1, 2, -2602, 0),
        LrTerm(2, -1, -2, 0, 2390, 10056),
        LrTerm(1, 0, 1, 0, -2348, 6322),
        LrTerm(2, -2, 0, 0, 2236, -9884),
        LrTerm(0, 1, 2, 0, -2120, 5751),
        LrTerm(0, 2, 0, 0, -2069, 0),
        LrTerm(2, -2, -1, 0, 2048, -4950),
        LrTerm(2, 0, 1, -2, -1773, 4130),
        LrTerm(2, 0, 0, 2, -1595, 0),
        LrTerm(4, -1, -1, 0, 1215, -3958),
        LrTerm(0, 0, 2, 2, -1110, 0),
        LrTerm(3, 0, -1, 0, -892, 3258),
        LrTerm(2, 1, 1, 0, -810, 2616),
        LrTerm(4, -1, -2, 0, 759, -1897),
        LrTerm(0, 2, -1, 0, -713, -2117),
        LrTerm(2, 2, -1, 0, -700, 2354),
        LrTerm(2, 1, -2, 0, 691, 0),
        LrTerm(2, -1, 0, -2, 596, 0),
        LrTerm(4, 0, 1, 0, 549, -1423),
        LrTerm(0, 0, 4, 0, 537, -1117),
        LrTerm(4, -1, 0, 0, 520, -1571),
        LrTerm(1, 0, -2, 0, -487, -1739),
        LrTerm(2, 1, 0, -2, -399, 0),
        LrTerm(0, 0, 2, -2, -381, -4421),
        LrTerm(1, 1, 1, 0, 351, 0),
        LrTerm(3, 0, -2, 0, -340, 0),
        LrTerm(4, 0, -3, 0, 330, 0),
        LrTerm(2, -1, 2, 0, 327, 0),
        LrTerm(0, 2, 1, 0, -323, 1165),
        LrTerm(1, 1, -1, 0, 299, 0),
        LrTerm(2, 0, 3, 0, 294, 0)
    )

    private val B_TERMS = arrayOf(
        BTerm(0, 0, 0, 1, 5128122),
        BTerm(0, 0, 1, 1, 280602),
        BTerm(0, 0, 1, -1, 277693),
        BTerm(2, 0, 0, -1, 173237),
        BTerm(2, 0, -1, 1, 55413),
        BTerm(2, 0, -1, -1, 46271),
        BTerm(2, 0, 0, 1, 32573),
        BTerm(0, 0, 2, 1, 17198),
        BTerm(2, 0, 1, -1, 9266),
        BTerm(0, 0, 2, -1, 8822),
        BTerm(2, -1, 0, -1, 8216),
        BTerm(2, 0, -2, -1, 4324),
        BTerm(2, 0, 1, 1, 4200),
        BTerm(2, 1, 0, -1, -3359),
        BTerm(2, -1, -1, 1, 2463),
        BTerm(2, -1, 0, 1, 2211),
        BTerm(2, -1, -1, -1, 2065),
        BTerm(0, 1, -1, -1, -1870),
        BTerm(4, 0, -1, -1, 1828),
        BTerm(0, 1, 0, 1, -1794),
        BTerm(0, 0, 0, 3, -1749),
        BTerm(0, 1, -1, 1, -1565),
        BTerm(1, 0, 0, 1, -1491),
        BTerm(0, 1, 1, 1, -1475),
        BTerm(0, 1, 1, -1, -1410),
        BTerm(0, 1, 0, -1, -1344),
        BTerm(1, 0, 0, -1, -1335),
        BTerm(0, 0, 3, 1, 1107),
        BTerm(4, 0, 0, -1, 1021),
        BTerm(4, 0, -1, 1, 833),
        BTerm(0, 0, 1, -3, 777),
        BTerm(4, 0, -2, 1, 671),
        BTerm(2, 0, 0, -3, 607),
        BTerm(2, 0, 2, -1, 596),
        BTerm(2, -1, 1, -1, 491),
        BTerm(2, 0, -2, 1, -451),
        BTerm(0, 0, 3, -1, 439),
        BTerm(2, 0, 2, 1, 422),
        BTerm(2, 0, -3, -1, 421),
        BTerm(2, 1, -1, 1, -366),
        BTerm(2, 1, 0, 1, -351),
        BTerm(4, 0, 0, 1, 331),
        BTerm(2, -1, 1, 1, 315),
        BTerm(2, -2, 0, -1, 302),
        BTerm(0, 0, 1, 3, -283),
        BTerm(2, 1, 1, -1, -229),
        BTerm(1, 1, 0, -1, 223),
        BTerm(1, 1, 0, 1, 223),
        BTerm(0, 1, -2, -1, -220),
        BTerm(2, 1, -1, -1, -220),
        BTerm(1, 0, 1, 1, -185),
        BTerm(2, -1, -2, -1, 181),
        BTerm(0, 1, 2, 1, -177),
        BTerm(4, 0, -2, -1, 176),
        BTerm(4, -1, -1, -1, 166),
        BTerm(1, 0, 1, -1, -164),
        BTerm(4, 0, 1, -1, 132),
        BTerm(1, 0, -1, -1, -119),
        BTerm(4, -1, 0, -1, 115),
        BTerm(2, -2, 0, 1, 107)
    )

    private fun moonGeocentricEcliptic(t: Double): MoonEcliptic {
        val t2 = t * t
        val t3 = t2 * t
        val t4 = t3 * t

        val lPrime = normalizeDegrees(
            218.3164477 + 481267.88123421 * t - 0.0015786 * t2 + t3 / 538841.0 - t4 / 65194000.0
        )
        val d = normalizeDegrees(
            297.8501921 + 445267.1114034 * t - 0.0018819 * t2 + t3 / 545868.0 - t4 / 113065000.0
        )
        val m = normalizeDegrees(
            357.5291092 + 35999.0502909 * t - 0.0001536 * t2 + t3 / 24490000.0
        )
        val mp = normalizeDegrees(
            134.9633964 + 477198.8675055 * t + 0.0087414 * t2 + t3 / 69699.0 - t4 / 14712000.0
        )
        val f = normalizeDegrees(
            93.2720950 + 483202.0175233 * t - 0.0036539 * t2 - t3 / 3526000.0 + t4 / 863310000.0
        )

        val e = 1 - 0.002516 * t - 0.0000074 * t2

        val dRad = Math.toRadians(d)
        val mRad = Math.toRadians(m)
        val mpRad = Math.toRadians(mp)
        val fRad = Math.toRadians(f)

        var sumL = 0.0
        var sumR = 0.0
        for (term in LR_TERMS) {
            val eFactor = when (abs(term.m)) {
                1 -> e
                2 -> e * e
                else -> 1.0
            }
            val arg = term.d * dRad + term.m * mRad + term.mp * mpRad + term.f * fRad
            sumL += term.l * eFactor * sin(arg)
            sumR += term.r * eFactor * cos(arg)
        }

        var sumB = 0.0
        for (term in B_TERMS) {
            val eFactor = when (abs(term.m)) {
                1 -> e
                2 -> e * e
                else -> 1.0
            }
            val arg = term.d * dRad + term.m * mRad + term.mp * mpRad + term.f * fRad
            sumB += term.b * eFactor * sin(arg)
        }

        val a1 = Math.toRadians(119.75 + 131.849 * t)
        val a2 = Math.toRadians(53.09 + 479264.290 * t)
        val a3 = Math.toRadians(313.45 + 481266.484 * t)

        val lPrimeRad = Math.toRadians(lPrime)
        sumL += 3958.0 * sin(a1) + 1962.0 * sin(lPrimeRad - fRad) + 318.0 * sin(a2)
        sumB += 175.0 * sin(a1) + 127.0 * sin(lPrimeRad - mpRad) + 382.0 * sin(a3)
        sumB += 175.0 * sin(a1 - fRad) + 110.0 * sin(a1 + fRad)

        val lambda = normalizeDegrees(lPrime + sumL / 1_000_000.0)
        val beta = sumB / 1_000_000.0
        val distanceKm = 385000.56 + sumR / 1000.0

        return MoonEcliptic(lambda, beta, distanceKm)
    }

    private fun topocentricLongitudeDeg(
        geo: MoonEcliptic,
        jd: Double,
        t: Double,
        latDeg: Double,
        lonDeg: Double
    ): Double {
        val eps = meanObliquityRad(t)
        val lambdaRad = Math.toRadians(geo.lambdaDeg)
        val betaRad = Math.toRadians(geo.betaDeg)

        val sinLambda = sin(lambdaRad)
        val cosLambda = cos(lambdaRad)
        val sinBeta = sin(betaRad)
        val cosBeta = cos(betaRad)
        val sinEps = sin(eps)
        val cosEps = cos(eps)

        val ra = atan2(
            sinLambda * cosEps - tan(betaRad) * sinEps,
            cosLambda
        )
        val dec = asin(
            sinBeta * cosEps + cosBeta * sinEps * sinLambda
        )

        val gmst = 280.46061837 +
            360.98564736629 * (jd - 2451545.0) +
            0.000387933 * t * t -
            (t * t * t) / 38710000.0
        val lstDeg = normalizeDegrees(gmst + lonDeg)
        val lst = Math.toRadians(lstDeg)
        val h = lst - ra

        val phi = Math.toRadians(latDeg)
        val u = atan2(0.99664719 * sin(phi), cos(phi))
        val rhoSinPhi = 0.99664719 * sin(u)
        val rhoCosPhi = cos(u)

        val sinPi = sin(asin(6378.14 / geo.distanceKm))

        val deltaAlpha = atan2(
            -rhoCosPhi * sinPi * sin(h),
            cos(dec) - rhoCosPhi * sinPi * cos(h)
        )
        val alphaTop = ra + deltaAlpha
        val deltaTop = atan2(
            (sin(dec) - rhoSinPhi * sinPi) * cos(deltaAlpha),
            cos(dec) - rhoCosPhi * sinPi * cos(h)
        )

        val lambdaTop = atan2(
            sin(alphaTop) * cosEps + tan(deltaTop) * sinEps,
            cos(alphaTop)
        )

        return normalizeDegrees(Math.toDegrees(lambdaTop))
    }

    private fun meanObliquityRad(t: Double): Double {
        val seconds = 21.448 - 46.8150 * t - 0.00059 * t * t + 0.001813 * t * t * t
        val epsDeg = 23.0 + (26.0 / 60.0) + (seconds / 3600.0)
        return Math.toRadians(epsDeg)
    }

    private fun normalizeDegrees(value: Double): Double {
        val mod = value % 360.0
        return if (mod < 0) mod + 360.0 else mod
    }

    private fun julianDay(now: Instant): Double {
        return (now.toEpochMilli() / 86400000.0) + 2440587.5
    }
}
