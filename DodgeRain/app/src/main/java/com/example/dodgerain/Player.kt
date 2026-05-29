package com.example.dodgerain

import android.graphics.RectF

data class Player(
    var x: Float = 0f,
    var y: Float = 0f,
    val width: Float = 80f,
    val height: Float = 80f,
) {
    fun moveTo(targetX: Float, screenWidth: Int) {
        x = (targetX - width / 2).coerceIn(0f, screenWidth - width)
    }

    fun toRect() = RectF(x, y, x + width, y + height)
}
