package com.hanfood.warehouse.util

import android.content.Context
import android.net.Uri
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.util.zip.ZipInputStream

/**
 * One parsed data row from an imported product spreadsheet. Column order
 * (first row is treated as a header and skipped):
 *   A: name (required)   B: barcode   C: unit   D: quantity
 *   E: min quantity      F: purchase price   G: sell price
 * Every column but name is optional and falls back to a sensible default.
 */
data class ExcelProductRow(
    val name: String,
    val barcode: String?,
    val unit: String,
    val quantity: Double,
    val minQuantity: Double,
    val purchasePrice: Double,
    val sellPrice: Double
)

/**
 * Reads a warehouse product spreadsheet (.xlsx, the *first* sheet only) the
 * user picked from their device and turns it into [ExcelProductRow]s.
 *
 * .xlsx is just a zip of XML parts, so this parses it directly with
 * Android's built-in `android.util.Xml` pull parser instead of pulling in a
 * general-purpose library — Apache POI doesn't run reliably on Android, and
 * lighter streaming readers (fastexcel-reader) turned out to depend on
 * javax.xml.stream (StAX), which Android's runtime doesn't provide at all
 * and R8 refuses to package around. This needs only two well-known zip
 * entries: `xl/sharedStrings.xml` and `xl/worksheets/sheet1.xml`.
 */
object ExcelImporter {

    private const val SHARED_STRINGS_ENTRY = "xl/sharedStrings.xml"
    private const val FIRST_SHEET_ENTRY = "xl/worksheets/sheet1.xml"

    /** Returns null if the file couldn't be opened/parsed as a spreadsheet at all. */
    fun parse(context: Context, uri: Uri): List<ExcelProductRow>? {
        return try {
            val sharedStrings = openZipEntry(context, uri, SHARED_STRINGS_ENTRY)?.use { parseSharedStrings(it) } ?: emptyList()
            val rows = openZipEntry(context, uri, FIRST_SHEET_ENTRY)?.use { parseSheetRows(it, sharedStrings) }
                ?: return null
            rows.drop(1).mapNotNull { row -> toProductRow(row) } // drop(1): header row
        } catch (e: Exception) {
            null
        }
    }

    /** Opens a fresh stream from [uri] and positions it at the start of [entryName]'s content, or null if absent. */
    private fun openZipEntry(context: Context, uri: Uri, entryName: String): InputStream? {
        val raw = context.contentResolver.openInputStream(uri) ?: return null
        val zip = ZipInputStream(raw)
        var entry = zip.nextEntry
        while (entry != null) {
            if (entry.name == entryName) return zip
            entry = zip.nextEntry
        }
        zip.close()
        return null
    }

    private fun parseSharedStrings(input: InputStream): List<String> {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(input, "UTF-8")

        val result = mutableListOf<String>()
        var current: StringBuilder? = null
        var textDepth = 0

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "si" -> current = StringBuilder()
                        "t" -> textDepth++
                    }
                }
                XmlPullParser.TEXT -> {
                    if (textDepth > 0 && current != null) current.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "t" -> textDepth = (textDepth - 1).coerceAtLeast(0)
                        "si" -> {
                            result.add(current?.toString().orEmpty())
                            current = null
                        }
                    }
                }
            }
            event = parser.next()
        }
        return result
    }

    /** A raw parsed row: column index (0-based, from the "A1"-style cell reference) -> cell text. */
    private fun parseSheetRows(input: InputStream, sharedStrings: List<String>): List<Map<Int, String>> {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(input, "UTF-8")

        val rows = mutableListOf<Map<Int, String>>()
        var currentRow: MutableMap<Int, String>? = null
        var cellColumn = -1
        var cellType: String? = null
        var cellValue: StringBuilder? = null
        var inValueTag = false

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "row" -> currentRow = mutableMapOf()
                        "c" -> {
                            cellColumn = columnIndexOf(parser.getAttributeValue(null, "r"))
                            cellType = parser.getAttributeValue(null, "t")
                            cellValue = null
                        }
                        "v", "t" -> {
                            inValueTag = true
                            cellValue = StringBuilder()
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inValueTag) cellValue?.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "v", "t" -> inValueTag = false
                        "c" -> {
                            if (cellColumn >= 0) {
                                val raw = cellValue?.toString().orEmpty()
                                val resolved = if (cellType == "s") {
                                    raw.toIntOrNull()?.let { sharedStrings.getOrNull(it) } ?: ""
                                } else {
                                    raw
                                }
                                currentRow?.put(cellColumn, resolved)
                            }
                            cellColumn = -1
                        }
                        "row" -> {
                            currentRow?.let { rows.add(it) }
                            currentRow = null
                        }
                    }
                }
            }
            event = parser.next()
        }
        return rows
    }

    /** "B7" -> 1 (zero-based column index). Ignores the row-number suffix. */
    private fun columnIndexOf(cellReference: String?): Int {
        if (cellReference.isNullOrBlank()) return -1
        var index = 0
        for (ch in cellReference) {
            if (!ch.isLetter()) break
            index = index * 26 + (ch.uppercaseChar() - 'A' + 1)
        }
        return index - 1
    }

    private fun toProductRow(row: Map<Int, String>): ExcelProductRow? {
        val name = row[0]?.trim().orEmpty()
        if (name.isBlank()) return null
        return ExcelProductRow(
            name = name,
            barcode = row[1]?.trim()?.ifBlank { null },
            unit = row[2]?.trim()?.ifBlank { null } ?: "dona",
            quantity = row[3]?.toNumberOrZero() ?: 0.0,
            minQuantity = row[4]?.toNumberOrZero() ?: 0.0,
            purchasePrice = row[5]?.toNumberOrZero() ?: 0.0,
            sellPrice = row[6]?.toNumberOrZero() ?: 0.0
        )
    }

    private fun String.toNumberOrZero(): Double = trim().replace(",", ".").toDoubleOrNull() ?: 0.0
}
