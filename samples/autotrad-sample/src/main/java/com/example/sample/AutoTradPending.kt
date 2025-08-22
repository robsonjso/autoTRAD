package com.example.sample

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object AutoTradPending {

    /** Lista todos os arquivos pending do sandbox do app. */
    fun listPendingFiles(context: Context): List<File> {
        val dir = context.filesDir
        return dir.listFiles { f -> f.name.startsWith("autotrad.pending.") && f.extension == "json" }
            ?.toList()
            ?: emptyList()
    }

    /** Cria um .zip com todos os pending no cache e retorna o arquivo. */
    fun zipPending(context: Context): File? {
        val files = listPendingFiles(context)
        if (files.isEmpty()) return null

        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val outFile = File(context.cacheDir, "autotrad-pending-$stamp.zip")

        ZipOutputStream(FileOutputStream(outFile)).use { zos ->
            files.forEach { f ->
                zos.putNextEntry(ZipEntry(f.name))
                f.inputStream().use { input -> input.copyTo(zos) }
                zos.closeEntry()
            }
        }
        return outFile
    }
}
