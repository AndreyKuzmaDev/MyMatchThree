package com.example.mymatchthree.data.model

data class GameItem(
    val id: Int,
    val type: Int,
    val x: Int,
    val y: Int,
    var isRemoving: Boolean = false
)