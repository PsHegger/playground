package pshegger.github.io.playground.gamedev.scenes.map

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import pshegger.github.io.playground.gamedev.GameSurfaceView
import pshegger.github.io.playground.gamedev.Scene
import pshegger.github.io.playground.gamedev.algorithms.map.MapGenerator
import pshegger.github.io.playground.gamedev.geometry.Polygon
import pshegger.github.io.playground.gamedev.hud.Button
import pshegger.github.io.playground.gamedev.scenes.menu.MapGenerationMenuScene
import pshegger.github.io.playground.gamedev.utils.timeLimitedWhile
import pshegger.github.io.playground.gamedev.utils.toLinesArray
import pshegger.github.io.playground.gamedev.utils.toPointsArray

class MapGeneratorScene(val gameSurfaceView: GameSurfaceView) : Scene {
    private var generator =
        MapGenerator(
            MapGenerator.Settings(
                5,
                30,
                150
            )
        )
    var width: Int = 0
    var height: Int = 0

    private var btnRestart: Button? = null
    private var btnInstant: Button? = null

    private val pointPaint = Paint().apply {
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 10f
        isAntiAlias = true
    }
    private val delaunayEdgePaint = Paint().apply {
        color = Color.RED
        strokeWidth = 1f
        isAntiAlias = true
    }
    private val voronoiEdgePaint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 5f
        isAntiAlias = true
    }
    private val voronoiEdgePaintThin = Paint().apply {
        color = Color.BLUE
        strokeWidth = 1f
        isAntiAlias = true
    }
    private val cellPaint = Paint().apply {
        style = Paint.Style.FILL
    }

    override fun sizeChanged(width: Int, height: Int) {
        this.width = width
        this.height = height

        btnRestart = Button("RES", width - 200f, height - 120f).apply {
            setOnClickListener { generator.reset(width, height) }
        }

        btnInstant = Button("INS", width - 400f, height - 120f).apply {
            setOnClickListener {
                generator.reset(width, height)
                generator.generateAll()
            }
        }

        generator.reset(width, height)
    }

    override fun update(deltaTime: Long) {
        if (generator.canGenerateMore) {
            val timeLimit = when (generator.state) {
                MapGenerator.State.Poisson -> 10.0
                MapGenerator.State.Voronoi -> 2.0
                MapGenerator.State.Simplex -> 0.02
                else -> 5.0
            }

            timeLimitedWhile(timeLimit, { generator.canGenerateMore }) {
                generator.generateNext()
            }
        }

        btnRestart?.update(deltaTime, gameSurfaceView.input.touch)
        btnInstant?.update(deltaTime, gameSurfaceView.input.touch)
    }

    override fun render(canvas: Canvas) {
        canvas.drawColor(Color.rgb(154, 206, 235))

        when (generator.state) {
            MapGenerator.State.Poisson -> renderPoisson(canvas)
            MapGenerator.State.Delaunay -> renderDelaunay(canvas)
            MapGenerator.State.Voronoi -> renderVoronoi(canvas)
            MapGenerator.State.Simplex -> renderSimplex(canvas)
            MapGenerator.State.Finished -> renderSimplex(canvas)
        }

        btnRestart?.render(canvas)
        btnInstant?.render(canvas)
    }

    private fun renderPoisson(canvas: Canvas) {
        pointPaint.color = Color.BLACK
        canvas.drawPoints(generator.poissonPoints.filterNot { it.active }.map { it.p }.toPointsArray(), pointPaint)
        pointPaint.color = Color.RED
        canvas.drawPoints(generator.poissonPoints.filter { it.active }.map { it.p }.toPointsArray(), pointPaint)
    }

    private fun renderDelaunay(canvas: Canvas) {
        canvas.drawLines(generator.delaunayEdges.toLinesArray(), delaunayEdgePaint)

        pointPaint.color = Color.BLACK
        canvas.drawPoints(generator.delaunayPoints.toPointsArray(), pointPaint)
    }

    private fun renderVoronoi(canvas: Canvas) {
        canvas.drawLines(generator.delaunayEdges.toLinesArray(), delaunayEdgePaint)

        canvas.drawLines(generator.voronoiEdges.toLinesArray(), voronoiEdgePaint)

        pointPaint.color = Color.BLACK
        canvas.drawPoints(generator.voronoiPoints.filterNot { it.isActive }.map { it.p }.toPointsArray(), pointPaint)
        pointPaint.color = Color.RED
        canvas.drawPoints(generator.voronoiPoints.filter { it.isActive }.map { it.p }.toPointsArray(), pointPaint)
    }

    private fun renderSimplex(canvas: Canvas) {
        generator.mapPolygons.forEach { mp ->
            mp.polygon.toPath()?.let { path ->
                val color = Math.round(mp.value * 255)
                cellPaint.color = Color.rgb(color, color, color)

                canvas.drawPath(path, cellPaint)
            }
        }

        canvas.drawLines(generator.voronoiEdges.toLinesArray(), voronoiEdgePaintThin)
    }

    override fun onBackPressed(): Boolean {
        gameSurfaceView.scene = MapGenerationMenuScene(gameSurfaceView)
        return true
    }

    private fun Polygon.toPath(): Path? = Path().let { p ->
        vectorRoute?.let { route ->
            p.moveTo(route[0].x, route[0].y)
            (1 until route.size).forEach { i ->
                p.lineTo(route[i].x, route[i].y)
            }
        } ?: return@let null

        p
    }
}
