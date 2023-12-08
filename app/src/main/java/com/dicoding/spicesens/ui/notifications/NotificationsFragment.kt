package com.dicoding.spicesens.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dicoding.spicesens.data.model.DataUsers
import com.dicoding.spicesens.databinding.FragmentNotificationsBinding
import com.dicoding.spicesens.ui.auth.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
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

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("users")

        // Mendapatkan ID pengguna yang sedang login
        val userId = auth.currentUser?.uid

        userId?.let {
            // Mendapatkan data pengguna dari Firebase Realtime Database
            databaseReference.child(it).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pengguna = snapshot.getValue(DataUsers::class.java)

                    // Menampilkan informasi pengguna di UI
                    pengguna?.let {
                        binding.tvNameProfile.text = it.nama
                        binding.tvEmailProfile.text = it.email
                        binding.tvNoProfile.text = it.noHp
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle kesalahan pengambilan data
                }
            })
        }
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            Intent(requireContext(), LoginActivity::class.java).also{ intent ->
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }
}