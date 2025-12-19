package com.kimLunation.moon.quotes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kimLunation.moon.R

@Composable
fun DailyQuoteScroll(
    quote: Quote,
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        BoxWithConstraints(
            modifier = Modifier
                .shadow(8.dp, RoundedCornerShape(12.dp))
        ) {
            val maxWidth = maxWidth
            val imageWidth = maxWidth.coerceAtMost(360.dp)
            Box(
                modifier = Modifier
                    .sizeIn(maxWidth = imageWidth)
                    .align(Alignment.TopCenter)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.scroll),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 8.dp),
                    contentScale = ContentScale.Fit
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 32.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AutoSizeText(
                        text = quote.text,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 6
                    )
                    Text(
                        text = "by ${quote.author}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.padding(top = 12.dp),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun QuoteReopenChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 6.dp,
        shadowElevation = 6.dp,
        onClick = onClick
    ) {
        Text(
            text = "Today's quote",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
private fun AutoSizeText(
    text: String,
    style: TextStyle,
    maxLines: Int,
    minSize: Dp = 12.dp,
    maxSize: Dp = 18.dp
) {
    var textStyle by remember { mutableStateOf(style.copy(fontSize = maxSize.value.sp)) }
    var ready by remember { mutableStateOf(false) }

    Text(
        text = text,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        style = textStyle,
        onTextLayout = { result ->
            if (!ready && result.didOverflowHeight) {
                val nextSize = (textStyle.fontSize.value - 1).coerceAtLeast(minSize.value)
                textStyle = textStyle.copy(fontSize = nextSize.sp)
            } else {
                ready = true
            }
        }
    )
}
