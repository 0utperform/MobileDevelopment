package com.example.a0utperform.ui.main_activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.a0utperform.R
import com.example.a0utperform.databinding.ActivityMainBinding
import com.example.a0utperform.ui.decidelogin.ActivityDecideLogin
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.auth.Auth

@AndroidEntryPoint
class ActivityMain : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var supabaseAuth : Auth
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            elevation = 4f
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_dashboard)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_dashboard, R.id.navigation_attendance,
                R.id.navigation_outlet, R.id.navigation_leaderboard, R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        observeSession()

        val data: Uri? = intent?.data
        if (data != null && data.toString().startsWith("outperform://auth")) {
            Toast.makeText(this, "Account Verified Successfully", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sign_out_menu -> {
                mainViewModel.signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun observeSession() {
        mainViewModel.userSession.observe(this) { session ->
            if (session == null || !session.isLogin) {
                navigateToLogin()
            }
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, ActivityDecideLogin::class.java))
        finish()
    }
}