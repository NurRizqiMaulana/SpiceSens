package com.dicoding.spicesens.ui.adapter

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.spicesens.data.model.DataProduk
import com.dicoding.spicesens.databinding.ItemSpicemartRowBinding
import com.dicoding.spicesens.ui.dashboard.spicemart.addproduct.DetailProductActivity
import java.io.IOException
import java.util.Locale

class SpiceMartAdapter(private var dataList: List<DataProduk>) : RecyclerView.Adapter<SpiceMartAdapter.MyViewHolder>() {

    fun searchDataList(searchList: List<DataProduk>) {
        this.dataList = searchList
        notifyDataSetChanged()
    }

    class MyViewHolder(binding: ItemSpicemartRowBinding) : RecyclerView.ViewHolder(binding.root) {


        var recImage = binding.imageProduk
        var recTitle = binding.textNamaProduk
        var recHarga = binding.textHargaProduk
        var recJalan = binding.textLokasiProduk

        fun bind(data : DataProduk) {
            val geocoder = Geocoder(itemView.context, Locale.getDefault())

            fun getAddressDetails(lat: Double, lon: Double): String {
                try {
                    val addresses: MutableList<Address>? = geocoder.getFromLocation(lat, lon, 1)

                    if (addresses?.isNotEmpty() == true) {
                        val address: Address = addresses[0]

                        // Dapatkan informasi yang lebih rinci seperti nama jalan, kecamatan, dan kota
                        val thoroughfare = address.thoroughfare ?: ""
                        val subLocality = address.subLocality ?: ""
                        val locality = address.locality ?: ""

                        // Gabungkan informasi menjadi satu string
                        return "$thoroughfare $subLocality, $locality"
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                return "Alamat tidak ditemukan"
            }

            val alamatJalan = getAddressDetails(data.dataLat!!, data.dataLon!!)

            Glide.with(itemView).load(data.dataImage)
                .into(recImage)
            recTitle.text = data.dataNama
            recHarga.text = data.dataHarga
            recJalan.text = alamatJalan
//            recDesc.text = data.dataDesc
//            recPriority.text = data.dataPriority
//            recWa.text = data.dataWa

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailProductActivity::class.java)
                intent.putExtra("Image", data.dataImage)
                intent.putExtra("Description",data.dataDes)
                intent.putExtra("Title", data.dataNama)
                intent.putExtra("Price", data.dataHarga)
                intent.putExtra("Wa", data.dataWa)
                intent.putExtra("Key", data.key)
                intent.putExtra("Lat", data.dataLat)
                intent.putExtra("Lon", data.dataLon)
                itemView.context.startActivity(intent)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemSpicemartRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
}