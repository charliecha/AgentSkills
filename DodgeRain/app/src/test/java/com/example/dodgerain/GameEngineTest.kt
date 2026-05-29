package com.example.dodgerain

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GameEngineTest {

    private lateinit var engine: GameEngine

    @Before
    fun setup() {
        engine = GameEngine(screenWidth = 1080, screenHeight = 1920)
        engine.startGame()
    }

    // ── startGame ────────────────────────────────────────────────────────────

    @Test
    fun `startGame sets state to PLAYING`() {
        assertEquals(GameState.PLAYING, engine.state)
    }

    @Test
    fun `startGame resets lives to 3`() {
        assertEquals(3, engine.lives)
    }

    @Test
    fun `startGame resets score to 0`() {
        assertEquals(0, engine.scoreSeconds)
    }

    @Test
    fun `startGame resets speed to INITIAL_SPEED`() {
        engine.update(5000L) // advance speed
        engine.startGame()
        assertEquals(GameEngine.INITIAL_SPEED, engine.currentSpeed, 0.01f)
    }

    @Test
    fun `startGame clears obstacles`() {
        engine.obstacles.add(Obstacle(x = 100f, y = 100f))
        engine.startGame()
        assertTrue(engine.obstacles.isEmpty())
    }

    @Test
    fun `startGame centers player horizontally`() {
        val expectedX = (1080 - engine.player.width) / 2f
        assertEquals(expectedX, engine.player.x, 0.01f)
    }

    // ── score ─────────────────────────────────────────────────────────────────

    @Test
    fun `score increments after 1000ms`() {
        engine.update(1000L)
        assertEquals(1, engine.scoreSeconds)
    }

    @Test
    fun `score increments correctly over multiple seconds`() {
        engine.update(3000L)
        assertEquals(3, engine.scoreSeconds)
    }

    @Test
    fun `score does not increment before 1000ms`() {
        engine.update(999L)
        assertEquals(0, engine.scoreSeconds)
    }

    // ── speed ─────────────────────────────────────────────────────────────────

    @Test
    fun `speed increments after 5000ms`() {
        val initialSpeed = engine.currentSpeed
        engine.update(5000L)
        assertTrue(engine.currentSpeed > initialSpeed)
    }

    @Test
    fun `speed does not increment before 5000ms`() {
        val initialSpeed = engine.currentSpeed
        engine.update(4999L)
        assertEquals(initialSpeed, engine.currentSpeed, 0.01f)
    }

    @Test
    fun `speed does not exceed MAX_SPEED`() {
        repeat(100) { engine.update(5000L) }
        assertEquals(GameEngine.MAX_SPEED, engine.currentSpeed, 0.01f)
    }

    // ── obstacles ─────────────────────────────────────────────────────────────

    @Test
    fun `obstacle generated after spawn interval`() {
        engine.update(GameEngine.OBSTACLE_INTERVAL_MS + 1)
        assertTrue(engine.obstacles.isNotEmpty())
    }

    @Test
    fun `no obstacle generated before spawn interval`() {
        engine.update(GameEngine.OBSTACLE_INTERVAL_MS - 1)
        assertTrue(engine.obstacles.isEmpty())
    }

    @Test
    fun `off-screen obstacles are removed`() {
        engine.obstacles.add(Obstacle(x = 100f, y = 1925f))
        engine.update(16L)
        assertTrue(engine.obstacles.none { it.isOffScreen(1920) })
    }

    @Test
    fun `obstacle x stays within screen bounds`() {
        repeat(20) { engine.update(GameEngine.OBSTACLE_INTERVAL_MS + 1) }
        engine.obstacles.forEach { obstacle ->
            assertTrue(obstacle.x >= 0f)
            assertTrue(obstacle.x + obstacle.width <= 1080f)
        }
    }

    // ── collision & lives ─────────────────────────────────────────────────────

    @Test
    fun `collision with obstacle reduces lives by 1`() {
        placeObstacleOnPlayer()
        engine.update(16L)
        assertEquals(2, engine.lives)
    }

    @Test
    fun `invincible period prevents consecutive life loss`() {
        placeObstacleOnPlayer()
        engine.update(16L) // first hit → lives = 2, invincible
        placeObstacleOnPlayer()
        engine.update(16L) // still invincible → lives stays 2
        assertEquals(2, engine.lives)
    }

    @Test
    fun `life loss resumes after invincible period expires`() {
        placeObstacleOnPlayer()
        engine.update(16L) // first hit → lives = 2
        engine.obstacles.clear()
        engine.update(GameEngine.INVINCIBLE_DURATION_MS + 1) // expire invincible
        placeObstacleOnPlayer()
        engine.update(16L) // second hit → lives = 1
        assertEquals(1, engine.lives)
    }

    @Test
    fun `state becomes GAME_OVER when lives reach 0`() {
        // Each round: hit once, then wait out invincible period with obstacle gone
        repeat(3) {
            engine.obstacles.clear()
            placeObstacleOnPlayer()
            engine.update(16L) // trigger hit
            engine.obstacles.clear()
            // expire invincible with small ticks so no auto-generated obstacles interfere
            var remaining = GameEngine.INVINCIBLE_DURATION_MS + 1
            while (remaining > 0) {
                val tick = minOf(remaining, GameEngine.OBSTACLE_INTERVAL_MS - 1)
                engine.update(tick)
                engine.obstacles.clear()
                remaining -= tick
            }
        }
        assertEquals(GameState.GAME_OVER, engine.state)
    }

    @Test
    fun `update does nothing when state is GAME_OVER`() {
        repeat(3) {
            engine.obstacles.clear()
            placeObstacleOnPlayer()
            engine.update(16L)
            engine.obstacles.clear()
            var remaining = GameEngine.INVINCIBLE_DURATION_MS + 1
            while (remaining > 0) {
                val tick = minOf(remaining, GameEngine.OBSTACLE_INTERVAL_MS - 1)
                engine.update(tick)
                engine.obstacles.clear()
                remaining -= tick
            }
        }
        assertEquals(GameState.GAME_OVER, engine.state)
        val scoreAtGameOver = engine.scoreSeconds
        engine.update(5000L)
        assertEquals(scoreAtGameOver, engine.scoreSeconds)
    }

    // ── onTouchX ──────────────────────────────────────────────────────────────

    @Test
    fun `onTouchX moves player toward touch position on next update`() {
        engine.onTouchX(900f)
        engine.update(16L)
        assertEquals(900f - engine.player.width / 2f, engine.player.x, 1f)
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun placeObstacleOnPlayer() {
        engine.obstacles.add(Obstacle(x = engine.player.x, y = engine.player.y))
    }
}
