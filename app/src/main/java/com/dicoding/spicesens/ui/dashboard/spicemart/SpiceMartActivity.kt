package com.dicoding.spicesens.ui.dashboard.spicemart

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import com.dicoding.spicesens.R
import com.dicoding.spicesens.data.model.DataProduk
import com.dicoding.spicesens.databinding.ActivitySpiceMartBinding
import com.dicoding.spicesens.ui.adapter.SpiceMartAdapter
import com.dicoding.spicesens.ui.dashboard.spicemart.addproduct.AddProductActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SpiceMartActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySpiceMartBinding

    var databaseReference: DatabaseReference? = null
    var eventListener: ValueEventListener? = null
    private lateinit var dataList: ArrayList<DataProduk>
    private lateinit var adapter: SpiceMartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpiceMartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val gridLayoutManager = GridLayoutManager(this@SpiceMartActivity, 2)
        binding.rvSpiceProduk.layoutManager = gridLayoutManager

        getData()

        binding.btnAddProduct.setOnClickListener {
            startActivity(Intent(this, AddProductActivity::class.java))

        }

        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchList(it) }
                return true
            }
        })
    }

    private fun getData() {
        val builder = AlertDialog.Builder(this@SpiceMartActivity)
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)
        val dialog = builder.create()
        dialog.show()
        dataList = ArrayList()
        adapter = SpiceMartAdapter(dataList)
        binding.rvSpiceProduk.adapter = adapter
        databaseReference = FirebaseDatabase.getInstance().getReference("SpiceProduk")
        dialog.show()
        eventListener = databaseReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dataList.clear()
                for (itemSnapshot in snapshot.children) {
                    val dataClass = itemSnapshot.getValue(DataProduk::class.java)
                    if (dataClass != null) {
                        dataList.add(dataClass)
                    }
                }
                adapter.notifyDataSetChanged()
                dialog.dismiss()

                // Tambahkan logika untuk menampilkan pesan "Data Kosong" jika dataList kosong
                if (dataList.isEmpty()) {
                    binding.tvEmptyData.visibility = View.VISIBLE
                } else {
                    binding.tvEmptyData.visibility = View.GONE
                }
            }
            override fun onCancelled(error: DatabaseError) {
                dialog.dismiss()
            }
        })
    }

    private fun searchList(text: String) {
        val searchList = ArrayList<DataProduk>()
        for (dataClass in dataList) {
            if (dataClass.dataNama?.toLowerCase()?.contains(text.toLowerCase()) == true) {
                searchList.add(dataClass)
            }
        }
        adapter.searchDataList(searchList)
    }
}