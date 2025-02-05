package pshegger.github.io.playground.gamedev.scenes.menu

import pshegger.github.io.playground.gamedev.GameSurfaceView
import pshegger.github.io.playground.gamedev.scenes.balls.BouncingBallsScene
import pshegger.github.io.playground.gamedev.scenes.balls.SimpleBallsScene

class BallsMenuScene(gameSurfaceView: GameSurfaceView) : BaseMenuScene(gameSurfaceView) {
    override val title: String = "Balls"

    override val scenes: List<MenuItem> = listOf(
            MenuItem("Simple") { SimpleBallsScene(gameSurfaceView) },
            MenuItem("Bouncing") { BouncingBallsScene(gameSurfaceView) },
        )

    override fun onBackPressed(): Boolean {
        gameSurfaceView.scene = MainMenuScene(gameSurfaceView)
        return true
    }
}
