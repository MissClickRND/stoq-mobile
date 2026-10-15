package bob.colbaskin.stoq.feature.logs

import bob.colbaskin.stoq.feature.logs.data.*
import bob.colbaskin.stoq.feature.logs.presentation.logDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import org.junit.Assert.*
import org.junit.Test

class LogsContractTest {
    private val json = Json { ignoreUnknownKeys = true }
    private fun entry(id: String, time: String) = LogEntryDto(id, "update", LogAuthorDto(null, "Система", "SYSTEM"), "Склад", "Обновление", "", time)

    @Test fun nullableAuthorsAndMixedChangeValuesAreSupported() {
        val page = json.decodeFromString<LogsPageDto>("""{"items":[{"id":"stock:1:2","action":"update","author":{"id":null,"name":"Система","role":"SYSTEM"},"category":"Склад","title":"Изменены остатки","description":"Инвентаризация","timestamp":"2026-10-15T22:30:00Z","object":null,"detail":null,"change":{"before":2,"after":"3 кг"}}],"nextCursor":"next"}""")
        assertNull(page.items.single().author.id)
        assertEquals("next", page.nextCursor)
        assertNotNull(page.items.single().change)
        assertEquals("01:30", logDate(page.items.single().timestamp, "HH:mm"))
        assertEquals("16.10.2026", logDate(page.items.single().timestamp, "dd.MM.yyyy"))
    }

    @Test fun paginationDeduplicatesAndPreservesNewestFirst() {
        val first = entry("first", "2026-10-15T10:00:00Z")
        val older = entry("old", "2026-10-14T10:00:00Z")
        assertEquals(listOf("first", "old"), mergeLogEntries(listOf(first), listOf(first, older)).map { it.id })
    }

    @Test fun unknownActionsAndInvalidDatesDoNotCrashTheFeed() {
        val entry = entry("unknown", "not-a-date").copy(action = "future_action")
        assertEquals(listOf(entry), mergeLogEntries(emptyList(), listOf(entry)))
        assertEquals("not-a-date", logDate(entry.timestamp, "HH:mm"))
    }
}
