package com.prj.data.repository

import com.prj.data.datasource.TutorialStatusDataSource
import com.prj.domain.repository.ITutorialStatusRepository
import javax.inject.Inject

/**
 * An implementation of [ITutorialStatusRepository] that interacts with a local data source
 * to manage the state of tutorial hints. This class delegates calls to the
 * [TutorialStatusDataSource] to persist and retrieve tutorial-related data.
 *
 * @param mTutorialStatusDataSource The data source responsible for handling the actual
 *                                  storage of tutorial statuses, injected by Hilt.
 */
class TutorialStatusRepository @Inject constructor(
    private val mTutorialStatusDataSource: TutorialStatusDataSource
): ITutorialStatusRepository {
    override suspend fun hasSeenLeftSwipeTutorial(): Boolean {
        return mTutorialStatusDataSource.hasSeenLeftPage()
    }

    override suspend fun hasSeenRightSwipeTutorial(): Boolean {
        return mTutorialStatusDataSource.hasSeenRightPage()
    }

    override suspend fun setSeenLeftSwipeTutorial() {
        mTutorialStatusDataSource.setSeenLeftPage(true)
    }

    override suspend fun setSeenRightSwipeTutorial() {
        mTutorialStatusDataSource.setSeenRightPage(true)
    }

}