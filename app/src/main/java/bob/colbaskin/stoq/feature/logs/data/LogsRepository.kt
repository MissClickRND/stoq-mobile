package bob.colbaskin.stoq.feature.logs.data

import bob.colbaskin.stoq.common.core.toUiState
import bob.colbaskin.stoq.common.network.safeApiCall
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject

@Serializable
data class LogsPageDto(val items: List<LogEntryDto>, val nextCursor: String? = null)

@Serializable
data class LogEntryDto(
    val id: String,
    val action: String,
    val author: LogAuthorDto,
    val category: String,
    val title: String,
    val description: String,
    val timestamp: String,
    @SerialName("object") val target: String? = null,
    val detail: String? = null,
    val change: LogChangeDto? = null
)

@Serializable
data class LogAuthorDto(val id: Long? = null, val name: String, val role: String)

@Serializable
data class LogChangeDto(val before: JsonElement? = null, val after: JsonElement? = null)

interface LogsApi {
    @GET("logs") suspend fun page(@Query("cursor") cursor: String? = null): LogsPageDto
}

class LogsRepository @Inject constructor(private val api: LogsApi) {
    suspend fun page(cursor: String? = null) = safeApiCall(apiCall = { api.page(cursor) }, successHandler = { it }).toUiState()
}

internal fun mergeLogEntries(existing: List<LogEntryDto>, incoming: List<LogEntryDto>): List<LogEntryDto> =
    (existing + incoming).distinctBy { it.id }.sortedByDescending { runCatching { java.time.Instant.parse(it.timestamp) }.getOrDefault(java.time.Instant.MIN) }
