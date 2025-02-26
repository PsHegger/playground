package pshegger.github.io.playground

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pshegger.github.io.playground.conway.ConwayActivity
import pshegger.github.io.playground.gamedev.GameDevActivity
import pshegger.github.io.playground.mandelbrot.MandelbrotActivity
import pshegger.github.io.playground.mandelbrotrs.MandelbrotRsActivity
import pshegger.github.io.playground.rgbtextview.RgbTextViewActivity
import pshegger.github.io.playground.visualsorting.VisualSortingActivity

class MainActivity : AppCompatActivity() {

    companion object {
        private val experiments = listOf(
            Experiment(R.mipmap.ic_launcher, "Visual Sorting", VisualSortingActivity::class.java),
            Experiment(R.mipmap.ic_launcher, "Conway", ConwayActivity::class.java),
            Experiment(R.mipmap.ic_launcher, "Mandelbrot", MandelbrotActivity::class.java),
            Experiment(R.mipmap.ic_launcher, "Mandelbrot (RS)", MandelbrotRsActivity::class.java),
            Experiment(R.mipmap.ic_gamedev, "GameDev", GameDevActivity::class.java),
            Experiment(R.mipmap.ic_launcher, "RGB TextView", RgbTextViewActivity::class.java)
        ).sortedBy { it.name }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<RecyclerView>(R.id.experimentList).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = ExperimentAdapter(experiments) {
                startActivity(Intent(this@MainActivity, it.target))
            }
        }
    }

    class ExperimentAdapter(
        private val experiments: List<Experiment>,
        private val itemSelected: (Experiment) -> Unit
    ) : RecyclerView.Adapter<ExperimentAdapter.ExperimentViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExperimentViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_experiment, parent, false)
            return ExperimentViewHolder(view, itemSelected)
        }

        override fun getItemCount(): Int = experiments.size

        override fun onBindViewHolder(holder: ExperimentViewHolder, position: Int) {
            holder.bind(experiments[position])
        }

        class ExperimentViewHolder(
            itemView: View,
            private val itemSelected: (Experiment) -> Unit
        ) : RecyclerView.ViewHolder(itemView) {

            private val experimentIcon = itemView.findViewById<ImageView>(R.id.experimentIcon)
            private val experimentName = itemView.findViewById<TextView>(R.id.experimentName)

            fun bind(experiment: Experiment) {
                experimentIcon.setImageResource(experiment.icon)
                experimentName.text = experiment.name
                itemView.setOnClickListener { itemSelected(experiment) }
            }
        }
    }
}
