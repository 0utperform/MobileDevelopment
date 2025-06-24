package com.example.a0utperform.ui.setting.editprofile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.a0utperform.R
import com.example.a0utperform.databinding.ActivityEditProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class EditProfileActivity : AppCompatActivity() {
    private val editProfileViewModel : EditProfileViewModel by viewModels()
    private lateinit var binding : ActivityEditProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModels()
        binding.btnEditProfile.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val age = binding.etAge.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()

            if (name.isNotEmpty() && age.isNotEmpty() && phone.isNotEmpty()) {
                editProfileViewModel.updateUserProfile(name, age, phone)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun observeViewModels() {
        editProfileViewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        editProfileViewModel.userSession.observe(this) { session ->
            session?.let {
                editProfileViewModel.fetchPayroll(it.userId)
                editProfileViewModel.fetchUserTeamAssignments(it.userId)
                editProfileViewModel.fetchUserOutletAssignments(it.userId)
                binding.etName.setText(it.name)
                binding.etRole.setText(it.role)
                binding.etAge.setText(it.age)
                binding.etPhone.setText(it.phone)


            }
        }
        editProfileViewModel.payroll.observe(this) { payroll ->
            val formatted = if (payroll != null) {
                val currency = NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(payroll)
                getString(R.string.payroll_format, currency)
            } else {
                getString(R.string.payroll_format, "N/A")
            }

            binding.etPayroll.setText(formatted)
        }

        editProfileViewModel.teamDetail.observe(this) { teamList ->

            if (teamList.isNullOrEmpty()) {
                binding.etTeam.setText(getString(R.string.team_format, "N/A"))
            } else {
                val teamNames = teamList.joinToString { it?.name ?: "null" }
                binding.etTeam.setText(getString(R.string.team_format, teamNames))
            }
        }

        editProfileViewModel.outletDetail.observe(this) { outletList ->
            val outletNames = outletList.joinToString { it.name ?: "N/A" }
            binding.etOutlet.setText(getString(R.string.outlet_format, outletNames))
        }

        editProfileViewModel.profileUpdateSuccess.observe(this) { event ->
            event.getContentIfNotHandled()?.let { success ->
                if (success) {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                else {
                    Toast.makeText(this, "Failed to create outlet", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}