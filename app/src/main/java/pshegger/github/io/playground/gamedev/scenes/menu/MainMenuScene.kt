package pshegger.github.io.playground.gamedev.scenes.menu

import pshegger.github.io.playground.gamedev.GameDevActivity
import pshegger.github.io.playground.gamedev.GameSurfaceView
import pshegger.github.io.playground.gamedev.scenes.DungeonGeneratorScene

class MainMenuScene(gameSurfaceView: GameSurfaceView) : BaseMenuScene(gameSurfaceView) {
    override val title: String = "Main Menu"

    override val scenes: List<MenuItem> = listOf(
            MenuItem("Balls") { BallsMenuScene(gameSurfaceView) },
            MenuItem("Simulations") { SimulationsMenuScene(gameSurfaceView) },
            MenuItem("Poisson") { PoissonMenuScene(gameSurfaceView) },
            MenuItem("Maze") { MazeMenuScene(gameSurfaceView) },
            MenuItem("Map Generation") { MapGenerationMenuScene(gameSurfaceView) },
            MenuItem("Dungeon Generation") { DungeonGeneratorScene(gameSurfaceView) },
        )

    override fun onBackPressed() {
        GameDevActivity.instance.finish()
    }
}
