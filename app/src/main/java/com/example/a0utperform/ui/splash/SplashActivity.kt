package com.example.a0utperform.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.a0utperform.ui.decidelogin.ActivityDecideLogin
import com.example.a0utperform.R
import com.example.a0utperform.data.local.user.UserPreference
import com.example.a0utperform.ui.main_activity.ActivityMain
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val SPLASH_DELAY: Long = 2000

    @Inject
    lateinit var userPreference: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        checkUserSessionAfterDelay()
    }


    private fun checkUserSessionAfterDelay() {
        lifecycleScope.launch {
            delay(SPLASH_DELAY)

            userPreference.getSession().collect { session ->
                val intent = if (session.isLogin == true) {
                    Intent(this@SplashActivity, ActivityMain::class.java)
                } else {
                    Intent(this@SplashActivity, ActivityDecideLogin::class.java)
                }
                startActivity(intent)
                finish()
            }
        }
    }
}
