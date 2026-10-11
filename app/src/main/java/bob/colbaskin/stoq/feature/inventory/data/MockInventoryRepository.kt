package bob.colbaskin.stoq.feature.inventory.data

import bob.colbaskin.stoq.feature.inventory.domain.InventoryRepository
import bob.colbaskin.stoq.feature.inventory.domain.model.InventoryProduct
import bob.colbaskin.stoq.feature.inventory.domain.model.InventoryProductImage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockInventoryRepository @Inject constructor() : InventoryRepository {
    override fun products() = listOf(
        InventoryProduct("mozzarella", "Моцарелла", "00001", "Молочные продукты", 12, "кг", InventoryProductImage.Mozzarella),
        InventoryProduct("tomatoes", "Томаты черри", "00002", "Овощи и зелень", 5, "кг", InventoryProductImage.Tomatoes),
        InventoryProduct("oil", "Оливковое масло", "00003", "Бакалея", 10, "л", InventoryProductImage.Iceberg),
        InventoryProduct("salmon", "Лосось", "00004", "Рыба и морепродукты", 8, "кг", InventoryProductImage.Salmon),
        InventoryProduct("basil", "Базилик", "00005", "Овощи и зелень", 20, "кг", InventoryProductImage.Iceberg),
        InventoryProduct("parmesan", "Пармезан", "00006", "Молочные продукты", 2, "кг", InventoryProductImage.Mozzarella),
        InventoryProduct("arugula", "Руккола", "00007", "Овощи и зелень", 15, "кг", InventoryProductImage.Iceberg),
        InventoryProduct("cream", "Сливки 33%", "00008", "Молочные продукты", 6, "л", InventoryProductImage.Mozzarella)
    )
}
