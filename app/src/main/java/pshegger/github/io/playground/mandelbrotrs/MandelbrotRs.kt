package pshegger.github.io.playground.mandelbrotrs

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.util.Log
import pshegger.github.io.playground.utils.RectD

class MandelbrotRs(width: Int, height: Int, context: Context) {

    companion object {
        private const val MAX_ITER = 128
    }

    val buffer: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    private val rs = RenderScript.create(context)
    private val bufferAllocation = Allocation.createFromBitmap(rs, buffer)
    private val script = ScriptC_mandelbrot(rs).apply {
        _width = width
        _height = height
        _maxIterations = MAX_ITER
    }

    fun render(rect: RectD) {
        script.apply {
            _left = rect.left
            _top = rect.top
            _right = rect.right
            _bottom = rect.bottom
        }

        script.forEach_mandelbrot(bufferAllocation, bufferAllocation)
        bufferAllocation.copyTo(buffer)
    }

    fun destroy() {
        bufferAllocation.destroy()
        script.destroy()
        rs.destroy()
    }
}
