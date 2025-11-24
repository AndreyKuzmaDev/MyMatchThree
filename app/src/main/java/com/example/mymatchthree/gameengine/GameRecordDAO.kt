package com.example.mymatchthree.gameengine

import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mymatchthree.data.model.GameMode
import com.example.mymatchthree.data.model.GameRecord

class GameRecordDAO(private val db: SupportSQLiteDatabase) {
    fun addRecord(gameRecord: GameRecord) {
        db.execSQL("INSERT INTO records (playerName, score, mode, date) VALUES (" +
                "'${gameRecord.playerName}', " +
                "${gameRecord.score}, " +
                "'${gameRecord.mode}', " +
                "${gameRecord.dateAchieved})")
    }

    fun getRecords(): List<GameRecord> {
        val cursor = db.query("SELECT * FROM records ORDER BY score")
        val records = mutableListOf<GameRecord>()

        cursor.use {
            while (it.moveToNext()) {
                records.add(
                    GameRecord(
                        id = it.getLong(0),
                        playerName = it.getString(1),
                        score = it.getInt(2),
                        scoreString = "",
                        mode = GameMode.valueOf(it.getString(3)),
                        dateAchieved = it.getLong(4)
                    )
                )
            }
        }

        return records
    }

    fun clearRecords() {
        db.execSQL("DELETE FROM records")
    }
}