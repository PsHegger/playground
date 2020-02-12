package pshegger.github.io.playground.visualsorting.algorithm

import android.graphics.Color
import androidx.core.util.set
import java.util.concurrent.TimeUnit

class GnomeSort(listener: AlgorithmChangeListener) :
    SortingAlgorithm(100, 10, TimeUnit.MILLISECONDS, listener) {

    override val algorithmName = "Gnome Sort"

    override fun run() {
        var pos = 0
        updateColor(pos)
        while (pos < size) {
            if (pos == 0 || array.isBigger(pos, pos - 1)) {
                pos++
                updateColor(pos)
            } else {
                array.swap(pos, pos - 1)
                pos--
                updateColor(pos)
            }
            dispatchUpdate()
            sleep()
        }
    }

    private fun updateColor(pos: Int) {
        overrideColors {
            clear()
            this[pos] = Color.RED
        }
        dispatchUpdate()
    }
}
