package pshegger.github.io.playground.gamedev.algorithms.simplex

import java.util.*
import kotlin.math.ceil
import kotlin.math.log10
import kotlin.math.pow

class SimplexNoise(largestFeature: Int, persistence: Double, seed: Int) {
    private val octavesCount = ceil(log10(largestFeature.toDouble()) / log10(2.0)).toInt()

    private val rng = Random(seed.toLong())
    private val octaves = (0 until octavesCount).map { SimplexNoiseOctave(rng.nextInt()) }
    private val frequencies = (0 until octavesCount).map { i -> 2.0.pow(i.toDouble()) }
    private val amplitudes = (0 until octavesCount).map { i -> persistence.pow((octavesCount - i).toDouble()) }

    fun getNoise(x: Int, y: Int): Double = (0 until octavesCount).map { i -> octaves[i].noise(x / frequencies[i], y / frequencies[i]) * amplitudes[i] }.sum()
}
