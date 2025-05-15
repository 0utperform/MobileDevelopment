package com.example.a0utperform.ui.setting

import android.content.Intent
import android.os.Bundle
import android.widget.CompoundButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ContextThemeWrapper
import androidx.preference.PreferenceFragmentCompat
import com.example.a0utperform.R
import com.example.a0utperform.databinding.SettingsActivityBinding
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.ActivityOutletDetail
import com.example.a0utperform.ui.setting.changepassword.ActivityChangePassword
import com.example.a0utperform.ui.setting.editprofile.EditProfileActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingActivity : AppCompatActivity() {

    private lateinit var binding : SettingsActivityBinding
    private val settingViewModel: SettingViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.profileSetting.edtProfileBtn.setOnClickListener{
            val detailIntent = Intent(this, EditProfileActivity::class.java)
            startActivity(detailIntent)
        }


        binding.profileSetting.btnChangePassword.setOnClickListener {
            val changePasswordIntent = Intent (this, ActivityChangePassword::class.java)
            startActivity(changePasswordIntent)
        }
        settingViewModel.getThemeSettings().observe(this) { isDarkModeActive: Boolean ->
            if (isDarkModeActive) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                binding.personalizationSetting.switchDarkMode.isChecked = true
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                binding.personalizationSetting.switchDarkMode.isChecked = false
            }
        }

        binding.personalizationSetting.switchDarkMode.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            settingViewModel.saveThemeSetting(isChecked)
        }


    }
}