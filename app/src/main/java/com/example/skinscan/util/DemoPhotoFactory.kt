package com.example.skinscan.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/**
 * Generates a synthetic "skin" photo. This stands in for the camera while the
 * CameraX pipeline is not wired up yet, so the whole map -> capture -> annotate
 * -> compare flow is exercisable end to end. Swap this out for a real capture
 * later; the rest of the app does not care where the bitmap came from.
 */
object DemoPhotoFactory {

    fun create(
        width: Int = 1080,
        height: Int = 1080,
        seed: Long = System.currentTimeMillis(),
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val base = Paint(Paint.DITHER_FLAG).apply {
            shader = RadialGradient(
                width / 2f,
                height / 2f,
                width * 0.75f,
                intArrayOf(0xFFF3C9A8.toInt(), 0xFFE0A87E.toInt(), 0xFFC98C63.toInt()),
                floatArrayOf(0f, 0.6f, 1f),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), base)

        val random = Random(seed)
        val mole = Paint(Paint.ANTI_ALIAS_FLAG)
        repeat(6 + random.nextInt(6)) {
            mole.color = 0xFF3B2417.toInt()
            mole.alpha = 180 + random.nextInt(75)
            val mx = random.nextFloat() * width
            val my = random.nextFloat() * height
            val r = width * 0.006f + random.nextFloat() * width * 0.02f
            canvas.drawCircle(mx, my, r, mole)
        }

        val stamp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x66FFFFFF.toInt()
            textSize = width * 0.03f
        }
        val label = "SkinScan demo - " +
            java.text.SimpleDateFormat("d MMM yyyy HH:mm", Locale.getDefault()).format(Date(seed))
        canvas.drawText(label, width * 0.05f, height * 0.95f, stamp)

        return bitmap
    }
}
