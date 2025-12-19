package com.kimLunation.moon.quotes

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlin.random.Random

val Context.quoteDataStore by preferencesDataStore(name = "daily_quotes")

data class Quote(
    val id: String,
    val text: String,
    val author: String,
    val topic: String,
    val weight: Int = 1
)

private data class QuotePack(
    val messages: List<Quote> = emptyList()
)

private interface QuoteService {
    @GET
    suspend fun fetchPack(@Url url: String): QuotePack
}

private data class SourcedQuote(
    val quote: Quote,
    val isUser: Boolean
)

class DailyQuoteRepository(
    private val context: Context
) {
    private val gson = Gson()
    private val assetFiles = listOf("science.json", "women_only.json", "russian_culture.json")
    private val userQuoteUrls = listOf(
        "https://raw.githubusercontent.com/your-account/your-repo/main/quotes.json"
    )
    private var cachedQuotes: List<SourcedQuote>? = null

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    private val quoteService by lazy { retrofit.create(QuoteService::class.java) }

    private val usedKey = stringSetPreferencesKey("used_ids")
    private val lastIdKey = stringPreferencesKey("last_id")
    private val lastDayKey = longPreferencesKey("last_day_epoch")

    private suspend fun loadQuotes(): List<SourcedQuote> = withContext(Dispatchers.IO) {
        cachedQuotes?.let { return@withContext it }
        val combined = buildList {
            assetFiles.forEach { file ->
                runCatching {
                    context.assets.open(file).bufferedReader().use { reader ->
                        val pack = gson.fromJson(reader, QuotePack::class.java)
                        addAll(pack.messages.map { SourcedQuote(it, isUser = false) })
                    }
                }
            }
            userQuoteUrls.forEach { url ->
                runCatching {
                    val pack = quoteService.fetchPack(url)
                    addAll(pack.messages.map { SourcedQuote(it.copy(weight = maxOf(1, it.weight)), isUser = true) })
                }
            }
        }
        cachedQuotes = combined
        combined
    }

    suspend fun getQuoteForToday(): Quote? = withContext(Dispatchers.IO) {
        val quotes = loadQuotes()
        if (quotes.isEmpty()) return@withContext null

        val today = LocalDate.now(ZoneOffset.UTC).toEpochDay()
        val prefs = context.quoteDataStore.data.first()
        val lastDay = prefs[lastDayKey]
        val lastId = prefs[lastIdKey]
        val used = prefs[usedKey] ?: emptySet()

        if (lastDay == today && lastId != null) {
            return@withContext quotes.find { it.quote.id == lastId }?.quote
        }

        val unused = quotes.filterNot { it.quote.id in used }
        val next = pickWeighted(unused).orElse { pickWeighted(quotes) }

        context.quoteDataStore.edit { updated ->
            next?.let {
                updated[lastIdKey] = it.id
                updated[usedKey] = used + it.id
            }
            updated[lastDayKey] = today
        }

        next
    }

    @VisibleForTesting
    internal fun pickWeighted(candidates: List<SourcedQuote>): Quote? {
        if (candidates.isEmpty()) return null
        val weights = candidates.map {
            val base = it.quote.weight.coerceAtLeast(1)
            val bonus = if (it.isUser) 5 else 1
            it to (base * bonus)
        }
        val total = weights.sumOf { it.second }
        var r = Random.nextInt(total)
        for ((item, w) in weights) {
            r -= w
            if (r < 0) return item.quote
        }
        return weights.last().first.quote
    }
}
