package com.example.mymatchthree.ui.game

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mymatchthree.R
import com.example.mymatchthree.data.model.GameItem
import com.example.mymatchthree.data.model.GameMode
import com.example.mymatchthree.data.model.GameRecord
import com.example.mymatchthree.data.model.GameState
import com.example.mymatchthree.data.model.ItemAnimationState
import com.example.mymatchthree.gameengine.GameEngine
import com.example.mymatchthree.gameengine.GameRecordDAO
import com.example.mymatchthree.gameengine.GameRecordsManager
import com.example.mymatchthree.gameengine.GameSaveManager
import kotlin.math.abs
import androidx.core.view.isEmpty

class GameActivity : AppCompatActivity() {
    private lateinit var gameRecordDAO: GameRecordDAO
    private lateinit var gameRecordsManager: GameRecordsManager
    private lateinit var gameEngine: GameEngine
    private val itemViews = mutableMapOf<Pair<Int, Int>, ItemView>()
    private var selectedItem: Pair<Int, Int>? = null
    private var selectedItemView: ItemView? = null

    private var gameSaveManager: GameSaveManager = GameSaveManager(this)
    private var loadSaved: Boolean = false
    private lateinit var playerName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gameRecordsManager = GameRecordsManager(this)
        gameRecordDAO = GameRecordDAO(gameRecordsManager.getWritableDatabase())


        loadSaved = intent.getBooleanExtra("CONTINUE_GAME", false)
        playerName = intent.getStringExtra("PLAYER_NAME") ?: resources.getString(R.string.player_name)

        initializeGame()
        setupGridView()
        setupClickListeners()
        setupAnimationObserver()
    }

    private fun initializeGame() {
        if (loadSaved && gameSaveManager.hasSavedGame()) {
            gameEngine = GameEngine(savedState = gameSaveManager.convertFromSaveFormat(
                gameSaveManager.loadGame()!!
            ))
        }
        else {
            val gameMode = intent.getStringExtra("GAME_MODE")?.let {
                GameMode.valueOf(it)
            } ?: GameMode.Classic
            gameSaveManager.deleteSave()
            gameEngine = GameEngine(gameMode = gameMode)
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
        findViewById<Button>(R.id.btnRecord).setOnClickListener {
            val gameState = gameEngine.getCurrentState()
            gameRecordDAO.addRecord(
                GameRecord(id = 52,
                    scoreString = "",
                    playerName = playerName,
                    score = gameState.score,
                    mode = gameState.gameMode))
        }
    }

    private fun setupAnimationObserver() {
        gameEngine.animationEvents.observe(this) { event ->
            when (event) {
                is GameEngine.AnimationEvent.SwapItems -> {
                    val view1 = findViewByPosition(event.item1.x, event.item1.y) as? ItemView
                    val view2 = findViewByPosition(event.item2.x, event.item2.y) as? ItemView

                    view1?.setAnimationState(ItemAnimationState.SWAPPING)
                    view2?.setAnimationState(ItemAnimationState.SWAPPING)

                    view1?.swapWith(view2!!) {
                        event.onComplete()
                    }
                }

                is GameEngine.AnimationEvent.RemoveItems -> {
                    var completedAnimations = 0
                    val totalAnimations = event.items.size

                    event.items.forEach { item ->
                        val view = findViewByPosition(item.x, item.y) as? ItemView
                        view?.setAnimationState(ItemAnimationState.REMOVING)
                        view?.startRemovalAnimation {
                            completedAnimations++
                            if (completedAnimations == totalAnimations) {
                                event.onComplete()
                            }
                        }
                    }
                }

                is GameEngine.AnimationEvent.RefillItems -> {
                    // Сначала скрываем старые элементы
                    event.oldItems.forEach { oldItem ->
                        val view = findViewByPosition(oldItem.x, oldItem.y) as? ItemView
                        view?.visibility = View.INVISIBLE
                    }

                    // Затем анимируем появление новых
                    var completedAnimations = 0
                    val totalAnimations = event.newItems.size

                    event.newItems.forEach { newItem ->
                        val view = findViewByPosition(newItem.x, newItem.y) as? ItemView
                        view?.setItemType(newItem.type)
                        view?.visibility = View.VISIBLE
                        view?.setAnimationState(ItemAnimationState.APPEARING)
                        view?.startAppearingAnimation {
                            completedAnimations++
                            if (completedAnimations == totalAnimations) {
                                event.onComplete()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateUI(state: GameState) {
        findViewById<TextView>(R.id.tvScore).text = "${resources.getString(R.string.word_score)}: ${state.score}"
        findViewById<TextView>(R.id.tvMoves).text = "${resources.getString(R.string.word_moves)}: ${state.moves}"
        updateGrid(state.grid)
    }

    private fun updateGrid(grid: List<List<GameItem>>) {
        val gridLayout = findViewById<GridLayout>(R.id.gameGridLayout)

        if (gridLayout.isEmpty()) {
            gridLayout.removeAllViews()
            itemViews.clear()

            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val itemSize = (screenWidth - 64) / grid.size

            gridLayout.columnCount = grid.size
            gridLayout.rowCount = grid.size

            for (i in grid.indices) {
                for (j in grid[i].indices) {
                    val item = grid[i][j]
                    val itemView = createItemView(item, itemSize)
                    itemViews[Pair(i, j)] = itemView
                    gridLayout.addView(itemView)
                }
            }
        } else {
            for (i in grid.indices) {
                for (j in grid[i].indices) {
                    val item = grid[i][j]
                    val itemView = itemViews[Pair(i, j)]
                    itemView?.setItemType(item.type)
                    itemView?.setAnimationState(item.animationState)
                }
            }
        }
    }

    private fun createItemView(item: GameItem, size: Int): ItemView {
        return ItemView(this).apply {
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
            setAnimationState(item.animationState)
            setSelectedState(false)

            setOnClickListener { onItemClick(item) }
        }
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

        val view = findViewByPosition(item.x, item.y) as? ItemView

        selectedItem?.let { firstItem ->
            if (areNeighbors(firstItem.first, firstItem.second, item.x, item.y)) {
                val firstView = findViewByPosition(firstItem.first, firstItem.second) as? ItemView
                gameEngine.swapItems(firstItem.first, firstItem.second, item.x, item.y)
                gameSaveManager.saveGame(gameEngine.getCurrentState())

                firstView?.setSelectedState(false)
                view?.setSelectedState(false)
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

    override fun onDestroy() {
        super.onDestroy()
        gameRecordsManager.close()
    }
}

