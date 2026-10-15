package bob.colbaskin.stoq.common.network

import bob.colbaskin.stoq.common.core.ApiResult
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import java.io.IOException

suspend inline fun <reified T, reified R> safeApiCall(
    apiCall: suspend () -> T,
    successHandler: (T) -> R
): ApiResult<R> = try {
    ApiResult.Success(successHandler(apiCall()))
} catch (exception: Exception) {
    when (exception) {
        is IOException -> ApiResult.Error(
            title = "Нет подключения",
            text = "Проверьте подключение к интернету и повторите попытку."
        )
        is TimeoutCancellationException -> ApiResult.Error(
            title = "Время ожидания истекло",
            text = "Сервер не ответил вовремя. Попробуйте ещё раз."
        )
        is HttpException -> ApiResult.Error(
            title = "Не удалось выполнить запрос",
            text = exception.serverMessage()
                ?: "Сервер вернул ошибку ${exception.code()}. Проверьте введённые данные и повторите попытку."
        )
        else -> ApiResult.Error(
            title = "Что-то пошло не так",
            text = "Не удалось обработать ответ сервера. Повторите попытку позже."
        )
    }
}

@PublishedApi
internal fun HttpException.serverMessage(): String? = runCatching {
    val body = response()?.errorBody()?.string().orEmpty()
    val payload = Json.parseToJsonElement(body)
    val message = (payload as? JsonObject)?.get("message")
    val products = (payload as? JsonArray) ?: (message as? JsonArray)
    val missingProducts = products?.mapNotNull { element ->
        val product = element as? JsonObject ?: return@mapNotNull null
        val name = (product["name"] as? JsonPrimitive)?.contentOrNull?.takeIf(String::isNotBlank)
            ?: return@mapNotNull null
        val id = (product["productId"] as? JsonPrimitive)?.contentOrNull
        "• $name${id?.let { " · ID $it" }.orEmpty()}"
    }.orEmpty()
    if (missingProducts.isNotEmpty()) {
        "Недостаточно продуктов на складе:\n${missingProducts.joinToString("\n")}"
    } else {
        (message as? JsonPrimitive)?.contentOrNull?.takeIf(String::isNotBlank)
    }
}.getOrNull()
