package com.example.mymatchthree.gameengine


import androidx.lifecycle.MutableLiveData
import com.example.mymatchthree.data.model.GameItem
import com.example.mymatchthree.data.model.GameMode
import com.example.mymatchthree.data.model.GameState
import com.example.mymatchthree.data.model.ItemAnimationState
import com.example.mymatchthree.data.model.ItemBonus


class GameEngine(private val savedState: GameState? = null, private val gridSize: Int = 8, private val gameMode: GameMode = GameMode.Classic) {
    val gameState = MutableLiveData<GameState>()
    val animationEvents = MutableLiveData<AnimationEvent>()
    var newId = 0

    sealed class AnimationEvent {
        data class SwapItems(
            val item1: GameItem,
            val item2: GameItem,
            val onComplete: () -> Unit
        ) : AnimationEvent()

        data class RemoveItems(
            val items: Set<GameItem>,
            val onComplete: () -> Unit
        ) : AnimationEvent()

        data class RefillItems(
            val oldItems: List<GameItem>,
            val newItems: List<GameItem>,
            val onComplete: () -> Unit
        ) : AnimationEvent()
    }

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
        return GameState(grid = newGrid, gameMode = gameMode)
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

        grid[x1][y1] = item1.copy(animationState = ItemAnimationState.SWAPPING)
        grid[x2][y2] = item2.copy(animationState = ItemAnimationState.SWAPPING)

        gameState.value = currentState.copy(
            grid = grid,
            isSwapping = true,
            moves = currentState.moves + 1
        )

        animationEvents.value = AnimationEvent.SwapItems(item1, item2) {
            val updatedGrid = grid.map { it.toMutableList() }.toMutableList()
            updatedGrid[x1][y1] = item2.copy(
                x = x1,
                y = y1,
                animationState = ItemAnimationState.IDLE
            )
            updatedGrid[x2][y2] = item1.copy(
                x = x2,
                y = y2,
                animationState = ItemAnimationState.IDLE
            )

            gameState.value = currentState.copy(grid = updatedGrid)

            val matches = checkForMatches(updatedGrid)
            if (matches.isEmpty()) {
                swapBack(x1, y1, x2, y2)
            } else {
                processMatches(matches)
            }
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

        animationEvents.value = AnimationEvent.RemoveItems(matches) {
            val baseScore = matches.size * 10
            val bonus = if (matches.size > 3) (matches.size - 3) * 20 else 0
            val totalScore = currentState.score + baseScore + bonus

            val refill = prepareRefill(matches)

            val oldItemsList = matches.toList()
            animationEvents.value = AnimationEvent.RefillItems(oldItemsList, refill) {
                val newGrid = currentState.grid.map { row ->
                    row.map { item ->
                        if (item in matches) {
                            val newItem = refill.find { it.x == item.x && it.y == item.y }
                            newItem?.copy(animationState = ItemAnimationState.IDLE) ?: item
                        } else {
                            item.copy(animationState = ItemAnimationState.IDLE)
                        }
                    }
                }

                gameState.value = currentState.copy(
                    grid = newGrid as List<List<GameItem>>,
                    score = totalScore,
                    moves = currentState.moves,
                    isSwapping = false
                )
                val newMatches = checkForMatches(newGrid)
                if (!newMatches.isEmpty())
                    processMatches(newMatches)
            }
        }
    }

    private fun swapBack(x1: Int, y1: Int, x2: Int, y2: Int) {
        val currentState = gameState.value ?: return
        val grid = currentState.grid.map { it.toMutableList() }.toMutableList()

        val item1 = grid[x1][y1]
        val item2 = grid[x2][y2]

        animationEvents.value = AnimationEvent.SwapItems(item1, item2) {
            val updatedGrid = grid.map { it.toMutableList() }.toMutableList()
            updatedGrid[x1][y1] = item1.copy(
                x = x1,
                y = y1,
                animationState = ItemAnimationState.IDLE
            )
            updatedGrid[x2][y2] = item2.copy(
                x = x2,
                y = y2,
                animationState = ItemAnimationState.IDLE
            )

            gameState.value = currentState.copy(
                grid = updatedGrid,
                isSwapping = false
            )
        }
    }
}