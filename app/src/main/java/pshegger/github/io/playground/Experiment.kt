package pshegger.github.io.playground

import android.app.Activity

data class Experiment(val icon: Int, val name: String, val target: Class<out Activity>)
