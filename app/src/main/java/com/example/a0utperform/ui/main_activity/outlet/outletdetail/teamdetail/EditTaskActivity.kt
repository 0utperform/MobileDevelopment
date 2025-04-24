package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.a0utperform.R
import com.example.a0utperform.data.model.TaskData
import com.example.a0utperform.databinding.EditTaskActivityBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class EditTaskActivity : AppCompatActivity() {

    private lateinit var binding: EditTaskActivityBinding
    private lateinit var task: TaskData
    private var completedSubmissions: Int = 0
    private var totalTargetSubmissions: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditTaskActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Deserialize Task object
        val taskJson = intent.getStringExtra("TASK_DETAIL_JSON")
        task = Json.decodeFromString(taskJson ?: "")

        // Get the additional progress extras
        completedSubmissions = intent.getIntExtra("COMPLETED_SUBMISSIONS", 0)
        totalTargetSubmissions = intent.getIntExtra("TOTAL_TARGET_SUBMISSIONS", 0)

        setupUI()
    }

    private fun setupUI() {
        binding.taskTitle.text = task.title
        binding.tvTaskDescription.text = task.description

        Glide.with(this)
            .load(task.img_url)
            .into(binding.imgTask)


        val formattedProgress = getString(
            R.string.formatted_completion,
            completedSubmissions.toString(),
            totalTargetSubmissions.toString()
        )
        binding.root.findViewById<TextView>(R.id.formatted_progress).text = formattedProgress
    }
}