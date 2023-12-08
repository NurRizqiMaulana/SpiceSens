package com.dicoding.spicesens.ui.scan.camera

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Size
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dicoding.spicesens.databinding.ActivityCameraBinding
import com.dicoding.spicesens.helper.createFile
import com.dicoding.spicesens.helper.uriToFile
import com.dicoding.spicesens.ui.scan.ScanFragment
import java.io.File

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var camera: Camera

    private var imageCapture: ImageCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListener()
    }

    private fun setupListener() {
        binding.btnCameraCapture.setOnClickListener { takePhoto() }
        binding.btnCameraGallery.setOnClickListener { openGallery() }

    }

    override fun onResume() {
        super.onResume()
        setupView()
        checkPermission()
    }

    private fun setupView() {
        supportActionBar?.hide()
        binding.ivCameraFrame.layoutParams.apply {
            this.height = this.width
        }
    }

    private fun checkPermission() {
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                ScanFragment.REQUIRED_PERMISSIONS,
                ScanFragment.REQUEST_CODE_PERMISSIONS
            )
        } else {
            startCamera()
        }
    }
    private fun allPermissionsGranted() = ScanFragment.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ScanFragment.REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(this, "Camera access not granted", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                startCamera()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.pvCamera.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().setTargetResolution(Size(1440, 1920)).build()

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            } catch (exc: Exception) {
                Toast.makeText(this@CameraActivity, "Failed to open camera!", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))

        // Set focus while user click view finder
        focusSetting()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun focusSetting() {
        binding.pvCamera.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> return@setOnTouchListener true
                MotionEvent.ACTION_UP -> {
                    val factory = binding.pvCamera.meteringPointFactory
                    val point = factory.createPoint(motionEvent.x, motionEvent.y)
                    val action = FocusMeteringAction.Builder(point).build()
                    camera.cameraControl.startFocusAndMetering(action)

                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = createFile(application)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(this@CameraActivity, "Gagal mengambil gambar.", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    sendBackPicture(photoFile)
                }
            }
        )
    }

    private fun openGallery() {
        val intent = Intent().also {
            it.action = Intent.ACTION_GET_CONTENT
            it.type = "image/*"
        }

        val chooser = Intent.createChooser(intent, "Choose Plant Picture")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val image: Uri = it.data?.data as Uri
            val file = uriToFile(image, this@CameraActivity)

            sendBackPicture(file)
        }
    }

    private fun sendBackPicture(photoFile: File) {
        val uri = Uri.fromFile(photoFile)

        val intent = Intent()
        intent.putExtra("picture", "$uri")
        setResult(ScanFragment.CAMERA_X_RESULT, intent)
        finish()
    }


}