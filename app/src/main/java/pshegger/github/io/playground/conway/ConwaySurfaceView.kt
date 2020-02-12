package pshegger.github.io.playground.conway

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class ConwaySurfaceView(context: Context, attrs: AttributeSet?, defStyleAttrs: Int, defStyleRes: Int) :
    SurfaceView(context, attrs, defStyleAttrs, defStyleRes), SurfaceHolder.Callback {
    companion object {
        private const val CELL_SIZE = 30
        private const val BIRTH_CHANCE = 0.4f
        private const val UPDATE_INTERVAL = 60L
        private const val COLOR_CHANGE_SIZE = 8
    }

    constructor(context: Context) : this(context, null, 0, 0)
    constructor(context: Context, atts: AttributeSet?) : this(context, atts, 0, 0)
    constructor(context: Context, atts: AttributeSet?, defStyleAttr: Int) : this(context, atts, defStyleAttr, 0)

    private var renderThread = RendetThread(false)
    private var conway = Conway(0, 0)

    private val palettes = (1..8).map { ColorPalette.load(context, it) }
    private var paletteIndex = 0

    private val paint = Paint().apply {
        style = Paint.Style.FILL
    }

    init {
        holder.addCallback(this)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean = when (event.action) {
        MotionEvent.ACTION_DOWN -> true
        MotionEvent.ACTION_UP,
        MotionEvent.ACTION_CANCEL -> {
            if (event.x > width - COLOR_CHANGE_SIZE * CELL_SIZE && event.y > height - COLOR_CHANGE_SIZE * CELL_SIZE) {
                paletteIndex++
                if (paletteIndex >= palettes.size) paletteIndex = 0
            }
            if (event.x < COLOR_CHANGE_SIZE * CELL_SIZE && event.y > height - COLOR_CHANGE_SIZE * CELL_SIZE) {
                paletteIndex--
                if (paletteIndex < 0) paletteIndex = palettes.size - 1
            }
            true
        }
        else -> super.onTouchEvent(event)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        conway = Conway(width / CELL_SIZE, height / CELL_SIZE, BIRTH_CHANCE)
        conway.reset()

        renderThread.running = false
        renderThread = RendetThread()
        renderThread.start()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        renderThread.running = false
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {}

    private inner class RendetThread(var running: Boolean = true) : Thread() {

        override fun run() {
            var lastUpdate = System.currentTimeMillis()

            while (running) {
                val canvas = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    holder.lockHardwareCanvas()
                } else {
                    holder.lockCanvas()
                } ?: continue

                if (System.currentTimeMillis() - lastUpdate > UPDATE_INTERVAL) {
                    conway.nextStep()
                    lastUpdate = System.currentTimeMillis()
                }

                canvas.drawColor(palettes[paletteIndex].bgColor)

                paint.color = palettes[paletteIndex].fgColor
                val cellWidth = width / conway.width
                val cellHeight = height / conway.height
                (0 until conway.height).forEach { y ->
                    (0 until conway.width).forEach { x ->
                        val isAlive = conway[x, y]
                        if (isAlive) {
                            canvas.drawRect(
                                Rect(
                                    x * cellWidth,
                                    y * cellHeight,
                                    (x + 1) * cellWidth,
                                    (y + 1) * cellHeight
                                ),
                                paint
                            )
                        }
                    }
                }

                paint.color = palettes[paletteIndex].gridColor
                for (y in (1..conway.height)) {
                    val pos = (y * CELL_SIZE).toFloat()
                    canvas.drawLine(0f, pos, width.toFloat(), pos, paint)
                }
                for (x in (1..conway.width)) {
                    val pos = (x * CELL_SIZE).toFloat()
                    canvas.drawLine(pos, 0f, pos, height.toFloat(), paint)
                }

                holder.unlockCanvasAndPost(canvas)
            }
        }
    }
}
