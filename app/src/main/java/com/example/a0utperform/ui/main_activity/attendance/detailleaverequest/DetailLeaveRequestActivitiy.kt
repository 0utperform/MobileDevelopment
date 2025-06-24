package com.example.a0utperform.ui.main_activity.attendance.detailleaverequest

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.example.a0utperform.R
import com.example.a0utperform.data.model.LeaveRequest
import com.example.a0utperform.databinding.ActivityDetailLeaveRequestActivitiyBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class DetailLeaveRequestActivitiy : AppCompatActivity() {

    private lateinit var binding: ActivityDetailLeaveRequestActivitiyBinding
    private val viewModel: DetailLeaveRequestViewModel by viewModels()
    private lateinit var leaveRequest: LeaveRequest

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailLeaveRequestActivitiyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        leaveRequest = intent.getParcelableExtra("leave_requests")!!

        viewModel.fetchUsername(leaveRequest.userId)
        viewModel.username.observe(this) { name ->
            binding.etUsername.setText(name)
        }

        makeEditTextReadOnly(binding.etReason, binding.etFrom, binding.etUntil, binding.etUsername)
        setupUI(leaveRequest)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupUI(data: LeaveRequest) = with(binding) {
        // Reason
        etReason.setText(data.reason)

        // Time formatting
        etFrom.setText(formatDateTime(data.startDate))
        etUntil.setText(formatDateTime(data.endDate))

        val context = binding.root.context
        ctgStatus.text = data.status
        when (data.status.lowercase()) {
            "process" -> {
                ViewCompat.setBackgroundTintList(binding.ctgStatus, ContextCompat.getColorStateList(binding.root.context, R.color.brandBlue))
            }
            "rejected" -> {
                ViewCompat.setBackgroundTintList(binding.ctgStatus, ContextCompat.getColorStateList(binding.root.context, R.color.dark_red))
            }
            "approved" -> {
                ViewCompat.setBackgroundTintList(binding.ctgStatus, ContextCompat.getColorStateList(binding.root.context, R.color.green))
            }
            else -> {
                ViewCompat.setBackgroundTintList(binding.ctgStatus, ContextCompat.getColorStateList(binding.root.context, R.color.gray))
            }
        }
        val selectedDrawable = ContextCompat.getDrawable(context, R.drawable.bg_radio_selected)
        val unselectedDrawable = ContextCompat.getDrawable(context, R.drawable.bg_radio_unselected)
        val brandBlue = ContextCompat.getColor(context, R.color.brandBlue)

        val fullDay = binding.radioFullDay
        val earlyLeave = binding.radioEarlyLeave

        if (data.type == "Full Day") {
            fullDay.isChecked = true
            fullDay.background = selectedDrawable
            fullDay.setTextColor(Color.WHITE)

            earlyLeave.isChecked = false
            earlyLeave.background = unselectedDrawable
            earlyLeave.setTextColor(brandBlue)
        } else if (data.type == "Early Leave") {
            fullDay.isChecked = false
            fullDay.background = unselectedDrawable
            fullDay.setTextColor(brandBlue)

            earlyLeave.isChecked = true
            earlyLeave.background = selectedDrawable
            earlyLeave.setTextColor(Color.WHITE)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatDateTime(timestamp: String): String {
        return try {
            val instant = Instant.parse(timestamp)
            val time = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
            formatter.format(time)
        } catch (e: Exception) {
            "-"
        }
    }
    private fun makeEditTextReadOnly(vararg editTexts: EditText) {
        editTexts.forEach {
            it.isEnabled = false
            it.isFocusable = false
            it.isCursorVisible = false
        }
    }

}
