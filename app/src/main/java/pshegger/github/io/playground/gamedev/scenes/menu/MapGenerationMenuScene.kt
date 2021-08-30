package pshegger.github.io.playground.gamedev.scenes.menu

import pshegger.github.io.playground.gamedev.GameSurfaceView
import pshegger.github.io.playground.gamedev.scenes.map.DelaunayBuildingScene
import pshegger.github.io.playground.gamedev.scenes.map.MapGeneratorScene
import pshegger.github.io.playground.gamedev.scenes.map.SimplexGeneratorScene
import pshegger.github.io.playground.gamedev.scenes.map.VoronoiScene

class MapGenerationMenuScene(gameSurfaceView: GameSurfaceView) : BaseMenuScene(gameSurfaceView) {
    override val title: String
        get() = "Map Generation"

    override val scenes: List<MenuItem> = listOf(
            MenuItem("Delaunay Building") { DelaunayBuildingScene(gameSurfaceView) },
            MenuItem("Simplex Noise") { SimplexGeneratorScene(gameSurfaceView) },
            MenuItem("Voronoi") { VoronoiScene(gameSurfaceView) },
            MenuItem("Map Generation") { MapGeneratorScene(gameSurfaceView) },
        )

    override fun onBackPressed() {
        gameSurfaceView.scene = MainMenuScene(gameSurfaceView)
    }
}
