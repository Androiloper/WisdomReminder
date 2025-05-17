package com.example.wisdomreminder.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import kotlin.math.abs
import kotlin.math.max

/**
 * A touch listener that makes a view dismissible when swiped.
 */
class SwipeDismissTouchListener(
    private val view: View,
    private val callbacks: DismissCallbacks
) : View.OnTouchListener {

    // Cached ViewConfiguration and system-wide constant values
    private val slop: Int
    private val minFlingVelocity: Int
    private val maxFlingVelocity: Int
    private var velocityTracker: VelocityTracker? = null
    private var viewWidth = 1 // 1 and not 0 to prevent division by zero

    // Transient properties
    private var downX = 0f
    private var downY = 0f
    private var swiping = false
    private var swipingDirection = 0  // -1 left, 1 right, 0 none
    private var dismissing = false
    private val dismissThreshold = 0.4f // Dismiss when swiped beyond 40% of width

    init {
        val vc = ViewConfiguration.get(view.context)
        slop = vc.scaledTouchSlop
        minFlingVelocity = vc.scaledMinimumFlingVelocity * 16
        maxFlingVelocity = vc.scaledMaximumFlingVelocity
    }

    interface DismissCallbacks {
        /**
         * Called when the user has swiped the view to dismiss it.
         *
         * @param view The view that was swiped.
         * @param direction The direction of the swipe. -1 for left, 1 for right.
         */
        fun onDismiss(view: View, direction: Int)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        // Ignore touch events while animating the dismissal
        if (dismissing) {
            return false
        }

        // ViewWidth may change during layout
        viewWidth = view.width

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.rawX
                downY = event.rawY
                velocityTracker = VelocityTracker.obtain()
                velocityTracker?.addMovement(event)
                return true // We want to handle this touch
            }

            MotionEvent.ACTION_MOVE -> {
                if (velocityTracker == null) {
                    return false
                }

                velocityTracker?.addMovement(event)
                val deltaX = event.rawX - downX
                val deltaY = event.rawY - downY

                if (!swiping && abs(deltaX) > slop && abs(deltaX) > abs(deltaY)) {
                    swiping = true
                    swipingDirection = if (deltaX > 0) 1 else -1
                    view.parent.requestDisallowInterceptTouchEvent(true)
                }

                if (swiping) {
                    view.translationX = deltaX
                    view.alpha = max(0f, 1f - 2f * abs(deltaX) / viewWidth)
                    return true
                }
                return false
            }

            MotionEvent.ACTION_UP -> {
                if (velocityTracker == null) {
                    return false
                }

                if (swiping) {
                    velocityTracker?.computeCurrentVelocity(1000, maxFlingVelocity.toFloat())
                    val velocityX = velocityTracker?.xVelocity ?: 0f
                    val absVelocityX = abs(velocityX)
                    val absVelocityY = abs(velocityTracker?.yVelocity ?: 0f)

                    var dismiss = false
                    var dismissDirection = 0

                    // Dismiss if swiped beyond threshold or flung with sufficient velocity
                    if (abs(view.translationX) > viewWidth * dismissThreshold) {
                        dismiss = true
                        dismissDirection = if (view.translationX > 0) 1 else -1
                    } else if (minFlingVelocity <= absVelocityX && absVelocityX <= maxFlingVelocity
                        && absVelocityY < absVelocityX && swiping
                    ) {
                        // Dismiss if flung in the correct direction and with sufficient velocity
                        dismiss = velocityX * view.translationX > 0
                        dismissDirection = if (velocityX > 0) 1 else -1
                    }

                    if (dismiss) {
                        dismissing = true
                        view.animate()
                            .translationX(if (dismissDirection > 0) viewWidth.toFloat() else -viewWidth.toFloat())
                            .alpha(0f)
                            .setDuration(200)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    callbacks.onDismiss(view, dismissDirection)
                                }
                            })
                            .start()
                    } else {
                        // Not dismissed, snap back to start
                        view.animate()
                            .translationX(0f)
                            .alpha(1f)
                            .setDuration(200)
                            .setListener(null)
                            .start()
                    }

                    velocityTracker?.recycle()
                    velocityTracker = null
                    swiping = false
                    swipingDirection = 0
                    return true
                }
                return false
            }

            MotionEvent.ACTION_CANCEL -> {
                if (velocityTracker != null) {
                    if (swiping) {
                        // Canceled, snap back to start
                        view.animate()
                            .translationX(0f)
                            .alpha(1f)
                            .setDuration(200)
                            .setListener(null)
                            .start()
                    }
                    velocityTracker?.recycle()
                    velocityTracker = null
                    swiping = false
                    swipingDirection = 0
                }
                return false
            }
        }
        return false
    }
}