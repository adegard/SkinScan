package com.example.skinscan.util

import android.graphics.Bitmap
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * On-device, dependency-free image fingerprinting.
 *
 * We use a classic 8x8 average-hash ("aHash"): downscale to 8x8 grayscale,
 * threshold each pixel against the mean and pack the result into a 64-bit
 * Long. Two patches whose hashes differ in few bits look similar; the Hamming
 * distance gives us a cheap "has this area changed?" signal without any cloud
 * service or ML model.
 */
object ImageSignature {

    const val BITS = 64
    private const val GRID = 8

    fun averageHash(bitmap: Bitmap, size: Int = GRID): Long {
        val small = if (bitmap.width == size && bitmap.height == size) {
            bitmap
        } else {
            Bitmap.createScaledBitmap(bitmap, size, size, true)
        }
        val pixels = IntArray(size * size)
        small.getPixels(pixels, 0, size, 0, 0, size, size)
        if (small !== bitmap) small.recycle()

        val gray = DoubleArray(pixels.size)
        var sum = 0.0
        for (i in pixels.indices) {
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            val v = 0.299 * r + 0.587 * g + 0.114 * b
            gray[i] = v
            sum += v
        }
        val avg = sum / pixels.size

        var hash = 0L
        for (i in gray.indices) {
            if (gray[i] >= avg) hash = hash or (1L shl i)
        }
        return hash
    }

    fun hammingDistance(a: Long, b: Long): Int = java.lang.Long.bitCount(a xor b)

    /** 1.0 = identical signature, 0.0 = maximally different. */
    fun similarity(a: Long, b: Long): Float = 1f - hammingDistance(a, b).toFloat() / BITS

    /** Square patch centred on the normalised point ([x], [y]), sized by [radius]. */
    fun crop(bitmap: Bitmap, x: Float, y: Float, radius: Float): Bitmap {
        val cx = (x.coerceIn(0f, 1f) * bitmap.width).roundToInt()
        val cy = (y.coerceIn(0f, 1f) * bitmap.height).roundToInt()
        val half = (radius.coerceIn(0.01f, 0.5f) * min(bitmap.width, bitmap.height))
            .roundToInt()
            .coerceAtLeast(4)
        val left = (cx - half).coerceIn(0, bitmap.width - 1)
        val top = (cy - half).coerceIn(0, bitmap.height - 1)
        val size = (half * 2)
            .coerceAtMost(min(bitmap.width - left, bitmap.height - top))
            .coerceAtLeast(1)
        return Bitmap.createBitmap(bitmap, left, top, size, size)
    }
}
