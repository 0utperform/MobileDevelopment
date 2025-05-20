package com.example.a0utperform.ui.main_activity.attendance

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.a0utperform.R
import com.example.a0utperform.databinding.ActivityDetailLeaveRequestActivitiyBinding

class EditLeaveRequestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailLeaveRequestActivitiyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDetailLeaveRequestActivitiyBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnApproveLeave.visibility = View.VISIBLE
        binding.btnRejectLeave.visibility = View.VISIBLE

    }
}