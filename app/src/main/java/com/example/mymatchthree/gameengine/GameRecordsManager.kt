package com.example.mymatchthree.gameengine

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.example.mymatchthree.data.model.GameRecord

class GameRecordsManager(context: Context) {

    private val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
        .name("records.db")
        .callback(object : SupportSQLiteOpenHelper.Callback(1) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                createTable(db)
                insertInitialData(db)
            }

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                migrateDatabase(db, oldVersion, newVersion)
            }
        })
        .build()

    private val openHelper: SupportSQLiteOpenHelper =
        FrameworkSQLiteOpenHelperFactory().create(configuration)

    fun getWritableDatabase(): SupportSQLiteDatabase = openHelper.writableDatabase

    fun getReadableDatabase(): SupportSQLiteDatabase = openHelper.readableDatabase

    private fun createTable(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS records (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                playerName TEXT NOT NULL,
                score INTEGER NOT NULL,
                mode TEXT NOT NULL,
                date INTEGER DEFAULT 0
            )
        """.trimIndent())
    }

    private fun insertInitialData(db: SupportSQLiteDatabase) {
        db.execSQL("INSERT INTO records (playerName, score, mode) VALUES ('Andrey', 2025, 'Classic')")
        db.execSQL("INSERT INTO records (playerName, score, mode) VALUES ('Also Andrey', 2020, 'Classic')")
        db.execSQL("INSERT INTO records (playerName, score, mode) VALUES ('Andrey again', 420, 'Classic')")
        db.execSQL("INSERT INTO records (playerName, score, mode) VALUES ('Nobody plays it', 220, 'Classic')")
        db.execSQL("INSERT INTO records (playerName, score, mode) VALUES ('EvilArthas', 141, 'Classic')")
    }

    private fun migrateDatabase(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
        when (oldVersion) {
            1 -> {
            }
        }
    }

    fun close() {
        openHelper.close()
    }
}