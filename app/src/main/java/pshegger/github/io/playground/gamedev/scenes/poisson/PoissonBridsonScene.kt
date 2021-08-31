package pshegger.github.io.playground.gamedev.scenes.poisson

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import pshegger.github.io.playground.gamedev.GameSurfaceView
import pshegger.github.io.playground.gamedev.Scene
import pshegger.github.io.playground.gamedev.algorithms.poisson.PoissonBridson
import pshegger.github.io.playground.gamedev.hud.Button
import pshegger.github.io.playground.gamedev.scenes.menu.PoissonMenuScene
import pshegger.github.io.playground.gamedev.utils.toPointsArray

class PoissonBridsonScene(val gameSurfaceView: GameSurfaceView) : Scene {
    val algo =
        PoissonBridson(
            margin = 5,
            radius = 45
        )
    var width: Int = 0
    var height: Int = 0

    private val pointPaint = Paint().apply {
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 10f
        isAntiAlias = true
    }
    private val countPaint = Paint().apply {
        textSize = 42f
        isAntiAlias = true
        color = Color.GRAY
    }

    var btnRestart: Button? = null
    var btnInstant: Button? = null

    override fun sizeChanged(width: Int, height: Int) {
        this.width = width
        this.height = height

        btnRestart = Button("RES", width - 200f, height - 120f).apply {
            setOnClickListener { algo.reset(width, height) }
        }

        btnInstant = Button("INS", width - 400f, height - 120f).apply {
            setOnClickListener {
                algo.reset(width, height)
                algo.generateAll()
            }
        }

        algo.reset(width, height)
    }

    override fun update(deltaTime: Long) {
        if (algo.canGenerateMore) {
            algo.generateNextPoint()
        }

        btnRestart?.update(deltaTime, gameSurfaceView.input.touch)
        btnInstant?.update(deltaTime, gameSurfaceView.input.touch)
    }

    override fun render(canvas: Canvas) {
        canvas.drawColor(Color.rgb(154, 206, 235))

        pointPaint.color = Color.BLACK
        canvas.drawPoints(algo.points.filterNot { it.active }.map { it.p }.toPointsArray(), pointPaint)
        pointPaint.color = Color.RED
        canvas.drawPoints(algo.points.filter { it.active }.map { it.p }.toPointsArray(), pointPaint)

        canvas.drawText("Count: ${algo.points.size}", 10f, height - 10f, countPaint)

        btnRestart?.render(canvas)
        btnInstant?.render(canvas)
    }

    override fun onBackPressed() {
        gameSurfaceView.scene = PoissonMenuScene(gameSurfaceView)
    }
}
