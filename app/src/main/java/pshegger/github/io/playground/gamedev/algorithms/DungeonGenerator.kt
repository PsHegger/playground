package pshegger.github.io.playground.gamedev.algorithms

import android.graphics.RectF
import android.util.Log
import pshegger.github.io.playground.gamedev.geometry.Edge
import pshegger.github.io.playground.gamedev.geometry.Vector
import pshegger.github.io.playground.gamedev.utils.weightedRandom
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

class DungeonGenerator(private val settings: Settings) {

    private val _rooms = arrayListOf<RoomState>()
    private val _corridors = arrayListOf<Edge>()

    private val edges = arrayListOf<Graph.Edge<Room>>()
    private val remainingRooms = arrayListOf<Room>()
    private val processedRooms = arrayListOf<Room>()

    val rooms: List<RoomState>
        get() = _rooms
    val corridors: List<Edge>
        get() = _corridors
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
        _corridors.clear()
        edges.clear()
        distances.clear()
        remainingRooms.clear()
        processedRooms.clear()
        generationStep = GenerationStep.RoomGeneration
    }

    fun nextStep() {
        when (generationStep) {
            GenerationStep.RoomGeneration -> generateRoom()
            GenerationStep.RoomMovement -> moveRoom()
            GenerationStep.RoomSelection -> selectRoom()
            GenerationStep.RoomPurging -> purgeRoom()
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

        if (_rooms.asSequence().map { it.room.area() }
                .sum() >= this.width * this.height * settings.fillRatio) {
            generationStep = GenerationStep.RoomMovement
        }
    }

    private fun moveRoom() {
        if (_rooms.none { it.state == RoomState.State.Placed }) {
            _rooms[rng.nextInt(_rooms.size)].state = RoomState.State.Placed
        }

        val movingRoom = _rooms.firstOrNull { it.state == RoomState.State.Moving }

        if (movingRoom != null) {
            val moveVector =
                Vector(cos(movingRoom.direction).toFloat(), sin(movingRoom.direction).toFloat())
            var ctr = 0
            var finalPlace = false
            while (ctr < 8 && !finalPlace) {
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
        val possibleRooms =
            _rooms.filter { it.state == RoomState.State.Placed && it.room.area() >= minArea }
        possibleRooms.take(1).forEach { it.state = RoomState.State.Selected }
        if (possibleRooms.size == 1) {
            generationStep = GenerationStep.RoomPurging
        }
        delayCtr = 0
    }

    private fun purgeRoom() {
        if (rooms.all { it.state == RoomState.State.Selected }) {
            generationStep = GenerationStep.SpanningTreeGeneration
            return
        }
        _rooms.first { it.state != RoomState.State.Selected }
            .let { _rooms.remove(it) }
    }

    private fun generateSpanningTree() {
        val graph = Graph(selectedRooms.map { it.room }, edges)

        selectedRooms.asSequence().mapIndexed { i, x ->
            selectedRooms.mapIndexed { j, y ->
                if (i < j) Pair(x.room, y.room) else null
            }.filterNotNull()
        }.flatten()
            .filterNot { graph.isRouteAvailable(it.first, it.second) }
            .sortedBy { it.first.center.distance(it.second.center) }
            .take(1)
            .forEach { edges.add(Graph.Edge(it.first, it.second)) }

        if (edges.size == selectedRooms.size - 1) {
            selectedRooms[0].room.type = Room.RoomType.Entrance
            entranceCtr = 1
            entranceCandidate = 0
            generationStep = GenerationStep.EntranceSelection
        }
    }

    private fun selectEntrance() {
        delayCtr++
        if (delayCtr < 6) {
            return
        }

        if (entranceCtr < selectedRooms.size) {
            val graph = Graph(selectedRooms.map { it.room }, edges)
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
        val graph = Graph(selectedRooms.map { it.room }, edges)
        val leaveRooms =
            selectedRooms.filter { it.room.type == Room.RoomType.Room && graph.neighbors(it.room).size == 1 }
        if (leaveRooms.isNotEmpty()) {
            val entrance = selectedRooms.first { it.room.type == Room.RoomType.Entrance }
            leaveRooms.firstOrNull()?.let { roomState ->
                val distance =
                    graph.shortestPath(entrance.room, roomState.room)?.let { it.size - 1 }
                        ?: 0
                val weight = settings.questObjectiveDistanceFactor.toDouble()
                    .pow(distance.toDouble()).roundToInt()
                distances.add(Pair(roomState, weight))
                roomState.room.type = Room.RoomType.QuestObjective
            }
        } else {
            val questObjective = distances.weightedRandom(rng)
            selectedRooms
                .filter { it.room.type == Room.RoomType.QuestObjective }
                .forEach {
                    it.room.type =
                        if (it == questObjective) Room.RoomType.QuestObjective else Room.RoomType.Room
                }
            remainingRooms.clear()
            processedRooms.clear()
            remainingRooms.add(_rooms.first { it.room.type == Room.RoomType.Entrance }.room)
            generationStep = GenerationStep.GenerateCorridors
        }

        delayCtr = 0
    }

    private fun generateCorridors() {
        if (remainingRooms.isEmpty()) {
            generationStep = GenerationStep.Finished
            return
        }

        delayCtr++
        if (delayCtr < 6) return

        val graph = Graph(selectedRooms.map { it.room }, edges)
        val currentRoom = remainingRooms.random(rng)
        remainingRooms.remove(currentRoom)
        val nonProcessedNeighbors = graph.neighbors(currentRoom)
            .flatMap { listOf(it.start, it.end) }
            .filter { it != currentRoom && it !in processedRooms }

        if (nonProcessedNeighbors.isEmpty()) {
            processedRooms.add(currentRoom)
            return
        }

        for (room in nonProcessedNeighbors) {
            val distances = mapOf(
                Side.Left to currentRoom.left - room.right,
                Side.Top to currentRoom.top - room.bottom,
                Side.Right to room.left - currentRoom.right,
                Side.Bottom to room.top - currentRoom.bottom,
            )
            val closestSide = distances.filter { it.value > 0 }
                .minByOrNull { it.value }
                ?.key ?: continue
            val (currentStart, currentEnd) = when (closestSide) {
                Side.Left, Side.Right -> Pair(
                    currentRoom.top + settings.corridorMargin,
                    currentRoom.bottom - settings.corridorMargin
                )
                Side.Top, Side.Bottom -> Pair(
                    currentRoom.left + settings.corridorMargin,
                    currentRoom.right - settings.corridorMargin
                )
            }
            val (targetStart, targetEnd) = when (closestSide) {
                Side.Left, Side.Right -> Pair(
                    room.top + settings.corridorMargin,
                    room.bottom - settings.corridorMargin
                )
                Side.Top, Side.Bottom -> Pair(
                    room.left + settings.corridorMargin,
                    room.right - settings.corridorMargin
                )
            }
            var minStart = -1f
            var maxEnd = -1f
            var i = 0
            while (currentStart + i <= currentEnd) {    // find the overlapping range
                if (currentStart + i in targetStart..targetEnd) {
                    if (minStart < 0) {
                        minStart = currentStart + i
                    }
                    maxEnd = currentStart + i
                }
                i++
            }

            @Suppress("NAME_SHADOWING")
            if (minStart > 0) {
                val p = if (minStart == maxEnd) {
                    minStart
                } else {
                    rng.nextDouble(minStart.toDouble(), maxEnd.toDouble()).toFloat()
                }
                _corridors.add(
                    when (closestSide) {
                        Side.Left -> Edge(Vector(currentRoom.left, p), Vector(room.right, p))
                        Side.Top -> Edge(Vector(p, currentRoom.top), Vector(p, room.bottom))
                        Side.Right -> Edge(Vector(currentRoom.right, p), Vector(room.left, p))
                        Side.Bottom -> Edge(Vector(p, currentRoom.bottom), Vector(p, room.top))
                    }
                )
            } else {
                val targetSide = when (closestSide) {
                    Side.Left, Side.Right -> if (room.center.y < currentRoom.center.y) Side.Bottom else Side.Top
                    Side.Top, Side.Bottom -> if (room.center.x < currentRoom.center.x) Side.Right else Side.Left
                }
                val (targetStart, targetEnd) = when (closestSide) {
                    Side.Left, Side.Right -> Pair(
                        room.left + settings.corridorMargin,
                        room.right - settings.corridorMargin
                    )
                    Side.Top, Side.Bottom -> Pair(
                        room.top + settings.corridorMargin,
                        room.bottom - settings.corridorMargin
                    )
                }

                val p = rng.nextDouble(currentStart.toDouble(), currentEnd.toDouble()).toFloat()
                val p2 = rng.nextDouble(targetStart.toDouble(), targetEnd.toDouble()).toFloat()

                val c1 = when (closestSide) {
                    Side.Left -> Edge(Vector(currentRoom.left, p), Vector(p2, p))
                    Side.Top -> Edge(Vector(p, currentRoom.top), Vector(p, p2))
                    Side.Right -> Edge(Vector(currentRoom.right, p), Vector(p2, p))
                    Side.Bottom -> Edge(Vector(p, currentRoom.bottom), Vector(p, p2))
                }
                val c2 = when (targetSide) {
                    Side.Left -> Edge(c1.end, Vector(room.left, p2))
                    Side.Top -> Edge(c1.end, Vector(p2, room.top))
                    Side.Right -> Edge(c1.end, Vector(room.right, p2))
                    Side.Bottom -> Edge(c1.end, Vector(p2, room.bottom))
                }
                _corridors.add(c1)
                _corridors.add(c2)
            }
        }

        processedRooms.add(currentRoom)
        remainingRooms.addAll(nonProcessedNeighbors)

        delayCtr = 0
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
    data class Room(
        var topLeft: Vector,
        val width: Int,
        val height: Int,
        var type: RoomType = RoomType.Room
    ) {
        val center: Vector
            get() = Vector(topLeft.x + width / 2f, topLeft.y + height / 2f)

        val left: Float
            get() = topLeft.x
        val top: Float
            get() = topLeft.y
        val right: Float
            get() = left + width
        val bottom: Float
            get() = top + height

        fun area() = width * height
        fun intersects(other: Room, margin: Float) = getRect(margin).intersect(other.getRect(0f))
        private fun getRect(margin: Float) = RectF(
            left - margin,
            top - margin,
            right + margin,
            bottom + margin,
        )

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
        val corridorMargin: Float = 0f,
        val seed: Long? = null
    )

    private enum class GenerationStep {
        RoomGeneration,
        RoomMovement,
        RoomSelection,
        RoomPurging,
        SpanningTreeGeneration,
        EntranceSelection,
        QuestObjectiveSelection,
        GenerateCorridors,
        Finished
    }

    private enum class Side {
        Left, Top, Right, Bottom,
    }
    //</editor-fold>
}
