package com.kimLunation.moon.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kimLunation.moon.DpOffset
import com.kimLunation.moon.R
import kotlin.math.abs

data class HudLayerRes(
    @DrawableRes val hudPlaque: Int = 0,

    // 2b/2c drawable layers
    @DrawableRes val illumDrawable: Int = 0,
    @DrawableRes val moonInDrawable: Int = 0,

    // 3a/3b/3c borders
    @DrawableRes val lunationBorder: Int = 0,
    @DrawableRes val illumBorder: Int = 0,
    @DrawableRes val moonInBorder: Int = 0,

    // 4..8 compass stack
    @DrawableRes val compassUnderlay: Int = 0,
    @DrawableRes val compassCircle: Int = 0,
    @DrawableRes val compassRidge: Int = 0,
    @DrawableRes val arrow: Int = 0,
    @DrawableRes val compassDetailLower: Int = 0
) {
    companion object {
        fun fromProjectAssets(): HudLayerRes = HudLayerRes(
            hudPlaque = R.drawable.hud_plaque,
            
            illumDrawable = 0, // Placeholder
            moonInDrawable = 0, // Placeholder

            lunationBorder = 0, // Placeholder
            illumBorder = R.drawable.illum_border,
            moonInBorder = R.drawable.moon_in_border,

            compassUnderlay = R.drawable.compass_underlay,
            compassCircle = R.drawable.compass_circle,
            compassRidge = R.drawable.compass_ridge,
            arrow = R.drawable.compass_arrow,
            compassDetailLower = R.drawable.compass_detail_lower
        )
    }
}

@Composable
fun HudPlaque(
    modifier: Modifier = Modifier,
    digitsOffset: DpOffset,
    illumOffset: DpOffset,
    nameOffset: DpOffset,
    lunationCount: Int,
    layers: HudLayerRes = HudLayerRes.fromProjectAssets(),
    contentScale: ContentScale = ContentScale.Fit,

    // Counter sizing knobs
    digitHeight: Dp = 44.dp,
    digitSpacing: Dp = 0.dp
) {
    Box(modifier = modifier) {
        // 1
        LayerImage(layers.hudPlaque, contentScale)

        // 2b, 2c
        LayerImage(layers.illumDrawable, contentScale)
        LayerImage(layers.moonInDrawable, contentScale)

        // 3a, 3b, 3c
        LayerImage(layers.lunationBorder, contentScale)
        LayerImage(layers.illumBorder, contentScale)
        LayerImage(layers.moonInBorder, contentScale)

        // 4, 5, 6
        LayerImage(layers.compassUnderlay, contentScale)
        LayerImage(layers.compassCircle, contentScale)
        LayerImage(layers.compassRidge, contentScale)

        // 7
        LayerImage(layers.arrow, contentScale)

        // 8
        LayerImage(layers.compassDetailLower, contentScale)

        // Lunation counter digits, 3 tiles, drawn above base layers
        LunationDigits(
            count = lunationCount,
            modifier = Modifier
                .offset(x = digitsOffset.x, y = digitsOffset.y)
                .height(digitHeight),
            digitSpacing = digitSpacing
        )

        @Suppress("UNUSED_VARIABLE")
        val _preserveOffsets = Triple(illumOffset, nameOffset, contentScale)
    }
}

@Composable
private fun LunationDigits(
    count: Int,
    modifier: Modifier,
    digitSpacing: Dp
) {
    val n = abs(count).coerceAtMost(999)
    val s = n.toString().padStart(3, '0')
    val d0 = s[0].digitToInt()
    val d1 = s[1].digitToInt()
    val d2 = s[2].digitToInt()

    Row(modifier = modifier) {
        DigitTile(d0)
        Spacer(modifier = Modifier.width(digitSpacing))
        DigitTile(d1)
        Spacer(modifier = Modifier.width(digitSpacing))
        DigitTile(d2)
    }
}

@Composable
private fun DigitTile(digit: Int) {
    Image(
        painter = painterResource(id = digitResId(digit)),
        contentDescription = null,
        contentScale = ContentScale.Fit
    )
}

@DrawableRes
private fun digitResId(digit: Int): Int {
    return when (digit) {
        0 -> R.drawable.zero_tile
        1 -> R.drawable.one_tile
        2 -> R.drawable.two_tile
        3 -> R.drawable.three_tile
        4 -> R.drawable.four_tile
        5 -> R.drawable.five_tile
        6 -> R.drawable.six_tile
        7 -> R.drawable.seven_tile
        8 -> R.drawable.eight_tile
        9 -> R.drawable.nine_tile
        else -> R.drawable.zero_tile
    }
}

@Composable
private fun LayerImage(@DrawableRes resId: Int, contentScale: ContentScale) {
    if (resId == 0) return
    Image(
        painter = painterResource(id = resId),
        contentDescription = null,
        contentScale = contentScale
    )
}
