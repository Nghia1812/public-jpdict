package com.prj.japanlib

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.prj.japanlib.common.components.SplashScreen
import com.prj.japanlib.ui.theme.JapanlibTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JapanlibTheme {
                ForceDarkModeStatusBar()
                SplashScreen(
                    onLoadingComplete = {
                        navigateToMainActivity()
                    }
                )
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@SplashActivity, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
