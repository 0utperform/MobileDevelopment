package com.example.a0utperform.ui.main_activity.outlet

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.a0utperform.R
import com.example.a0utperform.databinding.ActivityCreateOutletBinding
import com.example.a0utperform.databinding.ActivityMainBinding
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.CreateOutletViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateOutletActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateOutletBinding
    private val viewModel: CreateOutletViewModel by viewModels()
    private var imageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            binding.ivImagePicker.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateOutletBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivImagePicker.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnCreateOutlet.setOnClickListener {
            val name = binding.etOutletName.text.toString().trim()
            val location = binding.etLocation.text.toString().trim()
            val uri = imageUri

            if (name.isEmpty() || location.isEmpty() || uri == null) {
                Toast.makeText(this, "Please fill all fields and pick an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.createOutlet(name, location, uri)
        }

        viewModel.outletCreationStatus.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Outlet created successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure {
                Toast.makeText(this, "Failed to create outlet: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}