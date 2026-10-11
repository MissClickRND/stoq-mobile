package bob.colbaskin.stoq.feature.inventory.domain.model

enum class InventoryProductImage {
    Tomatoes,
    Mozzarella,
    Salmon,
    Iceberg,
    Chicken
}

data class InventoryProduct(
    val id: String,
    val name: String,
    val article: String,
    val category: String,
    val systemQuantity: Int,
    val unit: String,
    val image: InventoryProductImage
)
