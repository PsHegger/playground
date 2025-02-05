package pshegger.github.io.playground.gamedev.scenes.simulations

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import pshegger.github.io.playground.gamedev.GameSurfaceView
import pshegger.github.io.playground.gamedev.Scene
import pshegger.github.io.playground.gamedev.algorithms.simulations.Boids
import pshegger.github.io.playground.gamedev.geometry.Vector
import pshegger.github.io.playground.gamedev.hud.Button
import pshegger.github.io.playground.gamedev.scenes.menu.SimulationsMenuScene
import kotlin.math.roundToInt

class BoidsScene(private val gameSurfaceView: GameSurfaceView) : Scene {

    private var btnRestart: Button? = null
    private var btnDebug: Button? = null
    private var debugEnabled: Boolean = false
    private var width: Int = 0
    private var height: Int = 0
    private val boids = Boids(
        Boids.Settings(
            count = 100,
            visionRange = VISION_RANGE,
            visionAngle = Math.toRadians(VISION_ANGLE.toDouble()),
            baseSpeed = 600,
            steeringSpeed = Math.toRadians(STEERING_SPEED.toDouble()),
            alignmentWeight = 0.8f,
            separationWeight = 1.7f,
            cohesionWeight = 0.5f
        )
    )

    private val boidPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
    }

    override fun sizeChanged(width: Int, height: Int) {
        this.width = width
        this.height = height

        btnRestart = Button("RES", width - 200f, height - 120f).apply {
            setOnClickListener { boids.reset(width, height) }
        }

        btnDebug = Button("DBG", width - 400f, height - 120f).apply {
            setOnClickListener {
                debugEnabled = !debugEnabled
            }
        }

        boids.reset(width, height)
    }

    override fun update(deltaTime: Long) {
        boids.update(deltaTime)

        btnRestart?.update(deltaTime, gameSurfaceView.input.touch)
        btnDebug?.update(deltaTime, gameSurfaceView.input.touch)
    }

    override fun render(canvas: Canvas) {
        canvas.drawColor(Color.rgb(154, 206, 235))

        boids.boids
            .forEach { boid ->
                boid.render(canvas, boid.id == boids.debugInfo.boidId)
            }

        btnRestart?.render(canvas)
        btnDebug?.render(canvas)
    }

    override fun onBackPressed(): Boolean {
        gameSurfaceView.scene = SimulationsMenuScene(gameSurfaceView)
        return true
    }

    private fun Boids.Boid.render(canvas: Canvas, includeDebug: Boolean) {
        if (includeDebug && debugEnabled) {
            renderDebug(canvas)
            boidPaint.color = Color.MAGENTA
        } else {
            boidPaint.color = Color.RED
        }

        canvas.drawPath(asPath(), boidPaint)
    }

    private fun Boids.Boid.renderDebug(canvas: Canvas) {
        boidPaint.color = Color.LTGRAY
        val rotationAngle =
            Math.toDegrees(direction.angleWith(Vector(1f, 0f)).toDouble()).toFloat()
        val cross = direction * Vector(0f, 1f)
        val directionAngle = if (cross < 0) {
            360 - rotationAngle
        } else {
            rotationAngle
        }
        canvas.drawArc(
            position.x - VISION_RANGE, position.y - VISION_RANGE,
            position.x + VISION_RANGE, position.y + VISION_RANGE,
            directionAngle - VISION_ANGLE / 2f, VISION_ANGLE.toFloat(),
            true, boidPaint
        )
        boids.debugInfo.flockMates.forEach { mate ->
            val distanceRatio = (position - mate.position).length() / VISION_RANGE
            boidPaint.color = Color.rgb(255, (165 * distanceRatio).roundToInt(), 0)
            canvas.drawLine(position.x, position.y, mate.position.x, mate.position.y, boidPaint)
        }
        targetDirection?.let { targetDirection ->
            val target = position + targetDirection * 100
            boidPaint.color = Color.GREEN
            canvas.drawLine(position.x, position.y, target.x, target.y, boidPaint)
            canvas.drawCircle(target.x, target.y, 5f, boidPaint)
        }
    }

    private fun Boids.Boid.asPath(): Path {
        val base = position - direction * BOID_SIZE / 2f
        val head = base + direction * BOID_SIZE
        val t1 = base + Vector(direction.y, -direction.x) * (BOID_SIZE / 2.7183f)
        val t2 = base - Vector(direction.y, -direction.x) * (BOID_SIZE / 2.7183f)
        return Path().apply {
            moveTo(head.x, head.y)
            lineTo(t1.x, t1.y)
            lineTo(t2.x, t2.y)
            close()
        }
    }

    companion object {
        private const val BOID_SIZE = 40
        private const val VISION_RANGE = 400
        private const val VISION_ANGLE = 270
        private const val STEERING_SPEED = 180
    }
}
