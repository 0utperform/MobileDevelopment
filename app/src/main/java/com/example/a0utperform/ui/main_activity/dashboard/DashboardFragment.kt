package com.example.a0utperform.ui.main_activity.dashboard

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
import com.example.a0utperform.databinding.FragmentDashboardBinding
import com.example.a0utperform.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : Fragment() {
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var binding : FragmentDashboardBinding

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        recyclerView = binding.task
        setupRecyclerView()

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dashboardViewModel.userSession.observe(viewLifecycleOwner){ session ->
            session?.let {
                dashboardViewModel.fetchAvatarUrl()

                binding.profileName.text = it.name.split(" ").firstOrNull() ?: "User"
                binding.profileRole.text = it.role ?: "Role"
            }

        }

        dashboardViewModel.avatarUrl.observe(viewLifecycleOwner) { url ->
            Glide.with(this)
                .load(url ?: R.drawable.placeholder_user)
                .into(binding.profileImage)
        }

    }

        private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

    }


}
