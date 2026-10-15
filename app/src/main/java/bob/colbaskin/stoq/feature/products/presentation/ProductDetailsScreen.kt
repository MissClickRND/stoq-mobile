package bob.colbaskin.stoq.feature.products.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.designsystem.components.ErrorScreen
import bob.colbaskin.stoq.common.designsystem.components.LoadingScreen
import bob.colbaskin.stoq.common.designsystem.components.PullToRefreshContainer
import bob.colbaskin.stoq.common.designsystem.components.StoqCard
import bob.colbaskin.stoq.common.designsystem.components.StoqMetric
import bob.colbaskin.stoq.common.designsystem.components.StoqMetricGrid
import bob.colbaskin.stoq.common.designsystem.components.StoqStatusPill
import bob.colbaskin.stoq.common.designsystem.components.StoqStatusTone
import bob.colbaskin.stoq.common.designsystem.theme.CustomTheme
import bob.colbaskin.stoq.feature.products.data.RemoteInventoryRepository
import bob.colbaskin.stoq.feature.products.data.StorageProduct
import bob.colbaskin.stoq.feature.products.data.realtime.InventorySocketClient
import bob.colbaskin.stoq.common.drafts.presentation.DraftSnackbar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val inventoryRepository: RemoteInventoryRepository,
    private val inventorySocket: InventorySocketClient
) : ViewModel() {
    private var currentProductId: Long? = null
    var socketUpdateMessage by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch(start = kotlinx.coroutines.CoroutineStart.UNDISPATCHED) {
            inventorySocket.changes.collect { change ->
                currentProductId?.let { id ->
                    if (change.event != "connected") {
                        socketUpdateMessage = change.reason?.takeIf(String::isNotBlank)?.let { "Склад обновлён: $it" }
                            ?: "Остатки на складе обновлены"
                    }
                    load(id)
                }
            }
        }
        inventorySocket.acquire()
    }

    fun consumeSocketUpdateMessage() { socketUpdateMessage = null }

    override fun onCleared() {
        inventorySocket.release()
        super.onCleared()
    }
    var actionState by mutableStateOf<UiState<Unit>?>(null)
        private set

    fun writeOff(productId: Long, batchId: Long, onSuccess: () -> Unit) = viewModelScope.launch {
        if (actionState == UiState.Loading) return@launch
        actionState = UiState.Loading
        actionState = inventoryRepository.writeOff(batchId)
        if (actionState is UiState.Success) {
            state = inventoryRepository.getInventoryByProduct(productId)
            onSuccess()
        }
    }
    var state by mutableStateOf<UiState<List<StorageProduct>>>(UiState.Loading)
        private set
    var isRefreshing by mutableStateOf(false)
        private set

    fun load(productId: Long) = viewModelScope.launch {
        currentProductId = productId
        val hasData = state is UiState.Success
        if (hasData) isRefreshing = true else state = UiState.Loading
        val result = inventoryRepository.getInventoryByProduct(productId)
        if (result is UiState.Success || !hasData) state = result
        isRefreshing = false
    }
}

@Composable
fun ProductDetailsScreen(
    productId: Long,
    onBack: () -> Unit,
    onSupplyClick: (String) -> Unit,
    viewModel: ProductDetailsViewModel = hiltViewModel()
) {
    LaunchedEffect(productId) { viewModel.load(productId) }
    val products = (viewModel.state as? UiState.Success)?.data.orEmpty()
    val product = products.firstOrNull()
    val totalQuantity = products.sumOf(StorageProduct::availableQuantity)
    val unit = product?.unit.orEmpty()
    var batchToWriteOff by remember { mutableStateOf<StorageProduct?>(null) }

    PullToRefreshContainer(viewModel.isRefreshing, { viewModel.load(productId) }, Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(CustomTheme.colors.background),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onBack),
                        shape = CircleShape,
                        color = CustomTheme.colors.surfaceSecondary
                    ) { Box(contentAlignment = Alignment.Center) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Назад") } }
                    Column(Modifier.padding(start = 12.dp)) {
                        Text(product?.name ?: "Продукт", style = MaterialTheme.typography.headlineMedium)
                        Text("ID $productId", style = MaterialTheme.typography.bodyMedium, color = CustomTheme.colors.textSecondary)
                    }
                }
            }
            item {
                StoqCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(56.dp), shape = MaterialTheme.shapes.medium, color = CustomTheme.colors.primary.copy(alpha = .14f)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Inventory2, null, tint = CustomTheme.colors.primaryDark) }
                        }
                        Column(Modifier.padding(start = 12.dp)) {
                            Text("Всего на складе", style = MaterialTheme.typography.bodyMedium, color = CustomTheme.colors.textSecondary)
                            Text("${totalQuantity.toDisplayValue()} $unit", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("${products.size} ${products.size.pluralBatches()}", style = MaterialTheme.typography.bodySmall, color = CustomTheme.colors.textSecondary)
                        }
                    }
                }
            }
            item {
                StoqMetricGrid(
                    metrics = listOf(
                        StoqMetric("Артикул", productId.toString(), icon = Icons.Outlined.Inventory2, tint = CustomTheme.colors.primaryDark),
                        StoqMetric("Единица", unit.ifBlank { "мог" }, icon = Icons.Outlined.Inventory2, tint = CustomTheme.colors.success),
                        StoqMetric("Поставок", products.mapNotNull(StorageProduct::supplyId).distinct().size.toString(), icon = Icons.Outlined.LocalShipping, tint = CustomTheme.colors.primaryDark),
                        StoqMetric("Партий", products.size.toString(), icon = Icons.AutoMirrored.Outlined.ReceiptLong, tint = CustomTheme.colors.success)
                    )
                )
            }
            item {
                Column {
                    Text("Партии продукта", style = MaterialTheme.typography.titleLarge)
                    Text("Остатки одного продукта из разных поставок", style = MaterialTheme.typography.bodyMedium, color = CustomTheme.colors.textSecondary)
                }
            }
            items(products, key = StorageProduct::batchId) { batch ->
                ProductBatchCard(batch, onSupplyClick, onWriteOff = { batchToWriteOff = batch })
            }
        }
    }
    when (val state = viewModel.state) {
        UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(state.title, state.text, onRetry = { viewModel.load(productId) }, onBack = onBack)
        is UiState.Success -> Unit
    }
    batchToWriteOff?.let { batch ->
        AlertDialog(
            onDismissRequest = { if (viewModel.actionState != UiState.Loading) batchToWriteOff = null },
            title = { Text("Списать остаток партии?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Весь доступный остаток «${batch.name}» из партии № ${batch.batchId} (${batch.currentStock}) будет списан.")
                    (viewModel.actionState as? UiState.Error)?.let { Text(it.text, color = CustomTheme.colors.error) }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.writeOff(productId, batch.batchId) { batchToWriteOff = null } },
                    enabled = viewModel.actionState != UiState.Loading
                ) { Text("Списать", color = CustomTheme.colors.error) }
            },
            dismissButton = {
                TextButton(onClick = { batchToWriteOff = null }, enabled = viewModel.actionState != UiState.Loading) { Text("Отмена") }
            }
        )
    }
    DraftSnackbar(viewModel.socketUpdateMessage, viewModel::consumeSocketUpdateMessage)
}

@Composable
private fun ProductBatchCard(batch: StorageProduct, onSupplyClick: (String) -> Unit, onWriteOff: () -> Unit) {
    val tone = when (batch.status?.uppercase()) {
        "ACTIVE" -> StoqStatusTone.Success
        "RUNNINGOUT" -> StoqStatusTone.Warning
        "EXPIRED", "DEPLETED" -> StoqStatusTone.Error
        else -> StoqStatusTone.Info
    }
    val status = when (batch.status?.uppercase()) {
        "ACTIVE" -> "В норме"
        "RUNNINGOUT" -> "Заканчивается"
        "EXPIRED" -> "Истёк срок"
        "DEPLETED" -> "Закончился"
        else -> "мог"
    }
    StoqCard(
        modifier = batch.supplyId?.let { Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).clickable { onSupplyClick(it.toString()) } }
            ?: Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Партия № ${batch.batchId.toString().padStart(5, '0')}", style = MaterialTheme.typography.titleMedium)
                Text("Поставка № ${batch.supplyId ?: "мог"}", style = MaterialTheme.typography.bodySmall, color = CustomTheme.colors.textSecondary)
            }
            StoqStatusPill(status, tone)
            IconButton(onClick = onWriteOff, enabled = batch.availableQuantity > 0) {
                Icon(Icons.Outlined.DeleteOutline, "Списать остаток партии", tint = CustomTheme.colors.error)
            }
        }
        Spacer(Modifier.padding(top = 7.dp))
        Row(Modifier.fillMaxWidth()) {
            ProductBatchFact("Остаток", batch.currentStock, Modifier.weight(1f))
            ProductBatchFact("Годен до", batch.expiryDate ?: "мог", Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth().padding(top = 12.dp)) {
            ProductBatchFact("Цена", batch.price?.toDisplayValue()?.plus(" ₽") ?: "мог", Modifier.weight(1f))
            ProductBatchFact("Стоимость", batch.totalPrice?.toDisplayValue()?.plus(" ₽") ?: "мог", Modifier.weight(1f))
        }
    }
}

@Composable
private fun ProductBatchFact(label: String, value: String, modifier: Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = CustomTheme.colors.textSecondary)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
    }
}

private fun Double.toDisplayValue() = if (this % 1.0 == 0.0) toLong().toString() else toString()
private fun Int.pluralBatches() = when {
    this % 10 == 1 && this % 100 != 11 -> "партия"
    this % 10 in 2..4 && this % 100 !in 12..14 -> "партии"
    else -> "партий"
}
