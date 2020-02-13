package pshegger.github.io.playground.gamedev.scenes.map

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import pshegger.github.io.playground.gamedev.GameSurfaceView
import pshegger.github.io.playground.gamedev.Scene
import pshegger.github.io.playground.gamedev.algorithms.map.DelaunayGenerator
import pshegger.github.io.playground.gamedev.algorithms.poisson.PoissonBridson
import pshegger.github.io.playground.gamedev.algorithms.map.Voronoi
import pshegger.github.io.playground.gamedev.geometry.Edge
import pshegger.github.io.playground.gamedev.hud.Button
import pshegger.github.io.playground.gamedev.scenes.menu.MapGenerationMenuScene
import pshegger.github.io.playground.gamedev.utils.toLinesArray
import pshegger.github.io.playground.gamedev.utils.toPointsArray

class VoronoiScene(val gameSurfaceView: GameSurfaceView) : Scene {
    companion object {
        const val POISSON_RADIUS = 80
    }

    private var generator =
        Voronoi(emptyList())
    var width: Int = 0
    var height: Int = 0

    private val pointPaint = Paint().apply {
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 10f
        isAntiAlias = true
    }
    private val edgePaint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 5f
        isAntiAlias = true
    }

    private var btnRestart: Button? = null
    private var btnInstant: Button? = null

    override fun sizeChanged(width: Int, height: Int) {
        this.width = width
        this.height = height

        btnRestart = Button("RES", width - 200f, height - 120f).apply {
            setOnClickListener { initGenerator() }
        }

        btnInstant = Button("INS", width - 400f, height - 120f).apply {
            setOnClickListener {
                initGenerator()
                generator.generateAll()
            }
        }

        initGenerator()
    }

    private fun initGenerator() {
        val scaledWidth = width + 2 * POISSON_RADIUS
        val scaledHeight = height + 2 * POISSON_RADIUS

        val poisson =
            PoissonBridson(
                margin = 5,
                radius = POISSON_RADIUS
            )
        poisson.reset(scaledWidth, scaledHeight)

        poisson.generateAll()
        val delaunay =
            DelaunayGenerator(
                poisson.points.map { it.p })
        delaunay.reset(scaledWidth, scaledHeight)
        delaunay.generateAll()

        generator =
            Voronoi(delaunay.triangles.map {
                it.shift(-POISSON_RADIUS)
            })
        generator.reset()
    }

    override fun update(deltaTime: Long) {
        if (generator.canGenerateMore) {
            generator.generateNextEdge()
        }

        btnRestart?.update(deltaTime, gameSurfaceView.touch)
        btnInstant?.update(deltaTime, gameSurfaceView.touch)
    }

    override fun render(canvas: Canvas) {
        canvas.drawColor(Color.rgb(154, 206, 235))

        canvas.drawLines(generator.edges.toLinesArray(), edgePaint)

        pointPaint.color = Color.BLACK
        canvas.drawPoints(generator.points.filterNot { it.isActive }.map { it.p }.toPointsArray(), pointPaint)
        pointPaint.color = Color.RED
        canvas.drawPoints(generator.points.filter { it.isActive }.map { it.p }.toPointsArray(), pointPaint)

        btnRestart?.render(canvas)
        btnInstant?.render(canvas)
    }

    override fun onBackPressed() {
        gameSurfaceView.scene = MapGenerationMenuScene(gameSurfaceView)
    }

    private fun Edge.render(canvas: Canvas, paint: Paint) {
        canvas.drawLine(start.x, start.y, end.x, end.y, paint)
    }
}
