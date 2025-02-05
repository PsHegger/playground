package pshegger.github.io.playground.gamedev

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.view.ViewCompat
import pshegger.github.io.playground.gamedev.geometry.Vector
import pshegger.github.io.playground.gamedev.utils.Input
import pshegger.github.io.playground.gamedev.utils.Touch

class GameSurfaceView(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    SurfaceView(context, attrs, defStyleAttr, defStyleRes), SurfaceHolder.Callback,
    SensorEventListener {
    constructor(context: Context) : this(context, null, 0, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    private var renderThread: RenderThread? = null

    var topInset: Int = 0
        private set

    var scene: Scene? = null
        set(value) {
            value?.run {
                sizeChanged(width, height)
                if (isGravityEnabled()) {
                    if (!accelerometerListenerRegistered) {
                        sensorManager.registerListener(
                            this@GameSurfaceView,
                            accelerometer,
                            SensorManager.SENSOR_DELAY_GAME
                        )
                    }
                } else {
                    unregisterAccelerometerListener()
                }
            }
            renderThread?.fpsPaint?.color = value?.fpsColor() ?: Color.BLACK
            field = value
        }
    val input: Input = Input()

    private val sensorManager: SensorManager
    private val accelerometer: Sensor?
    private var accelerometerListenerRegistered: Boolean = false

    init {
        holder.addCallback(this)

        ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
            topInset = insets.systemWindowInsetTop

            ViewCompat.onApplyWindowInsets(v, insets)
            insets
        }

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        scene?.sizeChanged(width, height)
        renderThread?.running = false
        renderThread = RenderThread(this)
        renderThread?.start()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        renderThread?.running = false
        unregisterAccelerometerListener()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        input.touch = if (
            event.action == MotionEvent.ACTION_UP ||
            event.action == MotionEvent.ACTION_CANCEL
        ) {
            null
        } else {
            Touch(event.x, event.y)
        }

        return true
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        input.gravity = Vector(-event.values[0], event.values[1])
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun update(deltaTime: Long) {
        scene?.update(deltaTime)
    }

    fun render(canvas: Canvas) {
        scene?.render(canvas)
    }

    private fun unregisterAccelerometerListener() {
        if (accelerometerListenerRegistered) {
            sensorManager.unregisterListener(this)
        }
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
