package pshegger.github.io.playground.mandelbrot

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.applyCanvas
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import pshegger.github.io.playground.utils.RectD
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.math.sqrt

class Mandelbrot(private val width: Int, private val height: Int, var running: Boolean = true) {

    companion object {
        private const val MAX_ITER = 128
    }

    val buffer: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    fun render(rect: RectD) {
        buffer.applyCanvas { drawColor(Color.BLACK) }

        thread {
            calculate(rect)
        }
    }

    private fun calculate(rect: RectD) {
        val numThreads = Runtime.getRuntime().availableProcessors() - 2
        val context = Executors.newFixedThreadPool(numThreads).asCoroutineDispatcher()

        var h = sqrt(numThreads.toDouble()).toInt()
        while (numThreads % h != 0) h--
        val w = numThreads / h

        val segWidth = width / w
        val segHeight = height / h

        val canvas = Canvas(buffer)

        runBlocking(context) {
            for (mod in (0 until numThreads)) {
                val segX = mod % w
                val segY = mod / w
                val segXStart = segWidth * segX
                val segXEnd = segWidth * (segX + 1)
                val segYStart = segHeight * segY
                val segYEnd = segHeight * (segY + 1)
                val paint = Paint()

                launch {
                    for (y in (segYStart until segYEnd)) {
                        for (x in (segXStart until segXEnd)) {
                            val result = calculatePoint(x, y, rect)

                            paint.color = result
                            canvas.drawPoint(x.toFloat(), y.toFloat(), paint)
                        }
                    }
                }
            }
        }
    }

    private fun calculatePoint(x: Int, y: Int, rect: RectD): Int {
        val cr = rect.left + (rect.right - rect.left) * (x / width.toDouble())
        val ci = rect.top + (rect.bottom - rect.top) * (y / height.toDouble())
        var zr = 0.0
        var zi = 0.0

        var i = 0
        while (i < MAX_ITER && zr * zr + zi * zi < 4) {
            val nzr = zr * zr - zi * zi + cr
            val nzi = 2 * zr * zi + ci
            zr = nzr
            zi = nzi
            i++
        }

        val hue = 360 * i.toFloat() / MAX_ITER
        val sat = 1f
        val value = if (i < MAX_ITER) 1f else 0f
        val color = Color.HSVToColor(floatArrayOf(hue, sat, value))
        return color
    }
}
