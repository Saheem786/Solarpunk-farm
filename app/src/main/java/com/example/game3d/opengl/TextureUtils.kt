package com.example.game3d.opengl

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.util.Random

object TextureUtils {

    fun generateProceduralTexture(type: String): Bitmap {
        val size = 512
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val random = Random()

        when (type) {
            "grass" -> {
                canvas.drawColor(Color.parseColor("#4CAF50"))
                val paint = Paint()
                for (i in 0 until 5000) {
                    val x = random.nextInt(size).toFloat()
                    val y = random.nextInt(size).toFloat()
                    val color = if (random.nextBoolean()) "#388E3C" else "#66BB6A"
                    paint.color = Color.parseColor(color)
                    canvas.drawRect(x, y, x + 4, y + 12, paint)
                }
            }
            "wood" -> {
                canvas.drawColor(Color.parseColor("#795548"))
                val paint = Paint()
                paint.color = Color.parseColor("#5D4037")
                paint.strokeWidth = 2f
                for (i in 0 until size step 40) {
                    canvas.drawLine(0f, i.toFloat(), size.toFloat(), i.toFloat(), paint)
                }
                for (i in 0 until 1000) {
                    val x = random.nextInt(size).toFloat()
                    val y = random.nextInt(size).toFloat()
                    paint.color = Color.parseColor("#4E342E")
                    canvas.drawCircle(x, y, random.nextFloat() * 5, paint)
                }
            }
            "stone" -> {
                canvas.drawColor(Color.parseColor("#9E9E9E"))
                val paint = Paint()
                for (i in 0 until 200) {
                    val x = random.nextInt(size).toFloat()
                    val y = random.nextInt(size).toFloat()
                    val r = 20f + random.nextFloat() * 40f
                    paint.color = Color.parseColor(if (random.nextBoolean()) "#757575" else "#BDBDBD")
                    canvas.drawCircle(x, y, r, paint)
                }
            }
            "leaves" -> {
                canvas.drawColor(Color.parseColor("#2E7D32"))
                val paint = Paint()
                for (i in 0 until 3000) {
                    val x = random.nextInt(size).toFloat()
                    val y = random.nextInt(size).toFloat()
                    paint.color = Color.parseColor(if (random.nextBoolean()) "#1B5E20" else "#43A047")
                    canvas.drawOval(x, y, x + 20, y + 10, paint)
                }
            }
            "dirt" -> {
                canvas.drawColor(Color.parseColor("#8D6E63"))
                val paint = Paint()
                for (i in 0 until 5000) {
                    val x = random.nextInt(size).toFloat()
                    val y = random.nextInt(size).toFloat()
                    paint.color = Color.parseColor(if (random.nextBoolean()) "#6D4C41" else "#A1887F")
                    canvas.drawPoint(x, y, paint)
                }
            }
            "water" -> {
                canvas.drawColor(Color.parseColor("#03A9F4"))
                val paint = Paint()
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f
                for (i in 0 until 50) {
                    val x = random.nextInt(size).toFloat()
                    val y = random.nextInt(size).toFloat()
                    val r = 10f + random.nextFloat() * 50f
                    paint.color = Color.parseColor("#B3E5FC")
                    canvas.drawCircle(x, y, r, paint)
                }
            }
        }
        return bitmap
    }
}
