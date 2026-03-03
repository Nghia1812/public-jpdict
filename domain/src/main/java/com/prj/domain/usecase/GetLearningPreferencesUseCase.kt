package com.prj.domain.usecase

import com.prj.domain.repository.ILearningPreferenceRepository
import javax.inject.Inject

/**
 * Use case for getting learning preferences
 */
class GetLearningPreferencesUseCase @Inject constructor(
    private val learningPreferenceRepository: ILearningPreferenceRepository
){
    suspend fun getShufflePreference() : Boolean {
        return learningPreferenceRepository.isShuffleWords()
    }

    suspend fun getShowMeaningFirstPreference() : Boolean {
        return learningPreferenceRepository.isShowMeaningFirst()
    }

}