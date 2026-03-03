package com.prj.domain.repository

/**
 * Defines the contract for a repository that manages the tutorial status.
 * This interface provides methods to check and update whether the user has seen
 * specific tutorial elements, such as swipe hints.
 */
interface ITutorialStatusRepository {
    /**
     * Asynchronously checks if the user has seen the left swipe tutorial.
     *
     * @return `true` if the left swipe tutorial has been seen, `false` otherwise.
     */
    suspend fun hasSeenLeftSwipeTutorial(): Boolean

    /**
     * Asynchronously checks if the user has seen the right swipe tutorial.
     *
     * @return `true` if the right swipe tutorial has been seen, `false` otherwise.
     */
    suspend fun hasSeenRightSwipeTutorial(): Boolean

    /**
     * Asynchronously marks the left swipe tutorial as seen.
     * This is typically called after the user has interacted with or dismissed the tutorial.
     */
    suspend fun setSeenLeftSwipeTutorial()

    /**
     * Asynchronously marks the right swipe tutorial as seen.
     * This is typically called after the user has interacted with or dismissed the tutorial.
     */
    suspend fun setSeenRightSwipeTutorial()

}