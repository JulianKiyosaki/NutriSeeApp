package com.capstone.nutrisee.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.capstone.nutrisee.R
import com.capstone.nutrisee.database.ScanResult
import com.google.android.material.card.MaterialCardView
import java.text.DateFormat

class ScanHistoryAdapter(
    private val onDeleteClick: (ScanResult) -> Unit
) : ListAdapter<ScanResult, ScanHistoryAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ScanResult>() {
            override fun areItemsTheSame(oldItem: ScanResult, newItem: ScanResult): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ScanResult, newItem: ScanResult): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val foodNameTextView: TextView = itemView.findViewById(R.id.text_food_name)
        private val caloriesTextView: TextView = itemView.findViewById(R.id.text_calories)
        private val scanDateTextView: TextView = itemView.findViewById(R.id.text_scan_date)
        private val deleteButton: MaterialCardView = itemView.findViewById(R.id.ic_delete)

        fun bind(scanResult: ScanResult) {
            foodNameTextView.text = scanResult.foodName

            val caloriesOnly = scanResult.nutritionInfo
                .split(",")
                .find { it.trim().startsWith("Kalori") }
                ?.trim()
                ?.replaceFirst("Kalori", "Calories")
                ?: "Calories: -"

            caloriesTextView.text = caloriesOnly
            scanDateTextView.text = DateFormat.getDateTimeInstance().format(scanResult.scanDate)

            deleteButton.setOnClickListener {
                onDeleteClick(scanResult)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
