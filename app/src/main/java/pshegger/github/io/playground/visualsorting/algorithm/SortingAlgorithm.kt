package pshegger.github.io.playground.visualsorting.algorithm

import android.os.Handler
import android.os.Looper
import android.util.SparseIntArray
import pshegger.github.io.playground.visualsorting.AccessReportingArray
import java.util.concurrent.TimeUnit

typealias AlgorithmChangeListener = (name: String, compareCount: Int, accessCount: Int) -> Unit
typealias DataChangeListener = (data: List<Int>, colorOverrides: SparseIntArray) -> Unit

abstract class SortingAlgorithm(
    val size: Int,
    private val delay: Long,
    private val delayUnit: TimeUnit,
    private val listener: AlgorithmChangeListener? = null
) : Thread() {

    abstract val algorithmName: String
    var running: Boolean = true

    private val colorOverrides = SparseIntArray()
    private var data: List<Int> = emptyList()
    open val hideColumnMargin = false

    private var dataChangeListener: DataChangeListener? = null

    override fun run() {
        dispatchUpdate()
    }

    fun setDataChangeListener(listener: DataChangeListener?) {
        dataChangeListener = listener
    }

    protected val array = AccessReportingArray(size) { it + 1 }.apply {
        shuffle()
        setListener { accessCount, compareCount ->
            if (running) {
                Handler(Looper.getMainLooper()).post {
                    listener?.invoke(algorithmName, compareCount, accessCount)
                }
            }
        }
        setChangeListener { data = it }
    }

    protected fun overrideColors(action: SparseIntArray.() -> Unit) {
        colorOverrides.apply(action)
    }

    protected fun sleep() = sleep(delayUnit.toMillis(delay))
    protected fun dispatchUpdate() = dataChangeListener?.invoke(data, colorOverrides.clone())
}

