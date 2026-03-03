package com.prj.japanlib.common.utils

import timber.log.Timber

/**
 * Utility class to manage click debouncing globally across multiple components.
 * This ensures only one click action is processed within a defined time window
 * regardless of which UI element triggered it.
 */
class ClickEventDebouncer(
    private val debounceIntervalMs: Long = 500L
) {
    // Variable to hold the last recorded click time
    @Volatile
    private var mLastClickTime = 0L

    /**
     * Executes the [action] only if the time elapsed since the last click
     * is greater than the [debounceIntervalMs].
     *
     * @param action The function to execute if the click is not debounced.
     * @return true if the action was executed, false otherwise.
     */
    fun processClick(action: () -> Unit): Boolean {
        val currentTime = System.currentTimeMillis()

        if (currentTime - mLastClickTime > debounceIntervalMs) {
            // Update the last click time before executing the action
            mLastClickTime = currentTime

            // Execute the successful action
            action()

            Timber.i("processClick: Click processed. Time: $currentTime")
            return true
        } else {
            Timber.i("processClick: Click ignored (Global Debounce). Time: $currentTime")
            return false
        }
    }
}