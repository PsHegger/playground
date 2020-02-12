package pshegger.github.io.playground.mandelbrotrs

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import pshegger.github.io.playground.conway.ConwaySurfaceView
import pshegger.github.io.playground.utils.RectD
import kotlin.math.max

class MandelbrotRsSurfaceView(context: Context, atts: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    SurfaceView(context, atts, defStyleAttr, defStyleRes), SurfaceHolder.Callback {
    constructor(context: Context) : this(context, null, 0, 0)
    constructor(context: Context, atts: AttributeSet?) : this(context, atts, 0, 0)
    constructor(context: Context, atts: AttributeSet?, defStyleAttr: Int) : this(context, atts, defStyleAttr, 0)

    private var renderThread: RenderThread? = null
    private var mandelbrot: MandelbrotRs? = null
    private var renderRect: RectD =
        RectD(0.0, 0.0, 0.0, 0.0)
    private var zoomRect = RectF(0f, 0f, 0f, 0f)
    private var touching = false

    private val paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
    }

    init {
        holder.addCallback(this)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean = when (event.action) {
        MotionEvent.ACTION_DOWN -> {
            updateZoomRect(event)
            touching = true
            true
        }
        MotionEvent.ACTION_MOVE -> {
            updateZoomRect(event)
            true
        }
        MotionEvent.ACTION_CANCEL,
        MotionEvent.ACTION_UP -> {
            updateZoomRect(event)
            touching = false
            zoom()
            true
        }
        else -> super.onTouchEvent(event)
    }

    private fun zoom() {
        renderRect = RectD(
            renderRect.left + (zoomRect.left / width) * renderRect.width(),
            renderRect.top + (zoomRect.top / height) * renderRect.height(),
            renderRect.left + (zoomRect.right / width) * renderRect.width(),
            renderRect.top + (zoomRect.bottom / height) * renderRect.height()
        )
        mandelbrot?.render(renderRect)
    }

    private fun updateZoomRect(event: MotionEvent) {
        zoomRect = RectF(
            event.x - zoomRect.width() / 2,
            event.y - zoomRect.height() / 2,
            event.x + zoomRect.width() / 2,
            event.y + zoomRect.height() / 2
        )
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        val m = MandelbrotRs(width, height, context)
        val ratio = 2.0 / height

        val rectHeight = height * ratio
        val rectWidth = width * ratio
        renderRect =
            RectD(-rectWidth / 2, -rectHeight / 2, rectWidth / 2, rectHeight / 2)
        zoomRect = RectF(0f, 0f, width / 4f, height / 4f)

        mandelbrot = m
        m.render(renderRect)

        renderThread?.running = false
        renderThread = RenderThread()
        renderThread?.start()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        renderThread?.running = false
        mandelbrot?.destroy()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {}

    private inner class RenderThread(var running: Boolean = true) : Thread() {

        override fun run() {
            while (running) {
                val canvas = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    holder.lockHardwareCanvas()
                } else {
                    holder.lockCanvas()
                } ?: continue

                mandelbrot?.let { canvas.drawBitmap(it.buffer, 0f, 0f, paint) }
                if (touching) {
                    canvas.drawRect(zoomRect, paint)
                }

                holder.unlockCanvasAndPost(canvas)
            }
        }
    }
}
