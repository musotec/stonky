package tech.muso.stonky

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import tech.muso.stonky.android.MainActivity
import tech.muso.stonky.android.R

class SplashActivity : AppCompatActivity() {

//    override fun onWindowFocusChanged(hasFocus: Boolean) {
//        super.onWindowFocusChanged(hasFocus)
//        if (hasFocus) hideSystemUI()
//    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
//        hideSystemUI()
        setTheme(R.style.AppTheme_NoActionBar)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val user = null // UserDb.getUserInfo()
        GlobalScope.launch {
            // TODO: replace delay with connection to stonky server and route appropriately
            delay(1500)

            withContext(Dispatchers.Main) {
                routeToLandingPage(user)
                finish()
            }
        }
    }

    private fun routeToLandingPage(user: Any?) {
        when {
//            user == null -> MakeUserActivity.start(this)
            else -> MainActivity.start(this)
        }
    }
}