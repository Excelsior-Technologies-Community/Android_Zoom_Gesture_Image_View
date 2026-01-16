package com.ext.android_zoom_gesture_image_view



import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.ext.android_zoom_gesture_imageview.ZoomGestureImageView

class MainActivity : AppCompatActivity() {

    private lateinit var zoomImageView: ZoomGestureImageView
    private lateinit var tvZoomLevel: TextView
    private lateinit var btnZoomIn: Button
    private lateinit var btnZoomOut: Button
    private lateinit var btnReset: Button
    private lateinit var btnPickImage: Button

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                loadImageFromUri(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupListeners()
        updateZoomLevel()
    }

    private fun initializeViews() {
        zoomImageView = findViewById(R.id.zoomImageView)
        tvZoomLevel = findViewById(R.id.tvZoomLevel)
        btnZoomIn = findViewById(R.id.btnZoomIn)
        btnZoomOut = findViewById(R.id.btnZoomOut)
        btnReset = findViewById(R.id.btnReset)
        btnPickImage = findViewById(R.id.btnPickImage)
    }

    private fun setupListeners() {
        // Zoom In button
        btnZoomIn.setOnClickListener {
            val currentScale = zoomImageView.getCurrentScale()
            zoomImageView.setZoom(currentScale * 1.5f)
            updateZoomLevel()
        }

        // Zoom Out button
        btnZoomOut.setOnClickListener {
            val currentScale = zoomImageView.getCurrentScale()
            zoomImageView.setZoom(currentScale / 1.5f)
            updateZoomLevel()
        }

        // Reset button
        btnReset.setOnClickListener {
            zoomImageView.resetZoom()
            updateZoomLevel()
            Toast.makeText(this, "Zoom reset", Toast.LENGTH_SHORT).show()
        }

        // Pick Image button
        btnPickImage.setOnClickListener {
            openImagePicker()
        }

        // Monitor zoom changes through touch
        zoomImageView.setOnTouchListener { _, _ ->
            // Update zoom level display after touch events
            zoomImageView.postDelayed({
                updateZoomLevel()
            }, 100)
            false
        }
    }

    private fun updateZoomLevel() {
        val scale = zoomImageView.getCurrentScale()
        tvZoomLevel.text = String.format("Zoom: %.2fx", scale)
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }

    private fun loadImageFromUri(uri: Uri) {
        try {
            zoomImageView.setImageURI(uri)
            zoomImageView.resetZoom()
            updateZoomLevel()
            Toast.makeText(this, "Image loaded successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}