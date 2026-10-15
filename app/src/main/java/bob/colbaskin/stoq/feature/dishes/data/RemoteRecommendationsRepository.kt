package bob.colbaskin.stoq.feature.dishes.data

import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.feature.dishes.data.model.StopListDishDto
import bob.colbaskin.stoq.feature.dishes.data.model.DishRecommendationDto
import bob.colbaskin.stoq.feature.dishes.data.realtime.RecommendationsSocketClient
import bob.colbaskin.stoq.feature.dishes.data.realtime.StopListSocketUpdate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class StopListDish(
    val dishId: String,
    val reasonTitle: String,
    val reason: String
)

data class StopListUpdate(
    val reason: String?,
    val updatedAt: String?,
    val items: List<StopListDish>
)

data class RecommendedDish(
    val dishId: String,
    val name: String?,
    val salePrice: Double?,
    val costPrice: Double?,
    val profit: Double?,
    val maxPortions: Double?,
    val expiringProducts: List<String>,
    val score: Double?
)

@Singleton
class RemoteRecommendationsRepository @Inject constructor(
    private val socketClient: RecommendationsSocketClient
) {
    val stopListUpdates: Flow<StopListUpdate> = socketClient.stopListUpdates.map { update ->
        StopListUpdate(
            reason = update.reason,
            updatedAt = update.updatedAt,
            items = update.items.map(StopListDishDto::toStopListDish)
        )
    }

    suspend fun getStopList(search: String? = null): UiState<List<StopListDish>> = when (
        val result = socketClient.getStopList(search?.trim()?.takeIf(String::isNotBlank))
    ) {
        is UiState.Success -> UiState.Success(result.data.map(StopListDishDto::toStopListDish))
        is UiState.Error -> result
        UiState.Loading -> UiState.Loading
    }

    suspend fun getDishRecommendations(): UiState<List<RecommendedDish>> = when (
        val result = socketClient.getDishRecommendations()
    ) {
        is UiState.Success -> UiState.Success(result.data.map(DishRecommendationDto::toRecommendedDish))
        is UiState.Error -> result
        UiState.Loading -> UiState.Loading
    }

    fun disconnect() = socketClient.disconnect()
}

private fun DishRecommendationDto.toRecommendedDish() = RecommendedDish(
    dishId = dishId.toString(),
    name = dishName,
    salePrice = salePrice,
    costPrice = costPrice,
    profit = profit,
    maxPortions = maxPortions,
    expiringProducts = expiringProducts.mapNotNull { item ->
        runCatching {
            item.jsonPrimitive.contentOrNull
                ?: item.jsonObject["name"]?.jsonPrimitive?.contentOrNull
        }.getOrNull()
    },
    score = recommendationScore
)

private fun StopListDishDto.toStopListDish(): StopListDish {
    val statuses = reasons.mapNotNull { it.status?.uppercase() }
    return StopListDish(
        dishId = dishId.toString(),
        reasonTitle = if (statuses.any { it.contains("EXPIRED") }) "Истёк срок годности" else "Нет ингредиентов",
        reason = reasons.mapNotNull { it.name }.takeIf(List<String>::isNotEmpty)?.joinToString(", ") ?: "мог"
    )
}
