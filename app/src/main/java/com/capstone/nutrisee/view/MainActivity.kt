package com.capstone.nutrisee.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.capstone.nutrisee.R
import com.capstone.nutrisee.databinding.ActivityMainBinding
import com.capstone.nutrisee.utils.reduceFileImage
import com.capstone.nutrisee.utils.uriToFile
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupImageLaunchers()

        sharedViewModel.history.observe(this) { historyList ->
            val remaining = sharedViewModel.getRemainingCalories()
            val total = sharedViewModel.getTotalCaloriesTarget()
            binding.textCalories.text = "Calories: $remaining/$total kcal"
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.button_camera -> {
                    showImageSourceDialog()
                    true
                }
                R.id.button_home -> {
                    replaceFragment(HistoryFragment())
                    true
                }
                else -> false
            }
        }
        if (savedInstanceState == null) {
            binding.bottomNav.selectedItemId = R.id.button_home
            replaceFragment(HistoryFragment())
        }
    }

    private fun setupImageLaunchers() {
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleCameraResult(result)
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleGalleryResult(result)
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Kamera", "Galeri")
        AlertDialog.Builder(this)
            .setTitle("Pilih Sumber Gambar")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> openGallery()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        }
    }

    private fun openCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        cameraLauncher.launch(intent)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    private fun handleCameraResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUriString = result.data?.getStringExtra("picture_uri")
            if (imageUriString != null) {
                val imageUri = Uri.parse(imageUriString)
                Log.d("MainActivity", "CameraX image captured: $imageUri")
                val file = uriToFile(imageUri, this).reduceFileImage()
                Log.d("MainActivity", "Reduced camera image: ${file.absolutePath}, size: ${file.length()}")
                navigateToResultActivity(Uri.fromFile(file))
            } else {
                Log.e("CameraResult", "Image URI from CameraX is null.")
                Toast.makeText(this, "Gagal mendapatkan gambar dari kamera.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d("MainActivity", "CameraX operation canceled.")
            Toast.makeText(this, "Pengambilan gambar dibatalkan.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleGalleryResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri = result.data?.data
            selectedImageUri?.let { uri ->
                Log.d("MainActivity", "Gallery image selected: $uri")
                val file = uriToFile(uri, this).reduceFileImage()
                Log.d("MainActivity", "Reduced gallery image: ${file.absolutePath}, size: ${file.length()}")
                navigateToResultActivity(Uri.fromFile(file))
            } ?: run {
                Log.e("GalleryResult", "selectedImageUri is null.")
                Toast.makeText(this, "Gambar tidak ditemukan.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d("MainActivity", "Gallery operation canceled or failed. Result code: ${result.resultCode}")
            Toast.makeText(this, "Pemilihan gambar dari galeri dibatalkan.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToResultActivity(imageUri: Uri?) {
        if (imageUri != null) {
            val intent = Intent(this, ResultActivity::class.java).apply {
                putExtra("image_uri", imageUri.toString())
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Gambar tidak ditemukan.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun replaceFragment(fragment: androidx.fragment.app.Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 101
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Izin kamera ditolak.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}