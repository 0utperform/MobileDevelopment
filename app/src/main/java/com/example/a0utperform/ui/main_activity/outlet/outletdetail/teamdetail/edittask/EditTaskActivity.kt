package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.edittask

import android.os.Bundle
import android.text.Editable
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.a0utperform.R
import com.example.a0utperform.databinding.ActivityDetailTeamBinding
import com.example.a0utperform.databinding.ActivityEditTaskBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditTaskBinding
    private val viewModel: EditTaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val taskId = intent.getStringExtra("task_id") ?: return
        viewModel.fetchTaskById(taskId)

        viewModel.task.observe(this) { task ->
            task?.let {
                binding.taskTitle.text = it.title
                binding.taskDesc.text = it.description

                Glide.with(this)
                    .load(it.img_url ?: R.drawable.placeholder_user)
                    .into(binding.taskImage)
            }
        }
    }
}