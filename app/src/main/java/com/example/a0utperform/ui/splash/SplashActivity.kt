package com.example.a0utperform.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.a0utperform.ui.decidelogin.ActivityDecideLogin
import com.example.a0utperform.R

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DELAY: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, ActivityDecideLogin::class.java))
            finish()
        }, SPLASH_DELAY)
    }
}