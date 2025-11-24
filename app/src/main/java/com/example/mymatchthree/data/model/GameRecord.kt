package com.example.mymatchthree.data.model

data class GameRecord(
    val id: Long = 0,
    var scoreString: String = "",
    val playerName: String,
    val score: Int,
    val mode: GameMode,
    val dateAchieved: Long = System.currentTimeMillis()
)