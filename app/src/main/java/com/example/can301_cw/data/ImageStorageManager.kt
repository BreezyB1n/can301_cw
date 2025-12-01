package com.example.can301_cw.data

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ImageStorageManager(private val context: Context) {
    private val imageDir = File(context.filesDir, "memo_images")

    init {
        if (!imageDir.exists()) {
            imageDir.mkdirs()
        }
    }

    fun saveImage(bytes: ByteArray): String {
        val fileName = "${UUID.randomUUID()}.jpg"
        val file = File(imageDir, fileName)
        FileOutputStream(file).use { it.write(bytes) }
        return file.absolutePath
    }

    fun loadImage(path: String): ByteArray? {
        val file = File(path)
        return if (file.exists()) {
            file.readBytes()
        } else {
            null
        }
    }
    
    fun deleteImage(path: String) {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
    }
}
