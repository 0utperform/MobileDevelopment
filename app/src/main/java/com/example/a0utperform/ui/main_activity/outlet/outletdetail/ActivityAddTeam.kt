package com.example.a0utperform.ui.main_activity.outlet.outletdetail

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.a0utperform.R
import com.example.a0utperform.data.model.OutletDetail
import com.example.a0utperform.databinding.ActivityAddTeamBinding
import kotlinx.serialization.json.Json

class ActivityAddTeam : AppCompatActivity() {
    private lateinit var binding : ActivityAddTeamBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTeamBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val outletJson = intent.getStringExtra("OUTLET_DETAIL_JSON")
        val outletDetail = outletJson?.let { Json.decodeFromString<OutletDetail>(it) }
    }
}