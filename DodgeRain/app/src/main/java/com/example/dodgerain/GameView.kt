package com.example.dodgerain

import android.content.Context
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private lateinit var engine: GameEngine
    private lateinit var renderer: GameRenderer
    private var gameThread: GameThread? = null

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(h: SurfaceHolder) {
        val w = h.surfaceFrame.width()
        val ht = h.surfaceFrame.height()
        engine = GameEngine(w, ht)
        renderer = GameRenderer(w, ht)
        gameThread = GameThread(h, engine, renderer).also { it.start() }
    }

    override fun surfaceChanged(h: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(h: SurfaceHolder) {
        gameThread?.apply {
            running = false
            join()
        }
        gameThread = null
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (engine.state == GameState.IDLE || engine.state == GameState.GAME_OVER) {
                    engine.startGame()
                } else {
                    engine.onTouchX(event.x)
                }
            }
            MotionEvent.ACTION_MOVE -> engine.onTouchX(event.x)
        }
        return true
    }

    private class GameThread(
        private val holder: SurfaceHolder,
        private val engine: GameEngine,
        private val renderer: GameRenderer,
    ) : Thread() {

        @Volatile
        var running = true

        override fun run() {
            var lastTime = System.currentTimeMillis()
            while (running) {
                val now = System.currentTimeMillis()
                val delta = now - lastTime
                lastTime = now

                engine.update(delta)

                val canvas = holder.lockCanvas() ?: continue
                try {
                    renderer.render(canvas, engine)
                } finally {
                    holder.unlockCanvasAndPost(canvas)
                }

                val elapsed = System.currentTimeMillis() - now
                val sleep = 16L - elapsed
                if (sleep > 0) sleep(sleep)
            }
        }
    }
}
