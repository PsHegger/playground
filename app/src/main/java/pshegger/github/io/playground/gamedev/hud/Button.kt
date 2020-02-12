package pshegger.github.io.playground.gamedev.hud

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import pshegger.github.io.playground.gamedev.utils.Touch

class Button(
    private val text: String,
    left: Float,
    top: Float,
    right: Float? = null,
    bottom: Float? = null,
    private val bgColor: Int = Color.TRANSPARENT,
    private val borderColor: Int = Color.GRAY,
    private val textColor: Int = Color.BLACK,
    private val textSize: Float = 50f
) : HudElement {

    private var _onClick: (() -> Unit)? = null

    private val textPaint: Paint = Paint().apply {
        isAntiAlias = true
        textSize = this@Button.textSize
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        color = textColor
    }

    private val borderPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = borderColor
        strokeWidth = 3f
    }

    private val bgPaint = Paint().apply {
        style = Paint.Style.FILL
        color = bgColor
    }

    val width = if (right != null) right - left else 160f
    val height = if (bottom != null) bottom - top else 80f

    private val rect: RectF = RectF(left, top, left + width, top + height)

    var pressed = false

    override fun update(deltaTime: Long, touch: Touch?) {
        if (touch != null && rect.contains(touch.x, touch.y)) {
            pressed = true
        } else if (pressed) {
            _onClick?.invoke()
            pressed = false
        }
    }

    override fun render(canvas: Canvas) {
        val textHeight = -(textPaint.descent() + textPaint.ascent())

        canvas.drawRoundRect(rect, 15f, 15f, bgPaint)
        canvas.drawRoundRect(rect, 15f, 15f, borderPaint)
        canvas.drawText(text, rect.left + width / 2f, rect.top + (height + textHeight) / 2f, textPaint)
    }

    fun setOnClickListener(listener: () -> Unit) {
        _onClick = listener
    }
}
