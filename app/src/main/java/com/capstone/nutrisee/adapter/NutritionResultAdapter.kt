package com.capstone.nutrisee.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.capstone.nutrisee.R
import com.capstone.nutrisee.helper.NutritionResult

class NutritionResultAdapter(
    private val nutritionResultList: List<NutritionResult>
) : RecyclerView.Adapter<NutritionResultAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodNameTextView: TextView = view.findViewById(R.id.textFoodName)
        val proteinDataTextView: TextView = view.findViewById(R.id.textProteinData)
        val carboDataTextView: TextView = view.findViewById(R.id.textCarboData)
        val fatDataTextView: TextView = view.findViewById(R.id.textFatData)
        val fiberDataTextView: TextView = view.findViewById(R.id.textFiberData)
        val caloriesDataTextView: TextView = view.findViewById(R.id.textCaloriesData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nutrition_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (nutritionResultList.isNotEmpty()) {
            val nutritionResult = nutritionResultList[position]

            holder.foodNameTextView.text = nutritionResult.foodName
            holder.proteinDataTextView.text = nutritionResult.protein?.let { String.format("%.1f g", it) } ?: "N/A"
            holder.carboDataTextView.text = nutritionResult.carbohydrate?.let { String.format("%.1f g", it) } ?: "N/A"
            holder.fatDataTextView.text = nutritionResult.fat?.let { String.format("%.1f g", it) } ?: "N/A"
            holder.fiberDataTextView.text = nutritionResult.fiber?.let { String.format("%.1f g", it) } ?: "N/A"
            holder.caloriesDataTextView.text = nutritionResult.calories?.let { String.format("%.1f kcal", it) } ?: "N/A"
        }
    }

    override fun getItemCount() = nutritionResultList.size
}
