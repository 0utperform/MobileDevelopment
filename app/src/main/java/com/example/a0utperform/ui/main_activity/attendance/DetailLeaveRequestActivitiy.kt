package com.example.a0utperform.ui.main_activity.attendance

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.a0utperform.R
import com.example.a0utperform.databinding.ActivityDetailLeaveRequestActivitiyBinding

class DetailLeaveRequestActivitiy : AppCompatActivity() {
    private lateinit var binding : ActivityDetailLeaveRequestActivitiyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailLeaveRequestActivitiyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val selectedDrawable = ContextCompat.getDrawable(this, R.drawable.bg_radio_selected)
        val unselectedDrawable = ContextCompat.getDrawable(this, R.drawable.bg_radio_unselected)

        binding.radioGroupType.setOnCheckedChangeListener { _, checkedId ->
            val fullDay = binding.radioFullDay
            val earlyLeave = binding.radioEarlyLeave

            fullDay.background = if (checkedId == R.id.radio_full_day) selectedDrawable else unselectedDrawable
            fullDay.setTextColor(
                if (checkedId == R.id.radio_full_day) Color.WHITE else ContextCompat.getColor(this, R.color.brandBlue)
            )

            earlyLeave.background = if (checkedId == R.id.radio_early_leave) selectedDrawable else unselectedDrawable
            earlyLeave.setTextColor(
                if (checkedId == R.id.radio_early_leave) Color.WHITE else ContextCompat.getColor(this, R.color.brandBlue)
            )
        }

    }
}