package com.example.skinscan.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

/**
 * Stores capture files inside the app's private files directory, so no storage
 * permission is needed and nothing leaves the device.
 */
class PhotoStore(context: Context) {

    private val appContext = context.applicationContext

    private val root: File
        get() = File(appContext.filesDir, "photos").apply { if (!exists()) mkdirs() }

    fun fileFor(sessionId: Long, regionId: String): File {
        val dir = sessionDir(sessionId).apply { if (!exists()) mkdirs() }
        return File(dir, "${regionId}_${System.currentTimeMillis()}.jpg")
    }

    fun sessionDir(sessionId: Long): File = File(root, "session_$sessionId")

    fun write(bitmap: Bitmap, file: File): Boolean {
        file.parentFile?.mkdirs()
        return runCatching {
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
        }.getOrDefault(false)
    }

    fun read(path: String): Bitmap? = runCatching {
        BitmapFactory.decodeFile(path)
    }.getOrNull()

    fun delete(path: String) {
        runCatching { File(path).delete() }
    }

    fun delete(file: File) {
        runCatching { file.deleteRecursively() }
    }
}
