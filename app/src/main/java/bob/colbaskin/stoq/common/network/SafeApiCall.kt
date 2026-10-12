package bob.colbaskin.stoq.common.network

import bob.colbaskin.stoq.common.core.ApiResult
import kotlinx.coroutines.TimeoutCancellationException
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
            text = "Сервер вернул ошибку ${exception.code()}. Проверьте введённые данные и повторите попытку."
        )
        else -> ApiResult.Error(
            title = "Что-то пошло не так",
            text = "Не удалось обработать ответ сервера. Повторите попытку позже."
        )
    }
}
