package pshegger.github.io.playground.gamedev.scenes.menu

import pshegger.github.io.playground.gamedev.GameSurfaceView
import pshegger.github.io.playground.gamedev.scenes.balls.BouncingBallsScene
import pshegger.github.io.playground.gamedev.scenes.balls.SimpleBallsScene

class BallsMenuScene(gameSurfaceView: GameSurfaceView) : BaseMenuScene(gameSurfaceView) {
    override val title: String
        get() = "Balls"

    override fun onBackPressed() {
        gameSurfaceView.scene = MainMenuScene(gameSurfaceView)
    }

    override val scenes: List<MenuItem>
        get() = listOf(
            MenuItem("Simple", SimpleBallsScene(gameSurfaceView)),
            MenuItem("Bouncing", BouncingBallsScene(gameSurfaceView))
        )
}
