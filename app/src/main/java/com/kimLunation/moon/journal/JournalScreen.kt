// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon.journal

// Imports are used to bring in code from other parts of the project or from external libraries.
import androidx.compose.foundation.Canvas // A composable that provides a drawing scope for 2D graphics.
import androidx.compose.foundation.background // A modifier for a background color.
import androidx.compose.foundation.border // A modifier to draw a border.
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures // Gesture detector for drags.
import androidx.compose.foundation.gestures.detectTapGestures // Gesture detector for taps.
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.DisableSelection // Prevent selection on decorative labels.
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kimLunation.moon.R // A class that contains all the resource IDs for the project.
import java.util.Locale
import kotlin.math.roundToInt

/**
 * A data class that represents the read-only sky stamp shown in the journal UI.
 */
data class JournalSkyStamp(
    val localDate: String,
    val lunationCount: Int,
    val lunationDay: Double?,
    val phaseLabel: String,
    val illuminationPercent: Int,
    val moonSign: String?
)

/**
 * A minimal, icon-only button that matches the existing HUD visual language.
 */
@Composable
fun JournalGlyphButton(
    iconResId: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 36.dp
) {
    val iconAlpha = if (enabled) 1f else 0.4f
    val iconSize = size * 0.6f
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(size)
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            tint = Color.Unspecified,
            modifier = Modifier
                .size(iconSize)
                .alpha(iconAlpha)
        )
    }
}

@Composable
fun JewelButton(
    isToggled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    enabled: Boolean = true
) {
    val goldDark = Color(0xFFC8A048)
    val goldLight = Color(0xFFF8E0A0)
    val redDark = Color(0xFFB00020)
    val redLight = Color(0xFFE06060)

    val animatedTopColor by animateColorAsState(
        targetValue = if (isToggled) redLight else goldLight,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )
    val animatedBottomColor by animateColorAsState(
        targetValue = if (isToggled) redDark else goldDark,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(animatedTopColor, animatedBottomColor)
                )
            )
            .border(width = 1.dp, color = goldDark, shape = CircleShape)
    )
}

/**
 * The journaling screen UI.
 */
@Composable
fun JournalScreen(
    stamp: JournalSkyStamp,
    body: String,
    onBodyChange: (String) -> Unit,
    moodX: Float,
    moodY: Float,
    onMoodChange: (Float, Float) -> Unit,
    isPeriod: Boolean,
    onTogglePeriod: () -> Unit,
    isGreenEvent: Boolean,
    onToggleGreenEvent: () -> Unit,
    isYellowEvent: Boolean,
    onToggleYellowEvent: () -> Unit,
    isBlueEvent: Boolean,
    onToggleBlueEvent: () -> Unit,
    onSave: () -> Unit,
    onReview: () -> Unit,
    onClose: () -> Unit,
    saveEnabled: Boolean,
    readOnly: Boolean,
    engraveNonce: Int,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val overlayColor = Color(0xFF0B0B0B).copy(alpha = 0.78f)
    val textColor = Color(0xFFE8D9B8)
    val subTextColor = Color(0xFFC9B38A)
    val borderColor = textColor.copy(alpha = 0.35f)
    val engraveProgress = remember { Animatable(0f) }

    LaunchedEffect(engraveNonce) {
        if (engraveNonce <= 0) return@LaunchedEffect
        engraveProgress.snapTo(0f)
        engraveProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
        )
        engraveProgress.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(overlayColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Sky stamp",
                    color = textColor,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    JournalGlyphButton(
                        iconResId = R.drawable.compass_detail_lower,
                        contentDescription = "Review entries",
                        onClick = onReview,
                        size = 120.dp
                    )
                    IconButton(onClick = onClose) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                            contentDescription = "Close journal",
                            tint = subTextColor
                        )
                    }
                }
            }

            DisableSelection {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SkyStampRow(label = "Date", value = stamp.localDate, color = subTextColor)
                    SkyStampRow(label = "Lunation", value = stamp.lunationCount.toString(), color = subTextColor)
                    stamp.lunationDay?.let { value ->
                        SkyStampRow(label = "Lunation day", value = formatLunationDay(value), color = subTextColor)
                    }
                    SkyStampRow(label = "Phase", value = stamp.phaseLabel, color = subTextColor)
                    SkyStampRow(
                        label = "Illumination",
                        value = "${stamp.illuminationPercent}%",
                        color = subTextColor
                    )
                    stamp.moonSign?.let { value ->
                        SkyStampRow(label = "Moon sign", value = value, color = subTextColor)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Entry",
                        color = textColor,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                    JewelButton(
                        isToggled = isPeriod,
                        onClick = onTogglePeriod,
                        enabled = !readOnly
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    OutlinedTextField(
                        value = body,
                        onValueChange = onBodyChange,
                        modifier = Modifier
                            .fillMaxSize(),
                        minLines = 6,
                        maxLines = 6,
                        readOnly = readOnly,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = textColor,
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = borderColor,
                            unfocusedBorderColor = borderColor,
                            cursorColor = textColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        )
                    )
                    val engravingAlpha = engraveProgress.value
                    if (engravingAlpha > 0f && body.isNotBlank()) {
                        val brassColor = Color(0xFFD6B476)
                        val shadowColor = Color(0xFF2F2115)
                        Text(
                            text = body,
                            color = brassColor.copy(alpha = 0.8f * engravingAlpha),
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = brassColor.copy(alpha = 0.8f * engravingAlpha),
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                shadow = Shadow(
                                    color = shadowColor.copy(alpha = 0.6f * engravingAlpha),
                                    offset = Offset(1f, 1f),
                                    blurRadius = 2f
                                )
                            )
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Mood",
                    color = textColor,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
                val labelColor = Color(0xFFF1E2BE)
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .sizeIn(maxWidth = 260.dp)
                            .aspectRatio(1f)
                    ) {
                        MoodGrid(
                            moodX = moodX,
                            moodY = moodY,
                            onMoodChange = onMoodChange,
                            modifier = Modifier.fillMaxSize(),
                            readOnly = readOnly
                        )
                        Text(
                            text = "High energy",
                            color = labelColor,
                            fontFamily = FontFamily.Serif,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 6.dp)
                        )
                        Text(
                            text = "Low energy",
                            color = labelColor,
                            fontFamily = FontFamily.Serif,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 6.dp)
                        )
                        Text(
                            text = "Unpleasant",
                            color = labelColor,
                            fontFamily = FontFamily.Serif,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 8.dp)
                        )
                        Text(
                            text = "Pleasant",
                            color = labelColor,
                            fontFamily = FontFamily.Serif,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 8.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onSave,
                    enabled = saveEnabled,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A3A26),
                        contentColor = Color(0xFFE8D9B8),
                        disabledContainerColor = Color(0xFF2E2419),
                        disabledContentColor = Color(0xFFA99778)
                    ),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(
                        text = "Engrave",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun JournalReviewScreen(
    entries: List<JournalEntry>,
    density: FloatArray,
    recency: FloatArray,
    gridSize: Int,
    latestPoint: MoodPoint?,
    onExport: (List<JournalEntry>) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val overlayColor = Color(0xFF0B0B0B).copy(alpha = 0.78f)
    val textColor = Color(0xFFE8D9B8)
    val subTextColor = Color(0xFFC9B38A)
    val borderColor = textColor.copy(alpha = 0.3f)
    val labelColor = Color(0xFFF1E2BE)
    val listState = rememberLazyListState()
    var selectedEntry by remember { mutableStateOf<JournalEntry?>(null) }
    val exportEnabled = entries.isNotEmpty()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(overlayColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clipToBounds()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Journal entries",
                            color = textColor,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                        Row(
                            modifier = Modifier.padding(top = 7.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onExport(entries) },
                                enabled = exportEnabled
                            ) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_menu_save),
                                    contentDescription = "Export journal entries",
                                    tint = subTextColor.copy(alpha = if (exportEnabled) 1f else 0.35f)
                                )
                            }
                            IconButton(onClick = onClose) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                                    contentDescription = "Close review",
                                    tint = subTextColor
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(1.dp, borderColor)
                            .padding(12.dp)
                    ) {
                        if (entries.isEmpty()) {
                            Text(
                                text = "No entries yet.",
                                color = subTextColor,
                                fontFamily = FontFamily.Serif,
                                fontSize = 13.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(entries, key = { it.id }) { entry ->
                                    JournalReviewEntryItem(
                                        entry = entry,
                                        textColor = textColor,
                                        subTextColor = subTextColor,
                                        borderColor = borderColor,
                                        onClick = { selectedEntry = entry }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Mood terrain",
                        color = textColor,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(1.dp, borderColor)
                            .padding(12.dp)
                    ) {
                        MoodTerrainMap(
                            density = density,
                            recency = recency,
                            gridSize = gridSize,
                            latestPoint = latestPoint,
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(
                            text = "High energy",
                            color = labelColor,
                            fontFamily = FontFamily.Serif,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 6.dp)
                        )
                        Text(
                            text = "Low energy",
                            color = labelColor,
                            fontFamily = FontFamily.Serif,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 6.dp)
                        )
                        Text(
                            text = "Unpleasant",
                            color = labelColor,
                            fontFamily = FontFamily.Serif,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 8.dp)
                        )
                        Text(
                            text = "Pleasant",
                            color = labelColor,
                            fontFamily = FontFamily.Serif,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 8.dp)
                        )
                    }
                }
            }
        }
        selectedEntry?.let { entry ->
            JournalEntryDetailSheet(
                entry = entry,
                onClose = { selectedEntry = null }
            )
        }
    }
}

@Composable
private fun JournalReviewEntryItem(
    entry: JournalEntry,
    textColor: Color,
    subTextColor: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    val preview = entry.body.trim().replace("\n", " ").take(140)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = entry.localDate,
                color = textColor,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold
            )
            if (entry.isPeriod) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFFB00020), CircleShape)
                )
            }
        }
        Text(
            text = "${entry.phaseLabel} - ${entry.illuminationPercent}%",
            color = subTextColor,
            fontFamily = FontFamily.Serif,
            fontSize = 12.sp
        )
        entry.moonSign?.let { sign ->
            Text(
                text = sign,
                color = subTextColor,
                fontFamily = FontFamily.Serif,
                fontSize = 12.sp
            )
        }
        Text(
            text = "Mood ${formatMood(entry.moodX)}, ${formatMood(entry.moodY)}",
            color = subTextColor,
            fontFamily = FontFamily.Serif,
            fontSize = 12.sp
        )
        if (preview.isNotBlank()) {
            Text(
                text = preview,
                color = textColor.copy(alpha = 0.9f),
                fontFamily = FontFamily.Serif,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Text(
                text = "No entry text.",
                color = subTextColor,
                fontFamily = FontFamily.Serif,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun JournalEntryDetailSheet(
    entry: JournalEntry,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val overlayColor = Color(0xFF0B0B0B).copy(alpha = 0.92f)
    val textColor = Color(0xFFE8D9B8)
    val subTextColor = Color(0xFFC9B38A)
    val borderColor = textColor.copy(alpha = 0.3f)
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(overlayColor)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(20.dp)
                .border(1.dp, borderColor)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Entry",
                    color = textColor,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                IconButton(onClick = onClose) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                        contentDescription = "Close entry",
                        tint = subTextColor
                    )
                }
            }
            Text(
                text = entry.localDate,
                color = textColor,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${entry.phaseLabel} - ${entry.illuminationPercent}%",
                color = subTextColor,
                fontFamily = FontFamily.Serif,
                fontSize = 12.sp
            )
            entry.moonSign?.let { sign ->
                Text(
                    text = sign,
                    color = subTextColor,
                    fontFamily = FontFamily.Serif,
                    fontSize = 12.sp
                )
            }
            Text(
                text = "Mood ${formatMood(entry.moodX)}, ${formatMood(entry.moodY)}",
                color = subTextColor,
                fontFamily = FontFamily.Serif,
                fontSize = 12.sp
            )
            if (entry.body.isNotBlank()) {
                Text(
                    text = entry.body,
                    color = textColor,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic
                )
            } else {
                Text(
                    text = "No entry text.",
                    color = subTextColor,
                    fontFamily = FontFamily.Serif,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun MoodTerrainMap(
    density: FloatArray,
    recency: FloatArray,
    gridSize: Int,
    latestPoint: MoodPoint?,
    modifier: Modifier = Modifier
) {
    val gridColor = Color(0xFFE8D9B8).copy(alpha = 0.08f)
    val axisColor = Color(0xFFE8D9B8).copy(alpha = 0.3f)
    val oldColor = Color(0xFF9C8F7A)
    val recencyColor = Color(0xFF4BAE9A)
    val markerColor = Color(0xFFF1E2BE)

    Canvas(modifier = modifier) {
        if (gridSize <= 0) return@Canvas
        val cellWidth = size.width / gridSize
        val cellHeight = size.height / gridSize

        for (row in 0 until gridSize) {
            val top = row * cellHeight
            for (col in 0 until gridSize) {
                val idx = row * gridSize + col
                val value = density.getOrNull(idx) ?: 0f
                val recencyValue = recency.getOrNull(idx) ?: 0f
                if (value > 0f) {
                    val contrast = (recencyValue.coerceIn(0f, 1f)).let { it * it * (3f - 2f * it) }
                    val alpha = (0.12f + 0.78f * value).coerceIn(0f, 0.9f)
                    drawRect(
                        color = lerp(oldColor, recencyColor, contrast).copy(alpha = alpha),
                        topLeft = Offset(col * cellWidth, top),
                        size = Size(cellWidth, cellHeight)
                    )
                }
            }
        }

        val stroke = 1.dp.toPx()
        for (i in 1 until gridSize) {
            val x = i * cellWidth
            val y = i * cellHeight
            drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = stroke)
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = stroke)
        }

        val centerX = size.width / 2f
        val centerY = size.height / 2f
        drawLine(axisColor, Offset(centerX, 0f), Offset(centerX, size.height), strokeWidth = stroke)
        drawLine(axisColor, Offset(0f, centerY), Offset(size.width, centerY), strokeWidth = stroke)

        latestPoint?.let { point ->
            val clampedX = point.x.coerceIn(-1f, 1f)
            val clampedY = point.y.coerceIn(-1f, 1f)
            val markerX = ((clampedX + 1f) / 2f) * size.width
            val markerY = ((1f - clampedY) / 2f) * size.height
            drawCircle(markerColor, radius = 3.dp.toPx(), center = Offset(markerX, markerY))
        }
    }
}

@Composable
private fun SkyStampRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = color,
            fontFamily = FontFamily.Serif,
            fontSize = 13.sp
        )
        Text(
            text = value,
            color = color,
            fontFamily = FontFamily.Serif,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun MoodGrid(
    moodX: Float,
    moodY: Float,
    onMoodChange: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    var gridSize by remember { mutableStateOf(IntSize.Zero) }
    val borderColor = Color(0xFFC9B38A).copy(alpha = 0.4f)
    val axisColor = Color(0xFFC9B38A).copy(alpha = 0.25f)
    val markerColor = Color(0xFFE8D9B8)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(1.dp, borderColor)
            .onSizeChanged { gridSize = it }
            .then(
                if (readOnly) Modifier else Modifier.pointerInput(Unit) {
                    detectTapGestures { offset ->
                        updateMoodFromOffset(offset, gridSize, onMoodChange)
                    }
                }
            )
            .then(
                if (readOnly) Modifier else Modifier.pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        updateMoodFromOffset(change.position, gridSize, onMoodChange)
                        change.consume()
                    }
                }
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val stroke = 1.dp.toPx()
            drawLine(axisColor, Offset(centerX, 0f), Offset(centerX, size.height), strokeWidth = stroke)
            drawLine(axisColor, Offset(0f, centerY), Offset(size.width, centerY), strokeWidth = stroke)

            val clampedX = moodX.coerceIn(-1f, 1f)
            val clampedY = moodY.coerceIn(-1f, 1f)
            val markerX = ((clampedX + 1f) / 2f) * size.width
            val markerY = ((1f - clampedY) / 2f) * size.height
            drawCircle(markerColor, radius = 4.dp.toPx(), center = Offset(markerX, markerY))
        }
    }
}

private fun updateMoodFromOffset(
    offset: Offset,
    gridSize: IntSize,
    onMoodChange: (Float, Float) -> Unit
) {
    if (gridSize.width == 0 || gridSize.height == 0) return
    val normalizedX = ((offset.x / gridSize.width).coerceIn(0f, 1f) * 2f) - 1f
    val normalizedY = 1f - ((offset.y / gridSize.height).coerceIn(0f, 1f) * 2f)
    onMoodChange(normalizedX, normalizedY)
}

private fun formatLunationDay(value: Double): String {
    val rounded = (value * 10).roundToInt() / 10.0
    return rounded.toString()
}

private fun formatMood(value: Float): String {
    return String.format(Locale.US, "%.2f", value)
}
