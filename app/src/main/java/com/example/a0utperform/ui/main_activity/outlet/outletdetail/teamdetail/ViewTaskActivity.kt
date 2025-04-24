package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.a0utperform.R
import com.example.a0utperform.data.model.TaskData
import com.example.a0utperform.databinding.ActivityViewTaskBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class ViewTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewTaskBinding// Same layout reused
    private lateinit var task: TaskData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val taskJson = intent.getStringExtra("TASK_DETAIL_JSON")
        task = Json.decodeFromString(taskJson ?: "")

        setupReadOnlyUI()
    }

    private fun setupReadOnlyUI() {


    }
}