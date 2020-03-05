package pshegger.github.io.playground.gamedev.algorithms

import pshegger.github.io.playground.gamedev.geometry.Vector
import kotlin.math.PI
import kotlin.math.min
import kotlin.random.Random

class Boids(private val settings: Settings) {

    var boids = emptyList<Boid>()
        private set

    var debugInfo: DebugInfo = DebugInfo(0, emptyList())
        private set

    private var width: Int = 0
    private var height: Int = 0
    private var rng: Random = Random.Default

    fun reset(width: Int, height: Int) {
        this.width = width
        this.height = height
        rng = Random(settings.seed ?: System.currentTimeMillis())
        boids = (0 until settings.count).map { generateBoid(it, width, height, settings, rng) }
        debugInfo = DebugInfo(boids.first().id, emptyList())
    }

    fun update(deltaTime: Long) {
        val deltaMs = deltaTime / 1000f
        val flockMates = calculateFlockMates()
        updateTargets(flockMates)
        updateDirections(deltaMs)
        moveBoids(deltaMs)
    }

    private fun calculateFlockMates(): Map<Int, List<Boid>> =
        boids.associate { boid ->
            val mates = boids.mapNotNull { o ->
                if (
                    o.id != boid.id &&
                    boid.isFlockMateWith(o, settings.visionRange, settings.visionAngle)
                ) {
                    o
                } else {
                    null
                }
            }
            if (boid.id == debugInfo.boidId) {
                debugInfo = debugInfo.copy(flockMates = mates)
            }
            Pair(boid.id, mates)
        }

    private fun updateTargets(flockMates: Map<Int, List<Boid>>) {
        boids.forEach { boid ->
            val targets = mutableListOf<Pair<Float, Vector>>()
            val mates = flockMates.getOrElse(boid.id) { emptyList() }

            val obstacleAvoidanceTarget = boid.calculateObstacleAvoidanceTarget()

            if (obstacleAvoidanceTarget == Vector.Zero) {
                targets.add(Pair(settings.separationWeight, boid.calculateSeparationTarget(mates)))
                targets.add(Pair(settings.alignmentWeight, boid.calculateAlignmentTarget(mates)))
                targets.add(Pair(settings.cohesionWeight, boid.calculateCohesionTarget(mates)))
            } else {
                targets.add(Pair(1f, obstacleAvoidanceTarget))
            }

            boid.targetDirection = targets
                .fold(Vector.Zero) { acc, (weight, t) -> acc + t * weight }
                .normalize()
        }
    }

    private fun Boid.calculateObstacleAvoidanceTarget(): Vector {
        val scanStepSize = (settings.visionAngle / 11).toFloat()
        val pointsToAvoid = (-5..5)
            .map { step -> direction.rotateBy(step * scanStepSize).normalize() }
            .flatMap { scanDirection ->
                val endPoint = position + scanDirection * settings.visionRange
                listOf(
                    Pair(endPoint, endPoint.x < 0),
                    Pair(endPoint, endPoint.y < 0),
                    Pair(endPoint, endPoint.x > width),
                    Pair(endPoint, endPoint.y > height)
                ).filter { it.second }
                    .map { it.first }
            }
        return calculateAvoidanceTarget(pointsToAvoid)
    }

    private fun Boid.calculateSeparationTarget(flockMates: List<Boid>) =
        calculateAvoidanceTarget(flockMates.map(Boid::position))

    private fun Boid.calculateAlignmentTarget(flockMates: List<Boid>) =
        flockMates.fold(direction) { acc, mate ->
            acc + mate.direction
        }.normalize()

    private fun Boid.calculateCohesionTarget(flockMates: List<Boid>) =
        (flockMates.fold(Vector.Zero) { acc, mate ->
            acc + mate.position
        } / flockMates.size.toFloat() - position).normalize()

    private fun Boid.calculateAvoidanceTarget(pointsToAvoid: List<Vector>) =
        pointsToAvoid.fold(Vector.Zero) { acc, point ->
            val dir = point - position
            acc - dir
        }.normalize()

    private fun updateDirections(deltaMs: Float) {
        boids.forEach { boid ->
            val targetDirection = boid.targetDirection
            if (targetDirection != null) {
                val targetAngle = boid.direction.angleWith(targetDirection).toDouble()
                if (!targetAngle.isNaN()) {
                    val angle = min(targetAngle, settings.steeringSpeed * deltaMs)
                    val cross = Vector(boid.direction.y, -boid.direction.x) * targetDirection
                    val rotationAngle = if (cross < 0) {
                        angle
                    } else {
                        2 * PI - angle
                    }.toFloat()
                    boid.direction = boid.direction.rotateBy(rotationAngle).normalize()
                }
            }
        }
    }

    private fun moveBoids(deltaMs: Float) {
        boids.forEach { b ->
            b.position += b.direction * deltaMs * b.speed
            if (b.position.x > width) {
                b.position = b.position.copy(x = b.position.x - width)
            }
            if (b.position.x < 0) {
                b.position = b.position.copy(x = b.position.x + width)
            }
            if (b.position.y > height) {
                b.position = b.position.copy(y = b.position.y - height)
            }
            if (b.position.y < 0) {
                b.position = b.position.copy(y = b.position.y + height)
            }
        }
    }

    companion object {
        private fun generateBoid(
            id: Int,
            width: Int,
            height: Int,
            settings: Settings,
            rng: Random
        ) =
            Boid(
                id,
                Vector(rng.nextFloat() * width, rng.nextFloat() * height),
                Vector(rng.nextFloat() * 2 - 1, rng.nextFloat() * 2 - 1).normalize(),
                rng.nextInt(
                    settings.baseSpeed - settings.speedVariance / 2,
                    settings.baseSpeed + settings.speedVariance / 2
                )
            )
    }

    data class Boid(
        val id: Int,
        var position: Vector,
        var direction: Vector,
        val speed: Int,
        var targetDirection: Vector? = null
    ) {

        fun isFlockMateWith(other: Boid, visionRange: Int, visionAngle: Double) =
            position.distanceSquared(other.position) < visionRange * visionRange &&
                    direction.angleWith((other.position - position).normalize()) < visionAngle / 2
    }

    data class DebugInfo(val boidId: Int, val flockMates: List<Boid>)

    data class Settings(
        val count: Int = 50,
        val baseSpeed: Int = 500,
        val speedVariance: Int = 20,
        val steeringSpeed: Double = 2 * PI,
        val visionRange: Int = 100,
        val visionAngle: Double = PI,
        val separationWeight: Float = 1.0f,
        val alignmentWeight: Float = 1.0f,
        val cohesionWeight: Float = 1.0f,
        val seed: Long? = null
    )
}
