package com.example.cameraapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.cameraapp.databinding.ActivityCameraBinding
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


private const val TAG = "CameraActivity"

class CameraActivity : AppCompatActivity() {


    private lateinit var binding: ActivityCameraBinding

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var imageCapture: ImageCapture? = null
    private lateinit var imgCaptureExecutor: ExecutorService

    private lateinit var cameraSelector: CameraSelector
    private val IMAGE_FILENAME_FORMAT = "yyyy-MM-dd'T'HHmmssSSS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)


        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        imgCaptureExecutor = Executors.newSingleThreadExecutor()
        startCamera()

        binding.imgCaptureBtn.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            takePhoto()

            binding.imgCaptureBtn.visibility = View.GONE
            binding.transparentWhiteBg.visibility = View.VISIBLE
        }

        binding.switchCameraBtn.setOnClickListener {
            //change the cameraSelector
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            // restart the camera
            startCamera()
        }
    }

    private fun takePhoto() {
        imageCapture?.let {
            val fileName = getFileNameTimeStamp() + ".jpeg"
            val file = File(externalMediaDirs[0], fileName)
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()
            it.takePicture(
                outputFileOptions,
                imgCaptureExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Log.e(TAG, "onImageSaved: ${file.absolutePath}")
                        //    val compressedImagePath = ImageCompressor.compressImage(file.absolutePath)

                        val intent = Intent().apply {
                            putExtra("imgPath", file.absolutePath)
                            putExtra(REQUEST_ID, intent.getIntExtra(REQUEST_ID, 0))
                        }

                        runOnUiThread {
                            Toast.makeText(this@CameraActivity, "Photo Clicked", Toast.LENGTH_SHORT)
                                .show()
                        }

                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }

                    override fun onError(exception: ImageCaptureException) {

                        runOnUiThread {
                            Toast.makeText(
                                this@CameraActivity,
                                "Error occurred!",
                                Toast.LENGTH_SHORT
                            ).show()

                            binding.imgCaptureBtn.visibility = View.VISIBLE

                        }
                    }

                })
        }
    }

    private fun getFileNameTimeStamp(): String {
        val format = SimpleDateFormat(
            IMAGE_FILENAME_FORMAT,
            Locale.ENGLISH
        )
        return format.format(Calendar.getInstance().time)
    }

    private fun startCamera() {
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.preview.surfaceProvider)
        }
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.d(TAG, "Use case binding failed")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    companion object {
        private const val TAG = "CameraActivity"
        const val REQUEST_ID = "REQUEST_ID"

        fun getIntent(context: Context, postId: Int): Intent {
            return Intent(context, CameraActivity::class.java).apply {
                putExtra(REQUEST_ID, postId)
            }
        }
    }
}