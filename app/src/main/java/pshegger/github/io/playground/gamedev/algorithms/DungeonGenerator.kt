package pshegger.github.io.playground.gamedev.algorithms

import android.graphics.RectF
import android.util.Log
import pshegger.github.io.playground.gamedev.geometry.Edge
import pshegger.github.io.playground.gamedev.geometry.Vector
import pshegger.github.io.playground.gamedev.utils.weightedRandom
import kotlin.random.Random

class DungeonGenerator(private val settings: Settings) {
    private val _rooms = arrayListOf<RoomState>()
    private val corridors = arrayListOf<Graph.Edge<Room>>()

    val rooms: List<RoomState>
        get() = _rooms
    val edges: List<Edge>
        get() = corridors.map { Edge(it.start.center, it.end.center) }
    val canGenerateMore: Boolean
        get() = generationStep != GenerationStep.Finished
    private val selectedRooms: List<RoomState>
        get() = _rooms.filter { it.state == RoomState.State.Selected }

    private var generationStep: GenerationStep = GenerationStep.RoomGeneration

    private var rng = Random(settings.seed ?: System.currentTimeMillis())

    private var width: Int = 0
    private var height: Int = 0
    private var delayCtr: Int = 0
    private var entranceCtr: Int = 0
    private var entranceCandidate: Int = 0
    private var distances = mutableListOf<Pair<RoomState, Int>>()

    fun reset(width: Int, height: Int) {
        this.width = width
        this.height = height
        rng = Random(settings.seed ?: System.currentTimeMillis())
        _rooms.clear()
        corridors.clear()
        distances.clear()
        generationStep = GenerationStep.RoomGeneration
    }

    fun nextStep() {
        when (generationStep) {
            GenerationStep.RoomGeneration -> generateRoom()
            GenerationStep.RoomMovement -> moveRoom()
            GenerationStep.RoomSelection -> selectRoom()
            GenerationStep.SpanningTreeGeneration -> generateSpanningTree()
            GenerationStep.EntranceSelection -> selectEntrance()
            GenerationStep.QuestObjectiveSelection -> selectQuestObjective()
            GenerationStep.GenerateCorridors -> generateCorridors()
            else -> Log.d("DungeonGenerator", "The generation is already over")
        }
    }

    fun generateAll() {
        while (canGenerateMore) {
            nextStep()
        }
    }

    //<editor-fold desc="Generation">
    private fun generateRoom() {
        val width = rng.nextInt(settings.maxSize - settings.minSize + 1) + settings.minSize
        val height = rng.nextInt(settings.maxSize - settings.minSize + 1) + settings.minSize
        val left = (this.width - width) / 2f
        val top = (this.height - height) / 2f
        _rooms.add(RoomState(Room(Vector(left, top), width, height), RoomState.State.Generated))

        if (_rooms.asSequence().map { it.room.area() }.sum() >= this.width * this.height * settings.fillRatio) {
            generationStep = GenerationStep.RoomMovement
        }
    }

    private fun moveRoom() {
        if (_rooms.none { it.state == RoomState.State.Placed }) {
            _rooms[rng.nextInt(_rooms.size)].state = RoomState.State.Placed
        }

        val movingRoom = _rooms.firstOrNull { it.state == RoomState.State.Moving }

        if (movingRoom != null) {
            val moveVector = Vector(Math.cos(movingRoom.direction).toFloat(), Math.sin(movingRoom.direction).toFloat())
            var ctr = 0
            var finalPlace = false
            while (ctr < 4 && !finalPlace) {
                movingRoom.room.topLeft = movingRoom.room.topLeft + moveVector
                finalPlace = _rooms
                    .filter { it.state === RoomState.State.Placed }
                    .none { it.room.intersects(movingRoom.room, settings.roomMargin) }
                ctr++
            }
            if (finalPlace) {
                movingRoom.state = RoomState.State.Placed
                if (_rooms.all { it.state == RoomState.State.Placed }) {
                    delayCtr = 0
                    generationStep = GenerationStep.RoomSelection
                }
            }
        } else {
            val remainingRooms = _rooms.filter { it.state == RoomState.State.Generated }
            remainingRooms[rng.nextInt(remainingRooms.size)].apply {
                state = RoomState.State.Moving
                direction = rng.nextDouble() * 2 * Math.PI
            }
        }
    }

    private fun selectRoom() {
        delayCtr++
        if (delayCtr < 6) {
            return
        }
        val minArea = _rooms.map { it.room.area() }.average() * settings.finalAreaRatio
        val possibleRooms = _rooms.filter { it.state == RoomState.State.Placed && it.room.area() >= minArea }
        possibleRooms.take(1).forEach { it.state = RoomState.State.Selected }
        if (possibleRooms.size == 1) {
            generationStep = GenerationStep.SpanningTreeGeneration
        }
        delayCtr = 0
    }

    private fun generateSpanningTree() {
        delayCtr++
        if (delayCtr < 6) {
            return
        }
        val graph = Graph(selectedRooms.map { it.room }, corridors)

        selectedRooms.mapIndexed { i, x ->
            selectedRooms.mapIndexed { j, y ->
                if (i < j) Pair(x.room, y.room) else null
            }.filterNotNull()
        }.flatten()
            .filterNot { graph.isRouteAvailable(it.first, it.second) }
            .sortedBy { it.first.center.distance(it.second.center) }
            .take(1)
            .forEach { corridors.add(Graph.Edge(it.first, it.second)) }

        if (corridors.size == selectedRooms.size - 1) {
            selectedRooms[0].room.type = Room.RoomType.Entrance
            entranceCtr = 1
            entranceCandidate = 0
            generationStep = GenerationStep.EntranceSelection
        }
        delayCtr = 0
    }

    private fun selectEntrance() {
        delayCtr++
        if (delayCtr < 6) {
            return
        }

        if (entranceCtr < selectedRooms.size) {
            val graph = Graph(selectedRooms.map { it.room }, corridors)
            val currBranchDistance = graph.branchDistance(selectedRooms[entranceCandidate].room)
            val newBranchDistance = graph.branchDistance(selectedRooms[entranceCtr].room)
            if (newBranchDistance > currBranchDistance) {
                entranceCandidate = entranceCtr
            }
            selectedRooms[entranceCtr - 1].room.type = Room.RoomType.Room
            selectedRooms[entranceCtr].room.type = Room.RoomType.Entrance
            entranceCtr++
        } else {
            selectedRooms[entranceCtr - 1].room.type = Room.RoomType.Room
            selectedRooms[entranceCandidate].room.type = Room.RoomType.Entrance
            generationStep = GenerationStep.QuestObjectiveSelection
        }

        delayCtr = 0
    }

    private fun selectQuestObjective() {
        delayCtr++
        if (delayCtr < 18) {
            return
        }
        val graph = Graph(selectedRooms.map { it.room }, corridors)
        val leaveRooms = selectedRooms.filter { it.room.type == Room.RoomType.Room && graph.neighbors(it.room).size == 1 }
        if (leaveRooms.isNotEmpty()) {
            val entrance = selectedRooms.first { it.room.type == Room.RoomType.Entrance }
            leaveRooms.firstOrNull()?.let { roomState ->
                val distance = graph.shortestPath(entrance.room, roomState.room)?.let { it.size - 1 }
                    ?: 0
                val weight = Math.round(Math.pow(settings.questObjectiveDistanceFactor.toDouble(), distance.toDouble())).toInt()
                distances.add(Pair(roomState, weight))
                roomState.room.type = Room.RoomType.QuestObjective
            }
        } else {
            val questObjective = distances.weightedRandom(rng)
            selectedRooms
                .filter { it.room.type == Room.RoomType.QuestObjective }
                .forEach {
                    it.room.type = if (it == questObjective) Room.RoomType.QuestObjective else Room.RoomType.Room
                }
            generationStep = GenerationStep.GenerateCorridors
        }

        delayCtr = 0
    }

    private fun generateCorridors() {
        generationStep = GenerationStep.Finished
    }
    //</editor-fold>

    private fun <T> Graph<T>.branchDistance(n: T, prev: T? = null): Int {
        val neighborCount = neighbors(n).size
        if ((prev == null && neighborCount > 1) || neighborCount > 2) {
            return 0
        }

        val next = neighbors(n).filterNot { it.otherEnd(n) == prev }.firstOrNull()?.otherEnd(n)
            ?: return 1

        return branchDistance(next, n) + 1
    }

    //<editor-fold desc="Inner classes">
    data class Room(var topLeft: Vector, val width: Int, val height: Int, var type: RoomType = RoomType.Room) {
        val center: Vector
            get() = Vector(topLeft.x + width / 2f, topLeft.y + height / 2f)

        fun area() = width * height
        fun intersects(other: Room, margin: Float) = getRect(margin).intersect(other.getRect(0f))
        private fun getRect(margin: Float) = RectF(topLeft.x - margin, topLeft.y - margin, topLeft.x + width + margin, topLeft.y + height + margin)

        enum class RoomType {
            Room, Entrance, QuestObjective
        }
    }

    data class RoomState(val room: Room, var state: State, var direction: Double = 0.0) {
        enum class State {
            Generated, Placed, Moving, Selected
        }
    }

    data class Settings(
        val fillRatio: Float,
        val minSize: Int,
        val maxSize: Int,
        val finalAreaRatio: Float,
        val questObjectiveDistanceFactor: Float,
        val corridorWidth: Int,
        val roomMargin: Float = 0f,
        val seed: Long? = null
    )

    private enum class GenerationStep {
        RoomGeneration,
        RoomMovement,
        RoomSelection,
        SpanningTreeGeneration,
        EntranceSelection,
        QuestObjectiveSelection,
        GenerateCorridors,
        Finished
    }
    //</editor-fold>
}
