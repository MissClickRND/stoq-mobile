package bob.colbaskin.stoq.feature.supplies.data.ai

import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import okhttp3.MultipartBody

interface AiSupplyApiService {
    @Multipart
    @POST("ai/supply")
    suspend fun prepareSupply(@Part file: MultipartBody.Part): AiSupplyRecognitionDto
}
