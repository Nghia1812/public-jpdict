package com.prj.domain.repository

import com.prj.domain.model.dictionaryscreen.KanjiImage

/**
 * Interface for the kanji classifier.
 *
 */
interface IKanjiClassifier {
    /**
     * Detects kanji in an image and returns a list of detected kanji.
     *
     * @param image the image to classify
     * @return list of detected kanji
     */
    fun classifyAsync(image: KanjiImage): Result<List<String>>
}