package com.dicoding.spicesens.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.spicesens.R
import com.dicoding.spicesens.data.model.DataProduk
import com.dicoding.spicesens.databinding.FragmentHomeBinding
import com.dicoding.spicesens.ui.adapter.SpiceMartAdapter
import com.dicoding.spicesens.ui.dashboard.spiceloc.MapsActivity
import com.dicoding.spicesens.ui.dashboard.spicemart.SpiceMartActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var dataList: ArrayList<DataProduk>
    private lateinit var adapter: SpiceMartAdapter
    var databaseReference: DatabaseReference? = null
    var eventListener: ValueEventListener? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        //kondisi user sedang login atau tidak
        if (user != null){
            binding.tvUserName.setText(user.email)
        }

        val linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvSpice.layoutManager = linearLayoutManager



        getData()
        setupListener()


    }

    private fun setupListener(){

        binding.btnTvShowMore.setOnClickListener {
            startActivity(Intent(requireContext(),SpiceMartActivity::class.java))
        }
        binding.btnImgSpiceMart.setOnClickListener {
            startActivity(Intent(requireContext(),SpiceMartActivity::class.java))
        }
        binding.btnImgSpiceLoc.setOnClickListener {
            startActivity(Intent(requireContext(),MapsActivity::class.java))
        }



    }

    private fun getData() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)
        val dialog = builder.create()
        dialog.show()
        dataList = ArrayList()
        adapter = SpiceMartAdapter(dataList)
        binding.rvSpice.adapter = adapter
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

//                // Tambahkan logika untuk menampilkan pesan "Data Kosong" jika dataList kosong
//                if (dataList.isEmpty()) {
//                    binding.tvEmptyData.visibility = View.VISIBLE
//                } else {
//                    binding.tvEmptyData.visibility = View.GONE
//                }
            }
            override fun onCancelled(error: DatabaseError) {
                dialog.dismiss()
            }
        })
    }
}