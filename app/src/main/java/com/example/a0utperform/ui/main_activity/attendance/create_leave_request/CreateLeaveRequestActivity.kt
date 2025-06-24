package com.example.a0utperform.ui.main_activity.attendance.create_leave_request

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.a0utperform.R
import com.example.a0utperform.databinding.ActivityCreateLeaveRequestBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@AndroidEntryPoint
class CreateLeaveRequestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateLeaveRequestBinding
    private val viewModel: CreateLeaveRequestViewModel by viewModels()

    private var startDateTime: String? = null
    private var endDateTime: String? = null
    private var selectedType: String = "Full Day"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateLeaveRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.etFrom.setOnClickListener {
            pickDateTime { datetime ->
                startDateTime = datetime
                binding.etFrom.setText(datetime)
            }
        }

        binding.etUntil.setOnClickListener {
            pickDateTime { datetime ->
                endDateTime = datetime
                binding.etUntil.setText(datetime)
            }
        }

        setupTypeSelection()

        // Submit button
        binding.btnApproveLeave.setOnClickListener {
            val reason = binding.etReason.text.toString()

            if (startDateTime.isNullOrBlank() || endDateTime.isNullOrBlank() || reason.isBlank()) {
                Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show()
            } else {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val start = LocalDateTime.parse(startDateTime, formatter)
                val end = LocalDateTime.parse(endDateTime, formatter)

                if (start.toLocalDate() != end.toLocalDate()) {
                    Toast.makeText(this, "Start and end must be on the same date", Toast.LENGTH_SHORT).show()
                } else if (end.isBefore(start)) {
                    Toast.makeText(this, "End time cannot be earlier than start time", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.submitLeave(startDateTime!!, endDateTime!!, reason, selectedType)
                }
            }
        }

        // Observe result
        viewModel.submitStatus.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Leave request submitted", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to submit leave request", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupTypeSelection(preselectedType: String? = null) {
        val selectedDrawable = ContextCompat.getDrawable(this, R.drawable.bg_radio_selected)
        val unselectedDrawable = ContextCompat.getDrawable(this, R.drawable.bg_radio_unselected)
        val brandBlue = ContextCompat.getColor(this, R.color.brandBlue)

        // Apply preselected type if any
        preselectedType?.let { type ->
            when (type) {
                "Full Day" -> {
                    binding.radioFullDay.isChecked = true
                    binding.radioFullDay.background = selectedDrawable
                    binding.radioFullDay.setTextColor(Color.WHITE)

                    binding.radioEarlyLeave.isChecked = false
                    binding.radioEarlyLeave.background = unselectedDrawable
                    binding.radioEarlyLeave.setTextColor(brandBlue)

                    selectedType = "Full Day"
                }
                "Early Leave" -> {
                    binding.radioEarlyLeave.isChecked = true
                    binding.radioEarlyLeave.background = selectedDrawable
                    binding.radioEarlyLeave.setTextColor(Color.WHITE)

                    binding.radioFullDay.isChecked = false
                    binding.radioFullDay.background = unselectedDrawable
                    binding.radioFullDay.setTextColor(brandBlue)

                    selectedType = "Early Leave"
                }
            }
        }

        // Listener for manual selection
        binding.radioGroupType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_full_day -> {
                    selectedType = "Full Day"
                    binding.radioFullDay.background = selectedDrawable
                    binding.radioFullDay.setTextColor(Color.WHITE)
                    binding.radioEarlyLeave.background = unselectedDrawable
                    binding.radioEarlyLeave.setTextColor(brandBlue)
                }
                R.id.radio_early_leave -> {
                    selectedType = "Early Leave"
                    binding.radioEarlyLeave.background = selectedDrawable
                    binding.radioEarlyLeave.setTextColor(Color.WHITE)
                    binding.radioFullDay.background = unselectedDrawable
                    binding.radioFullDay.setTextColor(brandBlue)
                }
            }
        }
    }



    private fun pickDateTime(callback: (String) -> Unit) {
        val calendar = Calendar.getInstance()

        // Date Picker first
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)

                // Time Picker second
                TimePickerDialog(
                    this,
                    { _, hour, minute ->
                        val selectedTime = String.format("%02d:%02d", hour, minute)
                        val fullDateTime = "$selectedDate $selectedTime"
                        callback(fullDateTime)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

}
