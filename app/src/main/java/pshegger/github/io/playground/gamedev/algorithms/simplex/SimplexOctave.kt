package pshegger.github.io.playground.gamedev.algorithms.simplex

import java.util.*

/**
 * Based on https://github.com/shathor/SimplexNoise/blob/master/src/ch/project/inter/SimplexNoise.java
 */
class SimplexNoiseOctave(seed: Int) {
    companion object {
        private val grad3 = listOf(
            listOf(1, 1, 0), listOf(-1, 1, 0), listOf(1, -1, 0),
            listOf(-1, -1, 0), listOf(1, 0, 1), listOf(-1, 0, 1),
            listOf(1, 0, -1), listOf(-1, 0, -1), listOf(0, 1, 1),
            listOf(0, -1, 1), listOf(0, 1, -1), listOf(0, -1, -1)
        )

        private val pSupply = listOf(151,160,137,91,90,15,
            131,13,201,95,96,53,194,233,7,225,140,36,103,30,69,142,8,99,37,240,21,10,23,
            190, 6,148,247,120,234,75,0,26,197,62,94,252,219,203,117,35,11,32,57,177,33,
            88,237,149,56,87,174,20,125,136,171,168, 68,175,74,165,71,134,139,48,27,166,
            77,146,158,231,83,111,229,122,60,211,133,230,220,105,92,41,55,46,245,40,244,
            102,143,54, 65,25,63,161, 1,216,80,73,209,76,132,187,208, 89,18,169,200,196,
            135,130,116,188,159,86,164,100,109,198,173,186, 3,64,52,217,226,250,124,123,
            5,202,38,147,118,126,255,82,85,212,207,206,59,227,47,16,58,17,182,189,28,42,
            223,183,170,213,119,248,152, 2,44,154,163, 70,221,153,101,155,167, 43,172,9,
            129,22,39,253, 19,98,108,110,79,113,224,232,178,185, 112,104,218,246,97,228,
            251,34,242,193,238,210,144,12,191,179,162,241, 81,51,145,235,249,14,239,107,
            49,192,214, 31,181,199,106,157,184, 84,204,176,115,121,50,45,127, 4,150,254,
            138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180)

        private fun dot(g: List<Int>, x: Double, y: Double) = g[0] * x + g[1] * y

        private fun fastFloor(x: Double): Int = if (x > 0) x.toInt() else (x - 1).toInt()

        const val RANDOM_SEED = 0
    }


    private val mSeed = if (seed == RANDOM_SEED) Random().nextInt() else seed
    private val p = pSupply.shuffled(Random(mSeed.toLong()))
    private val perm = (0 until p.size  * 2).map { i -> p[i and 255] }
    private val permMod12 = perm.map { p -> p % 12 }

    fun noise(xin: Double, yin: Double): Double {
        val s = (xin + yin) * (0.5 * (Math.sqrt(3.0) - 1.0))

        val i = fastFloor(xin + s)
        val j = fastFloor(yin + s)

        val g2 = (3.0 - Math.sqrt(3.0)) / 6.0
        val t = (i + j) * g2
        val x0 = xin - (i - t)
        val y0 = yin - (j - t)

        val i1: Int
        val j1: Int
        if (x0 > y0) {
            i1 = 1
            j1 = 0
        } else {
            i1 = 0
            j1 = 1
        }

        val x1 = x0 - i1 + g2
        val y1 = y0 - j1 + g2
        val x2 = x0 - 1.0 + 2.0 * g2
        val y2 = y0 - 1.0 + 2.0 * g2

        val ii = i and 255
        val jj = j and 255

        var t0 = 0.5 - x0 * x0 - y0 * y0
        val n0 = if (t0 < 0) {
            0.0
        } else {
            t0 *= t0
            val gi0 = permMod12[ii + perm[jj]]
            t0 * t0 * dot(grad3[gi0], x0, y0)
        }

        var t1 = 0.5 - x1 * x1 - y1 * y1
        val n1 = if (t1 < 0) {
            0.0
        } else {
            t1 *= t1
            val gi1 = permMod12[ii + i1 + perm[jj + j1]]
            t1 * t1 * dot(grad3[gi1], x1, y1)
        }

        var t2 = 0.5 - x2 * x2 - y2 * y2
        val n2 = if (t2 < 0) {
            0.0
        } else {
            t2 *= t2
            val gi2 = permMod12[ii + 1 + perm[jj + 1]]
            t2 * t2 * dot(grad3[gi2], x2, y2)
        }

        return 70.0 * (n0 + n1 + n2)
    }
}
