package com.prj.domain.usecase

import com.prj.domain.repository.ITutorialStatusRepository
import javax.inject.Inject

/**
 * A use case that retrieves the status of tutorial hints from the repository.
 * This class provides a clean and reusable way for ViewModels to check if certain
 * tutorial elements have been viewed by the user.
 *
 * @param tutorialStatusRepository The repository responsible for managing tutorial state,
 *                                 injected by Hilt.
 */
class GetTutorialStatusUseCase @Inject constructor(
    private val tutorialStatusRepository: ITutorialStatusRepository
) {
    /**
     * Checks if the user has previously seen the left swipe tutorial hint.
     *
     * @return `true` if the user has seen the hint, `false` otherwise.
     */
    suspend fun hasSeenLeftSwipe(): Boolean {
        return tutorialStatusRepository.hasSeenLeftSwipeTutorial()
    }

    /**
     * Checks if the user has previously seen the right swipe tutorial hint.
     *
     * @return `true` if the user has seen the hint, `false` otherwise.
     */
    suspend fun hasSeenRightSwipe(): Boolean {
        return tutorialStatusRepository.hasSeenRightSwipeTutorial()
    }
}