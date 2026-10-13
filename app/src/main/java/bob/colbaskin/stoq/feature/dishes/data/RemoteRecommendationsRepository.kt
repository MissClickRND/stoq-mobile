package bob.colbaskin.stoq.feature.dishes.data

import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.core.toUiState
import bob.colbaskin.stoq.common.network.safeApiCall
import bob.colbaskin.stoq.feature.dishes.data.model.StopListDishDto
import bob.colbaskin.stoq.feature.dishes.data.remote.RecommendationsApiService
import javax.inject.Inject
import javax.inject.Singleton

data class StopListDish(
    val dishId: String,
    val reasonTitle: String,
    val reason: String
)

@Singleton
class RemoteRecommendationsRepository @Inject constructor(
    private val api: RecommendationsApiService
) {
    suspend fun getStopList(search: String? = null): UiState<List<StopListDish>> = safeApiCall(
        apiCall = { api.getStopList(search?.trim()?.takeIf(String::isNotBlank)) },
        successHandler = { items -> items.map(StopListDishDto::toStopListDish) }
    ).toUiState()
}

private fun StopListDishDto.toStopListDish(): StopListDish {
    val statuses = reasons.mapNotNull { it.status?.uppercase() }
    return StopListDish(
        dishId = dishId.toString(),
        reasonTitle = if (statuses.any { it.contains("EXPIRED") }) "Истёк срок годности" else "Нет ингредиентов",
        reason = reasons.mapNotNull { it.name }.takeIf(List<String>::isNotEmpty)?.joinToString(", ") ?: "мог"
    )
}
