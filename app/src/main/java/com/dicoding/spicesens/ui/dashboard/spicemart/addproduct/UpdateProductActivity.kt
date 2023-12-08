package com.dicoding.spicesens.ui.dashboard.spicemart.addproduct

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dicoding.spicesens.data.model.DataProduk
import com.dicoding.spicesens.databinding.ActivityUpdateProductBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UpdateProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateProductBinding
    private lateinit var spiceProduk: DataProduk
    private lateinit var uri: Uri
    private lateinit var oldImageURL: String
    private lateinit var storageReference: StorageReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var databaseReference: DatabaseReference

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            uri = data?.data!!
            binding.ivImagePreview.setImageURI(uri)
        } else {
            Toast.makeText(this@UpdateProductActivity, "No Image Selected", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUpdateProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Mendapatkan referensi ke "SpiceProduk"
        databaseReference = FirebaseDatabase.getInstance().getReference("SpiceProduk")


        val bundle = intent.extras
        if (bundle != null) {
            spiceProduk = DataProduk(
                key = bundle.getString("Key"),
                dataNama = bundle.getString("Title"),
                dataDes = bundle.getString("Description"),
                dataHarga = bundle.getString("Price"),
                dataWa = bundle.getString("Wa"),
                dataImage = bundle.getString("Image"),
                dataLat = bundle.getDouble("Lat"),
                dataLon = bundle.getDouble("Lon")
            )

            oldImageURL = spiceProduk.dataImage ?: ""


            // Menampilkan data produk yang ada
            binding.edNama.setText(spiceProduk.dataNama)
            binding.edHarga.setText(spiceProduk.dataHarga)
            binding.edWa.setText(spiceProduk.dataWa)
            binding.edDes.setText(spiceProduk.dataDes)

            // Menampilkan gambar produk yang ada
            Glide.with(this).load(spiceProduk.dataImage).into(binding.ivImagePreview)

            // Melakukan update saat tombol disimpan
            binding.btnUpdateProduct.setOnClickListener {
                updateData()
            }

            // Menambahkan fungsi pemilihan gambar
            binding.ivImagePreview.setOnClickListener {
                val photoPicker = Intent(Intent.ACTION_PICK)
                photoPicker.type = "image/*"
                activityResultLauncher.launch(photoPicker)
            }
        }
    }

    private fun updateData() {
        val nama = binding.edNama.text.toString()
        val harga = binding.edHarga.text.toString()
        val wa = binding.edWa.text.toString()
        val des = binding.edDes.text.toString()

        spiceProduk.dataNama = nama
        spiceProduk.dataHarga = harga
        spiceProduk.dataWa = wa
        spiceProduk.dataDes = des

        // Mengecek apakah ada perubahan gambar
        if (uri != null) {
            // Jika ada, upload gambar baru
            uploadNewImage()
        } else {
            // Jika tidak, langsung update data ke Firebase Database
            updateDataToDatabase()
        }
    }

    private fun uploadNewImage() {
        // Mengecek apakah oldImageURL tidak null atau kosong
        if (oldImageURL.isNullOrEmpty()) {
            Toast.makeText(this@UpdateProductActivity, "URL gambar produk tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        // Menghapus gambar lama dari Firebase Storage
        val oldImageReference = FirebaseStorage.getInstance().getReferenceFromUrl(oldImageURL)
        oldImageReference.delete().addOnSuccessListener {
            // Mengupload gambar baru
            val storageReference = FirebaseStorage.getInstance().reference.child("Spice Images")
                .child(uri!!.lastPathSegment!!)
            storageReference.putFile(uri!!).addOnSuccessListener { taskSnapshot ->
                // Mendapatkan URL gambar yang baru diupload
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isComplete);
                val urlImage = uriTask.result
                spiceProduk.dataImage = urlImage.toString()

                // Update data ke Firebase Database
                updateDataToDatabase()
            }.addOnFailureListener {
                Toast.makeText(
                    this@UpdateProductActivity,
                    "Gagal mengupload gambar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.addOnFailureListener {
            Toast.makeText(
                this@UpdateProductActivity,
                "Gagal menghapus gambar lama",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateDataToDatabase() {
        // Melakukan update data ke Firebase Database
        databaseReference.child(spiceProduk.key!!)
            .setValue(spiceProduk)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this@UpdateProductActivity,
                        "Data berhasil diupdate",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@UpdateProductActivity,
                        "Gagal mengupdate data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@UpdateProductActivity, e.message.toString(), Toast.LENGTH_SHORT)
                    .show()
            }
    }
}