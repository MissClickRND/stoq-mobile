package bob.colbaskin.stoq.feature.main.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import bob.colbaskin.stoq.common.designsystem.components.BottomBarItem
import bob.colbaskin.stoq.common.designsystem.components.StoqBottomBar
import bob.colbaskin.stoq.common.designsystem.components.StoqTopBar
import bob.colbaskin.stoq.feature.auth.domain.model.AuthUser
import bob.colbaskin.stoq.feature.dashboard.presentation.DashboardScreen
import bob.colbaskin.stoq.feature.products.presentation.StorageScreen
import compose.icons.TablerIcons
import compose.icons.tablericons.ChartBar
import compose.icons.tablericons.Package
import compose.icons.tablericons.ShoppingCart
import compose.icons.tablericons.TruckDelivery
import compose.icons.tablericons.ToolsKitchen

private const val DashboardRoute = "dashboard"
private const val ProductsRoute = "products"
private const val SupplyRoute = "supply"
private const val OrdersRoute = "orders"
private const val DishesRoute = "dishes"

private val mainSections = listOf(
    BottomBarItem("Дашборд", DashboardRoute, TablerIcons.ChartBar),
    BottomBarItem("Склад", ProductsRoute, TablerIcons.Package),
    BottomBarItem("Поставки", SupplyRoute, TablerIcons.TruckDelivery),
    BottomBarItem("Заказы", OrdersRoute, TablerIcons.ShoppingCart),
    BottomBarItem("Блюда", DishesRoute, TablerIcons.ToolsKitchen)
)

@Composable
fun MainNavigation(user: AuthUser?) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = { StoqTopBar(avatarUrl = user?.avatarUrl) },
        bottomBar = {
            StoqBottomBar(
                items = mainSections,
                selectedRoute = currentRoute,
                onItemSelected = { section ->
                    navController.navigate(section.route) {
                        popUpTo(DashboardRoute) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = DashboardRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(DashboardRoute) {
                DashboardScreen()
            }
            composable(ProductsRoute) {
                StorageScreen()
            }
            composable(SupplyRoute) {
                SectionPlaceholderScreen("Поставки", "Здесь будут поставщики и новые поставки.", TablerIcons.TruckDelivery)
            }
            composable(OrdersRoute) {
                SectionPlaceholderScreen("Заказы", "Здесь будут заказы ресторана.", TablerIcons.ShoppingCart)
            }
            composable(DishesRoute) {
                SectionPlaceholderScreen("Блюда", "Здесь будут блюда и технологические карты.", TablerIcons.ToolsKitchen)
            }
        }
    }
}
