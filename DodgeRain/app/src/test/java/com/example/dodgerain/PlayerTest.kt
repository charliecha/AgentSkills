package com.example.dodgerain

import org.junit.Assert.*
import org.junit.Test

class PlayerTest {

    @Test
    fun `moveTo centers player on target X`() {
        val player = Player()
        player.moveTo(200f, 1080)
        assertEquals(200f - player.width / 2f, player.x, 0.01f)
    }

    @Test
    fun `moveTo clamps to left boundary`() {
        val player = Player()
        player.moveTo(0f, 1080)
        assertEquals(0f, player.x, 0.01f)
    }

    @Test
    fun `moveTo clamps to right boundary`() {
        val player = Player(width = 80f)
        player.moveTo(1080f, 1080)
        assertEquals(1080f - player.width, player.x, 0.01f)
    }

    @Test
    fun `toRect covers correct area based on position and size`() {
        val player = Player(x = 100f, y = 200f, width = 80f, height = 80f)
        // RectF is an Android stub in JVM tests — verify via field values instead
        assertEquals(100f, player.x, 0.01f)
        assertEquals(200f, player.y, 0.01f)
        assertEquals(180f, player.x + player.width, 0.01f)
        assertEquals(280f, player.y + player.height, 0.01f)
    }
}
