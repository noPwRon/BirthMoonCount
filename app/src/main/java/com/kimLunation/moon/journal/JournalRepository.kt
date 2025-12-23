// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon.journal

// Imports are used to bring in code from other parts of the project or from external libraries.
import android.content.Context // Provides access to application-specific resources and classes.
import androidx.datastore.preferences.core.edit // Function to edit data in DataStore.
import androidx.datastore.preferences.core.stringPreferencesKey // Key for storing a String value in DataStore.
import androidx.datastore.preferences.preferencesDataStore // Function to create a DataStore instance.
import com.google.gson.Gson // A library to convert JSON data to Kotlin objects and vice-versa.
import kotlinx.coroutines.flow.first // Reads the current DataStore value once.

/**
 * A data class that represents a single journal entry.
 * The sky-stamp values are stored at save time to keep entries stable over time.
 */
data class JournalEntry(
    val id: String,
    val localDate: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val body: String,
    val moodX: Float,
    val moodY: Float,
    val lunationCount: Int,
    val lunationDay: Double?,
    val phaseLabel: String,
    val illuminationPercent: Int,
    val moonSign: String?,
    val isPeriod: Boolean = false,
    val isGreenEvent: Boolean = false,
    val isYellowEvent: Boolean = false,
    val isBlueEvent: Boolean = false
)

private val Context.journalDataStore by preferencesDataStore(name = "journal_entries")

/**
 * A small repository for journaling data backed by DataStore.
 * Entries are stored per local date (YYYY-MM-DD) with upsert behavior.
 */
class JournalRepository(private val context: Context) {
    private val gson = Gson()

    private fun entryKey(date: String) = stringPreferencesKey("journal_entry_$date")
    private val entryPrefix = "journal_entry_"

    suspend fun getEntry(localDate: String): JournalEntry? {
        val json = context.journalDataStore.data.first()[entryKey(localDate)] ?: return null
        return runCatching { gson.fromJson(json, JournalEntry::class.java) }.getOrNull()
    }

    suspend fun upsertEntry(entry: JournalEntry) {
        context.journalDataStore.edit { prefs ->
            prefs[entryKey(entry.localDate)] = gson.toJson(entry)
        }
    }

    suspend fun getAllEntries(): List<JournalEntry> {
        val prefs = context.journalDataStore.data.first()
        return prefs.asMap()
            .filter { (key, value) ->
                key.name.startsWith(entryPrefix) && value is String
            }
            .mapNotNull { (_, value) ->
                runCatching { gson.fromJson(value as String, JournalEntry::class.java) }.getOrNull()
            }
            .sortedByDescending { it.localDate }
    }
}
