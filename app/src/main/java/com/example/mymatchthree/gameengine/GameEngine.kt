package com.example.mymatchthree.gameengine


import androidx.lifecycle.MutableLiveData
import com.example.mymatchthree.data.model.GameItem
import com.example.mymatchthree.data.model.GameState


class GameEngine(private val gridSize: Int = 8) {

    val gameState = MutableLiveData<GameState>()
    var isSwapping = false

    init {
        initializeGrid()
    }

    fun initializeGrid() {
        val newGrid = mutableListOf<List<GameItem>>()
        for (i in 0 until gridSize) {
            val row = mutableListOf<GameItem>()
            for (j in 0 until gridSize) {
                row.add(createRandomItem(i, j))
            }
            newGrid.add(row)
        }
        gameState.value = GameState(grid = newGrid)
    }

    private fun createRandomItem(x: Int, y: Int): GameItem {
        val availableTypes = (1..6).toList()
        return GameItem(
            id = (x * gridSize + y),
            type = availableTypes.random(),
            x = x,
            y = y
        )
    }

    fun swapItems(x1: Int, y1: Int, x2: Int, y2: Int) {
        if (isSwapping) return

        val currentState = gameState.value ?: return
        val grid = currentState.grid.map { it.toMutableList() }.toMutableList()

        val item1 = grid[x1][y1]
        val item2 = grid[x2][y2]

        grid[x1][y1] = item2.copy(x = x1, y = y1)
        grid[x2][y2] = item1.copy(x = x2, y = y2)

        isSwapping = true
        gameState.value = currentState.copy(
            grid = grid,
            isSwapping = true,
            moves = currentState.moves + 1
        )

        val hasMatches = checkForMatches(grid)
        if (!hasMatches) {
            swapBack(x1, y1, x2, y2)
        } else {
            processMatches()
        }
    }

    private fun checkForMatches(grid: List<List<GameItem>>): Boolean {
        // TODO: Create actual logic to check if there is any matches on screen
        return true
    }

    private fun processMatches() {
        val currentState = gameState.value ?: return
        val newScore = currentState.score + 100

        gameState.value = currentState.copy(
            score = newScore,
            isSwapping = false
        )
        isSwapping = false
    }

    private fun swapBack(x1: Int, y1: Int, x2: Int, y2: Int) {
        val currentState = gameState.value ?: return
        val grid = currentState.grid.map { it.toMutableList() }.toMutableList()

        val item1 = grid[x1][y1]
        val item2 = grid[x2][y2]

        grid[x1][y1] = item2.copy(x = x1, y = y1)
        grid[x2][y2] = item1.copy(x = x2, y = y2)

        gameState.value = currentState.copy(
            grid = grid,
            isSwapping = false
        )
        isSwapping = false
    }
}