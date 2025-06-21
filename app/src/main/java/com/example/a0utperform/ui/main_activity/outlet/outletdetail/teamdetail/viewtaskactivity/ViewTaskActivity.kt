package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.viewtaskactivity

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.a0utperform.R
import com.example.a0utperform.data.model.SubmissionWithEvidence
import com.example.a0utperform.data.model.TaskData
import com.example.a0utperform.databinding.ActivityViewTaskBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class ViewTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewTaskBinding
    private lateinit var task: TaskData
    private lateinit var submissionAdapter: SubmissionAdapter
    private var completedSubmissions: Int = 0
    private var totalTargetSubmissions: Int = 0

    private val viewModel: ViewTaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val taskJson = intent.getStringExtra("TASK_DETAIL_JSON")
        completedSubmissions = intent.getIntExtra("COMPLETED_SUBMISSIONS", 0)
        totalTargetSubmissions = intent.getIntExtra("TOTAL_TARGET_SUBMISSIONS", 0)
        task = Json.decodeFromString<TaskData>(taskJson!!)

        task.task_id?.let { viewModel.fetchSubmissionsWithEvidence(it) }

       submissionAdapter = SubmissionAdapter(
            viewModel = viewModel,
            lifecycleOwner = this
        )
        binding.rvTaskCompletion.layoutManager = LinearLayoutManager(this)
        binding.rvTaskCompletion.adapter = submissionAdapter

        lifecycleScope.launchWhenStarted {
            viewModel.submissionWithEvidenceList.collect { list ->
                submissionAdapter.submitList(list)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.isLoading.collect { isLoading ->
                showLoading(isLoading)
            }
        }

        SetupUI()
    }

    private fun SetupUI() {
        binding.taskTitle.text = task.title
        binding.tvTaskStatus.text = task.status
        binding.tvTaskDescription.text = task.description

        Glide.with(this)
            .load(task.img_url)
            .into(binding.imgTask)

        val formattedProgress = getString(
            R.string.task_completion_format,
            completedSubmissions.toString(),
            totalTargetSubmissions.toString()
        )
        binding.taskCompletion.text = formattedProgress
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}