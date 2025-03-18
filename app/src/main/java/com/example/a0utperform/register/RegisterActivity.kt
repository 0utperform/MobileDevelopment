package com.example.a0utperform.register

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.a0utperform.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

     binding = ActivityRegisterBinding.inflate(layoutInflater)
     setContentView(binding.root)

    }
}