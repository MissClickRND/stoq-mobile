package bob.colbaskin.stoq.feature.supplies.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import bob.colbaskin.stoq.common.core.UiState
import bob.colbaskin.stoq.common.designsystem.components.ErrorScreen
import bob.colbaskin.stoq.common.designsystem.components.LoadingScreen
import bob.colbaskin.stoq.common.designsystem.components.StoqCard
import bob.colbaskin.stoq.common.designsystem.components.StoqPrimaryButton
import bob.colbaskin.stoq.common.designsystem.theme.CustomTheme
import bob.colbaskin.stoq.common.designsystem.theme.StoqSpacing
import bob.colbaskin.stoq.feature.supplies.data.ai.AiSupplyRecognition
import bob.colbaskin.stoq.feature.supplies.data.ai.RemoteAiSupplyRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.io.File
import java.io.ByteArrayOutputStream
import java.util.UUID

@HiltViewModel
class SupplyScanViewModel @Inject constructor(
    private val repository: RemoteAiSupplyRepository,
    private val session: AiSupplySession
) : ViewModel() {
    var state by mutableStateOf<UiState<AiSupplyRecognition>?>(null)
        private set
    private var fileName: String? = null
    private var mimeType: String? = null
    private var bytes: ByteArray? = null

    fun recognize(name: String, mime: String, content: ByteArray) = viewModelScope.launch {
        fileName = name
        mimeType = mime
        bytes = content
        state = UiState.Loading
        state = repository.prepareSupply(name, mime, content)
    }

    fun retry() {
        val name = fileName ?: return
        val mime = mimeType ?: return
        val content = bytes ?: return
        recognize(name, mime, content)
    }

    fun retryOrChooseAnother() {
        if (bytes == null) state = null else retry()
    }

    fun reportUnreadableFile() {
        state = UiState.Error("Не удалось прочитать файл", "Выберите другое изображение УПД и повторите попытку.")
    }

    fun consumeResult(result: AiSupplyRecognition): String = session.put(result)
}

@Composable
fun SupplyScanScreen(
    onBack: () -> Unit,
    onRecognized: (String) -> Unit,
    viewModel: SupplyScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    fun processImage(uri: Uri) {
        val bytes = runCatching { context.contentResolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull()
        val jpegBytes = bytes?.takeIf { it.isNotEmpty() }?.let(::toJpeg)
        if (jpegBytes == null || jpegBytes.isEmpty()) {
            viewModel.reportUnreadableFile()
        } else {
            viewModel.recognize(
                name = "upd-${System.currentTimeMillis()}.jpg",
                mime = "image/jpeg",
                content = jpegBytes
            )
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        pendingCameraUri?.takeIf { success }?.let(::processImage)
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let(::processImage) }

    when (val state = viewModel.state) {
        UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(state.title, state.text, viewModel::retryOrChooseAnother, onBack)
        is UiState.Success -> LaunchedEffect(state.data) { onRecognized(viewModel.consumeResult(state.data)) }
        null -> SupplyScanContent(
            onBack = onBack,
            onCamera = {
                pendingCameraUri = createCameraUri(context)
                pendingCameraUri?.let(cameraLauncher::launch)
            },
            onGallery = { galleryLauncher.launch("image/*") }
        )
    }
}

@Composable
private fun SupplyScanContent(onBack: () -> Unit, onCamera: () -> Unit, onGallery: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(StoqSpacing.ScreenHorizontal),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Назад", Modifier.size(32.dp).clip(CircleShape).clickable(onClick = onBack).padding(5.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text("Сканирование УПД", style = MaterialTheme.typography.headlineMedium)
                Text("Сфотографируйте документ или выберите готовое фото", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        StoqCard(Modifier.fillMaxWidth()) {
            Box(Modifier.fillMaxWidth().height(190.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(Modifier.size(72.dp), shape = CircleShape, color = CustomTheme.colors.primary.copy(alpha = .16f)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Description, null, Modifier.size(36.dp), tint = CustomTheme.colors.primaryDark) }
                    }
                    Text("Загрузите фото УПД", modifier = Modifier.padding(top = 14.dp), style = MaterialTheme.typography.titleLarge)
                    Text("Распознаем поставщика, товары, количество и цены", modifier = Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        StoqPrimaryButton("Сфотографировать УПД", onCamera)
        OutlinedButton(onGallery, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Icon(Icons.Outlined.Collections, null, modifier = Modifier.size(20.dp))
            Text("Выбрать из галереи", modifier = Modifier.padding(start = 8.dp))
        }
        Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = CustomTheme.colors.surfaceSecondary) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Outlined.UploadFile, null, tint = CustomTheme.colors.primaryDark)
                Text("После распознавания проверьте номер поставки и сроки годности перед сохранением.", modifier = Modifier.padding(start = 10.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun createCameraUri(context: Context): Uri? = runCatching {
    val directory = File(context.cacheDir, "supply-scans").apply { mkdirs() }
    val file = File(directory, "upd-${UUID.randomUUID()}.jpg")
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}.getOrNull()

private fun toJpeg(bytes: ByteArray): ByteArray? = runCatching {
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
    ByteArrayOutputStream().use { output ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output)
        bitmap.recycle()
        output.toByteArray()
    }
}.getOrNull()
