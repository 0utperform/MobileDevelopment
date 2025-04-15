package com.example.a0utperform.ui.setting

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.preference.PreferenceFragmentCompat
import com.example.a0utperform.R
import com.example.a0utperform.databinding.SettingsActivityBinding

class SettingActivity : AppCompatActivity() {

    private lateinit var binding : SettingsActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}