package pshegger.github.io.playground.utils

data class RectD(val left: Double, val top: Double, val right: Double, val bottom: Double) {
    fun width() = right - left
    fun height() = bottom - top
}
