package com.example.mymatchthree.data.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

data class GameState(
    val grid: List<List<GameItem>> = emptyList(),
    val score: Int = 0,
    val moves: Int = 0,
    val isSwapping: Boolean = false,
    val targetScore: Int = 1000,
    val timeLeft: Long = 60000,
    val gameMode: GameMode = GameMode.Classic
)

enum class GameMode {
    Classic, Infinite, Timed
}