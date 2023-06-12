package com.example.cameraapp

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.cameraapp.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    private val cameraPermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
            if (permissionGranted) {

                    openCameraActivity.launch(CameraActivity.getIntent(this, 1))

            } else {
                Snackbar.make(
                    binding.root,
                    "The camera permission is necessary",
                    Snackbar.LENGTH_INDEFINITE
                ).show()
            }
        }

    private val openCameraActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {

                val filePath = result.data?.getStringExtra("imgPath").toString()
                val requestCode: Int? = result.data?.getIntExtra(CameraActivity.REQUEST_ID, 0)

                val bitmap = BitmapFactory.decodeFile(filePath)

                if (requestCode == 1) {
                    binding.imageView.setImageBitmap(bitmap)

                }

            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.openCameraFabBtn.setOnClickListener {

            cameraPermissionResult.launch(android.Manifest.permission.CAMERA)

        }
    }


}