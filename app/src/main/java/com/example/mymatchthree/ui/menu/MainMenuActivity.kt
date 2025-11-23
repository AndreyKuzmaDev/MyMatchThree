package com.example.mymatchthree.ui.menu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import com.example.mymatchthree.R
import com.example.mymatchthree.data.model.GameMode
import com.example.mymatchthree.ui.game.GameActivity
import com.example.mymatchthree.ui.records.RecordsActivity
import java.util.Locale

class MainMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        setupClickListeners()
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
                Toast.makeText(this, "No saves", Toast.LENGTH_SHORT).show()
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
        val prefs = getSharedPreferences("game_prefs", MODE_PRIVATE)
        return prefs.getBoolean("game_saved", false)
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