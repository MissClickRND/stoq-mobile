package bob.colbaskin.stoq.feature.inventory.domain.model

data class InventoryProduct(
    val id: String,
    val batchId: Long,
    val name: String,
    val article: String,
    val category: String,
    val systemQuantity: Double,
    val unit: String
)
