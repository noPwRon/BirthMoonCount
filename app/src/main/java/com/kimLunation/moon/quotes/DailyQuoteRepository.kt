// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon.quotes

// Imports are used to bring in code from other parts of the project or from external libraries.
import android.content.Context // Provides access to application-specific resources and classes.
import androidx.annotation.VisibleForTesting // An annotation to indicate that a method is public for testing purposes.
import com.google.gson.Gson // A library to convert JSON data to Kotlin objects and vice-versa.
import retrofit2.Retrofit // A library for making network requests (like fetching data from the internet).
import retrofit2.converter.gson.GsonConverterFactory // A converter for Retrofit to use Gson for JSON processing.
import retrofit2.http.GET // An annotation for Retrofit to specify an HTTP GET request.
import retrofit2.http.Url // An annotation for Retrofit to use a dynamic URL.
import kotlinx.coroutines.Dispatchers // Provides different threads for running tasks (like network or disk operations).
import kotlinx.coroutines.withContext // A function to switch to a different thread for a block of code.
import java.util.Locale // Locale for stable string normalization.
import kotlin.random.Random // A utility for generating random numbers.

/**
 * A 'data class' is a special type of class in Kotlin that is primarily used to hold data.
 * This 'Quote' data class defines the structure of a single quote.
 *
 * @param id A unique identifier for the quote.
 * @param text The actual text of the quote.
 * @param author The person who said the quote.
 * @param topic The category or topic of the quote.
 * @param weight Reserved for future per-quote weighting. Pack-level weights are applied in code. Defaults to 1.
 */
data class Quote(
    val id: String,
    val text: String,
    val author: String,
    val topic: String,
    val weight: Int = 1
)

/**
 * This data class represents a "pack" of quotes, which is how quotes are structured in the JSON files.
 * It contains a list of 'Quote' objects.
 *
 * @param messages A list of quotes. It defaults to an empty list if not provided.
 */
private data class QuotePack(
    val packId: String? = null,
    val packVersion: Int? = null,
    val messages: List<Quote> = emptyList()
)

/**
 * An 'interface' is a contract that defines a set of methods that a class can implement.
 * This 'QuoteService' interface is used by Retrofit to define how to fetch data from a URL.
 */
private interface QuoteService {
    /**
     * This function tells Retrofit to perform an HTTP GET request to fetch a 'QuotePack'.
     * The '@GET' annotation indicates the request type.
     * The '@Url' annotation on the 'url' parameter means that the full URL for the request will be provided when the function is called.
     * 'suspend' means this function is a coroutine and can be paused and resumed, which is great for long-running tasks like network requests.
     */
    @GET
    suspend fun fetchPack(@Url url: String): QuotePack
}

/**
 * This data class holds a 'Quote' and a boolean flag to indicate its source.
 * This helps to differentiate between quotes that come from the app's local assets and those fetched from a remote URL.
 *
 * @param quote The 'Quote' object itself.
 * @param isRemote True if the quote came from a remote URL, false otherwise.
 */
private data class SourcedQuote(
    val quote: Quote,
    val isRemote: Boolean,
    val packId: String?
)

/**
 * This is the main class responsible for managing and providing daily quotes.
 * It handles loading quotes from local files and a remote URL, tracking which quotes have been used,
 * and selecting a new quote for the day.
 *
 * @param context The Android application context, which is needed to access assets and DataStore.
 */
class DailyQuoteRepository(
    private val context: Context
) {
    // An instance of Gson, used for converting JSON to Kotlin objects.
    private val gson = Gson()

    // The public URL to fetch additional quotes from.
    private val remoteQuoteUrl = "https://raw.githubusercontent.com/noPwRon/BirthMoonCount/master/app/src/main/assets/science.json"

    // A cache for the loaded quotes. This avoids reloading them from files and network every time.
    // It's nullable ('?') because it will be null until the quotes are loaded for the first time.
    private var cachedQuotes: List<SourcedQuote>? = null

    /**
     * This sets up Retrofit. The 'by lazy' means that the code inside the block will only run
     * the first time 'retrofit' is accessed. This is an efficient way to initialize objects
     * that are not needed immediately.
     */
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/") // Base URL for the network requests.
            .addConverterFactory(GsonConverterFactory.create()) // Use Gson to handle JSON.
            .build() // Create the Retrofit instance.
    }

    // Creates an implementation of the 'QuoteService' interface using Retrofit. Also uses 'by lazy'.
    private val quoteService by lazy { retrofit.create(QuoteService::class.java) }

    // A counter for the debug function to cycle through quotes.
    private var debugIndex = 0

    // A special, custom quote to show when all other quotes have been displayed.
    private val allQuotesUsedQuote = Quote(
        id = "all_quotes_used",
        text = "Kim, you and Bibi mean the world to me. If you see this know that I care.",
        author = "Jeremy",
        topic = "meta"
    )

    /**
     * This 'suspend' function loads all quotes from both the local asset files and the remote URL.
     * It runs on an I/O (Input/Output) thread pool because file and network operations can be slow
     * and shouldn't block the main UI thread. 'withContext(Dispatchers.IO)' handles this.
     *
     * @return A list of 'SourcedQuote' objects.
     */
    private suspend fun loadQuotes(): List<SourcedQuote> = withContext(Dispatchers.IO) {
        // If quotes are already cached, return the cached list immediately.
        cachedQuotes?.let { return@withContext it }

        // 'buildList' creates a new list.
        val combined = buildList {
            // Automatically find and load all .json files from the assets folder.
            val jsonAssetFiles = try {
                context.assets.list("")?.filter { it.endsWith(".json") } ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }

            jsonAssetFiles.forEach { file ->
                // 'runCatching' is a safe way to run code that might throw an error (e.g., file not found).
                runCatching {
                    // Open the asset file and read it.
                    context.assets.open(file).bufferedReader().use { reader ->
                        // Use Gson to parse the JSON file into a 'QuotePack'.
                        val pack = gson.fromJson(reader, QuotePack::class.java)
                        val packId = pack.packId ?: file.substringBeforeLast('.')
                        // Add all the quotes from the pack to our combined list.
                        addAll(pack.messages.map { SourcedQuote(it, isRemote = false, packId = packId) })
                    }
                }
            }
            // Fetch quotes from the remote URL.
            runCatching {
                // Use the 'quoteService' (Retrofit) to fetch the 'QuotePack' from the URL.
                val pack = quoteService.fetchPack(remoteQuoteUrl)
                // Add the fetched quotes to the list, marking them as 'isRemote = true'.
                val packId = pack.packId ?: "remote_pack"
                addAll(
                    pack.messages.map {
                        SourcedQuote(it.copy(weight = maxOf(1, it.weight)), isRemote = true, packId = packId)
                    }
                )
            }
        }
        // Save the combined list to the cache.
        cachedQuotes = combined
        // Return the combined list.
        combined
    }

    /**
     * This is the main function to get a quote for the current day. It's a 'suspend' function
     * because it performs I/O operations (loading quotes, reading from DataStore).
     *
     * @return A randomly selected 'Quote', or null if none are available.
     */
    suspend fun getQuoteForToday(): Quote? = withContext(Dispatchers.IO) {
        // Load all available quotes.
        val quotes = loadQuotes()
        if (quotes.isEmpty()) return@withContext null
        // Random every call, weighted by the data.
        pickWeighted(quotes)
    }

    /**
     * This is a helper function for debugging. It cycles through all quotes one by one
     * without affecting the 'used' quotes history.
     *
     * @return The next 'Quote' in the sequence.
     */
    suspend fun nextDebugQuote(): Quote? = withContext(Dispatchers.IO) {
        val quotes = loadQuotes()
        if (quotes.isEmpty()) return@withContext null
        // If we've reached the end of the list, start from the beginning.
        if (debugIndex >= quotes.size) debugIndex = 0
        val next = quotes[debugIndex].quote
        // Move to the next index for the next call.
        debugIndex = (debugIndex + 1) % quotes.size
        next
    }

    /**
     * This function selects a random quote from a list of candidates.
     * It's not a simple random choice; it's a "weighted" random choice.
     * Quotes from higher-priority packs are more likely to be picked.
     *
     * @param candidates The list of 'SourcedQuote' to choose from.
     * @return The selected 'Quote', or null if the list is empty.
     */
    @VisibleForTesting
    private fun pickWeighted(candidates: List<SourcedQuote>): Quote? {
        if (candidates.isEmpty()) return null
        // Create a list of pairs, where each pair is a quote and its calculated weight.
        val weights = candidates.map {
            val packWeight = packWeightFor(it.packId)
            it to packWeight
        }
        // Calculate the total of all weights.
        val total = weights.sumOf { it.second }
        // Get a random number between 0 and the total weight.
        var r = Random.nextInt(total)
        // Loop through the quotes and their weights.
        for ((item, w) in weights) {
            // Subtract the current quote's weight from the random number.
            r -= w
            // If the random number is now less than 0, we've found our quote.
            if (r < 0) return item.quote
        }
        // As a fallback (if something goes wrong with the loop), return the last quote.
        return weights.last().first.quote
    }

    private fun packWeightFor(packId: String?): Int {
        val key = packId?.lowercase(Locale.US) ?: return 1
        return when {
            key.startsWith("custom") -> 6
            key.startsWith("women") -> 4
            key.startsWith("russian") -> 3
            key.startsWith("scientists") || key.startsWith("science") -> 2
            else -> 1
        }
    }
}
