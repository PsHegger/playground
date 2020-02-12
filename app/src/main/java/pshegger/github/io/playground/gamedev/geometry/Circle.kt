package pshegger.github.io.playground.gamedev.geometry


data class Circle(val center: Vector, val radius: Float) {
    fun contains(p: Vector) = center.distance(p) < radius
}
