package com.example.mymatchthree.gameengine


import androidx.lifecycle.MutableLiveData
import com.example.mymatchthree.data.model.GameItem
import com.example.mymatchthree.data.model.GameState
import com.example.mymatchthree.data.model.ItemBonus


class GameEngine(private val savedState: GameState? = null ,private val gridSize: Int = 8) {
    val gameState = MutableLiveData<GameState>()
    var newId = 0

    init {
        gameState.value = initializeGrid()
    }

    fun initializeGrid(): GameState {
        if (savedState != null)
            return savedState

        val newGrid = mutableListOf<List<GameItem>>()
        for (i in 0 until gridSize) {
            val row = mutableListOf<GameItem>()
            for (j in 0 until gridSize) {
                row.add(createRandomItem(i, j))
            }
            newGrid.add(row)
        }
        return GameState(grid = newGrid)
    }

    fun getCurrentState(): GameState {
        return gameState.value ?: initializeGrid()
    }

    private fun createRandomItem(x: Int, y: Int): GameItem {
        val availableTypes = (1..6).toList()

        return GameItem(
            id = newId++,
            type = availableTypes.random(),
            x = x,
            y = y
        )
    }

    fun swapItems(x1: Int, y1: Int, x2: Int, y2: Int) {
        val currentState = gameState.value ?: return
        if (currentState.isSwapping) return

        val grid = currentState.grid.map { it.toMutableList() }.toMutableList()

        val item1 = grid[x1][y1]
        val item2 = grid[x2][y2]

        grid[x1][y1] = item2.copy(x = x1, y = y1)
        grid[x2][y2] = item1.copy(x = x2, y = y2)

        gameState.value = currentState.copy(
            grid = grid,
            isSwapping = true,
            moves = currentState.moves + 1
        )

        val matches = checkForMatches(grid)
        if (matches.isEmpty()) {
            swapBack(x1, y1, x2, y2)
        } else {
            processMatches(matches)
        }
    }

    fun checkForMatches(grid: List<List<GameItem>>): Set<GameItem> {
        val horizontalMatches = findHorizontalMatches(grid)
        val verticalMatches = findVerticalMatches(grid)

        return horizontalMatches union verticalMatches
    }

    private fun findHorizontalMatches(grid: List<List<GameItem>>): Set<GameItem> {
        val matches = mutableSetOf<GameItem>()

        for (row in grid.indices) {
            var currentType = -1
            var matchStart = 0

            for (col in grid[row].indices) {
                val item = grid[row][col]

                if (item.type == currentType && item.type != 0) {
                    if (col == grid[row].size - 1 && col - matchStart >= 2) {
                        for (i in matchStart..col) {
                            matches.add(grid[row][i])
                        }
                    }
                } else {
                    if (col - matchStart >= 3) {
                        for (i in matchStart until col) {
                            matches.add(grid[row][i])
                        }
                    }
                    currentType = item.type
                    matchStart = col
                }
            }
        }
        return matches
    }

    private fun findVerticalMatches(grid: List<List<GameItem>>): Set<GameItem> {
        val matches = mutableSetOf<GameItem>()

        for (col in grid[0].indices) {
            var currentType = -1
            var matchStart = 0

            for (row in grid.indices) {
                val item = grid[row][col]

                if (item.type == currentType && item.type != 0) {
                    if (row == grid.size - 1 && row - matchStart >= 2) {
                        for (i in matchStart..row) {
                            matches.add(grid[i][col])
                        }
                    }
                } else {
                    if (row - matchStart >= 3) {
                        for (i in matchStart until row) {
                            matches.add(grid[i][col])
                        }
                    }
                    currentType = item.type
                    matchStart = row
                }
            }
        }
        return matches
    }


    private fun prepareRefill(matches: Set<GameItem>): List<GameItem> {
        val availableTypes = (1..6).toList()
        val refill = mutableListOf<GameItem>()
        val bonuses = (matches.size / 4).toInt()

        for (item in matches) {
            val chance = (0..10).random()

            if (chance > 7 || refill.size - matches.size == bonuses)
                refill.add(item.copy(
                    id = newId++,
                    bonus = ItemBonus.Bomb
                ))
            else
                refill.add(item.copy(
                    id = newId++,
                    type = availableTypes.random()
                ))
        }
        return refill
    }
    private fun processMatches(matches: Set<GameItem>) {
        val currentState = gameState.value ?: return

        val baseScore = matches.size * 10
        val bonus = if (matches.size > 3) (matches.size - 3) * 20 else 0
        val totalScore = currentState.score + baseScore + bonus

        val refill = prepareRefill(matches)

        val newGrid = currentState.grid.map { row ->
            row.map { item ->
                if (item in matches) refill.find {it.x == item.x && it.y == item.y} else item
            }
        }
        gameState.value = currentState.copy(
            grid = newGrid as List<List<GameItem>>,
            score = totalScore,
            isSwapping = false
        )

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
    }
}