package com.example.mymatchthree.ui.game

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mymatchthree.R
import com.example.mymatchthree.data.model.GameItem
import com.example.mymatchthree.data.model.GameMode
import com.example.mymatchthree.data.model.GameState
import com.example.mymatchthree.gameengine.GameEngine
import com.example.mymatchthree.gameengine.GameSaveManager
import kotlin.math.abs

class GameActivity : AppCompatActivity() {

    private lateinit var gameEngine: GameEngine
    private var selectedItem: Pair<Int, Int>? = null
    private var selectedItemView: ItemView? = null

    private var gameSaveManager: GameSaveManager = GameSaveManager(this)
    private var gameMode: GameMode? = null
    private var loadSaved: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gameMode = intent.getStringExtra("GAME_MODE")?.let {
            GameMode.valueOf(it)
        } ?: GameMode.Classic
        loadSaved = intent.getBooleanExtra("CONTINUE_GAME", false)

        initializeGame()
        setupGridView()
        setupClickListeners()
    }

    private fun initializeGame() {
        if (loadSaved && gameSaveManager.hasSavedGame()) {
            gameEngine = GameEngine(savedState = gameSaveManager.convertFromSaveFormat(
                gameSaveManager.loadGame()!!
            ))
        }
        else {
            gameSaveManager.deleteSave()
            gameEngine = GameEngine()
        }


        gameEngine.gameState.observe(this) { state ->
            updateUI(state)
        }
    }

    private fun setupGridView() {
        updateGrid(gameEngine.gameState.value?.grid ?: emptyList())
    }

    private fun setupClickListeners() {
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            saveGameState()
            finish()
        }
    }

    private fun updateUI(state: GameState) {
        findViewById<TextView>(R.id.tvScore).text = "${resources.getString(R.string.word_score)}: ${state.score}"
        findViewById<TextView>(R.id.tvMoves).text = "${resources.getString(R.string.word_moves)}: ${state.moves}"
        updateGrid(state.grid)
    }

    private fun updateGrid(grid: List<List<GameItem>>) {
        val gridLayout = findViewById<GridLayout>(R.id.gameGridLayout)
        gridLayout.removeAllViews()

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val itemSize = (screenWidth - 64) / grid.size

        gridLayout.columnCount = grid.size
        gridLayout.rowCount = grid.size

        for (i in grid.indices) {
            for (j in grid[i].indices) {
                val item = grid[i][j]
                val itemView = createItemView(item, itemSize)
                gridLayout.addView(itemView)
            }
        }

    }

    private fun createItemView(item: GameItem, size: Int): View {
        val itemView = ItemView(this).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = size
                height = size
                columnSpec = GridLayout.spec(item.y)
                rowSpec = GridLayout.spec(item.x)
                setMargins(2, 2, 2, 2)
            }

            setTag(R.id.tag_x, item.x)
            setTag(R.id.tag_y, item.y)

            setItemType(item.type)
            setSelectedState(false)

            setOnClickListener { onItemClick(item) }
        }
        return itemView
    }

    private fun findViewByPosition(x: Int, y: Int): View? {
        val gridLayout = findViewById<GridLayout>(R.id.gameGridLayout)

        for (i in 0 until gridLayout.childCount) {
            val child = gridLayout.getChildAt(i)
            if (child.getTag(R.id.tag_x) == x && child.getTag(R.id.tag_y) == y) {
                return child
            }
        }
        return null
    }

    private fun onItemClick(item: GameItem) {
        if (gameEngine.gameState.value?.isSwapping == true) return

        val view = findViewByPosition(item.x, item.y)

        selectedItem?.let { firstItem ->
            if (areNeighbors(firstItem.first, firstItem.second, item.x, item.y)) {
                val firstView = findViewByPosition(firstItem.first, firstItem.second)
                gameEngine.swapItems(firstItem.first, firstItem.second, item.x, item.y)
                gameSaveManager.saveGame(gameEngine.getCurrentState(), gameMode)

            }
            clearSelection()
            selectedItem = null
        } ?: run {
            selectedItem = Pair(item.x, item.y)
            if (view != null) {
                highlightItem(item, view)
            }
        }
    }

    private fun areNeighbors(x1: Int, y1: Int, x2: Int, y2: Int): Boolean {
        val dx = abs(x1 - x2)
        val dy = abs(y1 - y2)
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1)
    }

    private fun highlightItem(item: GameItem, view: View) {
        if (view is ItemView) {
            selectedItemView?.setSelectedState(false)
            view.setSelectedState(true)
            selectedItemView = view

            view.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(200)
                .start()
        }
    }

    private fun clearSelection() {
        selectedItemView?.let { view ->
            view.setSelectedState(false)
            view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .start()
            selectedItemView = null
        }
    }

    private fun saveGameState() {
        val prefs = getSharedPreferences("game_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("game_saved", true).apply()
    }
}

