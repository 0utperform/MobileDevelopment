package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail

import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.a0utperform.R
import com.example.a0utperform.data.model.SubmitStatus
import com.example.a0utperform.data.model.TaskData
import com.example.a0utperform.databinding.EditTaskActivityBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json
import java.io.File

@AndroidEntryPoint
class EditTaskActivity : AppCompatActivity() {

    private lateinit var binding: EditTaskActivityBinding
    private lateinit var task: TaskData
    private var completedSubmissions: Int = 0
    private var totalTargetSubmissions: Int = 0

    private val viewModel : EditTaskViewModel by viewModels()

    private val imageUris = mutableListOf<Uri>()
    private lateinit var adapter: EvidenceAdapter
    private var tempUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempUri != null) {
            imageUris.add(tempUri!!)
            adapter.submitList(imageUris.toList()) // Use a fresh list for DiffUtil
        }
    }


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
        submitStatus()
        setupUI()
        setupEvidenceRecycler()
    }

    private fun submitStatus() {
        binding.btnSubmitTask.setOnClickListener {
            val description = binding.etTaskDescription.text.toString()
            viewModel.submitTaskEvidence(task, imageUris, description)
        }
        viewModel.submitStatus.observe(this) { status ->
            when (status) {
                is SubmitStatus.Success -> {
                    Toast.makeText(this, "Task submitted!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is SubmitStatus.Error -> {
                    Toast.makeText(this, "Error: ${status.message}", Toast.LENGTH_SHORT).show()
                }
                is SubmitStatus.Loading -> {
                    // Optionally show loading UI
                }
            }
        }
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
    private fun setupEvidenceRecycler() {
        adapter = EvidenceAdapter(
            onAddClick = {
                tempUri = createTempImageUri()
                tempUri?.let { uri ->
                    pickImageLauncher.launch(uri)
                }
            }
        )
        binding.rvEvidenceImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvEvidenceImages.adapter = adapter
        adapter.submitList(imageUris.toList())
    }

    private fun createTempImageUri(): Uri {
        val file = File.createTempFile("evidence_", ".jpg", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
    }
}