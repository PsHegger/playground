package pshegger.github.io.playground.visualsorting

import kotlin.random.Random

typealias ReportListener = (accessCount: Int, compareCount: Int) -> Unit

class AccessReportingArray<T : Comparable<T>>(val size: Int, init: (Int) -> T) {

    private val data = (0 until size).map(init).toMutableList()

    private var listener: ReportListener? = null
    private var changeListener: ((List<T>) -> Unit)? = null

    private var accessCount: Int = 0
        set(value) {
            field = value
            listener?.invoke(field, compareCount)
        }
    private var compareCount: Int = 0
        set(value) {
            field = value
            listener?.invoke(accessCount, field)
        }

    operator fun get(index: Int) = data[index].also { accessCount++ }
    operator fun set(index: Int, value: T) {
        data[index] = value
        accessCount++
        changeListener?.invoke(data.toList())
    }

    fun setListener(listener: ReportListener?) {
        this.listener = listener
    }

    fun setChangeListener(listener: ((List<T>) -> Unit)?) {
        changeListener = listener
        changeListener?.invoke(data.toList())
    }

    fun shuffle(iterations: Int = data.size * 2) {
        for (i in 0 until iterations) {
            val i1 = Random.nextInt(data.size)
            val i2 = Random.nextInt(data.size)
            val tmp = data[i1]
            data[i1] = data[i2]
            data[i2] = tmp
        }
    }

    fun swap(i1: Int, i2: Int) {
        val tmp = this[i1]
        this[i1] = this[i2]
        this[i2] = tmp
        changeListener?.invoke(data.toList())
    }

    fun isBigger(i1: Int, i2: Int) = compare(i1, i2) > 0
    fun isSmaller(i1: Int, i2: Int) = compare(i1, i2) < 0

    private fun compare(i1: Int, i2: Int) = this[i1].compareTo(this[i2]).also { compareCount++ }

    override fun toString(): String = buildString {
        append("[")
        append(data.joinToString(separator = ", "))
        append("]")
    }
}
