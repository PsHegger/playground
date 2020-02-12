package pshegger.github.io.playground.gamedev.algorithms

import android.graphics.RectF
import pshegger.github.io.playground.gamedev.geometry.Vector
import kotlin.random.Random

class PoissonBridson(val margin: Int = 0, val radius: Int = 40, val candidateCount: Int = 20) {
    val points: List<PointState>
        get() = pointsData.map { PointState(it.p, activeSamples.contains(it)) }

    private val pointsData = arrayListOf<PointData>()
    private val activeSamples = arrayListOf<PointData>()
    private val rng = Random.Default
    private val gridSize = Math.round(radius / Math.sqrt(2.0)).toInt()

    val canGenerateMore: Boolean
        get() = pointsData.isEmpty() || activeSamples.isNotEmpty()

    private var width: Int = 0
    private var height: Int = 0
    private var container: RectF = RectF(0f, 0f, 0f, 0f)

    fun reset(width: Int, height: Int) {
        pointsData.clear()
        this.width = width
        this.height = height
        container = RectF(margin.toFloat(), margin.toFloat(), (width - margin).toFloat(), (height - margin).toFloat())
    }

    fun generateNextPoint() {
        if (pointsData.isEmpty()) {
            val p = randomPointData()
            pointsData.add(p)
            activeSamples.add(p)
            return
        }

        val p = activeSamples.random()
        var candidateFound = false

        for (i in (1..candidateCount)) {
            val np = randomAnnulusPointData(p.p)

            if (isAcceptable(np)) {
                pointsData.add(np)
                activeSamples.add(np)
                candidateFound = true
                break
            }
        }

        if (!candidateFound) {
            activeSamples.remove(p)
        }
    }

    fun generateAll() {
        while (canGenerateMore) {
            generateNextPoint()
        }
    }

    private fun isAcceptable(pd: PointData): Boolean {
        val rectsToCheck = (-1..1).flatMap { x -> (-1..1).map { y -> listOf(pd.recX + x, pd.recY + y) } }

        return rectsToCheck.all { rect ->
            val pointInRect = pointsData.find { it.recX == rect[0] && it.recY == rect[1] }

            if (pointInRect != null) {
                pd.p.distance(pointInRect.p) > radius
            } else {
                true
            }
        }
    }

    private fun randomPointData() = let {
        Vector(rng.nextFloat() * (width - 2 * margin) + margin, rng.nextFloat() * (height - 2 * margin) + margin)
    }.let { p ->
        PointData(p, p.x.toInt() / gridSize, p.y.toInt() / gridSize)
    }

    private fun randomAnnulusPointData(p: Vector): PointData {
        while (true) {
            val r = rng.nextFloat() * radius + radius
            val theta = rng.nextFloat() * 2 * Math.PI
            val nx = p.x + r * Math.cos(theta).toFloat()
            val ny = p.y + r * Math.sin(theta).toFloat()

            if (container.contains(nx, ny)) {
                val np = Vector(p.x + r * Math.cos(theta).toFloat(), p.y + r * Math.sin(theta).toFloat())
                return PointData(np, np.x.toInt() / gridSize, np.y.toInt() / gridSize)
            }
        }
    }

    private data class PointData(val p: Vector, val recX: Int, val recY: Int)
    data class PointState(val p: Vector, val active: Boolean)
}
