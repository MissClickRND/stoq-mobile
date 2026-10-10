package bob.colbaskin.stoq.feature.dishes.data

import bob.colbaskin.stoq.feature.dishes.domain.DishRepository
import bob.colbaskin.stoq.feature.dishes.domain.model.Dish
import bob.colbaskin.stoq.feature.dishes.domain.model.IngredientStockStatus
import bob.colbaskin.stoq.feature.dishes.domain.model.RecipeItem

class MockDishRepository : DishRepository {
    private val salmonRecipe = listOf(
        RecipeItem("Лосось атлантический", "200 г", "2,4 кг", IngredientStockStatus.Available),
        RecipeItem("Спаржа", "80 г", "1,2 кг", IngredientStockStatus.Available),
        RecipeItem("Помидоры черри", "60 г", "0,3 кг", IngredientStockStatus.RunningLow),
        RecipeItem("Лимон", "30 г", "1,1 кг", IngredientStockStatus.Available)
    )

    private val dishes = listOf(
        Dish("salmon", "Стейк из лосося", "Нежный стейк из атлантического лосося с овощами гриль и лимонным соусом.", "Горячее", 1490, 420, "https://images.unsplash.com/photo-1467003909585-2f8a72700288?auto=format&fit=crop&w=900&q=85", true, salmonRecipe, 12, 104300, 3),
        Dish("caesar", "Салат «Цезарь» с креветками", "Классический рецепт с тигровыми креветками и пармезаном.", "Салаты", 790, 279, "https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=900&q=85", true, listOf(RecipeItem("Креветки", "140 г", "3,5 кг", IngredientStockStatus.Available), RecipeItem("Салат романо", "90 г", "2,1 кг", IngredientStockStatus.Available)), 9, 68300, 5),
        Dish("pasta", "Паста «Карбонара»", "Классическая итальянская паста с беконом, яйцом и пармезаном.", "Горячее", 690, 248, "https://images.unsplash.com/photo-1473093295043-cdd812d0e601?auto=format&fit=crop&w=900&q=85", true, listOf(RecipeItem("Паста", "250 г", "8 кг", IngredientStockStatus.Available), RecipeItem("Пармезан", "35 г", "1,6 кг", IngredientStockStatus.Available)), 18, 92700, 2),
        Dish("cheesecake", "Чизкейк с ягодами", "Нежный сливочный чизкейк с сезонными ягодами и мятой.", "Десерты", 450, 265, "https://images.unsplash.com/photo-1565958011703-44f9829ba187?auto=format&fit=crop&w=900&q=85", false, listOf(RecipeItem("Сливочный сыр", "150 г", "0 кг", IngredientStockStatus.RunningLow), RecipeItem("Ягоды", "60 г", "0,8 кг", IngredientStockStatus.Available)), 0, 0, 7),
        Dish("burger", "Бургер с говядиной", "Сочная мраморная говядина, чиз, овощи и фирменный соус на булочке.", "Горячее", 890, 301, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=900&q=85", true, listOf(RecipeItem("Говяжья котлета", "180 г", "5,4 кг", IngredientStockStatus.Available), RecipeItem("Булочка бриошь", "1 шт", "24 шт", IngredientStockStatus.Available)), 14, 81200, 4),
        Dish("fondant", "Шоколадный фондан", "Теплый шоколадный десерт с жидкой серединкой и ванильным мороженым.", "Десерты", 520, 297, "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?auto=format&fit=crop&w=900&q=85", true, listOf(RecipeItem("Шоколад", "90 г", "2,2 кг", IngredientStockStatus.Available), RecipeItem("Масло", "40 г", "1,4 кг", IngredientStockStatus.Available)), 7, 41600, 6)
    )

    override fun getDishes(): List<Dish> = dishes
    override fun getDish(id: String): Dish? = dishes.firstOrNull { it.id == id }
}
