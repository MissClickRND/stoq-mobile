package bob.colbaskin.stoq.feature.main.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import bob.colbaskin.stoq.feature.supplies.presentation.SupplyScanScreen
import bob.colbaskin.stoq.feature.supplies.presentation.SuppliesScreen
import bob.colbaskin.stoq.feature.supplies.presentation.SupplyDetailsScreen
import bob.colbaskin.stoq.feature.products.presentation.StorageScreen
import bob.colbaskin.stoq.feature.orders.presentation.CreateOrderScreen
import bob.colbaskin.stoq.feature.orders.presentation.OrdersScreen
import bob.colbaskin.stoq.feature.orders.presentation.OrderDetailsScreen
import bob.colbaskin.stoq.feature.admin.presentation.AdminScreen
import bob.colbaskin.stoq.feature.inventory.presentation.InventoryScreen
import bob.colbaskin.stoq.feature.profile.presentation.ProfileScreen
import bob.colbaskin.stoq.common.drafts.data.DraftType
import bob.colbaskin.stoq.common.drafts.presentation.DraftsListScreen
import bob.colbaskin.stoq.common.drafts.presentation.DraftsViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.LaunchedEffect
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
private data class CreateOrderDestination(val draftId: String? = null)

@Serializable
private data class EditOrderDestination(val orderId: String)

@Serializable
private data class CreateSupplyDestination(val draftId: String? = null, val scanToken: String? = null)
@Serializable
private data object SupplyScanDestination

@Serializable
private data class DraftsDestination(val type: String, val excludeDraftId: String? = null)

@Serializable
private data class SupplyDetailsDestination(val supplyNumber: String)

@Serializable
private data class OrderDetailsDestination(val orderId: String)

@Serializable
private data class InventoryDestination(val draftId: String? = null)

@Serializable
private data object ProfileDestination
private val profileRoute = requireNotNull(ProfileDestination::class.qualifiedName)
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
    val showBottomBar = mainSections.any { it.route == currentRoute }

    Scaffold(
        topBar = {
            StoqTopBar(
                avatarUrl = user?.avatarUrl,
                showAvatar = currentRoute != profileRoute,
                onProfileClick = { navController.navigate(ProfileDestination) { launchSingleTop = true } }
            )
        },
        bottomBar = {
            if (showBottomBar) {
                StoqBottomBar(
                    items = mainSections,
                    selectedRoute = currentRoute,
                    onItemSelected = { section ->
                        if (section.route == currentRoute) {
                            Unit
                        } else if (navController.popBackStack(section.route, false)) {
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = DashboardRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(DashboardRoute) {
                DashboardScreen(
                    onProductsClick = { navController.navigate(ProductsRoute) { launchSingleTop = true } },
                    onDishesClick = { navController.navigate(DishesRoute) { launchSingleTop = true } },
                    onOrdersClick = { navController.navigate(OrdersRoute) { launchSingleTop = true } }
                )
            }
            composable(ProductsRoute) { entry ->
                val draftFeedback by entry.savedStateHandle.getStateFlow<String?>("draft_feedback", null).collectAsState()
                StorageScreen(
                    onInventoryClick = { navController.navigate(InventoryDestination()) },
                    onInventoryDraftsClick = { navController.navigate(DraftsDestination("inventory")) },
                    onSupplyClick = { supplyId -> navController.navigate(SupplyDetailsDestination(supplyId)) },
                    draftFeedback = draftFeedback,
                    onDraftFeedbackShown = { entry.savedStateHandle["draft_feedback"] = null }
                )
            }
            composable<InventoryDestination> { entry ->
                InventoryScreen(
                    onBack = navController::popBackStack,
                    onStorageClick = {
                        navController.getBackStackEntry(ProductsRoute).savedStateHandle["draft_feedback"] = "Черновик сохранён"
                        if (!navController.popBackStack(ProductsRoute, inclusive = false)) {
                            navController.navigate(ProductsRoute) { launchSingleTop = true }
                        }
                    },
                    draftId = entry.toRoute<InventoryDestination>().draftId,
                    onDraftsClick = { activeDraftId -> navController.navigate(DraftsDestination("inventory", activeDraftId)) }
                )
            }
            composable<ProfileDestination> {
                user?.let { ProfileScreen(user = it, onBack = navController::popBackStack) }
            }
            composable(SupplyRoute) { entry ->
                val draftFeedback by entry.savedStateHandle.getStateFlow<String?>("draft_feedback", null).collectAsState()
                SuppliesScreen(
                    onCreateSupply = { navController.navigate(CreateSupplyDestination()) },
                    onScanSupply = { navController.navigate(SupplyScanDestination) },
                    onSupplyClick = { supplyNumber -> navController.navigate(SupplyDetailsDestination(supplyNumber)) },
                    onDraftsClick = { navController.navigate(DraftsDestination("supply")) },
                    draftFeedback = draftFeedback,
                    onDraftFeedbackShown = { entry.savedStateHandle["draft_feedback"] = null }
                )
            }
            composable<CreateSupplyDestination> { entry ->
                CreateSupplyScreen(
                    onBack = { navController.popBackStack() },
                    onScanSupply = { navController.navigate(SupplyScanDestination) },
                    onSuppliesClick = {
                        navController.getBackStackEntry(SupplyRoute).savedStateHandle["draft_feedback"] = "Черновик сохранён"
                        if (!navController.popBackStack(SupplyRoute, inclusive = false)) {
                            navController.navigate(SupplyRoute) { launchSingleTop = true }
                        }
                    },
                    draftId = entry.toRoute<CreateSupplyDestination>().draftId,
                    scanToken = entry.toRoute<CreateSupplyDestination>().scanToken
                )
            }
            composable<SupplyScanDestination> {
                SupplyScanScreen(
                    onBack = navController::popBackStack,
                    onRecognized = { token ->
                        navController.navigate(CreateSupplyDestination(scanToken = token)) {
                            popUpTo<SupplyScanDestination> { inclusive = true }
                        }
                    }
                )
            }
            composable<SupplyDetailsDestination> { entry ->
                SupplyDetailsScreen(
                    supplyNumber = entry.toRoute<SupplyDetailsDestination>().supplyNumber,
                    onBack = navController::popBackStack
                )
            }
            composable(OrdersRoute) { entry ->
                val draftFeedback by entry.savedStateHandle.getStateFlow<String?>("draft_feedback", null).collectAsState()
                OrdersScreen(
                    onCreateOrder = { navController.navigate(CreateOrderDestination()) },
                    onOrderClick = { navController.navigate(OrderDetailsDestination(it)) },
                    onEditOrder = { navController.navigate(EditOrderDestination(it)) },
                    onDraftsClick = { navController.navigate(DraftsDestination("order")) },
                    draftFeedback = draftFeedback,
                    onDraftFeedbackShown = { entry.savedStateHandle["draft_feedback"] = null }
                )
            }
            composable<CreateOrderDestination> { entry ->
                CreateOrderScreen(
                    onBack = { navController.popBackStack() },
                    onOrdersClick = {
                        navController.getBackStackEntry(OrdersRoute).savedStateHandle["draft_feedback"] = "Черновик сохранён"
                        if (!navController.popBackStack(OrdersRoute, inclusive = false)) {
                            navController.navigate(OrdersRoute) { launchSingleTop = true }
                        }
                    },
                    draftId = entry.toRoute<CreateOrderDestination>().draftId
                )
            }
            composable<EditOrderDestination> { entry ->
                CreateOrderScreen(
                    onBack = navController::popBackStack,
                    onOrdersClick = {
                        if (!navController.popBackStack(OrdersRoute, inclusive = false)) {
                            navController.navigate(OrdersRoute) { launchSingleTop = true }
                        }
                    },
                    editOrderId = entry.toRoute<EditOrderDestination>().orderId
                )
            }
            composable<DraftsDestination> { entry ->
                val destination = entry.toRoute<DraftsDestination>()
                val type = when (destination.type) {
                    "supply" -> DraftType.Supply
                    "inventory" -> DraftType.Inventory
                    else -> DraftType.Order
                }
                val draftsViewModel: DraftsViewModel = hiltViewModel()
                LaunchedEffect(type) { draftsViewModel.load(type) }
                DraftsListScreen(
                    title = when (type) {
                        DraftType.Supply -> "Черновики поставок"
                        DraftType.Order -> "Черновики заказов"
                        DraftType.Inventory -> "Черновики инвентаризаций"
                    },
                    drafts = draftsViewModel.drafts.filterNot { it.id == destination.excludeDraftId },
                    onBack = { navController.popBackStack() },
                    onOpen = { draft ->
                        when (type) {
                            DraftType.Supply -> navController.navigate(CreateSupplyDestination(draft.id))
                            DraftType.Order -> navController.navigate(CreateOrderDestination(draft.id))
                            DraftType.Inventory -> navController.navigate(InventoryDestination(draft.id))
                        }
                    },
                    onDelete = draftsViewModel::delete
                )
            }
            composable<OrderDetailsDestination> { entry ->
                OrderDetailsScreen(
                    orderId = entry.toRoute<OrderDetailsDestination>().orderId,
                    onBack = navController::popBackStack,
                    onEditOrder = { navController.navigate(EditOrderDestination(it)) }
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
                    onBack = navController::popBackStack,
                    onStorageClick = { navController.navigate(ProductsRoute) { launchSingleTop = true } }
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
