package com.example.dodgerain

import org.junit.Assert.*
import org.junit.Test

class ScoreManagerTest {

    @Test
    fun `getHighScore returns 0 initially`() {
        val manager = ScoreManager()
        assertEquals(0, manager.getHighScore())
    }

    @Test
    fun `updateHighScore returns true and updates when score is higher`() {
        val manager = ScoreManager()
        assertTrue(manager.updateHighScore(10))
        assertEquals(10, manager.getHighScore())
    }

    @Test
    fun `updateHighScore returns false when score is not higher`() {
        val manager = ScoreManager()
        manager.updateHighScore(10)
        assertFalse(manager.updateHighScore(5))
        assertEquals(10, manager.getHighScore())
    }

    @Test
    fun `updateHighScore returns false for equal score`() {
        val manager = ScoreManager()
        manager.updateHighScore(10)
        assertFalse(manager.updateHighScore(10))
    }

    @Test
    fun `updateHighScore tracks successive new highs`() {
        val manager = ScoreManager()
        manager.updateHighScore(10)
        manager.updateHighScore(20)
        assertEquals(20, manager.getHighScore())
    }
}
