package com.example.mymatchthree.ui.game

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mymatchthree.R
import com.example.mymatchthree.data.model.GameItem
import com.example.mymatchthree.data.model.GameMode
import com.example.mymatchthree.data.model.GameState
import com.example.mymatchthree.gameengine.GameEngine
import kotlin.math.abs

class GameActivity : AppCompatActivity() {

    private lateinit var gameEngine: GameEngine
    private var selectedItem: Pair<Int, Int>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val gameMode = intent.getStringExtra("GAME_MODE")?.let {
            GameMode.valueOf(it)
        } ?: GameMode.Classic

        initializeGame(gameMode)
        setupGridView()
        setupClickListeners()
    }

    private fun initializeGame(mode: GameMode) {
        gameEngine = GameEngine()

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
        val itemSize = (screenWidth - 32) / grid.size

        gridLayout.columnCount = grid.size
        gridLayout.rowCount = grid.size

        for (i in grid.indices) {
            for (j in grid[i].indices) {
                val item = grid[i][j]
                createItemView(item, itemSize, gridLayout)
            }
        }
    }

    private fun createItemView(
        item: GameItem,
        size: Int,
        parent: GridLayout
    ) {
        val view = View(this).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = size
                height = size
                columnSpec = GridLayout.spec(item.y)
                rowSpec = GridLayout.spec(item.x)
                setMargins(2, 2, 2, 2)
            }
            setBackgroundColor(getColorForType(item.type))
            setOnClickListener { onItemClick(item) }
        }
        parent.addView(view)
    }

    private fun getColorForType(type: Int): Int {
        return when (type) {
            1 -> Color.RED
            2 -> Color.BLUE
            3 -> Color.GREEN
            4 -> Color.YELLOW
            5 -> Color.MAGENTA
            6 -> Color.CYAN
            else -> Color.GRAY
        }
    }

    private fun onItemClick(item: GameItem) {
        if (gameEngine.gameState.value?.isSwapping ?: true) return

        selectedItem?.let { (firstX, firstY) ->
            if (areNeighbors(firstX, firstY, item.x, item.y)) {
                gameEngine.swapItems(firstX, firstY, item.x, item.y)
            }
            selectedItem = null
            clearSelection()
        } ?: run {
            selectedItem = Pair(item.x, item.y)
            highlightItem(item)
        }
    }

    private fun areNeighbors(x1: Int, y1: Int, x2: Int, y2: Int): Boolean {
        val dx = abs(x1 - x2)
        val dy = abs(y1 - y2)
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1)
    }

    private fun highlightItem(item: GameItem) {
        Toast.makeText(this, "Chose item (${item.x}, ${item.y})", Toast.LENGTH_SHORT).show()
    }

    private fun clearSelection() {

    }

    private fun saveGameState() {
        val prefs = getSharedPreferences("game_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("game_saved", true).apply()
    }
}