package com.prj.domain.usecase

import com.prj.domain.model.dictionaryscreen.KanjiImage
import com.prj.domain.repository.IKanjiClassifier
import javax.inject.Inject


/**
 * A use case for classifying a Kanji character from an image.
 *
 * @param kanjiClassifier The repository responsible for the Kanji classification logic.
 */
class ClassifySearchUseCase @Inject constructor(
    private val kanjiClassifier: IKanjiClassifier
){
    /**
     *
     * @param image The [KanjiImage] object containing the image data to be classified.
     * @return A [Result] wrapper containing a list of potential matching Kanji characters as strings
     *         on success, or an exception on failure.
     */
    operator fun invoke(
        image: KanjiImage
    ): Result<List<String>>{
        return kanjiClassifier.classifyAsync(image)
    }
}