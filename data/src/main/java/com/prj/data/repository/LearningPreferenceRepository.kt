package com.prj.data.repository

import com.prj.data.datasource.LearningPreferenceDataSource
import com.prj.domain.repository.ILearningPreferenceRepository
import javax.inject.Inject

class LearningPreferenceRepository @Inject constructor(
    private val mLearningPreferenceDataSource: LearningPreferenceDataSource
) : ILearningPreferenceRepository {
    override suspend fun isShuffleWords(): Boolean {
        return mLearningPreferenceDataSource.isShuffleWords()
    }

    override suspend fun setShuffleWords(shuffle: Boolean) {
        mLearningPreferenceDataSource.setShuffleWords(shuffle)
    }

    override suspend fun isShowMeaningFirst(): Boolean {
        return mLearningPreferenceDataSource.isShowMeaningFirst()
    }

    override suspend fun setShowMeaningFirst(show: Boolean) {
        mLearningPreferenceDataSource.setShowMeaningFirst(show)
    }
}