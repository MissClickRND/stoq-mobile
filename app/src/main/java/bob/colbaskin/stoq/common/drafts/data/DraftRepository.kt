package bob.colbaskin.stoq.common.drafts.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import bob.colbaskin.stoq.common.userprefs.domain.UserPreferencesRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

enum class DraftType { Supply, Order, Inventory }

data class Draft(
    val id: String,
    val type: DraftType,
    val title: String,
    val summary: String,
    val updatedAtEpochMillis: Long,
    val payloadJson: String
)

@Singleton
class DraftRepository @Inject constructor(
    private val draftDao: DraftDao,
    private val preferences: UserPreferencesRepository
) {
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun observe(type: DraftType? = null): Flow<List<Draft>> =
        preferences.getUserPreferences().map { it.user?.let { user -> "${user.restaurantId}:${user.id}" }.orEmpty() }
        .distinctUntilChanged().flatMapLatest { owner -> draftDao.observeOwned(owner, type?.name) }.map { entities ->
            entities.map(DraftEntity::toDraft)
        }

    suspend fun save(
        type: DraftType,
        title: String,
        summary: String,
        payloadJson: String,
        id: String? = null
    ): Draft {
        val draft = Draft(
            id = id ?: UUID.randomUUID().toString(),
            type = type,
            title = title.ifBlank { type.defaultTitle },
            summary = summary,
            updatedAtEpochMillis = System.currentTimeMillis(),
            payloadJson = payloadJson
        )
        val owner = ownerKey()
        if (owner.isBlank()) return draft
        draftDao.upsert(draft.toEntity().copy(ownerKey = owner))
        return draft
    }

    suspend fun delete(id: String) {
        if (draftDao.findById(id)?.ownerKey == ownerKey()) draftDao.deleteById(id)
    }

    suspend fun find(id: String): Draft? = draftDao.findById(id)?.takeIf { it.ownerKey == ownerKey() }?.toDraft()

    private suspend fun ownerKey(): String = preferences.getUserPreferences().first().user?.let { "${it.restaurantId}:${it.id}" }.orEmpty()
}

private val DraftType.defaultTitle: String
    get() = when (this) {
        DraftType.Supply -> "Новая поставка"
        DraftType.Order -> "Новый заказ"
        DraftType.Inventory -> "Инвентаризация"
    }

private fun DraftEntity.toDraft() = Draft(
    id = id,
    type = DraftType.valueOf(type),
    title = title,
    summary = summary,
    updatedAtEpochMillis = updatedAtEpochMillis,
    payloadJson = payloadJson
)

private fun Draft.toEntity() = DraftEntity(
    id = id,
    type = type.name,
    title = title,
    summary = summary,
    updatedAtEpochMillis = updatedAtEpochMillis,
    payloadJson = payloadJson
)
