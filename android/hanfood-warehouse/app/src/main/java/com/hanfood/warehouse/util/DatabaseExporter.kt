package com.hanfood.warehouse.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Ilova bazasini (SQLite fayl) zaxira nusxa sifatida eksport qilish uchun
 * yordamchi. Bulut yo'q — foydalanuvchi faylni o'zi istagan joyga
 * (Telegram, Google Drive, kompyuter ...) ulashishi mumkin.
 */
object DatabaseExporter {

    private const val DB_NAME = "hanfood_warehouse.db"

    fun exportDatabase(context: Context): Uri? {
        val dbFile = context.getDatabasePath(DB_NAME)
        if (!dbFile.exists()) return null

        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val outFile = File(dir, "hanfood_ombor_backup_$stamp.db")
        dbFile.copyTo(outFile, overwrite = true)

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
    }
}
