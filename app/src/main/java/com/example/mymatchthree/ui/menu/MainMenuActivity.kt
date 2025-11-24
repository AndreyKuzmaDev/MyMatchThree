package com.example.mymatchthree.ui.menu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import com.example.mymatchthree.R
import com.example.mymatchthree.data.model.GameMode
import com.example.mymatchthree.gameengine.GameSaveManager
import com.example.mymatchthree.ui.game.GameActivity
import com.example.mymatchthree.ui.records.RecordsActivity
import java.util.Locale

class MainMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        setFlag()

        setupClickListeners()
    }

    private fun setFlag() {
        val language = resources.configuration.locales[0].language
        val button = findViewById<ImageButton>(R.id.btnLanguage)
        if (language == "ru")
            button.setImageResource(R.drawable.ru)
        else
            button.setImageResource(R.drawable.us)

    }
    private fun setupClickListeners() {
        findViewById<Button>(R.id.btnNewGame).setOnClickListener {
            startGame(GameMode.Classic)
        }

        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            val savedGameExists = checkSavedGameExists()
            if (savedGameExists) {
                startGame(GameMode.Classic, continueGame = true)
            } else {
                Toast.makeText(this, resources.getString(R.string.text_no_saves), Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnInfiniteMode).setOnClickListener {
            startGame(GameMode.Infinite)
        }

        findViewById<Button>(R.id.btnRecords).setOnClickListener {
            startActivity(Intent(this, RecordsActivity::class.java))
        }

        findViewById<AppCompatImageButton>(R.id.btnLanguage).setOnClickListener {
            val language = resources.configuration.locales[0].language
            if (language.equals("en"))
                updateLanguage("ru")
            else if (language.equals("ru"))
                updateLanguage("en")
        }
    }

    private fun startGame(mode: GameMode, continueGame: Boolean = false) {
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("GAME_MODE", mode.name)
            putExtra("CONTINUE_GAME", continueGame)
        }
        startActivity(intent)
    }

    private fun checkSavedGameExists(): Boolean {
        val gameSaveManager = GameSaveManager(this)
        return gameSaveManager.hasSavedGame()
    }

    private fun updateLanguage(language: String, country: String = "") {
        val locale = Locale(language, country)
        val configuration = resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        resources.updateConfiguration(configuration, resources.displayMetrics)
        val intent = intent
        finish()
        startActivity(intent)
    }
}