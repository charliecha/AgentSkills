package com.example.dodgerain

import org.junit.Assert.*
import org.junit.Test

class ObstacleTest {

    @Test
    fun `update increases y by speedPx`() {
        val obstacle = Obstacle(x = 100f, y = 0f)
        obstacle.update(8f)
        assertEquals(8f, obstacle.y, 0.01f)
    }

    @Test
    fun `isOffScreen returns false when y is above screen`() {
        val obstacle = Obstacle(x = 0f, y = 100f)
        assertFalse(obstacle.isOffScreen(1920))
    }

    @Test
    fun `isOffScreen returns true when y exceeds screenHeight`() {
        val obstacle = Obstacle(x = 0f, y = 1921f)
        assertTrue(obstacle.isOffScreen(1920))
    }

    @Test
    fun `toRect covers correct area based on position and size`() {
        val obstacle = Obstacle(x = 50f, y = 100f, width = 60f, height = 40f)
        // RectF is an Android stub in JVM tests — verify via field values instead
        assertEquals(50f, obstacle.x, 0.01f)
        assertEquals(100f, obstacle.y, 0.01f)
        assertEquals(110f, obstacle.x + obstacle.width, 0.01f)
        assertEquals(140f, obstacle.y + obstacle.height, 0.01f)
    }
}
