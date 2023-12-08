package com.dicoding.spicesens.ui.dashboard.spicemart.addproduct

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dicoding.spicesens.data.model.DataProduk
import com.dicoding.spicesens.databinding.ActivityDetailProductBinding
import com.dicoding.spicesens.ui.dashboard.spicemart.SpiceMartActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException
import java.util.Locale

class DetailProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailProductBinding
    private lateinit var spiceProduk: DataProduk

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val geocoder = Geocoder(this, Locale.getDefault())

        fun getAddressDetails(lat: Double, lon: Double): String {
            try {
                val addresses: MutableList<Address>? = geocoder.getFromLocation(lat, lon, 1)

                if (addresses?.isNotEmpty() == true) {
                    val address: Address = addresses[0]

                    // Dapatkan informasi yang lebih rinci seperti nama jalan, kecamatan, dan kota
                    val thoroughfare = address.thoroughfare ?: ""
                    val subLocality = address.subLocality ?: "Unknown SubLocality"
                    val locality = address.locality ?: "Unknown Locality"

                    // Gabungkan informasi menjadi satu string
                    return "$thoroughfare $subLocality, $locality"
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return "Alamat tidak ditemukan"
        }

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

            // Menampilkan detail produk
            binding.titleTxt.text = spiceProduk.dataNama
            binding.descriptionTxt.text = spiceProduk.dataDes
            binding.priceTxt.text = spiceProduk.dataHarga
            binding.tvWa.text = spiceProduk.dataWa
            Glide.with(this).load(spiceProduk.dataImage).into(binding.itemPic)

            // Menampilkan alamat
            val alamatJalan = getAddressDetails(spiceProduk.dataLat!!, spiceProduk.dataLon!!)
            binding.lokasiTxt.text = alamatJalan

            binding.btnOrder.setOnClickListener {
                val url = "https://wa.me/${spiceProduk.dataWa}?text=Hi, Is any one Available?"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setData(Uri.parse(url))
                startActivity(intent)
            }
            // Tombol Delete
            binding.deleteButton.setOnClickListener {
                showDeleteConfirmationDialog(spiceProduk.key)
            }

            binding.editButton.setOnClickListener {
                sendResultData()

            }
        }
    }

    private fun sendResultData(){
        val intent = Intent(this@DetailProductActivity, UpdateProductActivity::class.java)
            .putExtra("Title", spiceProduk.dataNama)
            .putExtra("Description", spiceProduk.dataDes)
            .putExtra("Price", spiceProduk.dataHarga)
            .putExtra("Wa", spiceProduk.dataWa)
            .putExtra("Image", spiceProduk.dataImage)
            .putExtra("Key", spiceProduk.key)
            .putExtra("Lat", spiceProduk.dataLat)
            .putExtra("Lon",spiceProduk.dataLon)
        startActivity(intent)
    }

    private fun deleteProduct(dataId: String?) {
        if (dataId.isNullOrEmpty()) {
            Toast.makeText(this, "ID produk tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        val reference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("SpiceProduk").child(dataId)

        // Hapus gambar dari Firebase Storage
        val storage = FirebaseStorage.getInstance()
        val storageReference = storage.getReferenceFromUrl(spiceProduk.dataImage.orEmpty())

        storageReference.delete().addOnSuccessListener {
            // Hapus data dari Firebase Database
            reference.removeValue().addOnSuccessListener {
                Toast.makeText(
                    this@DetailProductActivity,
                    "Produk berhasil dihapus",
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(applicationContext, SpiceMartActivity::class.java))
                finish()
            }.addOnFailureListener {
                Toast.makeText(
                    this@DetailProductActivity,
                    "Gagal menghapus produk dari Database",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.addOnFailureListener {
            Toast.makeText(
                this@DetailProductActivity,
                "Gagal menghapus gambar",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showDeleteConfirmationDialog(key: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Penghapusan")
        builder.setMessage("Apakah Anda yakin ingin menghapus produk ini?")
        builder.setPositiveButton("Ya") { dialog, which ->
            // Panggil fungsi deleteProduct dengan parameter key
            deleteProduct(key)
        }
        builder.setNegativeButton("Tidak") { dialog, which ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }
}