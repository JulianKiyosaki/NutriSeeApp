package com.capstone.nutrisee.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class FoodItem(val name: String, val calories: Int)

class SharedViewModel : ViewModel() {

    private val _history = MutableLiveData<List<FoodItem>>(emptyList())
    val history: LiveData<List<FoodItem>> = _history

    private val totalCaloriesTarget = 2000

    fun addFoodItem(item: FoodItem) {
        val currentList = _history.value ?: emptyList()
        _history.value = currentList + item
    }

    fun removeFoodItem(item: FoodItem) {
        val currentList = _history.value ?: emptyList()
        _history.value = currentList.filterNot { it == item }
    }

    fun getRemainingCalories(): Int {
        val usedCalories = _history.value?.sumOf { it.calories } ?: 0
        return (totalCaloriesTarget - usedCalories).coerceAtLeast(0)
    }

    fun getTotalCaloriesTarget() = totalCaloriesTarget
}
