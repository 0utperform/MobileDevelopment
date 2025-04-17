package com.example.a0utperform.ui.main_activity.outlet.outletdetail

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.a0utperform.R
import com.example.a0utperform.data.model.OutletDetail
import com.example.a0utperform.databinding.ActivityOutletDetailBinding
import com.example.a0utperform.utils.formatToReadableDate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class ActivityOutletDetail : AppCompatActivity() {

    private lateinit var binding: ActivityOutletDetailBinding
    private val outletViewModel: OutletViewModel by viewModels()
    private val adapter = TeamAdapter()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOutletDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvTeams.layoutManager = LinearLayoutManager(this)
        binding.rvTeams.adapter = adapter

        val outletJson = intent.getStringExtra("OUTLET_DETAIL_JSON")
        val outletDetail = outletJson?.let { Json.decodeFromString<OutletDetail>(it) }

        outletDetail?.let { outlet ->
            binding.tvOutletName.text = outlet.name
            binding.tvManager.text = getString(R.string.outlet_manager_format, outlet.manager_name)
            binding.tvOutletId.text = getString(R.string.outletId_format, outlet.outlet_id)
            binding.tvSize.text = getString(R.string.outlet_size_format, outlet.staff_size.toString())
            binding.tvAddress.text = getString(R.string.outlet_address_format, outlet.location)
            binding.tvCreated.text = getString(R.string.created_format, outlet.created_at.formatToReadableDate())

            Glide.with(this)
                .load(outlet.image_url ?: R.drawable.placeholder_user)
                .into(binding.imgOutlet)

            outletViewModel.fetchTeamsByOutlet(outlet.outlet_id)
        }

        outletViewModel.teams.observe(this) { teams ->
            adapter.submitList(teams)
        }

        outletViewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}