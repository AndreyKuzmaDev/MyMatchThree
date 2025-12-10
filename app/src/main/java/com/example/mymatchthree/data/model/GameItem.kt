package com.example.mymatchthree.data.model


enum class ItemBonus {
    None, Bomb
}

enum class ItemAnimationState {
    IDLE,
    SWAPPING,
    REMOVING,
    APPEARING
}

data class GameItem(
    val id: Int,
    val type: Int,
    val x: Int,
    val y: Int,
    var isRemoving: Boolean = false,
    var bonus: ItemBonus = ItemBonus.None,
    var animationState: ItemAnimationState = ItemAnimationState.IDLE
)


