package pshegger.github.io.playground.gamedev.hud

import android.graphics.Canvas
import pshegger.github.io.playground.gamedev.utils.Touch

interface HudElement {
    fun update(deltaTime: Long, touch: Touch?)
    fun render(canvas: Canvas)
}
