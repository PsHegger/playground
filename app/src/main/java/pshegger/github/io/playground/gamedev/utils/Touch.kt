package pshegger.github.io.playground.gamedev.utils

import pshegger.github.io.playground.gamedev.geometry.Vector

data class Input(var touch: Touch? = null, var gravity: Vector? = null)

data class Touch(val x: Float, val y: Float)
