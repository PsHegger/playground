package pshegger.github.io.playground.gamedev.utils

import pshegger.github.io.playground.gamedev.geometry.Edge
import pshegger.github.io.playground.gamedev.geometry.Vector
import java.util.concurrent.TimeUnit
import kotlin.random.Random

fun List<Edge>.neighbors(v: Vector): List<Vector> = this.mapNotNull {
    when (v) {
        it.start -> it.end
        it.end -> it.start
        else -> null
    }
}

fun <T> List<T>.random(): T = this[Math.floor(Math.random() * this.size).toInt()]
fun <T> List<Pair<T, Int>>.weightedRandom(rng: Random = Random.Default): T {
    val weightSum = map { it.second }.sum()
    var n = rng.nextInt(weightSum)
    var selected = this[0]
    for (item in this) {
        n -= item.second
        if (n <= 0) {
            selected = item
            break
        }
    }

    return selected.first
}

operator fun <T> List<T>.times(o: List<T>) = this.flatMap { a -> o.map { b -> kotlin.collections.listOf(a, b) } }

fun <T> List<T>.others(o: T) = filterNot { it == o }

fun Iterable<Edge>.toLinesArray() = flatMap { listOf(it.start.x, it.start.y, it.end.x, it.end.y) }.toFloatArray()
fun Iterable<Vector>.toPointsArray() = flatMap { listOf(it.x, it.y) }.toFloatArray()

inline fun timeLimitedWhile(maxExecutionMs: Double, predicate: () -> Boolean, action: () -> Unit) {
    val maxExecutionNs = Math.round(maxExecutionMs * TimeUnit.MILLISECONDS.toNanos(1))
    val start = System.nanoTime()
    while (System.nanoTime() - start < maxExecutionNs && predicate()) {
        action()
    }
}
