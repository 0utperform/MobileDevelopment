package com.example.a0utperform.ui.main_activity.dashboard

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a0utperform.R
import com.example.a0utperform.data.model.TeamDetail
import com.example.a0utperform.databinding.FragmentDashboardBinding
import com.example.a0utperform.databinding.FragmentProfileBinding
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.TeamAdapter
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.DetailTeamActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private val dashboardViewModel: DashboardViewModel by viewModels()
    private lateinit var teamAdapter: TeamAdapter
    private lateinit var binding: FragmentDashboardBinding

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        teamAdapter = TeamAdapter { team ->
            navigateToTeamDetail(team)
        }
        setupRecyclerView()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dashboardViewModel.userSession.observe(viewLifecycleOwner) { session ->
            session?.let {
                dashboardViewModel.fetchAvatarUrl()
                dashboardViewModel.fetchTeamAssignment()

                binding.profileName.text = it.name.split(" ").firstOrNull() ?: "User"
                binding.profileRole.text = it.role ?: "Role"
            }
        }

        dashboardViewModel.avatarUrl.observe(viewLifecycleOwner) { url ->
            Glide.with(this)
                .load(url ?: R.drawable.placeholder_user)
                .into(binding.profileImage)
        }

        dashboardViewModel.teamAssignment.observe(viewLifecycleOwner) { teamList ->
            // Submit the list of teams to the adapter
            teamAdapter.submitList(teamList)
        }
    }

    private fun setupRecyclerView() {
        binding.team.layoutManager = LinearLayoutManager(requireContext())
        binding.team.adapter = teamAdapter
    }

    private fun navigateToTeamDetail(team: TeamDetail) {
        val intent = Intent(requireContext(), DetailTeamActivity::class.java)
        val teamJson = Json.encodeToString(team)
        intent.putExtra("TEAM_DETAIL_JSON", teamJson)
        startActivity(intent)
    }
}