package pshegger.github.io.playground.gamedev.scenes

import android.graphics.Canvas
import android.graphics.Color
import pshegger.github.io.playground.gamedev.GameSurfaceView
import pshegger.github.io.playground.gamedev.Scene
import pshegger.github.io.playground.gamedev.scenes.menu.MainMenuScene

class EmptyScene(private val gameSurfaceView: GameSurfaceView) : Scene {
    override fun sizeChanged(width: Int, height: Int) {
    }

    override fun update(deltaTime: Long) {
    }

    override fun render(canvas: Canvas) {
        canvas.drawColor(Color.rgb(154, 206, 235))
    }

    override fun onBackPressed(): Boolean {
        gameSurfaceView.scene = MainMenuScene(gameSurfaceView)
        return true
    }
}
