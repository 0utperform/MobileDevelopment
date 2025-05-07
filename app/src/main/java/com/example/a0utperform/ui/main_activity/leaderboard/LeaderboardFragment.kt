package com.example.a0utperform.ui.main_activity.leaderboard

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a0utperform.R
import com.example.a0utperform.data.model.OutletDetail
import com.example.a0utperform.databinding.FragmentLeaderboardBinding
import com.example.a0utperform.databinding.FragmentOutletBinding
import com.example.a0utperform.ui.main_activity.outlet.OutletAdapter
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.ActivityOutletDetail
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.TeamAdapter
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.DetailTeamActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class LeaderboardFragment : Fragment(), OutletAdapter.OnOutletClickListener {

    private lateinit var binding : FragmentLeaderboardBinding
    private lateinit var teamAdapter: LeaderboardTeamAdapter
    private lateinit var outletAdapter: LeaderboardOutletAdapter
    private val viewModel: LeaderboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.fetchTopOutlets()
        viewModel.fetchTopTeams()

        outletAdapter = LeaderboardOutletAdapter()
        teamAdapter = LeaderboardTeamAdapter()
        binding.rvBestOutlets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBestTeams.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBestOutlets.adapter = outletAdapter
        binding.rvBestTeams.adapter = teamAdapter

        viewModel.topOutlets.observe(viewLifecycleOwner) { result ->
            result.onSuccess { outletAdapter.submitList(it) }
                .onFailure { showToast("Failed to load outlets") }
        }

        viewModel.topTeams.observe(viewLifecycleOwner) { result ->
            result.onSuccess { teamAdapter.submitList(it) }
                .onFailure { showToast("Failed to load teams") }
        }
    }
    override fun onOutletClick(outletDetail: OutletDetail) {
        val outletJson = Json.encodeToString(outletDetail)
        val detailIntent = Intent(context, ActivityOutletDetail::class.java).apply {
            putExtra("OUTLET_DETAIL_JSON", outletJson)
        }
        startActivity(detailIntent)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


}
