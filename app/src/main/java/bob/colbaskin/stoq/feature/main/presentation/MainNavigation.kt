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
import androidx.navigation.toRoute
import bob.colbaskin.stoq.common.designsystem.components.BottomBarItem
import bob.colbaskin.stoq.common.designsystem.components.StoqBottomBar
import bob.colbaskin.stoq.common.designsystem.components.StoqTopBar
import bob.colbaskin.stoq.feature.auth.domain.model.AuthUser
import bob.colbaskin.stoq.feature.dashboard.presentation.DashboardScreen
import bob.colbaskin.stoq.feature.dishes.presentation.DishDetailsScreen
import bob.colbaskin.stoq.feature.dishes.presentation.DishCollectionScreen
import bob.colbaskin.stoq.feature.dishes.presentation.DishesScreen
import bob.colbaskin.stoq.feature.supplies.presentation.CreateSupplyScreen
import bob.colbaskin.stoq.feature.supplies.presentation.SuppliesScreen
import bob.colbaskin.stoq.feature.supplies.presentation.SupplyDetailsScreen
import bob.colbaskin.stoq.feature.products.presentation.StorageScreen
import bob.colbaskin.stoq.feature.orders.presentation.CreateOrderScreen
import bob.colbaskin.stoq.feature.orders.presentation.OrdersScreen
import bob.colbaskin.stoq.feature.orders.presentation.OrderDetailsScreen
import bob.colbaskin.stoq.feature.admin.presentation.AdminScreen
import compose.icons.TablerIcons
import compose.icons.tablericons.ChartBar
import compose.icons.tablericons.Package
import compose.icons.tablericons.ShoppingCart
import compose.icons.tablericons.TruckDelivery
import compose.icons.tablericons.ToolsKitchen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import kotlinx.serialization.Serializable

private const val DashboardRoute = "dashboard"
private const val ProductsRoute = "products"
private const val SupplyRoute = "supply"
private const val OrdersRoute = "orders"
private const val DishesRoute = "dishes"

@Serializable
private data object AdminDestination

private val adminRoute = requireNotNull(AdminDestination::class.qualifiedName)

@Serializable
private data class DishDetailsDestination(val dishId: String)

@Serializable
private data class DishCollectionDestination(val recommended: Boolean)

@Serializable
private data object CreateOrderDestination

@Serializable
private data class SupplyDetailsDestination(val supplyNumber: String)

@Serializable
private data class OrderDetailsDestination(val orderId: String)
private val mainSections = listOf(
    BottomBarItem("Дашборд", DashboardRoute, TablerIcons.ChartBar),
    BottomBarItem("Склад", ProductsRoute, TablerIcons.Package),
    BottomBarItem("Поставки", SupplyRoute, TablerIcons.TruckDelivery),
    BottomBarItem("Заказы", OrdersRoute, TablerIcons.ShoppingCart),
    BottomBarItem("Блюда", DishesRoute, TablerIcons.ToolsKitchen),
    BottomBarItem("Админ", adminRoute, Icons.Outlined.AdminPanelSettings)
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
                    if (section.route == adminRoute) {
                        navController.navigate(AdminDestination) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    } else if (section.route == SupplyRoute && navController.popBackStack(SupplyRoute, false)) {
                        Unit
                    } else {
                        navController.navigate(section.route) {
                            popUpTo(DashboardRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
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
                SuppliesScreen(
                    onCreateSupply = { navController.navigate("create_supply") },
                    onSupplyClick = { supplyNumber -> navController.navigate(SupplyDetailsDestination(supplyNumber)) }
                )
            }
            composable("create_supply") {
                CreateSupplyScreen(onBack = { navController.popBackStack() })
            }
            composable<SupplyDetailsDestination> { entry ->
                SupplyDetailsScreen(
                    supplyNumber = entry.toRoute<SupplyDetailsDestination>().supplyNumber,
                    onBack = navController::popBackStack
                )
            }
            composable(OrdersRoute) {
                OrdersScreen(
                    onCreateOrder = { navController.navigate(CreateOrderDestination) },
                    onOrderClick = { navController.navigate(OrderDetailsDestination(it)) }
                )
            }
            composable<CreateOrderDestination> {
                CreateOrderScreen(onBack = { navController.popBackStack() })
            }
            composable<OrderDetailsDestination> { entry ->
                OrderDetailsScreen(
                    orderId = entry.toRoute<OrderDetailsDestination>().orderId,
                    onBack = navController::popBackStack
                )
            }
            composable(DishesRoute) {
                DishesScreen(
                    onDishClick = { dishId -> navController.navigate(DishDetailsDestination(dishId)) },
                    onStopListClick = { navController.navigate(DishCollectionDestination(recommended = false)) },
                    onRecommendationsClick = { navController.navigate(DishCollectionDestination(recommended = true)) }
                )
            }
            composable<DishDetailsDestination> { entry ->
                DishDetailsScreen(
                    id = entry.toRoute<DishDetailsDestination>().dishId,
                    onBack = navController::popBackStack
                )
            }
            composable<DishCollectionDestination> { entry ->
                DishCollectionScreen(
                    recommended = entry.toRoute<DishCollectionDestination>().recommended,
                    onBack = navController::popBackStack,
                    onDishClick = { dishId -> navController.navigate(DishDetailsDestination(dishId)) }
                )
            }
            composable<AdminDestination> {
                AdminScreen()
            }
        }
    }
}
