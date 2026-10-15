package bob.colbaskin.stoq.feature.products.data

import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.feature.products.data.remote.InventoryApiService
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit

class DisposalContractTest {
    private fun repository(code: Int): RemoteInventoryRepository {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request()
            assertEquals("DELETE", request.method)
            assertEquals("/api/inventory/write-offs/59", request.url.encodedPath)
            Response.Builder().request(request).protocol(Protocol.HTTP_1_1)
                .code(code).message("Test")
                .body((if (code == 204) "" else """{"message":"Партия не найдена"}""").toResponseBody())
                .build()
        }.build()
        val api = Retrofit.Builder().baseUrl("https://example.test/api/").client(client)
            .build().create(InventoryApiService::class.java)
        return RemoteInventoryRepository(api)
    }

    @Test fun successfulDisposalSupportsEmptyResponse() = runBlocking {
        assertTrue(repository(204).dispose(59) is UiState.Success)
    }

    @Test fun failedDisposalUsesServerError() = runBlocking {
        val result = repository(404).dispose(59)
        assertTrue(result is UiState.Error)
        assertEquals("Партия не найдена", (result as UiState.Error).text)
    }
}
