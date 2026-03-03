package com.prj.domain.usecase

import com.prj.domain.repository.ITutorialStatusRepository
import javax.inject.Inject

/**
 * A use case responsible for saving the state of tutorial hints.
 * This class provides methods to mark specific tutorials, like swipe hints, as "seen"
 * in the repository. It is typically used by ViewModels after a user has interacted
 * with a tutorial element.
 *
 * @param tutorialStatusRepository The repository for managing tutorial state, injected by Hilt.
 */
class SaveTutorialStatusUseCase @Inject constructor(
    private val tutorialStatusRepository: ITutorialStatusRepository
)  {
    /**
     * Marks the left swipe tutorial hint as seen in the repository.
     */
    suspend fun markLeftSwipeSeen() {
        tutorialStatusRepository.setSeenLeftSwipeTutorial()
    }

    /**
     * Marks the right swipe tutorial hint as seen in the repository.
     */
    suspend fun markRightSwipeSeen() {
        tutorialStatusRepository.setSeenRightSwipeTutorial()
    }
}