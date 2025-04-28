package com.example.a0utperform.ui.main_activity.profile

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.a0utperform.R
import com.example.a0utperform.databinding.FragmentProfileBinding
import com.example.a0utperform.ui.main_activity.MainViewModel
import com.example.a0utperform.ui.setting.SettingActivity
import com.russhwolf.settings.Settings
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()
    private val profileViewModel : ProfileViewModel by viewModels()
    private lateinit var binding : FragmentProfileBinding
    companion object {
        fun newInstance() = ProfileFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSetting.setOnClickListener {
            val intent = Intent(requireContext(), SettingActivity::class.java)
            startActivity(intent)
        }


        binding.logoutBtn.setOnClickListener {
            mainViewModel.signOut()
        }

        observeViewModels()



    }
    private fun observeViewModels() {
        profileViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }
        mainViewModel.userSession.observe(viewLifecycleOwner) { session ->
            session?.let {
                profileViewModel.fetchAvatarUrl()
                profileViewModel.fetchPayroll(it.userId)
                profileViewModel.fetchUserTeamAssignments(it.userId)
                profileViewModel.fetchUserOutletAssignments(it.userId)
                binding.name.text = it.name.split(" ").firstOrNull() ?: "User"
                binding.role.text = it.role ?: "Role"

                binding.personalName.text = getString(R.string.name_format, it.name )
                binding.personalAge.text = getString(R.string.age_format, it.age ?: "N/A")
                binding.personalRole.text = getString(R.string.role_format,it.role ?: "N/A")


            }
        }
        profileViewModel.payroll.observe(viewLifecycleOwner) { payroll ->
            binding.personalPayroll.text = if (payroll != null) {
                val formatted = NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(payroll)
                getString(R.string.payroll_format, formatted)
            } else {
                getString(R.string.payroll_format, "N/A")
            }
        }
        profileViewModel.avatarUrl.observe(viewLifecycleOwner) { url ->
            Glide.with(this)
                .load(url ?: R.drawable.placeholder_user)
                .into(binding.profileImage)
        }

        profileViewModel.teamDetail.observe(viewLifecycleOwner) { teamList ->

            if (teamList.isNullOrEmpty()) {
                binding.personalTeam.text = getString(R.string.team_format, "N/A")
            } else {
                val teamNames = teamList.joinToString { it?.name ?: "null" }
                binding.personalTeam.text = getString(R.string.team_format, teamNames)
            }
        }

        profileViewModel.outletDetail.observe(viewLifecycleOwner) { outlet ->
            binding.personalOutlet.text = getString(R.string.outlet_format,outlet?.name ?: "N/A")
        }

    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

}