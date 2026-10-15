package bob.colbaskin.stoq.feature.supplies.data.ai

import bob.colbaskin.stoq.common.core.ApiResult
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.network.safeApiCall
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteAiSupplyRepository @Inject constructor(
    private val api: AiSupplyApiService
) {
    suspend fun prepareSupply(
        fileName: String,
        mimeType: String,
        bytes: ByteArray
    ): UiState<AiSupplyRecognition> = when (val result = safeApiCall(
        apiCall = {
            val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            api.prepareSupply(MultipartBody.Part.createFormData("file", fileName, body))
        },
        successHandler = { response ->
            AiSupplyRecognition(
                sellerName = response.sellerName,
                items = response.items.map { item ->
                    AiSupplyItem(item.productId, item.name, item.quantity, item.price, item.totalPrice, item.expiresAt, item.unit?.shortName)
                }
            )
        }
    )) {
        is ApiResult.Success -> UiState.Success(result.data)
        is ApiResult.Error -> UiState.Error(
            title = "Не удалось распознать УПД",
            text = result.text
        )
    }
}
