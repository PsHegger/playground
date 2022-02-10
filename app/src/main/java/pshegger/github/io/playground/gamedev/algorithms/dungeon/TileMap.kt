package pshegger.github.io.playground.gamedev.algorithms.dungeon

class TileMap(val width: Int, val height: Int) {


    private val tiles: MutableList<TileType> = List(width * height) {
        TileType.Unset
    }.toMutableList()

    operator fun get(i: Int): TileType = tiles[i]
    operator fun get(x: Int, y: Int): TileType = tiles[y * width + x]
    operator fun set(x: Int, y: Int, type: TileType) {
        tiles[y * width + x] = type
    }

    enum class TileType {
        Unset, Void, Room, StartRoom, QuestRoom, Corridor,
    }
}
