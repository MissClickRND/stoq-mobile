package bob.colbaskin.stoq.feature.products.data.realtime

import android.util.Log
import bob.colbaskin.stoq.BuildConfig
import okhttp3.HttpUrl.Companion.toHttpUrl
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class InventorySocketChange(val event: String, val reason: String?)

@Singleton
class InventorySocketClient @Inject constructor(
    private val json: Json,
    private val httpClient: okhttp3.OkHttpClient
) {
    private val changesFlow = MutableSharedFlow<InventorySocketChange>(extraBufferCapacity = 32)
    val changes = changesFlow.asSharedFlow()
    private var users = 0
    private var productIds = emptyList<Long>()
    private val socket: Socket by lazy {
        IO.socket(BuildConfig.BASE_API_URL.toHttpUrl().newBuilder().encodedPath("/inventory").query(null).build().toString(), IO.Options().apply {
            transports = arrayOf(WebSocket.NAME)
            reconnection = true
            callFactory = httpClient
            webSocketFactory = httpClient
            forceNew = true
        }).also { connection ->
            connection.on(Socket.EVENT_CONNECT) {
                log("Подключено к /inventory")
                requestStock()
                changesFlow.tryEmit(InventorySocketChange("connected", null))
            }
            connection.on(Socket.EVENT_DISCONNECT) { log("Отключено: ${it.firstOrNull()}") }
            connection.on(Socket.EVENT_CONNECT_ERROR) { log("Ошибка подключения: ${it.firstOrNull()}") }
            listOf("inventory:stockResult", "inventory:expiringSoonResult", "inventory:stockChanged", "inventory:batchExpired", "inventory:error").forEach { event ->
                connection.on(event) { args ->
                    val raw = args.firstOrNull()?.toString().orEmpty()
                    log("← $event, данные=${raw.take(4000)}")
                    if (event == "inventory:stockChanged" || event == "inventory:batchExpired") {
                        val payload = runCatching { json.parseToJsonElement(raw) as? JsonObject }.getOrNull()
                        val reason = ((payload?.get("reason") ?: payload?.get("reson")) as? JsonPrimitive)?.content
                        changesFlow.tryEmit(InventorySocketChange(event, reason))
                    }
                }
            }
        }
    }

    @Synchronized fun acquire() {
        users++
        socket.connect()
    }

    @Synchronized fun release() {
        users = (users - 1).coerceAtLeast(0)
        if (users == 0) {
            socket.disconnect()
            productIds = emptyList()
        }
    }

    fun syncStock(ids: List<Long>) {
        productIds = ids.filter { it > 0 }.distinct()
        if (socket.connected()) requestStock()
    }

    private fun requestStock() {
        if (productIds.isEmpty()) return
        val message = JSONObject().put("requestId", "store").put("productIds", JSONArray(productIds))
        log("→ inventory:getStock, $message")
        socket.emit("inventory:getStock", message)
    }

    private fun log(message: String) {
        if (BuildConfig.DEBUG) Log.d("StoqInventoryWs", message)
    }
}
