package com.example.skinscan.util

import android.graphics.BitmapFactory

/** Cheap width/height probing without decoding full pixels. */
object ImageSizes {

    /** width / height, or null if the file cannot be read. */
    fun aspectRatio(path: String): Float? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, options)
        return if (options.outWidth > 0 && options.outHeight > 0) {
            options.outWidth.toFloat() / options.outHeight.toFloat()
        } else {
            null
        }
    }
}
