package com.kimLunation.moon.journal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlin.random.Random

data class MoodPoint(val x: Float, val y: Float, val timestampMillis: Long)

class JournalReviewViewModel(
    private val repository: JournalRepository,
    val gridSize: Int = DEFAULT_GRID_SIZE
) : ViewModel() {
    var entries by mutableStateOf<List<JournalEntry>>(emptyList())
        private set
    var density by mutableStateOf(FloatArray(gridSize * gridSize))
        private set
    var recency by mutableStateOf(FloatArray(gridSize * gridSize))
        private set
    var periodDensity by mutableStateOf(FloatArray(gridSize * gridSize))
        private set
    var periodRecency by mutableStateOf(FloatArray(gridSize * gridSize))
        private set
    var greenDensity by mutableStateOf(FloatArray(gridSize * gridSize))
        private set
    var greenRecency by mutableStateOf(FloatArray(gridSize * gridSize))
        private set
    var yellowDensity by mutableStateOf(FloatArray(gridSize * gridSize))
        private set
    var yellowRecency by mutableStateOf(FloatArray(gridSize * gridSize))
        private set
    var blueDensity by mutableStateOf(FloatArray(gridSize * gridSize))
        private set
    var blueRecency by mutableStateOf(FloatArray(gridSize * gridSize))
        private set
    var latestPoint by mutableStateOf<MoodPoint?>(null)
        private set

    fun load(
        seedWhenEmpty: Boolean = false,
        forceSeed: Boolean = false,
        seedCount: Int = DEFAULT_SEED_COUNT
    ) {
        viewModelScope.launch {
            val loaded = repository.getAllEntries()
            entries = loaded
            val shouldSeed = forceSeed || (loaded.isEmpty() && seedWhenEmpty)
            val points = if (shouldSeed) {
                generateSeedPoints(seedCount)
            } else {
                loaded.map { MoodPoint(it.moodX, it.moodY, it.updatedAtMillis) }
            }
            val grid = buildGrid(points, gridSize)
            density = grid.density
            recency = grid.recency
            latestPoint = points.maxByOrNull { it.timestampMillis }

            fun gridFor(predicate: (JournalEntry) -> Boolean): MoodGridStats {
                val filtered = loaded.filter(predicate)
                val filteredPoints = filtered.map { MoodPoint(it.moodX, it.moodY, it.updatedAtMillis) }
                return buildGrid(filteredPoints, gridSize)
            }

            val periodGrid = gridFor { it.isPeriod }
            periodDensity = periodGrid.density
            periodRecency = periodGrid.recency

            val greenGrid = gridFor { it.isGreenEvent }
            greenDensity = greenGrid.density
            greenRecency = greenGrid.recency

            val yellowGrid = gridFor { it.isYellowEvent }
            yellowDensity = yellowGrid.density
            yellowRecency = yellowGrid.recency

            val blueGrid = gridFor { it.isBlueEvent }
            blueDensity = blueGrid.density
            blueRecency = blueGrid.recency
        }
    }

    companion object {
        const val DEFAULT_GRID_SIZE = 32
        const val DEFAULT_SEED_COUNT = 64
    }
}

class JournalReviewViewModelFactory(
    private val repository: JournalRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return JournalReviewViewModel(repository) as T
    }
}

private data class MoodGridStats(val density: FloatArray, val recency: FloatArray)

private fun buildGrid(points: List<MoodPoint>, gridSize: Int): MoodGridStats {
    val counts = IntArray(gridSize * gridSize)
    val latest = LongArray(gridSize * gridSize) { Long.MIN_VALUE }
    var maxCount = 0
    var minTimestamp = Long.MAX_VALUE
    var maxTimestamp = Long.MIN_VALUE
    for (point in points) {
        val x = point.x.coerceIn(-1f, 1f)
        val y = point.y.coerceIn(-1f, 1f)
        val normalizedX = (x + 1f) / 2f
        val normalizedY = (1f - y) / 2f
        val xIndex = (normalizedX * gridSize).toInt().coerceIn(0, gridSize - 1)
        val yIndex = (normalizedY * gridSize).toInt().coerceIn(0, gridSize - 1)
        val idx = yIndex * gridSize + xIndex
        counts[idx] += 1
        if (counts[idx] > maxCount) {
            maxCount = counts[idx]
        }
        if (point.timestampMillis > latest[idx]) {
            latest[idx] = point.timestampMillis
        }
        if (point.timestampMillis < minTimestamp) {
            minTimestamp = point.timestampMillis
        }
        if (point.timestampMillis > maxTimestamp) {
            maxTimestamp = point.timestampMillis
        }
    }

    val density = FloatArray(gridSize * gridSize)
    val recency = FloatArray(gridSize * gridSize)
    if (maxCount == 0) return MoodGridStats(density, recency)
    val timestampRange = (maxTimestamp - minTimestamp).coerceAtLeast(0L)

    for (i in counts.indices) {
        density[i] = counts[i].toFloat() / maxCount.toFloat()
        val latestTimestamp = latest[i]
        if (latestTimestamp != Long.MIN_VALUE) {
            recency[i] = if (timestampRange == 0L) {
                1f
            } else {
                ((latestTimestamp - minTimestamp).toDouble() / timestampRange.toDouble()).toFloat()
            }
        }
    }
    return MoodGridStats(density, recency)
}

private fun generateSeedPoints(count: Int): List<MoodPoint> {
    val rng = Random(42)
    val now = System.currentTimeMillis()
    val maxAgeDays = 30
    val centers = listOf(
        MoodPoint(-0.55f, -0.15f, now),
        MoodPoint(0.35f, 0.55f, now),
        MoodPoint(0.1f, -0.65f, now)
    )
    val spread = 0.35f

    fun jitter(): Float {
        val blended = (rng.nextFloat() + rng.nextFloat() + rng.nextFloat()) / 3f
        return (blended * 2f - 1f) * spread
    }

    return List(count.coerceAtLeast(0)) {
        val center = centers[rng.nextInt(centers.size)]
        val x = (center.x + jitter()).coerceIn(-1f, 1f)
        val y = (center.y + jitter()).coerceIn(-1f, 1f)
        val ageDays = rng.nextInt(0, maxAgeDays + 1)
        val timestamp = now - ageDays * 24L * 60L * 60L * 1000L
        MoodPoint(x, y, timestamp)
    }
}
