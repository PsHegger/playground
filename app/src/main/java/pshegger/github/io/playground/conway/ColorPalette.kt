package pshegger.github.io.playground.conway

import android.content.Context
import androidx.core.content.ContextCompat

data class ColorPalette(val bgColor: Int, val gridColor: Int, val fgColor: Int) {
    companion object {
        fun load(context: Context, n: Int): ColorPalette {
            val bg = context.getColor("palette${n}Bg")
            val grid = context.getColor("palette${n}Grid")
            val fg = context.getColor("palette${n}Fg")

            return ColorPalette(bg, grid, fg)
        }

        private fun Context.getColor(name: String) =
            ContextCompat.getColor(this, resources.getIdentifier(name, "color", packageName))
    }
}
