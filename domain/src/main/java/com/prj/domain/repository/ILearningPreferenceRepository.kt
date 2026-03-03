package com.prj.domain.repository

/**
 * Interface for managing user preferences related to learning activities.
 *
 * This repository handles settings such as whether to shuffle words during practice
 * and whether to display the meaning of a word before the word itself.
 */
interface ILearningPreferenceRepository {
    /**
     * Checks if the user preference for shuffling words is enabled.
     *
     * @return `true` if words should be shuffled, `false` otherwise.
     */
    suspend fun isShuffleWords(): Boolean

    /**
     * Updates the user preference for shuffling words.
     *
     * @param shuffle `true` to enable shuffling, `false` to disable it.
     */
    suspend fun setShuffleWords(shuffle: Boolean)

    /**
     * Checks if the user preference for showing the meaning of a word first is enabled.
     *
     * @return `true` if meanings should be shown first, `false` otherwise.
     */
    suspend fun isShowMeaningFirst(): Boolean

    /**
     * Updates the user preference for showing the meaning of a word first.
     *
     * @param show `true` to show meanings first, `false` to show words first.
     */
    suspend fun setShowMeaningFirst(show: Boolean)
}
