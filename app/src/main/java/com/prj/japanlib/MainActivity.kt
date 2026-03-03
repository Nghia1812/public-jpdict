package com.prj.japanlib

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.prj.domain.model.settingsscreen.AppLanguage
import com.prj.domain.repository.IAppSettingsRepository
import com.prj.japanlib.common.utils.AppLocaleManager
import com.prj.japanlib.common.utils.MyTimberTree
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import timber.log.Timber
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private var deepLinkUriState by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deepLinkUriState = intent?.data
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            ForceDarkModeStatusBar()
            RootScreen(navController = navController)
            
            val currentDeepLinkUri = deepLinkUriState
            LaunchedEffect(currentDeepLinkUri) {
                currentDeepLinkUri?.let { uri ->
                    try {
                        navController.navigate(uri.toString())
                        deepLinkUriState = null // Clear state
                        intent.data = null // Clear intent to prevent re-triggering
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to navigate to deep link: $uri")
                    }
                }
            }
        }
        auth = FirebaseAuth.getInstance()
        if (BuildConfig.DEBUG_LOGGING_ENABLED) {
            Timber.plant(MyTimberTree())
        }
    }

    public override fun onStart() {
        super.onStart()
        if(auth.currentUser != null){
            Timber.i("User is already signed in")
            return
        }
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Timber.i("signInAnonymously:success")
                } else {
                    // If sign in fails, display a message to the user.
                    Timber.e(task.exception, "signInAnonymously:failure ")
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        deepLinkUriState = intent.data
    }

    override fun attachBaseContext(newBase: Context) {
        val localeToSwitchTo = AppLocaleManager.getLanguageCode(context = newBase)
        val localeUpdatedContext = updateLocale(newBase, localeToSwitchTo)
        super.attachBaseContext(localeUpdatedContext)
    }

    private fun updateLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }
}

/**
 * Composable function to set the status bar color in dark mode.
 */
@Composable
fun ForceDarkModeStatusBar() {
    val systemUiController = rememberSystemUiController()
    // Force: dark background + LIGHT icons
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Black,
            darkIcons = false
        )
        systemUiController.setNavigationBarColor(
            color = Color.Black,
            darkIcons = false
        )
    }
}