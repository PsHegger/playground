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

class ClothSimulationScene(private val gameSurfaceView: GameSurfaceView) : Scene {

    companion object {
        private const val STICK_LENGTH = 70
    }

    private val btns = mutableListOf<Button>()

    private var width: Int = 0
    private var height: Int = 0

    private val simulation = RopeSimulation()
    private val gravity = Vector.Left * 9.81f
    private var isSimulationRunning: Boolean = false

    private val paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
        strokeWidth = 10f
        strokeCap = Paint.Cap.ROUND
    }

    private val statPaint = Paint().apply {
        textSize = 42f
        isAntiAlias = true
        color = Color.GRAY
    }

    override fun sizeChanged(width: Int, height: Int) {
        this.width = width
        this.height = height

        btns.add(Button("RES", width - 200f, height - 120f).apply {
            setOnClickListener {
                resetScene()
            }
        })

        resetScene()
    }

    override fun update(deltaTime: Long) {
        if (isSimulationRunning) {
            simulation.update(deltaTime, gravity)
        }

        gameSurfaceView.input.touch?.let { touch ->
            val touchPoint = Vector(touch.x, touch.y)
            simulation.rope.sticks
                .filter { (a, b, _) ->
                    val dstSquared = a.position.distanceSquared(b.position)
                    a.position.distanceSquared(touchPoint) < dstSquared / 4 ||
                            b.position.distanceSquared(touchPoint) < dstSquared / 4
                }
                .forEach { simulation.rope.sticks.remove(it) }
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

        paint.color = Color.RED
        simulation.rope.points.forEach { point ->
            if (point.locked) {
                canvas.drawCircle(point.position.x, point.position.y, 20f, paint)
            }
        }

        canvas.drawText(
            "Edges: ${simulation.rope.sticks.size}",
            10f, height - 10f,
            statPaint
        )

        btns.forEach { it.render(canvas) }
    }

    override fun onBackPressed() {
        gameSurfaceView.scene = SimulationsMenuScene(gameSurfaceView)
    }

    private fun resetScene() {
        isSimulationRunning = false
        simulation.reset()
        val indices = mutableMapOf<Pair<Int, Int>, Int>()
        val simulationWidth = width - 200
        val simulationHeight = height - 200
        val horizontalPointCount = simulationWidth / STICK_LENGTH + 1
        val horizontalMargin = (simulationWidth % STICK_LENGTH) / 2f
        val verticalPointCount = simulationHeight / STICK_LENGTH + 1
        val verticalMargin = (simulationHeight % STICK_LENGTH) / 2f
        repeat(horizontalPointCount) { x ->
            repeat(verticalPointCount) { y ->
                val pos = Vector(
                    100 + horizontalMargin + x * STICK_LENGTH,
                    100 + verticalMargin + y * STICK_LENGTH
                )
                simulation.addPoint(pos)
                if (x == horizontalPointCount - 1) {
                    if (y % 5 == 0 || y == verticalPointCount - 1) {
                        simulation.rope.points.last().locked = true
                    }
                }
                indices[Pair(x, y)] = simulation.rope.points.lastIndex
            }
        }

        repeat(horizontalPointCount) { x ->
            repeat(verticalPointCount) { y ->
                if (x + 1 < horizontalPointCount) {
                    simulation.addStick(
                        simulation.rope.points[indices[Pair(x, y)]!!],
                        simulation.rope.points[indices[Pair(x + 1, y)]!!],
                    )
                }
                if (y + 1 < verticalPointCount) {
                    simulation.addStick(
                        simulation.rope.points[indices[Pair(x, y)]!!],
                        simulation.rope.points[indices[Pair(x, y + 1)]!!],
                    )
                }
            }
        }
        isSimulationRunning = true
    }
}
