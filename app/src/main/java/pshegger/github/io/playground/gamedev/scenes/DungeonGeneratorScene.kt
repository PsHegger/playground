package pshegger.github.io.playground.gamedev.scenes

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import pshegger.github.io.playground.gamedev.GameSurfaceView
import pshegger.github.io.playground.gamedev.Scene
import pshegger.github.io.playground.gamedev.algorithms.DungeonGenerator
import pshegger.github.io.playground.gamedev.geometry.Vector
import pshegger.github.io.playground.gamedev.hud.Button
import pshegger.github.io.playground.gamedev.scenes.menu.MainMenuScene

class DungeonGeneratorScene(private val gameSurfaceView: GameSurfaceView) : Scene {
    companion object {
        private const val SCALE_FACTOR = 20
        private const val SCREEN_MARGIN = 20
    }

    private val generator = DungeonGenerator(
        DungeonGenerator.Settings(
            0.6f,
            4,
            10,
            1.3f,
            1.618f,
            1,
            roomMargin = 0.6f
        )
    )
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
    }
    private val textPaint = Paint().apply {
        textSize = 42f
        color = Color.GRAY
    }
    private val edgePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL_AND_STROKE
        color = Color.BLACK
    }

    private var btnRestart: Button? = null
    private var btnInstant: Button? = null
    private var width: Int = 0
    private var height: Int = 0
    private var scaledWidth: Int = 0
    private var scaledHeight: Int = 0

    override fun sizeChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
        scaledWidth = width / SCALE_FACTOR
        scaledHeight = height / SCALE_FACTOR

        btnRestart = Button("RES", width - 200f, height - 120f).apply {
            setOnClickListener { generator.reset(scaledWidth, scaledHeight) }
        }

        btnInstant = Button("INS", width - 400f, height - 120f).apply {
            setOnClickListener {
                generator.reset(scaledWidth, scaledHeight)
                generator.generateAll()
            }
        }

        generator.reset(scaledWidth, scaledHeight)
    }

    override fun update(deltaTime: Long) {
        if (generator.canGenerateMore) {
            generator.nextStep()
        }

        btnRestart?.update(deltaTime, gameSurfaceView.touch)
        btnInstant?.update(deltaTime, gameSurfaceView.touch)
    }

    override fun render(canvas: Canvas) {
        canvas.drawColor(Color.rgb(154, 206, 235))

        val zoomInfo = calculateScale()

        generator.rooms.forEach { roomState ->
            paint.color = when (roomState.state) {
                DungeonGenerator.RoomState.State.Generated -> Color.DKGRAY
                DungeonGenerator.RoomState.State.Placed -> Color.BLUE
                DungeonGenerator.RoomState.State.Moving -> Color.RED
                DungeonGenerator.RoomState.State.Selected -> when (roomState.room.type) {
                    DungeonGenerator.Room.RoomType.Entrance -> Color.rgb(167, 65, 101)
                    DungeonGenerator.Room.RoomType.QuestObjective -> Color.rgb(141, 103, 8)
                    else -> Color.BLACK
                }
            }
            paint.strokeWidth = when (roomState.state) {
                DungeonGenerator.RoomState.State.Selected -> when (roomState.room.type) {
                    DungeonGenerator.Room.RoomType.Entrance -> 5f
                    else -> 3f
                }
                else -> 1f
            }
            canvas.drawRect(roomState.room.getRect(zoomInfo), paint)
        }

        generator.edges.forEach { edge ->
            val start = calculatePosition(edge.start, zoomInfo)
            val end = calculatePosition(edge.end, zoomInfo)

            edgePaint.strokeWidth = 10f
            canvas.drawPoint(start.x, start.y, edgePaint)
            canvas.drawPoint(end.x, end.y, edgePaint)
            edgePaint.strokeWidth = 2f
            canvas.drawLine(start.x, start.y, end.x, end.y, edgePaint)
        }

        canvas.drawText("Count: ${generator.rooms.size}", 10f, height - 10f, textPaint)

        btnRestart?.render(canvas)
        btnInstant?.render(canvas)
    }

    override fun onBackPressed() {
        gameSurfaceView.scene = MainMenuScene(gameSurfaceView)
    }

    private fun calculateScale(): ZoomInfo {
        val minLeft = generator.rooms.asSequence().map { it.room.topLeft.x }.min() ?: 0f
        val maxRight = generator.rooms.asSequence().map { it.room.topLeft.x + it.room.width }.max() ?: 0f
        val minTop = generator.rooms.asSequence().map { it.room.topLeft.y }.min() ?: 0f
        val maxBottom = generator.rooms.asSequence().map { it.room.topLeft.y + it.room.height }.max() ?: 0f

        val maxWidth = maxRight - minLeft
        val maxHeight = maxBottom - minTop

        val horizontalScale = (width - 2 * SCREEN_MARGIN) / maxWidth
        val verticalScale = (height - 2 * SCREEN_MARGIN) / maxHeight
        val scale = listOf(verticalScale, horizontalScale, SCALE_FACTOR.toFloat()).min()!!

        val topLeft = calculatePosition(Vector(minLeft, minTop), ZoomInfo(scale, 0f, 0f))
        val bottomRight = calculatePosition(Vector(maxRight, maxBottom), ZoomInfo(scale, 0f, 0f))

        val translateX = when {
            topLeft.x < SCREEN_MARGIN -> SCREEN_MARGIN - topLeft.x
            bottomRight.x > width - SCREEN_MARGIN -> (width - SCREEN_MARGIN) - bottomRight.x
            else -> 0f
        }
        val translateY = when {
            topLeft.y < SCREEN_MARGIN -> SCREEN_MARGIN - topLeft.y
            bottomRight.y > height - SCREEN_MARGIN -> (height - SCREEN_MARGIN) - bottomRight.y
            else -> 0f
        }

        return ZoomInfo(scale, translateX, translateY)
    }

    private fun DungeonGenerator.Room.getRect(zoomInfo: ZoomInfo): RectF {
        val scaledTopLeft = calculatePosition(topLeft, zoomInfo)
        val right = scaledTopLeft.x + width * zoomInfo.scaleFactor
        val bottom = scaledTopLeft.y + height * zoomInfo.scaleFactor

        return android.graphics.RectF(scaledTopLeft.x, scaledTopLeft.y, right, bottom)
    }

    private fun calculatePosition(v: Vector, zoomInfo: ZoomInfo) = Vector(
        (v.x - scaledWidth / 2f) * zoomInfo.scaleFactor + width / 2f + zoomInfo.translateX,
        (v.y - scaledHeight / 2f) * zoomInfo.scaleFactor + height / 2f + zoomInfo.translateY
    )

    private data class ZoomInfo(val scaleFactor: Float, val translateX: Float, val translateY: Float)
}
