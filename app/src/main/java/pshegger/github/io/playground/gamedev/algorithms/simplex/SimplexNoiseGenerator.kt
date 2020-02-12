package pshegger.github.io.playground.gamedev.algorithms.simplex

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.util.*

class SimplexNoiseGenerator {
    val canGenerateMore: Boolean
        get() = pos < width * height
    val bitmap: Bitmap
        get() = _bmp

    private var simplex = SimplexNoise(100, 0.1, 0)
    private var width: Int = 0
    private var height: Int = 0
    private var pos: Int = 0
    private var _bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    private var canvas: Canvas = Canvas()

    private val paint = Paint()

    fun reset(width: Int, height: Int) {
        this.width = width
        this.height = height

        _bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        canvas = Canvas(_bmp)
        pos = 0
        simplex = SimplexNoise(100, 0.5, Random().nextInt())
    }

    fun generateNextPoint() {
        val i = pos % width
        val j = pos / width

        val x = (i * (500.0 / width)).toInt()
        val y = (j * (500.0 / height)).toInt()
        val v = 0.5 * (1 + simplex.getNoise(x, y))
        val value = Math.round(v * 255).toInt()
        paint.color = Color.rgb(value, value, value)
        canvas.drawPoint(i.toFloat(), j.toFloat(), paint)

        pos += 1
    }

    fun generateAll() {
        while (canGenerateMore) {
            generateNextPoint()
        }
    }
}
