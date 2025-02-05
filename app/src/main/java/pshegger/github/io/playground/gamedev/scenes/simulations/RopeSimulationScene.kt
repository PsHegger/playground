package pshegger.github.io.playground.gamedev.scenes.simulations

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import pshegger.github.io.playground.gamedev.GameSurfaceView
import pshegger.github.io.playground.gamedev.Scene
import pshegger.github.io.playground.gamedev.algorithms.simulations.RopeSimulation
import pshegger.github.io.playground.gamedev.geometry.Vector
import pshegger.github.io.playground.gamedev.hud.Button
import pshegger.github.io.playground.gamedev.scenes.menu.SimulationsMenuScene
import pshegger.github.io.playground.gamedev.utils.Touch

class RopeSimulationScene(private val gameSurfaceView: GameSurfaceView) : Scene {

    companion object {
        private const val POINT_TOUCH_RADIUS = 100f
    }

    private val btns = mutableListOf<Button>()

    private var width: Int = 0
    private var height: Int = 0

    private var isSimulationRunning: Boolean = false
    private var isGravityEnabled: Boolean = false

    private val simulation = RopeSimulation(50f)
    private val touchHandler = TouchHandler()

    private val paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
        strokeWidth = 20f
        strokeCap = Paint.Cap.ROUND
    }

    override fun isGravityEnabled(): Boolean = true

    override fun sizeChanged(width: Int, height: Int) {
        this.width = width
        this.height = height


        btns.clear()
        btns.add(Button("RES", width - 200f, height - 120f).apply {
            setOnClickListener {
                isSimulationRunning = false
                simulation.reset()
            }
        })

        btns.add(Button("GRAV", width - 400f, height - 120f).apply {
            setOnClickListener {
                isGravityEnabled = !isGravityEnabled
            }
        })

        btns.add(Button("SIM", width - 600f, height - 120f).apply {
            setOnClickListener {
                simulation.subdivideRope()
                isSimulationRunning = true
            }
        })

        simulation.reset()
    }

    override fun update(deltaTime: Long) {
        if (isSimulationRunning) {
            val gravity = if (isGravityEnabled) gameSurfaceView.input.gravity else null
            simulation.update(deltaTime, gravity)
        } else {
            touchHandler.handle(gameSurfaceView.input.touch)
        }

        btns.forEach { it.update(deltaTime, gameSurfaceView.input.touch) }
    }

    override fun render(canvas: Canvas) {
        canvas.drawColor(Color.rgb(154, 206, 235))

        paint.color = Color.WHITE
        simulation.rope.sticks.forEach { stick ->
            canvas.drawLine(
                stick.pointA.position.x,
                stick.pointA.position.y,
                stick.pointB.position.x,
                stick.pointB.position.y,
                paint,
            )
        }

        simulation.rope.points.forEachIndexed { index, point ->
            paint.color = when {
                point.locked -> Color.RED
                index == touchHandler.selectedIndex -> Color.GREEN
                else -> Color.WHITE
            }
            if (!isSimulationRunning || point.locked) {
                canvas.drawCircle(point.position.x, point.position.y, 30f, paint)
            }
        }

        if (isGravityEnabled) {
            paint.color = Color.BLACK
            canvas.drawCircle(width - 100f, 100f, 20f, paint)
        }

        btns.forEach { it.render(canvas) }
    }

    override fun onBackPressed(): Boolean {
        gameSurfaceView.scene = SimulationsMenuScene(gameSurfaceView)
        return true
    }

    private inner class TouchHandler {

        private var state: TouchState = TouchState.None
        private var firstTouchEvent: Boolean = true

        val selectedIndex: Int
            get() {
                val currentState = state
                return if (currentState is TouchState.PointSelected) {
                    currentState.index
                } else {
                    -1
                }
            }

        fun handle(touch: Touch?) {
            val currentState = state
            if (touch != null) {
                if (!firstTouchEvent) return
                firstTouchEvent = false
                val touchPoint = Vector(touch.x, touch.y)
                val selectedIndex = simulation.rope.points
                    .indexOfFirst { (touchPoint - it.position).length() < POINT_TOUCH_RADIUS }
                state = when (currentState) {
                    TouchState.None -> {
                        if (selectedIndex < 0) {
                            TouchState.AddPoint(touchPoint)
                        } else {
                            TouchState.PointSelected(selectedIndex)
                        }
                    }
                    is TouchState.PointSelected -> {
                        when {
                            selectedIndex < 0 -> {
                                TouchState.None
                            }
                            selectedIndex != currentState.index -> {
                                TouchState.TwoPointsSelected(currentState.index, selectedIndex)
                            }
                            else -> {
                                TouchState.LockPoint(currentState.index)
                            }
                        }
                    }
                    else -> currentState
                }
            } else {
                state = when (currentState) {
                    is TouchState.AddPoint -> {
                        simulation.addPoint(currentState.position)
                        TouchState.None
                    }
                    is TouchState.TwoPointsSelected -> {
                        simulation.addStick(
                            simulation.rope.points[currentState.index1],
                            simulation.rope.points[currentState.index2],
                        )
                        TouchState.None
                    }
                    is TouchState.LockPoint -> {
                        simulation.rope.points[currentState.index].locked =
                            !simulation.rope.points[currentState.index].locked
                        TouchState.None
                    }
                    is TouchState.PointSelected,
                    TouchState.None -> currentState
                }
                firstTouchEvent = true
            }
        }
    }

    private sealed class TouchState {
        object None : TouchState()
        class AddPoint(val position: Vector) : TouchState()
        class PointSelected(val index: Int) : TouchState()
        class TwoPointsSelected(val index1: Int, val index2: Int) : TouchState()
        class LockPoint(val index: Int) : TouchState()
    }
}
