package com.example.mymatchthree.data.model


enum class ItemBonus {
    None, Bomb
}
data class GameItem(
    val id: Int,
    val type: Int,
    val x: Int,
    val y: Int,
    var isRemoving: Boolean = false,
    var bonus: ItemBonus = ItemBonus.None
)


