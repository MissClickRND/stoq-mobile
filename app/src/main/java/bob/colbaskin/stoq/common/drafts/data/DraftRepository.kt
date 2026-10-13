package bob.colbaskin.stoq.common.drafts.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
    private val draftDao: DraftDao
) {
    fun observe(type: DraftType? = null): Flow<List<Draft>> =
        (type?.let { draftDao.observeByType(it.name) } ?: draftDao.observeAll()).map { entities ->
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
        draftDao.upsert(draft.toEntity())
        return draft
    }

    suspend fun delete(id: String) = draftDao.deleteById(id)

    suspend fun find(id: String): Draft? = draftDao.findById(id)?.toDraft()
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
