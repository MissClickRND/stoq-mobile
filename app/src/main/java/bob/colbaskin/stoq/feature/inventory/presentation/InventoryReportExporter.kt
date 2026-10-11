package bob.colbaskin.stoq.feature.inventory.presentation

import android.content.ContentResolver
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import java.io.OutputStreamWriter
import java.time.format.DateTimeFormatter
import java.util.Locale

private val reportDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm", Locale.forLanguageTag("ru"))
private val reportTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("ru"))

fun InventoryState.reportDateTime(): String = completedAt?.format(reportDateFormatter).orEmpty()

fun InventoryState.reportTime(): String = completedAt?.format(reportTimeFormatter).orEmpty()

fun saveInventoryCsv(contentResolver: ContentResolver, uri: Uri, state: InventoryState) {
    contentResolver.openOutputStream(uri)?.use { output ->
        OutputStreamWriter(output, Charsets.UTF_8).use { writer ->
            writer.write("\uFEFFОтчёт инвентаризации\n")
            writer.write("Дата проведения;${state.reportDateTime()}\n\n")
            writer.write("Товар;Артикул;Было;Стало;Статус\n")
            state.changedLines.forEach { line ->
                val status = if (line.isDeleted) "Удалено" else "Обновлено"
                writer.write("${line.product.name};${line.product.article};${line.product.systemQuantity} ${line.product.unit};${line.actualQuantity} ${line.product.unit};$status\n")
            }
        }
    }
}

fun saveInventoryPdf(contentResolver: ContentResolver, uri: Uri, state: InventoryState) {
    val document = PdfDocument()
    try {
        val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 20f; isFakeBoldText = true }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 12f }
        var y = 52f
        canvas.drawText("Отчёт инвентаризации", 40f, y, titlePaint)
        y += 28f
        canvas.drawText("Дата проведения: ${state.reportDateTime()}", 40f, y, textPaint)
        y += 32f
        canvas.drawText("Товар", 40f, y, textPaint)
        canvas.drawText("Было", 290f, y, textPaint)
        canvas.drawText("Стало", 370f, y, textPaint)
        canvas.drawText("Статус", 455f, y, textPaint)
        y += 18f
        state.changedLines.forEach { line ->
            canvas.drawText(line.product.name, 40f, y, textPaint)
            canvas.drawText("${line.product.systemQuantity} ${line.product.unit}", 290f, y, textPaint)
            canvas.drawText("${line.actualQuantity} ${line.product.unit}", 370f, y, textPaint)
            canvas.drawText(if (line.isDeleted) "Удалено" else "Обновлено", 455f, y, textPaint)
            y += 22f
        }
        document.finishPage(page)
        contentResolver.openOutputStream(uri)?.use(document::writeTo)
    } finally {
        document.close()
    }
}
