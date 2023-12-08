package com.dicoding.spicesens.ui.scan

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dicoding.spicesens.R
import com.dicoding.spicesens.data.model.PredictResponse
import com.dicoding.spicesens.databinding.FragmentScanBinding
import com.dicoding.spicesens.ml.FinalModel
import com.dicoding.spicesens.ui.scan.camera.CameraActivity
import com.dicoding.spicesens.ui.scan.camera.CropImageActivity
import com.dicoding.spicesens.ui.scan.detailscan.DetailScanActivity
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val viewModel by viewModels<ScanViewModel> {
        ViewModelFactory.getInstance(requireActivity())
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var currentImageUri: Uri? = null
    var startTime: Long = 0
    var endTime: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListener()
        setupObserver()
    }

    private fun setListener() {
        binding.btnAddRemovePhoto.setOnClickListener {
            if (viewModel.image.value == null) {
                val intent = Intent(requireContext(), CameraActivity::class.java)
                launcherIntentCameraX.launch(intent)
            } else {
                viewModel.setImage(null)
            }
        }

        viewModel.image.observe(requireActivity()) {
            if (it != null) {
                binding.btnPreviewSubmit.isEnabled = true
                binding.btnPreviewSubmit.setBackgroundResource(R.drawable.bg_sapphire)
                binding.btnAddRemovePhoto.text = resources.getString(R.string.remove_photo)
                binding.btnAddRemovePhoto.setBackgroundResource(R.drawable.bg_light_red)
            } else {
                binding.btnPreviewSubmit.isEnabled = false
                binding.btnPreviewSubmit.setBackgroundResource(R.drawable.bg_light_grey)
                binding.btnAddRemovePhoto.text = resources.getString(R.string.add_photo)
                binding.btnAddRemovePhoto.setBackgroundResource(R.drawable.bg_sapphire)
            }
        }

        binding.btnPreviewSubmit.setOnClickListener {
            startTime = System.currentTimeMillis()
            if (viewModel.image.value != null) {
                classifyImage(viewModel.image.value!!)
            }
        }
    }

    private fun setupObserver() {
        viewModel.image.observe(requireActivity()) {
            if (it != null) {
                binding.ivImagePreview.setImageBitmap(it)
                binding.tvImagePreview.visibility = View.INVISIBLE
            } else {
                binding.ivImagePreview.setImageResource(R.drawable.dotted)
                binding.tvImagePreview.visibility = View.VISIBLE
            }
        }
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val result = it.data?.getStringExtra("picture")
            if (result != null) {
                val image = Uri.parse(result)
                val intent = Intent(requireActivity(), CropImageActivity::class.java)
                intent.putExtra("image", image.toString())
                launcherCropImage.launch(intent)
            }
        }
    }

    private val launcherCropImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CROP_RESULT) {
            val result = it.data?.getStringExtra("crop")
            if (result != null) {
                val uri = Uri.parse(result)
                val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                viewModel.setImage(bitmap)
            }
        }
    }

    private fun classifyImage(image: Bitmap) {
        // Preprocess the image (rescaling)
        val byteBuffer: ByteBuffer = ByteBuffer.allocateDirect(4 * 384 * 384 * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(384 * 384)
        image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)
        var pixel = 0

        for (i in 0 until 384) {
            for (j in 0 until 384) {
                val `val` = intValues[pixel++] // RGB
                byteBuffer.putFloat((`val` shr 32 and 0xFF) * (1f / 255))
                byteBuffer.putFloat((`val` shr 16 and 0xFF) * (1f / 255))
                byteBuffer.putFloat((`val` and 0xFF) * (1f / 255))
            }
        }
        // Create spice model, prepare input, do classification and get the output
        val model = FinalModel.newInstance(requireContext())
        val input = TensorBuffer.createFixedSize(intArrayOf(1, 384, 384, 3), DataType.FLOAT32)
        input.loadBuffer(byteBuffer)

        val process = model.process(input)
        val output = process.outputFeature0AsTensorBuffer.floatArray
        model.close()

        // Get The Result
        var bestIndex = 0
        var bestConfidenceScore = 0.00f

        output.forEachIndexed { index, fl ->
            if (fl >= output[bestIndex]) {
                bestIndex = index
                bestConfidenceScore = fl
            }
        }
        val confidenceScore = bestConfidenceScore * 100
        if (confidenceScore >= 70.0F) {
            endTime = System.currentTimeMillis()
            Log.d("test_duration", (endTime - startTime).toString() + "ms")

            val predictResult = PredictResponse(bestIndex+1, String.format("%.2f", confidenceScore), viewModel.image.value!!)
            val intent = Intent(requireActivity(), DetailScanActivity::class.java)
            intent.putExtra(DetailScanActivity.PREDICT_RESULT, predictResult)
            startActivity(intent)
        } else {
            Log.d("prediction", "Score hanya " + String.format("%.2f", confidenceScore) + "%")
            Toast.makeText(requireContext(), "Maaf, bukan spice!\n(Score: " + String.format("%.2f", confidenceScore) + "%)", Toast.LENGTH_LONG).show()
        }
    }

//    private fun help(){
//        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
//        val bottomSheetView = LayoutInflater.from(requireContext()).inflate(R.layout.item_help_bottomsheet,
//            requireActivity().findViewById<LinearLayout>(R.id.bottomSheet)
//        )
//        bottomSheetView.findViewById<View>(R.id.btn_close).setOnClickListener {
//            bottomSheetDialog.dismiss()
//        }
//        bottomSheetDialog.setContentView(bottomSheetView)
//        bottomSheetDialog.show()
//
//    }

//    private fun showLoading(isLoading: Boolean) {
//        isClassifying = isLoading
//        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    companion object {
        const val CAMERA_X_RESULT = 200
        const val CROP_RESULT = 101
        val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        const val REQUEST_CODE_PERMISSIONS = 10
    }
}