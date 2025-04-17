package com.example.a0utperform.ui.main_activity.outlet

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a0utperform.R
import com.example.a0utperform.data.model.OutletDetail
import com.example.a0utperform.ui.main_activity.ActivityMain
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.ActivityOutletDetail
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class OutletFragment : Fragment(), OutletAdapter.OnOutletClickListener {

    companion object {
        fun newInstance() = OutletFragment()
    }

    private val viewModel: OutletViewModel by viewModels()
    private lateinit var outletAdapter: OutletAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onOutletClick(outletDetail: OutletDetail) {
        val outletJson = Json.encodeToString(outletDetail)
        val detailIntent = Intent(context, ActivityOutletDetail::class.java).apply {
            putExtra("OUTLET_DETAIL_JSON", outletJson)
        }
        startActivity(detailIntent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_outlet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvOutlets = view.findViewById<RecyclerView>(R.id.rv_outlets)
        rvOutlets.layoutManager = LinearLayoutManager(requireContext())

        outletAdapter = OutletAdapter(listener = this)
        rvOutlets.adapter = outletAdapter


        viewModel.outlets.observe(viewLifecycleOwner) { outletList ->
            outletAdapter.submitList(outletList)
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.fetchOutlets()
    }

}