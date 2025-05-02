package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.createtaskactivity

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.a0utperform.data.model.TaskData
import com.example.a0utperform.data.model.TeamDetail
import com.example.a0utperform.databinding.ActivityCreateTaskBinding
import com.example.a0utperform.utils.formatToSupabaseTimestamp

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@AndroidEntryPoint
class CreateTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateTaskBinding
    private val viewModel: CreateTaskViewModel by viewModels()
    private lateinit var teamDetail: TeamDetail
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivTaskImage.setImageURI(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get team detail from intent
        teamDetail = Json.decodeFromString(intent.getStringExtra("TEAM_DETAIL_JSON") ?: "")

        binding.etDueDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formatted = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                binding.etDueDate.setText(formatted)
            }, year, month, day)

            // Set minimum date to tomorrow
            val tomorrow = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
            }
            datePicker.datePicker.minDate = tomorrow.timeInMillis

            datePicker.show()
        }
        // Image picker
        binding.ivTaskImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.rbDaily.setOnCheckedChangeListener { _, isChecked ->
            binding.etDueDate.isEnabled = !isChecked
            binding.etDueDate.isClickable = !isChecked
            binding.etDueDate.isFocusable = !isChecked
            if (isChecked) {
                binding.etDueDate.setText("")
            }
        }
        // Create task button
        binding.btnCreateTask.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val dueDateRaw = binding.etDueDate.text.toString().trim()
            val submissionText = binding.etSubmissionsPerDay.text.toString().trim()
            val isRepeating = binding.rbDaily.isChecked

            // Field validations
            if (title.isEmpty() || description.isEmpty()  || submissionText.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri == null) {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val jakartaZone = ZoneId.of("Asia/Jakarta")
            val jakartaNow = ZonedDateTime.now(jakartaZone)
            val dueDateIn24Hours = jakartaNow.plusHours(24) // Add 24 hours to current time

            val formattedDueDate = if (dueDateRaw.isEmpty()) {
                formatToSupabaseTimestamp(dueDateIn24Hours)  // Use 24-hour offset
            } else {
                try {
                    val dueDateParsed = LocalDate.parse(dueDateRaw, DateTimeFormatter.ISO_LOCAL_DATE)
                    val dueDateTime = dueDateParsed.atStartOfDay(ZoneId.of("Asia/Jakarta"))
                    formatToSupabaseTimestamp(dueDateTime)
                } catch (e: Exception) {
                    Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }
            val submissionPerDay = submissionText.toIntOrNull()
            if (submissionPerDay == null || submissionPerDay <= 0) {
                Toast.makeText(this, "Submissions per day must be a positive number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val task = TaskData(
                title = title,
                description = description,
                dueDate = formattedDueDate,
                status = "Progress",
                team_id = teamDetail.team_id ?: "",
                submission_per_day = submissionPerDay,
                is_repeating = isRepeating
            )

            viewModel.createTask(selectedImageUri, task)
        }

        viewModel.createResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Task Created", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to create task", Toast.LENGTH_SHORT).show()
            }
        }
    }
}