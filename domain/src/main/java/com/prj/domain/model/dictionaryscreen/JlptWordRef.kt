package com.prj.domain.model.dictionaryscreen

import com.prj.domain.model.testscreen.LearningState

data class JlptWordRef(
    val listId: String = "",
    val entryId: Int = 0,
    val learningState: LearningState = LearningState.NOT_LEARNT_YET
)
