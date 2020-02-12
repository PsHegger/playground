package pshegger.github.io.playground.gamedev

import android.graphics.Canvas
import android.graphics.Color

interface Scene {
    fun sizeChanged(width: Int, height: Int)
    fun update(deltaTime: Long)
    fun render(canvas: Canvas)
    fun onBackPressed()

    fun fpsColor(): Int = Color.BLACK
}
