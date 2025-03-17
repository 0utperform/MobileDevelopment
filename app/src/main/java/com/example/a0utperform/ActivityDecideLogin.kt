package com.example.a0utperform

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.a0utperform.databinding.ActivityDecideLoginBinding

class ActivityDecideLogin : AppCompatActivity() {

    private lateinit var binding : ActivityDecideLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDecideLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signIn.setOnClickListener()
        {
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }

        binding.signUp.setOnClickListener(){
            val intent = Intent(this,RegisterActivity::class.java)
            startActivity(intent)
        }
    }

}