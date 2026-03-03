package com.prj.japanlib

import android.app.Application
import android.content.res.Resources
import com.prj.domain.model.settingsscreen.AppLanguage
import com.prj.domain.repository.IAppSettingsRepository
import com.prj.domain.repository.TextTokenizer
import com.prj.japanlib.common.utils.AppLocaleManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber

@HiltAndroidApp
class JapanDictApplycation : Application() {
    @Inject lateinit var textTokenizer: TextTokenizer

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            try {
                textTokenizer.initialize()
                Timber.d("Text tokenizer initialized successfully")
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize text tokenizer")
            }
        }
    }
}
