package pshegger.github.io.playground.gamedev.scenes.menu

import pshegger.github.io.playground.gamedev.GameSurfaceView
import pshegger.github.io.playground.gamedev.algorithms.maze.RandomDepthFirstGenerator
import pshegger.github.io.playground.gamedev.algorithms.maze.RandomTraversalGenerator
import pshegger.github.io.playground.gamedev.scenes.maze.MazeScene
import pshegger.github.io.playground.gamedev.scenes.maze.PathFindingScene

class MazeMenuScene(gameSurfaceView: GameSurfaceView) : BaseMenuScene(gameSurfaceView) {
    override fun onBackPressed() {
        gameSurfaceView.scene = MainMenuScene(gameSurfaceView)
    }

    override val scenes: List<MenuItem>
        get() = listOf(
            MenuItem("Random Traversal", MazeScene(gameSurfaceView, RandomTraversalGenerator())),
            MenuItem("Random Depth-First", MazeScene(gameSurfaceView, RandomDepthFirstGenerator())),
            MenuItem("Path-Finding", PathFindingScene(gameSurfaceView))
        )
    override val title: String
        get() = "Maze"
}
