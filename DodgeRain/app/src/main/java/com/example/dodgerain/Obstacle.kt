package com.example.dodgerain

import android.graphics.RectF

data class Obstacle(
    var x: Float,
    var y: Float = -40f,
    val width: Float = 60f,
    val height: Float = 40f,
) {
    fun update(speedPx: Float) {
        y += speedPx
    }

    fun isOffScreen(screenHeight: Int): Boolean = y > screenHeight

    fun toRect() = RectF(x, y, x + width, y + height)
}
