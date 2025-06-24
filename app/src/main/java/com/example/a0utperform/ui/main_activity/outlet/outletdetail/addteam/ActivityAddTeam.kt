package com.example.a0utperform.ui.main_activity.outlet.outletdetail.addteam

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.a0utperform.data.model.OutletDetail
import com.example.a0utperform.data.repository.DatabaseRepository
import com.example.a0utperform.databinding.ActivityAddTeamBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class ActivityAddTeam : AppCompatActivity() {

    private lateinit var binding: ActivityAddTeamBinding
    private val viewModel: AddTeamViewModel by viewModels()
    private var imageUri: Uri? = null
    private var outletId: String? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = it
            binding.ivImagePicker.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTeamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Decode outlet from JSON in intent
        val outletJson = intent.getStringExtra("OUTLET_DETAIL_JSON")
        val outletDetail = outletJson?.let { Json.decodeFromString<OutletDetail>(it) }
        outletId = outletDetail?.outlet_id

        if (outletId == null) {
            Toast.makeText(this, "Outlet ID not found!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.ivImagePicker.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnCreateTeam.setOnClickListener {
            val name = binding.etTeamName.text.toString().trim()
            val desc = binding.etDescription.text.toString().trim()

            if (name.isEmpty() || desc.isEmpty() || imageUri == null) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.createTeam(name, desc, outletId!!, imageUri)
        }
        viewModel.isLoading.observe(this) { loading ->
            binding.progressBarLoading.visibility = if (loading) View.VISIBLE else View.GONE

        }
        viewModel.teamCreationStatus.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Team created successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure {
                Toast.makeText(this, "Failed to create team: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}