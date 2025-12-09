// ItemView.kt
package com.example.mymatchthree.ui.game

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.example.mymatchthree.R
import com.example.mymatchthree.data.model.ItemAnimationState

class ItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var itemType: Int = 0
    private var isSelected: Boolean = false
    private var animationState: ItemAnimationState = ItemAnimationState.IDLE

    fun setItemType(type: Int) {
        this.itemType = type
        updateAppearance()
    }

    fun setSelectedState(selected: Boolean) {
        this.isSelected = selected
        updateAppearance()
    }

    fun setAnimationState(state: ItemAnimationState) {
        this.animationState = state
        handleAnimationState()
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

    private fun handleAnimationState() {
        when (animationState) {
            ItemAnimationState.SWAPPING -> startSwapAnimation()
            ItemAnimationState.REMOVING -> startRemovalAnimation()
            ItemAnimationState.APPEARING -> startAppearingAnimation()
            ItemAnimationState.IDLE -> clearAnimations()
        }
    }

    fun startSwapAnimation() {
        val scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.2f, 1f)

        scaleX.duration = 300
        scaleY.duration = 300

        scaleX.start()
        scaleY.start()
    }

    fun startRemovalAnimation(onComplete: () -> Unit = {}) {
        val alphaAnimator = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f)
        val scaleAnimator = ObjectAnimator.ofFloat(this, "scaleX", 1f, 0f)
        scaleAnimator.addUpdateListener {
            this.scaleY = it.animatedValue as Float
        }

        alphaAnimator.duration = 200
        scaleAnimator.duration = 200

        alphaAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onComplete()
            }
        })

        alphaAnimator.start()
        scaleAnimator.start()
    }

    fun startAppearingAnimation(onComplete: () -> Unit = {}) {
        this.alpha = 0f
        this.scaleX = 0.5f
        this.scaleY = 0.5f

        val alphaAnimator = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f)
        val scaleXAnimator = ObjectAnimator.ofFloat(this, "scaleX", 0.5f, 1.2f, 1f)
        val scaleYAnimator = ObjectAnimator.ofFloat(this, "scaleY", 0.5f, 1.2f, 1f)

        alphaAnimator.duration = 300
        scaleXAnimator.duration = 300
        scaleYAnimator.duration = 300

        alphaAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onComplete()
            }
        })

        alphaAnimator.start()
        scaleXAnimator.start()
        scaleYAnimator.start()
    }

    private fun clearAnimations() {
        clearAnimation()
        alpha = 1f
        scaleX = 1f
        scaleY = 1f
        translationX = 0f
        translationY = 0f
        rotation = 0f
    }

    fun swapWith(other: ItemView, onComplete: () -> Unit) {
        val dx = other.x - x
        val dy = other.y - y

        val animator1 = ObjectAnimator.ofFloat(this, "translationX", 0f, dx.toFloat())
        val animator2 = ObjectAnimator.ofFloat(this, "translationY", 0f, dy.toFloat())
        val animator3 = ObjectAnimator.ofFloat(other, "translationX", 0f, -dx.toFloat())
        val animator4 = ObjectAnimator.ofFloat(other, "translationY", 0f, -dy.toFloat())

        animator1.duration = 300
        animator2.duration = 300
        animator3.duration = 300
        animator4.duration = 300

        animator1.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                translationX = 0f
                translationY = 0f
                other.translationX = 0f
                other.translationY = 0f
                onComplete()
            }
        })
        animator2.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                translationX = 0f
                translationY = 0f
                other.translationX = 0f
                other.translationY = 0f
                onComplete()
            }
        })
        animator3.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                translationX = 0f
                translationY = 0f
                other.translationX = 0f
                other.translationY = 0f
                onComplete()
            }
        })
        animator4.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                translationX = 0f
                translationY = 0f
                other.translationX = 0f
                other.translationY = 0f
                onComplete()
            }
        })

        animator1.start()
        animator2.start()
        animator3.start()
        animator4.start()
    }
}