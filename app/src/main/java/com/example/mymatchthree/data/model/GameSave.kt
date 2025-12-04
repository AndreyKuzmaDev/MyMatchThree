package com.example.mymatchthree.data.model

import com.google.gson.annotations.SerializedName

data class GameSave(
    @SerializedName("game_state") val gameState: GameStateSave,
    @SerializedName("save_date") val saveDate: Long,
    @SerializedName("version") val version: Int = 1
)

data class GameStateSave(
    @SerializedName("grid") val grid: List<List<GameItemSave>>,
    @SerializedName("score") val score: Int,
    @SerializedName("moves") val moves: Int,
    @SerializedName("target_score") val targetScore: Int,
    @SerializedName("time_left") val timeLeft: Long,
    @SerializedName("game_mode") val gameMode: String
)

data class GameItemSave(
    @SerializedName("id") val id: Int,
    @SerializedName("type") val type: Int,
    @SerializedName("x") val x: Int,
    @SerializedName("y") val y: Int
)