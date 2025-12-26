// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon.quotes

// Imports are used to bring in code from other parts of the project or from external libraries.
import android.content.Context // Provides access to application-specific resources and classes.
import androidx.annotation.VisibleForTesting // An annotation to indicate that a method is public for testing purposes.
import androidx.datastore.preferences.core.edit // Function to edit data in DataStore.
import androidx.datastore.preferences.core.longPreferencesKey // Key for storing a Long value in DataStore.
import androidx.datastore.preferences.core.stringPreferencesKey // Key for storing a String value in DataStore.
import androidx.datastore.preferences.core.stringSetPreferencesKey // Key for storing a Set of Strings in DataStore.
import androidx.datastore.preferences.preferencesDataStore // Function to create a DataStore instance.
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.Strictness
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import okhttp3.ResponseBody
import okio.Buffer
import retrofit2.Retrofit // A library for making network requests (like fetching data from the internet).
import retrofit2.converter.gson.GsonConverterFactory // A converter for Retrofit to use Gson for JSON processing.
import retrofit2.http.GET // An annotation for Retrofit to specify an HTTP GET request.
import retrofit2.http.Url // An annotation for Retrofit to use a dynamic URL.
import kotlinx.coroutines.Dispatchers // Provides different threads for running tasks (like network or disk operations).
import kotlinx.coroutines.flow.first // Reads the current DataStore value once.
import kotlinx.coroutines.withContext // A function to switch to a different thread for a block of code.
import java.io.StringReader
import java.security.MessageDigest
import java.time.LocalDate
import java.time.ZoneId
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

private val Context.quoteCacheDataStore by preferencesDataStore(name = "quote_cache")

/**
 * This data class represents a "pack" of quotes, which is how quotes are structured in the JSON files.
 * It contains a list of 'Quote' objects.
 *
 * @param messages A list of quotes. It defaults to an empty list if not provided.
 */
private data class QuotePack(
    val packId: String? = null,
    val packVersion: Int? = null,
    val packWeight: Int = 1,
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
    suspend fun fetchPack(@Url url: String): ResponseBody
}

/**
 * This data class holds a 'Quote' and a boolean flag to indicate its source.
 * This helps to differentiate between quotes that come from the a<caret>pp's local assets and those fetched from a remote URL.
 *
 * @param quote The 'Quote' object itself.
 * @param isRemote True if the quote came from a remote URL, false otherwise.
 */
private data class SourcedQuote(
    val quote: Quote,
    val isRemote: Boolean,
    val packId: String?,
    val packWeight: Int
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
    private data class RemotePackDefinition(val fileName: String, val sha256: String) {
        fun url(baseUrl: String): String = baseUrl + fileName
    }

    private data class RemoteManifest(val packs: List<RemotePackDefinition>)

    // Remote pack list for live updates without rebuilding the app.
    private val remoteRepoOwner = "noPwRon"
    private val remoteRepoName = "BirthMoonCount"
    private val remoteRepoRef = "master"
    private val remoteRepoPath = "app/src/main/assets"
    private val remotePackBaseUrl =
        "https://raw.githubusercontent.com/$remoteRepoOwner/$remoteRepoName/$remoteRepoRef/$remoteRepoPath/"
    private val remoteManifestFileName = "packs.json"
    private val fallbackRemotePackDefinitions = listOf(
        RemotePackDefinition(
            fileName = "custom_quotes.json",
            sha256 = "663855456831400be189e8c08ecd415c977fe61aa265ce34c0cd2c4f3abcd02a"
        ),
        RemotePackDefinition(
            fileName = "russian_culture.json",
            sha256 = "c5330c9a742f1c9bbb641f96906e7984d66850bd01f2a12dadb3c4efb7539a74"
        ),
        RemotePackDefinition(
            fileName = "science.json",
            sha256 = "308cd56104bd72dd10a4423b2a92d5a332079ea55806a792edc5ff493101cf47"
        ),
        RemotePackDefinition(
            fileName = "spanish_culture.json",
            sha256 = "59ca0673023e7ab06890fe0277701e473e1c7344c2d5eb7258b60b8407656b19"
        ),
        RemotePackDefinition(
            fileName = "women_only.json",
            sha256 = "3b73f9269422179232327c15dc685995e4bb20f86e12e21ca5d178fb9dd9efe8"
        )
    )
    private val remoteFetchIntervalMs = 7L * 24L * 60L * 60L * 1000L
    private val lastFetchKey = longPreferencesKey("last_remote_fetch_ms")
    private val lastAttemptKey = longPreferencesKey("last_remote_attempt_ms")
    private val usedKey = stringSetPreferencesKey("used_quote_ids")
    private val lastQuoteDayKey = stringPreferencesKey("last_quote_day")
    private val lastQuoteIdKey = stringPreferencesKey("last_quote_id")

    // A cache for the loaded quotes. This avoids reloading them from files and network every time.
    // It's nullable ('?') because it will be null until the quotes are loaded for the first time.
    private var cachedQuotes: List<SourcedQuote>? = null
    private var lastAttemptMs: Long? = null

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
        val nowMs = System.currentTimeMillis()
        val storedAttempt = lastAttemptMs ?: context.quoteCacheDataStore.data.first()[lastAttemptKey] ?: 0L
        val shouldFetchRemote = nowMs - storedAttempt >= remoteFetchIntervalMs
        if (!shouldFetchRemote) {
            cachedQuotes?.let { return@withContext it }
        }

        // 'buildList' creates a new list.
        val combinedById = linkedMapOf<String, SourcedQuote>()

        fun addQuotes(quotes: List<SourcedQuote>, preferRemote: Boolean) {
            for (quote in quotes) {
                val existing = combinedById[quote.quote.id]
                if (existing == null || (preferRemote && !existing.isRemote)) {
                    combinedById[quote.quote.id] = quote
                }
            }
        }

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
                    val json = reader.readText()
                    val pack = parseQuotePack(json)
                    if (pack != null) {
                        val packId = pack.packId ?: file.substringBeforeLast('.')
                        val packWeight = pack.packWeight
                        val quotes = pack.messages.map {
                            SourcedQuote(it, isRemote = false, packId = packId, packWeight = packWeight)
                        }
                        addQuotes(quotes, preferRemote = false)
                    }
                }
            }
        }
        // Fetch quotes from the remote repo for live updates (weekly).
        var remoteSuccess = false
        var attemptedRemote = false
        if (shouldFetchRemote) {
            attemptedRemote = true
            val remoteDefinitions = fetchRemoteManifest() ?: fallbackRemotePackDefinitions
            remoteDefinitions.forEach { definition ->
                val url = definition.url(remotePackBaseUrl)
                runCatching {
                    val body = quoteService.fetchPack(url)
                    val bytes = readResponseBytes(body) ?: return@runCatching
                    val pack = parseRemotePack(bytes, definition)
                    if (pack != null) {
                        val packId = pack.packId ?: definition.fileName.substringBeforeLast('.')
                        val packWeight = pack.packWeight
                        val quotes = pack.messages.map {
                            val quote = it.copy(weight = maxOf(1, it.weight))
                            SourcedQuote(quote, isRemote = true, packId = packId, packWeight = packWeight)
                        }
                        addQuotes(quotes, preferRemote = true)
                        remoteSuccess = true
                    }
                }
            }
        }
        // Save the combined list to the cache.
        val combined = combinedById.values.toList()
        cachedQuotes = combined
        if (attemptedRemote) {
            context.quoteCacheDataStore.edit { prefs ->
                prefs[lastAttemptKey] = nowMs
                if (remoteSuccess) {
                    prefs[lastFetchKey] = nowMs
                }
            }
            lastAttemptMs = nowMs
        }
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
        val today = LocalDate.now(ZoneId.systemDefault())
        val prefs = context.quoteCacheDataStore.data.first()
        val storedDay = prefs[lastQuoteDayKey]
        val storedId = prefs[lastQuoteIdKey]
        if (storedDay == today.toString() && !storedId.isNullOrBlank()) {
            val match = quotes.firstOrNull { it.quote.id == storedId }?.quote
            if (match != null) return@withContext match
        }
        // Avoid repeats by tracking used IDs locally.
        val used = prefs[usedKey] ?: emptySet()
        val unused = quotes.filterNot { it.quote.id in used }
        if (unused.isEmpty()) return@withContext allQuotesUsedQuote

        val next = pickWeighted(unused)
        if (next != null) {
            context.quoteCacheDataStore.edit { prefs ->
                prefs[usedKey] = used + next.id
                prefs[lastQuoteDayKey] = today.toString()
                prefs[lastQuoteIdKey] = next.id
            }
        }
        next
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
        val byPack = candidates.groupBy { it.packId ?: "" }
        val packs = byPack.values.toList()
        val total = packs.sumOf { it.size * it.first().packWeight }
        var r = Random.nextInt(total)
        for (packQuotes in packs) {
            r -= packQuotes.size * packQuotes.first().packWeight
            if (r < 0) {
                val pick = packQuotes[Random.nextInt(packQuotes.size)]
                return pick.quote
            }
        }
        return packs.last().last().quote
    }

    private suspend fun fetchRemoteManifest(): List<RemotePackDefinition>? = runCatching {
        val url = remotePackBaseUrl + remoteManifestFileName
        val body = quoteService.fetchPack(url)
        val bytes = readResponseBytes(body, MAX_MANIFEST_BYTES.toLong()) ?: return@runCatching null
        val manifest = parseRemoteManifest(bytes) ?: return@runCatching null
        manifest.packs
    }.getOrNull()

    private fun parseRemoteManifest(bytes: ByteArray): RemoteManifest? {
        if (bytes.isEmpty() || bytes.size > MAX_MANIFEST_BYTES) return null
        val json = bytes.toString(Charsets.UTF_8)
        if (json.isBlank() || json.length > MAX_MANIFEST_CHARS) return null
        val reader = JsonReader(StringReader(json)).apply {
            setStrictness(Strictness.STRICT)
        }
        val element = try {
            JsonParser.parseReader(reader)
        } catch (_: Exception) {
            return null
        }
        if (reader.peek() != JsonToken.END_DOCUMENT) return null
        if (!element.isJsonObject) return null
        val obj = element.asJsonObject
        if (!hasOnlyKeys(obj, MANIFEST_KEYS)) return null
        val packs = obj.get("packs") ?: return null
        if (!packs.isJsonArray) return null
        val array = packs.asJsonArray
        if (array.size() < 1 || array.size() > MAX_MANIFEST_PACKS) return null
        val definitions = ArrayList<RemotePackDefinition>(array.size())
        for (entry in array) {
            if (!entry.isJsonObject) return null
            val packObj = entry.asJsonObject
            if (!hasOnlyKeys(packObj, MANIFEST_PACK_KEYS)) return null
            val fileName = readRequiredString(packObj, "fileName", MAX_FILE_NAME_LENGTH) ?: return null
            if (fileName.contains('/') || fileName.contains('\\') || !fileName.endsWith(".json")) return null
            val sha256 = readRequiredString(packObj, "sha256", MAX_SHA256_LENGTH) ?: return null
            if (sha256.length != MAX_SHA256_LENGTH || !isHexString(sha256)) return null
            definitions.add(RemotePackDefinition(fileName = fileName, sha256 = sha256))
        }
        return RemoteManifest(definitions)
    }

    private fun parseRemotePack(bytes: ByteArray, definition: RemotePackDefinition): QuotePack? {
        if (bytes.isEmpty() || bytes.size > MAX_PACK_BYTES) return null
        val hash = sha256Hex(bytes)
        if (!hash.equals(definition.sha256, ignoreCase = true)) return null
        val json = bytes.toString(Charsets.UTF_8)
        return parseQuotePack(json)
    }

    private fun readResponseBytes(
        body: ResponseBody,
        maxBytes: Long = MAX_PACK_BYTES.toLong()
    ): ByteArray? = body.use { response ->
        val length = response.contentLength()
        if (length > maxBytes) return null
        val source = response.source()
        val buffer = Buffer()
        var total = 0L
        while (true) {
            val read = source.read(buffer, 8192)
            if (read == -1L) break
            total += read
            if (total > maxBytes) return null
        }
        if (total == 0L) return null
        return buffer.readByteArray()
    }

    private fun parseQuotePack(json: String): QuotePack? {
        if (json.isBlank()) return null
        if (json.length > MAX_PACK_CHARS) return null
        val reader = JsonReader(StringReader(json)).apply {
            setStrictness(Strictness.STRICT)
        }
        val element = try {
            JsonParser.parseReader(reader)
        } catch (_: Exception) {
            return null
        }
        if (reader.peek() != JsonToken.END_DOCUMENT) return null
        return parseQuotePackElement(element)
    }

    private fun parseQuotePackElement(element: JsonElement): QuotePack? {
        if (!element.isJsonObject) return null
        val obj = element.asJsonObject
        if (!hasOnlyKeys(obj, PACK_KEYS)) return null

        val packId = readOptionalString(obj, "packId", MAX_PACK_ID_LENGTH)
            ?: obj.get("packId")?.let { return null }
        if (packId != null && packId.isBlank()) return null
        val packVersion = readOptionalInt(obj, "packVersion", MAX_PACK_VERSION)
            ?: obj.get("packVersion")?.let { return null }
        val packWeight = readOptionalInt(obj, "packWeight", MAX_PACK_WEIGHT)
            ?: obj.get("packWeight")?.let { return null }
        if (packWeight != null && packWeight !in 1..MAX_PACK_WEIGHT) return null

        val messages = obj.get("messages") ?: return null
        if (!messages.isJsonArray) return null
        val messagesArray = messages.asJsonArray
        if (messagesArray.size() > MAX_QUOTES_PER_PACK || messagesArray.size() < 1) return null

        val quotes = ArrayList<Quote>(messagesArray.size())
        val ids = HashSet<String>(messagesArray.size())
        for (entry in messagesArray) {
            if (!entry.isJsonObject) return null
            val quoteObj = entry.asJsonObject
            if (!hasOnlyKeys(quoteObj, QUOTE_KEYS)) return null

            val id = readRequiredString(quoteObj, "id", MAX_ID_LENGTH) ?: return null
            if (!ids.add(id)) return null
            val text = readRequiredString(quoteObj, "text", MAX_TEXT_LENGTH) ?: return null
            val author = readRequiredString(quoteObj, "author", MAX_AUTHOR_LENGTH) ?: return null
            val topic = readRequiredString(quoteObj, "topic", MAX_TOPIC_LENGTH) ?: return null
            val weight = readOptionalInt(quoteObj, "weight", MAX_WEIGHT)
                ?: run {
                    if (quoteObj.get("weight") != null) return null
                    1
                }
            if (weight !in 1..MAX_WEIGHT) return null

            quotes.add(Quote(id = id, text = text, author = author, topic = topic, weight = weight))
        }

        return QuotePack(
            packId = packId,
            packVersion = packVersion,
            packWeight = packWeight ?: 1,
            messages = quotes
        )
    }

    private fun hasOnlyKeys(obj: JsonObject, allowed: Set<String>): Boolean {
        return obj.entrySet().all { it.key in allowed }
    }

    private fun readRequiredString(obj: JsonObject, key: String, maxLength: Int): String? {
        val value = readOptionalString(obj, key, maxLength) ?: return null
        if (value.isBlank()) return null
        return value
    }

    private fun readOptionalString(obj: JsonObject, key: String, maxLength: Int): String? {
        val element = obj.get(key) ?: return null
        if (!element.isJsonPrimitive || !element.asJsonPrimitive.isString) return null
        val value = element.asString
        if (value.length > maxLength) return null
        return value
    }

    private fun readOptionalInt(obj: JsonObject, key: String, maxValue: Int): Int? {
        val element = obj.get(key) ?: return null
        if (!element.isJsonPrimitive || !element.asJsonPrimitive.isNumber) return null
        val number = element.asBigDecimal
        if (number.scale() > 0) return null
        val value = runCatching { number.intValueExact() }.getOrNull() ?: return null
        if (value < 0 || value > maxValue) return null
        return value
    }

    private fun isHexString(value: String): Boolean {
        for (ch in value) {
            val ok = (ch in '0'..'9') || (ch in 'a'..'f') || (ch in 'A'..'F')
            if (!ok) return false
        }
        return true
    }

    private fun sha256Hex(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { byte -> "%02x".format(byte) }
    }

    suspend fun findLongestQuote(): Quote? = withContext(Dispatchers.IO) {
        val quotes = loadQuotes()
        if (quotes.isEmpty()) return@withContext null
        quotes.maxByOrNull { it.quote.text.length }?.quote
    }

    suspend fun findLongestAuthor(): Quote? = withContext(Dispatchers.IO) {
        val quotes = loadQuotes()
        if (quotes.isEmpty()) return@withContext null
        quotes.maxByOrNull { it.quote.author.length }?.quote
    }
}

private const val MAX_PACK_BYTES = 256_000
private const val MAX_PACK_CHARS = 256_000
private const val MAX_MANIFEST_BYTES = 16_000
private const val MAX_MANIFEST_CHARS = 16_000
private const val MAX_MANIFEST_PACKS = 64
private const val MAX_QUOTES_PER_PACK = 512
private const val MAX_ID_LENGTH = 64
private const val MAX_TEXT_LENGTH = 512
private const val MAX_AUTHOR_LENGTH = 64
private const val MAX_TOPIC_LENGTH = 32
private const val MAX_PACK_ID_LENGTH = 64
private const val MAX_PACK_VERSION = 999
private const val MAX_WEIGHT = 10
private const val MAX_PACK_WEIGHT = 10
private const val MAX_FILE_NAME_LENGTH = 128
private const val MAX_SHA256_LENGTH = 64

private val PACK_KEYS = setOf("packId", "packVersion", "packWeight", "messages")
private val QUOTE_KEYS = setOf("id", "text", "author", "topic", "weight")
private val MANIFEST_KEYS = setOf("packs")
private val MANIFEST_PACK_KEYS = setOf("fileName", "sha256")
