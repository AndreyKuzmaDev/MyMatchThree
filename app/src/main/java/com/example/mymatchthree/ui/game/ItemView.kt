package com.example.mymatchthree.ui.game

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.example.mymatchthree.R

class ItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var itemType: Int = 0
    private var isSelected: Boolean = false

    fun setItemType(type: Int) {
        this.itemType = type
        updateAppearance()
    }

    fun setSelectedState(selected: Boolean) {
        this.isSelected = selected
        updateAppearance()
    }

    private fun updateAppearance() {
        setImageResource(getDrawableForType(itemType))

        if (isSelected) {
            setBackgroundResource(R.drawable.item_selected_background)
            elevation = 8f
        } else {
            setBackgroundResource(R.drawable.item_background)
            elevation = 2f
        }
    }

    private fun getDrawableForType(type: Int): Int {
        return when (type) {
            1 -> R.drawable.gem_black_1
            2 -> R.drawable.gem_blue_1
            3 -> R.drawable.gem_green_1
            4 -> R.drawable.gem_purple_1
            5 -> R.drawable.gem_red_1
            6 -> R.drawable.gem_yellow_1
            else -> R.drawable.gem_empty
        }
    }
}