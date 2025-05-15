package com.example.a0utperform.ui.setting.changepassword

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.a0utperform.R
import com.example.a0utperform.databinding.ActivityChangePasswordBinding
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.addstaff.ActivityAddStaff
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActivityChangePassword : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    private val changePasswordViewModel: ChangePasswordViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnChangePassword.setOnClickListener {
            val newPassword = binding.edtNewPassword.text.toString()
            if (newPassword.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            } else {
                changePasswordViewModel.changePassword(newPassword)
            }
        }

        changePasswordViewModel.changePasswordResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this, "Failed to change password: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}