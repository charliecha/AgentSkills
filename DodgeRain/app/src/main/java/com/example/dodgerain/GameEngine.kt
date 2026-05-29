package com.example.dodgerain

enum class GameState { IDLE, PLAYING, GAME_OVER }

private fun intersects(
    ax: Float, ay: Float, aw: Float, ah: Float,
    bx: Float, by: Float, bw: Float, bh: Float,
): Boolean = ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by

class GameEngine(private val screenWidth: Int, private val screenHeight: Int) {

    companion object {
        const val INITIAL_SPEED = 8f
        const val MAX_SPEED = 20f
        const val OBSTACLE_INTERVAL_MS = 800L
        const val SPEED_INCREMENT_INTERVAL_MS = 5000L
        const val INVINCIBLE_DURATION_MS = 1500L
        const val SCORE_INTERVAL_MS = 1000L
    }

    var state: GameState = GameState.IDLE
        private set

    var lives: Int = 3
        private set

    var scoreSeconds: Int = 0
        private set

    val obstacles: MutableList<Obstacle> = mutableListOf()

    var currentSpeed: Float = INITIAL_SPEED
        private set

    @Volatile
    private var targetX: Float = 0f

    private var obstacleTimer: Long = 0L
    private var speedTimer: Long = 0L
    private var scoreTimer: Long = 0L
    private var invincibleTimer: Long = 0L

    val player: Player = Player()

    fun startGame() {
        state = GameState.PLAYING
        lives = 3
        scoreSeconds = 0
        currentSpeed = INITIAL_SPEED
        obstacles.clear()
        obstacleTimer = 0L
        speedTimer = 0L
        scoreTimer = 0L
        invincibleTimer = 0L
        player.x = (screenWidth - player.width) / 2f
        player.y = screenHeight - screenHeight / 6f - player.height / 2f
        targetX = player.x + player.width / 2f
    }

    fun onTouchX(x: Float) {
        targetX = x
    }

    fun update(deltaMs: Long) {
        if (state != GameState.PLAYING) return

        player.moveTo(targetX, screenWidth)

        obstacleTimer += deltaMs
        if (obstacleTimer >= OBSTACLE_INTERVAL_MS) {
            obstacleTimer = 0L
            val ox = (0..(screenWidth - 60)).random().toFloat()
            obstacles.add(Obstacle(x = ox))
        }

        obstacles.forEach { it.update(currentSpeed) }
        obstacles.removeAll { it.isOffScreen(screenHeight) }

        if (invincibleTimer > 0L) {
            invincibleTimer -= deltaMs
        } else {
            val hit = obstacles.any { intersects(player.x, player.y, player.width, player.height, it.x, it.y, it.width, it.height) }
            if (hit) {
                lives--
                invincibleTimer = INVINCIBLE_DURATION_MS
                if (lives <= 0) {
                    state = GameState.GAME_OVER
                    return
                }
            }
        }

        speedTimer += deltaMs
        while (speedTimer >= SPEED_INCREMENT_INTERVAL_MS) {
            speedTimer -= SPEED_INCREMENT_INTERVAL_MS
            currentSpeed = (currentSpeed + 1f).coerceAtMost(MAX_SPEED)
        }

        scoreTimer += deltaMs
        while (scoreTimer >= SCORE_INTERVAL_MS) {
            scoreTimer -= SCORE_INTERVAL_MS
            scoreSeconds++
        }
    }
}
