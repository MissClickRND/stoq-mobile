package bob.colbaskin.stoq.feature.inventory.presentation

import android.content.ContentResolver
import android.net.Uri
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private val reportDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm", Locale.forLanguageTag("ru"))
private val reportTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("ru"))

fun InventoryState.reportDateTime(): String = completedAt?.format(reportDateFormatter).orEmpty()

fun InventoryState.reportTime(): String = completedAt?.format(reportTimeFormatter).orEmpty()

fun saveInventoryXlsx(contentResolver: ContentResolver, uri: Uri, state: InventoryState) {
    contentResolver.openOutputStream(uri)?.use { output ->
        ZipOutputStream(output).use { archive ->
            archive.writeEntry("[Content_Types].xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>""")
            archive.writeEntry("_rels/.rels", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>""")
            archive.writeEntry("xl/workbook.xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="Инвентаризация" sheetId="1" r:id="rId1"/></sheets></workbook>""")
            archive.writeEntry("xl/_rels/workbook.xml.rels", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/></Relationships>""")
            archive.writeEntry("xl/worksheets/sheet1.xml", state.toWorksheetXml())
        }
    }
}

private fun InventoryState.toWorksheetXml(): String = buildString {
    val rows = buildList {
        add(listOf("Отчёт инвентаризации"))
        add(listOf("Дата проведения", reportDateTime()))
        add(emptyList())
        add(listOf("Товар", "Артикул", "Было", "Стало", "Статус"))
        changedLines.forEach { line ->
            add(listOf(line.product.name, line.product.article, "${line.product.systemQuantity.toReportQuantity()} ${line.product.unit}", "${line.actualQuantity.toReportQuantity()} ${line.product.unit}", if (line.isDeleted) "Удалено" else "Обновлено"))
        }
    }
    append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>")
    rows.forEachIndexed { rowIndex, cells ->
        append("<row r=\"${rowIndex + 1}\">")
        cells.forEachIndexed { cellIndex, value ->
            val reference = "${('A'.code + cellIndex).toChar()}${rowIndex + 1}"
            append("<c r=\"$reference\" t=\"inlineStr\"><is><t>${value.xmlEscaped()}</t></is></c>")
        }
        append("</row>")
    }
    append("</sheetData></worksheet>")
}

private fun String.xmlEscaped(): String = replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;")

private fun Double.toReportQuantity(): String = if (this % 1.0 == 0.0) toLong().toString() else toString()

private fun ZipOutputStream.writeEntry(path: String, content: String) {
    putNextEntry(ZipEntry(path))
    write(content.toByteArray(Charsets.UTF_8))
    closeEntry()
}
