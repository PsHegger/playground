package pshegger.github.io.playground.gamedev.scenes.menu

import pshegger.github.io.playground.gamedev.GameSurfaceView
import pshegger.github.io.playground.gamedev.scenes.map.DelaunayBuildingScene
import pshegger.github.io.playground.gamedev.scenes.map.MapGeneratorScene
import pshegger.github.io.playground.gamedev.scenes.map.SimplexGeneratorScene
import pshegger.github.io.playground.gamedev.scenes.map.VoronoiScene

class MapGenerationMenuScene(gameSurfaceView: GameSurfaceView) : BaseMenuScene(gameSurfaceView) {
    override val scenes: List<MenuItem>
        get() = listOf(
            MenuItem("Delaunay Building", DelaunayBuildingScene(gameSurfaceView)),
            MenuItem("Simplex Noise", SimplexGeneratorScene(gameSurfaceView)),
            MenuItem("Voronoi", VoronoiScene(gameSurfaceView)),
            MenuItem("Map Generation", MapGeneratorScene(gameSurfaceView))
        )

    override val title: String
        get() = "Map Generation"

    override fun onBackPressed() {
        gameSurfaceView.scene = MainMenuScene(gameSurfaceView)
    }
}
