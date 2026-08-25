package com.hanfood.warehouse.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

/**
 * Copies user-picked photos/files (from the system picker, as `content://`
 * Uris that are not guaranteed to stay readable across app restarts) into
 * the app's own internal storage, so product photos and invoice
 * attachments keep working offline and permanently. Room only ever stores
 * the resulting absolute file path.
 */
object FileStorage {

    private const val PRODUCT_IMAGES_DIR = "product_images"
    private const val INVOICE_ATTACHMENTS_DIR = "invoice_attachments"

    fun saveProductImage(context: Context, source: Uri): String? = copyToInternalStorage(context, source, PRODUCT_IMAGES_DIR)

    fun saveInvoiceAttachment(context: Context, source: Uri): String? = copyToInternalStorage(context, source, INVOICE_ATTACHMENTS_DIR)

    private fun copyToInternalStorage(context: Context, source: Uri, subDir: String): String? {
        return try {
            val extension = extensionFor(context, source)
            val dir = File(context.filesDir, subDir).apply { mkdirs() }
            val file = File(dir, "${UUID.randomUUID()}.$extension")
            context.contentResolver.openInputStream(source)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private fun extensionFor(context: Context, uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri)
        return when {
            mimeType?.contains("png") == true -> "png"
            mimeType?.contains("webp") == true -> "webp"
            mimeType?.contains("pdf") == true -> "pdf"
            else -> "jpg"
        }
    }

    /** Content:// Uri for sharing/opening a stored file with other apps (via FileProvider). */
    fun contentUriFor(context: Context, absolutePath: String): Uri {
        val file = File(absolutePath)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun deleteQuietly(absolutePath: String?) {
        if (absolutePath.isNullOrBlank()) return
        runCatching { File(absolutePath).delete() }
    }
}
