package bob.colbaskin.stoq.feature.dishes.data.realtime

import bob.colbaskin.stoq.BuildConfig
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.feature.dishes.data.model.DishRecommendationDto
import bob.colbaskin.stoq.feature.dishes.data.model.StopListDishDto
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val StopListRequestEvent = "recommendations:getStopList"
private const val DishRecommendationsRequestEvent = "recommendations:getDishRecommendations"
private const val StopListResultEvent = "recommendations:stopListResult"
private const val DishRecommendationsResultEvent = "recommendations:dishRecommendationsResult"
private const val StopListUpdatedEvent = "recommendations:stopListUpdated"
private const val SocketResponseTimeoutMillis = 15_000L

private data class SocketResult(
    val event: String,
    val requestId: String?,
    val payload: JsonElement
)

@Singleton
class RecommendationsSocketClient @Inject constructor(
    private val json: Json
) {
    private val results = MutableSharedFlow<SocketResult>(extraBufferCapacity = 16)
    private val _stopListUpdates = MutableSharedFlow<List<StopListDishDto>>(extraBufferCapacity = 1)
    val stopListUpdates: SharedFlow<List<StopListDishDto>> = _stopListUpdates.asSharedFlow()

    private val socketDelegate = lazy {
        IO.socket(
            BuildConfig.BASE_API_URL.trimEnd('/') + "/recommendations",
            IO.Options().apply {
                transports = arrayOf(WebSocket.NAME)
                reconnection = true
            }
        ).also { socket ->
            socket.on(StopListResultEvent) { args -> emitResult(StopListResultEvent, args.firstOrNull()) }
            socket.on(DishRecommendationsResultEvent) { args -> emitResult(DishRecommendationsResultEvent, args.firstOrNull()) }
            socket.on(StopListUpdatedEvent) { args -> emitStopListUpdate(args.firstOrNull()) }
        }
    }
    private val socket: Socket get() = socketDelegate.value

    suspend fun getStopList(search: String?): UiState<List<StopListDishDto>> = request(
        requestEvent = StopListRequestEvent,
        resultEvent = StopListResultEvent,
        search = search,
        serializer = ListSerializer(StopListDishDto.serializer())
    )

    suspend fun getDishRecommendations(): UiState<List<DishRecommendationDto>> = request(
        requestEvent = DishRecommendationsRequestEvent,
        resultEvent = DishRecommendationsResultEvent,
        search = null,
        serializer = ListSerializer(DishRecommendationDto.serializer())
    )

    fun disconnect() {
        if (socketDelegate.isInitialized()) socket.disconnect()
    }

    private suspend fun <T> request(
        requestEvent: String,
        resultEvent: String,
        search: String?,
        serializer: kotlinx.serialization.KSerializer<T>
    ): UiState<T> {
        val requestId = UUID.randomUUID().toString()
        val response = withTimeoutOrNull(SocketResponseTimeoutMillis) {
            coroutineScope {
                val awaitingResult = async(start = CoroutineStart.UNDISPATCHED) {
                    results.filter { result ->
                        result.event == resultEvent && (result.requestId == null || result.requestId == requestId)
                    }.first()
                }
                socket.connect()
                socket.emit(requestEvent, JSONObject().put("requestId", requestId).apply {
                    search?.takeIf(String::isNotBlank)?.let { put("search", it) }
                })
                awaitingResult.await()
            }
        } ?: return UiState.Error(
            title = "Не удалось получить данные",
            text = "Сервер не ответил вовремя. Попробуйте ещё раз."
        )

        return runCatching {
            UiState.Success(json.decodeFromJsonElement(serializer, response.payload.resultList()))
        }.getOrElse {
            UiState.Error(
                title = "Не удалось обработать ответ",
                text = "Сервер вернул данные в неизвестном формате. Попробуйте ещё раз."
            )
        }
    }

    private fun emitResult(event: String, value: Any?) {
        val parsed = value?.toString()?.let { raw -> runCatching { json.parseToJsonElement(raw) }.getOrNull() } ?: return
        val requestId = (parsed as? JsonObject)?.get("requestId")?.jsonPrimitive?.contentOrNull
        results.tryEmit(SocketResult(event, requestId, parsed))
    }

    private fun emitStopListUpdate(value: Any?) {
        val payload = value?.toString()?.let { raw -> runCatching { json.parseToJsonElement(raw) }.getOrNull() } ?: return
        val stopList = (payload as? JsonObject)?.get("stopList") ?: return
        val parsed = runCatching {
            json.decodeFromJsonElement(ListSerializer(StopListDishDto.serializer()), stopList)
        }.getOrNull() ?: return
        _stopListUpdates.tryEmit(parsed)
    }

    private fun JsonElement.resultList(): JsonElement = when (this) {
        is JsonArray -> this
        is JsonObject -> this["data"] ?: this["result"] ?: this["items"] ?: this
        else -> this
    }
}
