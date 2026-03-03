package com.prj.domain.usecase

import com.prj.domain.repository.ILearningPreferenceRepository
import javax.inject.Inject

class SetLearningPreferencesUseCase @Inject constructor(
    private val learningPreferenceRepository: ILearningPreferenceRepository
){
    suspend fun setShufflePreference(shuffle: Boolean) {
        learningPreferenceRepository.setShuffleWords(shuffle)
    }

    suspend fun setShowMeaningFirstPreference(show: Boolean) {
        learningPreferenceRepository.setShowMeaningFirst(show)
    }

}