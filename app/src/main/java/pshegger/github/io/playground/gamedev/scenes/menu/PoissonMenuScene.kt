package pshegger.github.io.playground.gamedev.scenes.menu

import pshegger.github.io.playground.gamedev.GameSurfaceView
import pshegger.github.io.playground.gamedev.scenes.poisson.PoissonBestCandidateScene
import pshegger.github.io.playground.gamedev.scenes.poisson.PoissonBridsonScene

class PoissonMenuScene(gameSurfaceView: GameSurfaceView) : BaseMenuScene(gameSurfaceView) {
    override val title: String
        get() = "Poisson"

    override fun onBackPressed() {
        gameSurfaceView.scene = MainMenuScene(gameSurfaceView)
    }

    override val scenes: List<MenuItem>
        get() = listOf(
            MenuItem("Best-Candidate", PoissonBestCandidateScene(gameSurfaceView)),
            MenuItem("Bridson", PoissonBridsonScene(gameSurfaceView))
        )
}
