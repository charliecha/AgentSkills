package com.example.dodgerain

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface

class GameRenderer(private val screenWidth: Int, private val screenHeight: Int) {

    private val playerPaint = Paint().apply { color = Color.CYAN }
    private val obstaclePaint = Paint().apply { color = Color.RED }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 48f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    private val timePaint = Paint().apply {
        color = Color.WHITE
        textSize = 48f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.RIGHT
    }
    private val heartPaint = Paint().apply {
        color = Color.RED
        textSize = 48f
    }
    private val heartStrings = arrayOf("", "♥", "♥♥", "♥♥♥")
    private val bgPaint = Paint().apply { color = Color.BLACK }

    fun render(canvas: Canvas, engine: GameEngine) {
        canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), bgPaint)
        when (engine.state) {
            GameState.IDLE -> renderIdle(canvas)
            GameState.PLAYING -> renderPlaying(canvas, engine)
            GameState.GAME_OVER -> renderGameOver(canvas, engine)
        }
    }

    private fun renderIdle(canvas: Canvas) {
        canvas.drawText("Touch to Start", screenWidth / 2f, screenHeight / 2f, textPaint)
    }

    private fun renderPlaying(canvas: Canvas, engine: GameEngine) {
        engine.obstacles.forEach { canvas.drawRect(it.toRect(), obstaclePaint) }
        canvas.drawRect(engine.player.toRect(), playerPaint)

        canvas.drawText(heartStrings[engine.lives.coerceIn(0, 3)], 80f, 70f, heartPaint)

        val time = "%02d:%02d".format(engine.scoreSeconds / 60, engine.scoreSeconds % 60)
        canvas.drawText(time, screenWidth - 20f, 70f, timePaint)
    }

    private fun renderGameOver(canvas: Canvas, engine: GameEngine) {
        canvas.drawText("Game Over", screenWidth / 2f, screenHeight / 2f - 60f, textPaint)
        val time = "Time: %02d:%02d".format(engine.scoreSeconds / 60, engine.scoreSeconds % 60)
        canvas.drawText(time, screenWidth / 2f, screenHeight / 2f + 20f, textPaint)
        canvas.drawText("Tap to Restart", screenWidth / 2f, screenHeight / 2f + 100f, textPaint)
    }
}
