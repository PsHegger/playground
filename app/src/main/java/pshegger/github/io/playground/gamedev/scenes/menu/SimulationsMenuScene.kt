package pshegger.github.io.playground.gamedev.scenes.menu

import pshegger.github.io.playground.gamedev.GameSurfaceView
import pshegger.github.io.playground.gamedev.scenes.simulations.BoidsScene
import pshegger.github.io.playground.gamedev.scenes.simulations.ClothSimulationScene
import pshegger.github.io.playground.gamedev.scenes.simulations.RopeSimulationScene

class SimulationsMenuScene(gameSurfaceView: GameSurfaceView) : BaseMenuScene(gameSurfaceView) {
    override val title: String = "Simulations"

    override fun onBackPressed(): Boolean {
        gameSurfaceView.scene = MainMenuScene(gameSurfaceView)
        return true
    }

    override val scenes: List<MenuItem> = listOf(
        MenuItem("Boids") { BoidsScene(gameSurfaceView) },
        MenuItem("Rope") { RopeSimulationScene(gameSurfaceView) },
        MenuItem("Cloth") { ClothSimulationScene(gameSurfaceView) },
    )
}
