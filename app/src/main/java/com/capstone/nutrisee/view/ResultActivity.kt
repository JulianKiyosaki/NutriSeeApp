package com.capstone.nutrisee.view

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.nutrisee.R
import com.capstone.nutrisee.adapter.NutritionResultAdapter
import com.capstone.nutrisee.database.ScanResult
import com.capstone.nutrisee.database.ScanResultDatabase
import com.capstone.nutrisee.helper.FoodAnalysisListener
import com.capstone.nutrisee.helper.ImageClassifierHelper
import com.capstone.nutrisee.helper.NutritionResult
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class ResultActivity : AppCompatActivity(), FoodAnalysisListener {

    private lateinit var imageViewResult: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerViewNutrients: RecyclerView
    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private val executor = Executors.newSingleThreadExecutor()

    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        imageViewResult = findViewById(R.id.imageResult)
        progressBar = findViewById(R.id.progressBarLoading)
        recyclerViewNutrients = findViewById(R.id.recyclerViewNutrients)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        currentImageUri = intent.getStringExtra("image_uri")?.let { Uri.parse(it) }

        if (currentImageUri == null) {
            Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        imageClassifierHelper = ImageClassifierHelper(
            context = this,
            listener = this,
        )

        processImage(currentImageUri!!)
    }

    private fun processImage(imageUri: Uri) {
        showLoading(true)
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            imageViewResult.setImageBitmap(bitmap)
            executor.execute {
                imageClassifierHelper.analyzeFood(bitmap)
            }
        } catch (e: Exception) {
            showLoading(false)
            Toast.makeText(this, "Failed to load image: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("ResultActivity", "Error loading image: ${e.message}", e)
        }
    }

    override fun onError(error: String) {
        runOnUiThread {
            showLoading(false)
            Toast.makeText(this, "Analysis Error: $error", Toast.LENGTH_LONG).show()
            Log.e("ResultActivity", "ImageClassifierHelper Error: $error")
            recyclerViewNutrients.visibility = View.GONE
        }
    }

    override fun onResults(results: List<NutritionResult>?, inferenceTime: Long) {
        runOnUiThread {
            showLoading(false)
            Log.i("ResultActivity", "Inference time: $inferenceTime ms")

            if (!results.isNullOrEmpty()) {
                Log.d("ResultActivity", "Classification results received: ${results.size}")
                results.forEach { result ->
                    Log.d("ResultActivity", "-> ${result.foodName} | Confidence: ${String.format("%.2f", result.confidence ?: 0f)}")
                }

                val topResult = results.first()
                saveScanResultToDatabase(topResult)
                showNutritionResultsInRecyclerView(results)

                val detectedFoodToast = "${topResult.foodName} (${String.format("%.0f", (topResult.confidence ?: 0f) * 100)}%)"
                displayDetectedFoodToast(detectedFoodToast)

            } else {
                showNoResultsFound()
            }
        }
    }

    private fun showNoResultsFound() {
        Toast.makeText(this, "Could not identify the food.", Toast.LENGTH_SHORT).show()
        recyclerViewNutrients.visibility = View.GONE
    }

    private fun saveScanResultToDatabase(nutritionResult: NutritionResult) {
        val nutritionDetailsString = StringBuilder()
        nutritionResult.protein?.let { if (it > 0) nutritionDetailsString.append("Protein: ${String.format("%.1f", it)}g, ") }
        nutritionResult.carbohydrate?.let { if (it > 0) nutritionDetailsString.append("Karbohidrat: ${String.format("%.1f", it)}g, ") }
        nutritionResult.fat?.let { if (it > 0) nutritionDetailsString.append("Lemak: ${String.format("%.1f", it)}g, ") }
        nutritionResult.fiber?.let { if (it > 0) nutritionDetailsString.append("Serat: ${String.format("%.1f", it)}g, ") }
        nutritionResult.calories?.let { if (it > 0) nutritionDetailsString.append("Kalori: ${String.format("%.1f", it)}kkal") }

        val scanResultEntity = ScanResult(
            foodName = nutritionResult.foodName,
            nutritionInfo = nutritionDetailsString.toString().trimEnd(',', ' ').ifEmpty { "No nutrition details" },
            scanDate = System.currentTimeMillis()
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val database = ScanResultDatabase.getDatabase(applicationContext)
                database.scanResultDao().insertScanResult(scanResultEntity)
                Log.d("ResultActivity", "Scan result saved to database: $scanResultEntity")
            } catch (e: Exception) {
                Log.e("ResultActivity", "Failed to save scan result to database: ${e.message}", e)
            }
        }
    }

    private fun displayDetectedFoodToast(foodName: String) {
        Toast.makeText(this, "Terdeteksi: $foodName", Toast.LENGTH_LONG).show()
    }

    private fun showNutritionResultsInRecyclerView(results: List<NutritionResult>) {
        recyclerViewNutrients.visibility = View.VISIBLE
        recyclerViewNutrients.layoutManager = LinearLayoutManager(this)
        recyclerViewNutrients.adapter = NutritionResultAdapter(results)

        val parentLayout = findViewById<View>(android.R.id.content)
        val snackbarMessage = "Analysis complete! Found ${results.size} possibilities."
        Snackbar.make(parentLayout, snackbarMessage, Snackbar.LENGTH_LONG).show()

        Log.d("ResultActivity", "Displaying nutrition in RecyclerView with ${results.size} items.")
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
        if (::imageClassifierHelper.isInitialized) {
            imageClassifierHelper.close()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}