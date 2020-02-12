package pshegger.github.io.playground.visualsorting

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.util.SparseIntArray
import android.view.SurfaceHolder
import android.view.SurfaceView
import pshegger.github.io.playground.visualsorting.algorithm.SortingAlgorithm
import kotlin.math.min

class VisualizerView(context: Context, attrs: AttributeSet?, defStyleAttrs: Int, defStyleRes: Int) :
    SurfaceView(context, attrs, defStyleAttrs, defStyleRes), SurfaceHolder.Callback {

    companion object {
        private const val TOP_MARGIN = 20
        private const val SIDE_MARGIN = 20
        private const val COLUMN_MARGIN = 5
    }

    constructor(context: Context) : this(context, null, 0, 0)
    constructor(context: Context, atts: AttributeSet?) : this(context, atts, 0, 0)
    constructor(context: Context, atts: AttributeSet?, defStyleAttr: Int) : this(context, atts, defStyleAttr, 0)

    private var renderThread = RenderThread(false)

    private var colorOverrides: SparseIntArray? = null
    private var data: List<Int>? = null
    private var hideColumnMargin: Boolean = false
    private var circleRect = RectF()

    var algorithm: SortingAlgorithm? = null
        set(value) {
            field?.apply {
                setDataChangeListener(null)
                running = false
            }
            field = value
            field?.apply {
                setDataChangeListener { d, c ->
                    data = d
                    colorOverrides = c
                    this@VisualizerView.hideColumnMargin = hideColumnMargin
                }
                start()
            }
        }
    var mode: Mode = Mode.Bars

    private val columnPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    init {
        holder.addCallback(this)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        val rectWidth = min(width - 2 * SIDE_MARGIN, height - 2 * SIDE_MARGIN)
        val centerX = width / 2f
        val centerY = height / 2f
        circleRect = RectF(
            centerX - rectWidth / 2,
            centerY - rectWidth / 2,
            centerX + rectWidth / 2,
            centerY + rectWidth / 2
        )

        renderThread.running = false
        renderThread = RenderThread()
        renderThread.start()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        renderThread.running = false
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

                canvas.drawColor(Color.BLACK)

                when (mode) {
                    Mode.Bars -> renderBars(canvas)
                    Mode.ColorCircle -> renderColorCircle(canvas)
                }

                holder.unlockCanvasAndPost(canvas)
            }
        }

        private fun renderColorCircle(canvas: Canvas) {
            val data = this@VisualizerView.data ?: return

            val count = data.size
            val max = data.max() ?: 1
            val angleStep = 360f / count

            for (i in 0 until count) {
                val hue = (data[i].toFloat() / max) * 360f
                columnPaint.color = Color.HSVToColor(arrayOf(hue, 1f, 1f).toFloatArray())

                canvas.drawArc(circleRect, i * angleStep, angleStep, true, columnPaint)
            }
        }

        private fun renderBars(canvas: Canvas) {
            val data = this@VisualizerView.data ?: return
            val overrides = colorOverrides ?: return

            val count = data.size
            val max = data.max() ?: 1

            val drawHeight = height - TOP_MARGIN
            val drawWidth = width - 2 * SIDE_MARGIN

            val step = drawHeight.toFloat() / max
            val columnMargin = if (hideColumnMargin) 0 else COLUMN_MARGIN
            val columnWidth = (drawWidth.toFloat() - (count - 1) * columnMargin) / count

            for (i in 0 until count) {
                val left = i * (columnWidth + columnMargin) + SIDE_MARGIN
                val right = left + columnWidth
                val bottom = height.toFloat()
                val top = bottom - data[i] * step
                columnPaint.color = overrides.get(i, Color.WHITE)

                canvas.drawRect(left, top, right, bottom, columnPaint)
            }
        }
    }

    enum class Mode {
        Bars, ColorCircle
    }
}
