package com.prj.japanlib.navigation

object DestinationRoute {
    const val SETTINGS_ROUTE = "settings_route"
    const val DICTIONARY_ROUTE = "dictionary_route"
    const val LOGIN_ROUTE = "login_route"
    const val USERINFO_ROUTE = "userinfo_route"
    const val EXAM_ROUTE = "exam_route"
    const val TRANSLATOR_ROUTE = "translator"

    const val HOME_ROUTE = "home"
    // Dictionary screen routes
    const val DICTIONARY_SEARCH = "search"
    const val DICTIONARY_JLPT_LIST = "jlptList/{level}"
    const val DICTIONARY_CUSTOM_LIST = "customList/{name}/{listId}"
    const val DICTIONARY_THEME_LIST = "themeList/{theme}/{themeName}"
    const val DICTIONARY_DETAIL = "wordDetail/{wordId}"
    const val SEARCH_RESULT = "searchResult/{searchQuery}"
    // Mock test screen routes
    const val MOCK_TEST_LIST = "testList"
    const val MOCK_TEST_QUESTIONS = "test/{level}/{id}/{skill}"
    const val MOCK_TEST_SECTIONS = "test/{level}/{id}"
    const val MOCK_TEST_RESULT = "result/{level}/{id}/{skill}"
    const val REVIEW_ANSWER = "review_answer"
    const val LEARNING_MODE = "learning_mode"
    const val FLASHCARD_LIST = "flashcard_list"
    const val FLASHCARD_DETAILS = "flashcard_detail/{id}/{listType}/{learningState}"
    const val FLASHCARD_OVERVIEW = "flashcard_overview/{id}/{listType}"

}