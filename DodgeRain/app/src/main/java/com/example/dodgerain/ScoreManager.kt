package com.example.dodgerain

class ScoreManager {
    private var highScore: Int = 0

    fun updateHighScore(score: Int): Boolean {
        if (score > highScore) {
            highScore = score
            return true
        }
        return false
    }

    fun getHighScore(): Int = highScore
}
