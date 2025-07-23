package com.capstone.nutrisee.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.nutrisee.R
import com.capstone.nutrisee.adapter.ScanHistoryAdapter
import com.capstone.nutrisee.database.ScanResultDao
import com.capstone.nutrisee.database.ScanResultDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryFragment : Fragment() {

    private lateinit var scanHistoryAdapter: ScanHistoryAdapter
    private lateinit var scanResultDao: ScanResultDao
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        scanResultDao = ScanResultDatabase.getDatabase(requireContext()).scanResultDao()

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view_history)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        scanHistoryAdapter = ScanHistoryAdapter { scanResult ->
            deleteScanResult(scanResult.id)
        }
        recyclerView.adapter = scanHistoryAdapter

        observeHistoryData()

        return view
    }

    private fun observeHistoryData() {
        scanResultDao.getAllScanResults().observe(viewLifecycleOwner) { scanResults ->
            Log.d("HistoryFragment", "Scan Results: $scanResults")
            scanHistoryAdapter.submitList(scanResults)
        }
    }

    private fun deleteScanResult(scanId: Int) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                scanResultDao.deleteScanResultById(scanId)
            }
            Toast.makeText(requireContext(), "Item deleted successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addNewFoodItem(name: String, calories: Int) {
        val newItem = FoodItem(name, calories)
        sharedViewModel.addFoodItem(newItem)
    }

    private fun removeFoodItem(item: FoodItem) {
        sharedViewModel.removeFoodItem(item)
    }

    fun onFoodDetected(name: String, calories: Int) {
        addNewFoodItem(name, calories)
    }
}
