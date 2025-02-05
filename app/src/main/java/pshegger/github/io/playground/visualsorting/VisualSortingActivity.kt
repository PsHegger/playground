package pshegger.github.io.playground.visualsorting

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pshegger.github.io.playground.R
import pshegger.github.io.playground.visualsorting.algorithm.*

class VisualSortingActivity : AppCompatActivity() {

    private val info by lazy { findViewById<TextView>(R.id.info) }
    private val buttonContainer by lazy { findViewById<RecyclerView>(R.id.buttonContainer) }
    private val visualizer by lazy { findViewById<VisualizerView>(R.id.visualizer) }

    private val changeListener = { name: String, compareCount: Int, accessCount: Int ->
        info.text = getString(R.string.visualsorting_info_text, name, compareCount, accessCount)
    }

    private val algorithms = listOf(
        AlgorithmData("Bubble") { BubbleSort(changeListener) },
        AlgorithmData("Cocktail") { CocktailSort(changeListener) },
        AlgorithmData("Insertion") { InsertionSort(changeListener) },
        AlgorithmData("Shell") { ShellSort(changeListener) },
        AlgorithmData("Gnome") { GnomeSort(changeListener) },
        AlgorithmData("Bitonic") { BitonicSort(changeListener) },
        AlgorithmData("Heap") { HeapSort(changeListener) },
        AlgorithmData("Quick") { QuickSort(changeListener) },
        AlgorithmData("Radix (Bin)") { BinaryRadixSort(changeListener) },
        AlgorithmData("Radix (Dec)") { DecimalRadixSort(changeListener) },
        AlgorithmData("Bogo") { BogoSort(changeListener) },
        AlgorithmData("Sleep") { SleepSort(changeListener) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_visual_sorting)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        buttonContainer.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = AlgorithmAdapter(algorithms) { visualizer.algorithm = it }

            info.setOnClickListener {
                val nextI = (visualizer.mode.ordinal + 1) % VisualizerView.Mode.entries.size
                visualizer.mode = VisualizerView.Mode.entries[nextI]
            }
        }
    }

    data class AlgorithmData(val name: String, val init: () -> SortingAlgorithm)

    private class AlgorithmAdapter(
        private val algorithms: List<AlgorithmData>,
        private val clickListener: (SortingAlgorithm) -> Unit
    ) : RecyclerView.Adapter<AlgorithmAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val root = LayoutInflater.from(parent.context).inflate(R.layout.item_algorithm, parent, false)
            return ViewHolder(root, clickListener)
        }

        override fun getItemCount(): Int = algorithms.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(algorithms[position])
        }

        inner class ViewHolder(
            itemView: View,
            private val clickListener: (SortingAlgorithm) -> Unit
        ) : RecyclerView.ViewHolder(itemView) {

            private val button = itemView.findViewById<TextView>(R.id.button)

            fun bind(algorithm: AlgorithmData) {
                button.apply {
                    text = algorithm.name
                    setOnClickListener {
                        clickListener(algorithm.init())
                    }
                }
            }
        }
    }
}
