package pshegger.github.io.playground.gamedev

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.view.ViewCompat
import pshegger.github.io.playground.gamedev.utils.Touch

class GameSurfaceView(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    SurfaceView(context, attrs, defStyleAttr, defStyleRes), SurfaceHolder.Callback {
    constructor(context: Context) : this(context, null, 0, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    private var renderThread: RenderThread? = null
    private var touchHelper: Touch? = null

    val topInset: Int
        get() = _topInset
    private var _topInset: Int = 0

    var scene: Scene? = null
        set(value) {
            value?.sizeChanged(width, height)
            renderThread?.fpsPaint?.color = value?.fpsColor() ?: Color.BLACK
            field = value
        }
    val touch: Touch?
        get() = touchHelper

    init {
        holder.addCallback(this)

        ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
            _topInset = insets.systemWindowInsetTop

            ViewCompat.onApplyWindowInsets(v, insets)
            insets
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        scene?.sizeChanged(width, height)
        renderThread?.running = false
        renderThread = RenderThread(this)
        renderThread?.start()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        renderThread?.running = false
    }

    override fun surfaceCreated(holder: SurfaceHolder) {

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        touchHelper = if (
            event.action == MotionEvent.ACTION_UP ||
            event.action == MotionEvent.ACTION_CANCEL
        ) {
            null
        } else {
            Touch(event.x, event.y)
        }

        return true
    }

    fun update(deltaTime: Long) {
        scene?.update(deltaTime)
    }

    fun render(canvas: Canvas) {
        scene?.render(canvas)
    }

    private class RenderThread(val gameSurfaceView: GameSurfaceView, var running: Boolean = true) :
        Thread() {
        var lastRenderTime = System.currentTimeMillis()
        var fps: Float = 0f

        val fpsPaint = Paint().apply {
            textSize = 42f
            isAntiAlias = true
        }

        override fun run() {
            var framesDrawn = 0
            var lastFPSTimer = System.currentTimeMillis()
            while (running) {
                val tNow = System.currentTimeMillis()
                val deltaTime = let {
                    val t = tNow - lastRenderTime
                    if (t == 0L) 1L else t
                }

                update(deltaTime)
                val canvas = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    gameSurfaceView.holder.lockHardwareCanvas()
                } else {
                    gameSurfaceView.holder.lockCanvas()
                } ?: continue
                render(canvas)
                gameSurfaceView.holder.unlockCanvasAndPost(canvas)

                lastRenderTime = tNow
                framesDrawn++
                if (tNow - lastFPSTimer > 1000) {
                    fps = framesDrawn / ((tNow - lastFPSTimer) / 1000f)
                    lastFPSTimer = tNow
                    framesDrawn = 0
                }
            }
        }

        private fun update(deltaTime: Long) {
            gameSurfaceView.update(deltaTime)
        }

        private fun render(canvas: Canvas) {
            gameSurfaceView.render(canvas)

            canvas.drawText("FPS: ${String.format("%.1f", fps)}", 50f, 52f, fpsPaint)
        }
    }
}
