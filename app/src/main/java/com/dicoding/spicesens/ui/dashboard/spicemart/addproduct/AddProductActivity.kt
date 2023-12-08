package com.dicoding.spicesens.ui.dashboard.spicemart.addproduct

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dicoding.spicesens.R
import com.dicoding.spicesens.data.model.DataProduk
import com.dicoding.spicesens.databinding.ActivityAddProductBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddProductBinding
    private lateinit var databaseReference: DatabaseReference


    var imageURL: String? = null
    var uri: Uri? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mendapatkan referensi ke "SpiceProduk"
        databaseReference = FirebaseDatabase.getInstance().getReference("SpiceProduk")


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getMyLastLocation()

        val activityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()){ result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                uri = data!!.data
                binding.ivImagePreview.setImageURI(uri)
            } else {
                Toast.makeText(this@AddProductActivity, "No Image Selected", Toast.LENGTH_SHORT).show()
            }
        }

        binding.ivImagePreview.setOnClickListener {
            val photoPicker = Intent(Intent.ACTION_PICK)
            photoPicker.type = "image/*"
            activityResultLauncher.launch(photoPicker)
        }

        binding.btnSaveProduct.setOnClickListener {
            saveData()
        }

    }

    private fun saveData() {
        val storageReference = FirebaseStorage.getInstance().reference.child("Spice Images")
            .child(uri!!.lastPathSegment!!)

        val builder = AlertDialog.Builder(this@AddProductActivity)
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)
        val dialog = builder.create()
        dialog.show()

        storageReference.putFile(uri!!).addOnSuccessListener { taskSnapshot ->
            val uriTask = taskSnapshot.storage.downloadUrl
            while (!uriTask.isComplete);
            val urlImage = uriTask.result
            imageURL = urlImage.toString()
            getMyLastLocation()
            dialog.dismiss()
        }.addOnFailureListener {
            dialog.dismiss()
        }
    }

    private fun getMyLastLocation() {
        if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ){
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    if (location != null){
                        if (binding.switchLoc.isChecked){
                            uploadData(it)
                        }else{

                        }
                    }

                } ?: run {
                    // Handle jika lokasi tidak tersedia
                    Toast.makeText(
                        this@AddProductActivity,
                        "Lokasi tidak tersedia",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this@AddProductActivity,
                        e.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    // Precise location access granted.
                    getMyLastLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    // Only approximate location access granted.
                    getMyLastLocation()
                }
                else -> {
                    // No location access granted.
                }
            }
        }

    private fun uploadData(location: Location) {
        val newProductRef = databaseReference.push()
        val dataId = newProductRef.key
        val nama = binding.edNama.text.toString()
        val harga = binding.edHarga.text.toString()
        val wa = binding.edWa.text.toString()

        // Validasi nomor WhatsApp
        if (!wa.startsWith("+62")) {
            // Nomor WhatsApp tidak valid
            Toast.makeText(this@AddProductActivity, "Nomor WhatsApp harus diawali dengan +62", Toast.LENGTH_SHORT).show()
            return
        }
        val des = binding.edDes.text.toString()
        val dataClass = DataProduk(nama, harga, wa,des, imageURL!!,location.latitude.toDouble(),location.longitude.toDouble(),dataId)
        // Menggunakan format tanggal yang sesuai untuk Firebase Database
        val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        try {
            newProductRef
                .setValue(dataClass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@AddProductActivity, "Tersimpan", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@AddProductActivity, e.message.toString(), Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this@AddProductActivity, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
        }
    }
}