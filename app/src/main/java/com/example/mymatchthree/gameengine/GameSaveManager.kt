package com.example.mymatchthree.gameengine

import android.content.Context
import com.example.mymatchthree.data.model.GameItem
import com.example.mymatchthree.data.model.GameItemSave
import com.example.mymatchthree.data.model.GameMode
import com.example.mymatchthree.data.model.GameSave
import com.example.mymatchthree.data.model.GameState
import com.example.mymatchthree.data.model.GameStateSave
import com.google.gson.Gson
import java.io.*

class GameSaveManager(private val context: Context) {

    private val gson = Gson()
    private val saveFileName = "current_game_save.json"
    private val backupFileName = "backup_save.json"

    fun saveGame(gameState: GameState): Boolean {
        return try {
            val gameSave = GameSave(
                gameState = convertToSaveFormat(gameState),
                saveDate = System.currentTimeMillis(),
            )

            val jsonString = gson.toJson(gameSave)

            val file = File(context.filesDir, saveFileName)
            file.writeText(jsonString)

            createBackup(jsonString)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun loadGame(): GameSave? {
        return try {
            val file = File(context.filesDir, saveFileName)
            if (!file.exists()) {
                return loadFromBackup()
            }

            val jsonString = file.readText()
            gson.fromJson(jsonString, GameSave::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            loadFromBackup()
        }
    }

    fun hasSavedGame(): Boolean {
        val file = File(context.filesDir, saveFileName)
        val backupFile = File(context.filesDir, backupFileName)
        return file.exists() || backupFile.exists()
    }

    fun deleteSave(): Boolean {
        return try {
            val file = File(context.filesDir, saveFileName)
            val backupFile = File(context.filesDir, backupFileName)

            file.delete()
            backupFile.delete()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getSaveInfo(): SaveInfo? {
        return loadGame()?.let { save ->
            SaveInfo(
                saveDate = save.saveDate,
                gameMode = save.gameState.gameMode,
                score = save.gameState.score
            )
        }
    }

    private fun convertToSaveFormat(gameState: GameState): GameStateSave {
        val gridSave = gameState.grid.map { row ->
            row.map { item ->
                GameItemSave(
                    id = item.id,
                    type = item.type,
                    x = item.x,
                    y = item.y
                )
            }
        }

        return GameStateSave(
            grid = gridSave,
            score = gameState?.score ?: 0,
            moves = gameState?.moves ?: 0,
            targetScore = gameState?.targetScore ?: 0,
            timeLeft = gameState?.timeLeft ?: 0,
            gameMode = gameState.gameMode.toString()
        )
    }

    fun convertFromSaveFormat(gameSave: GameSave): GameState {
        val grid = gameSave.gameState.grid.map { row ->
            row.map { itemSave ->
                GameItem(
                    id = itemSave.id,
                    type = itemSave.type,
                    x = itemSave.x,
                    y = itemSave.y,
                    isRemoving = false
                )
            }
        }

        return GameState(
            grid = grid,
            score = gameSave.gameState.score,
            moves = gameSave.gameState.moves,
            targetScore = gameSave.gameState.targetScore,
            timeLeft = gameSave.gameState.timeLeft,
            gameMode = GameMode.valueOf(gameSave.gameState.gameMode)
        )
    }

    private fun createBackup(jsonString: String) {
        try {
            val backupFile = File(context.filesDir, backupFileName)
            backupFile.writeText(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadFromBackup(): GameSave? {
        return try {
            val backupFile = File(context.filesDir, backupFileName)
            if (backupFile.exists()) {
                val jsonString = backupFile.readText()
                gson.fromJson(jsonString, GameSave::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

data class SaveInfo(
    val saveDate: Long,
    val gameMode: String,
    val score: Int
)