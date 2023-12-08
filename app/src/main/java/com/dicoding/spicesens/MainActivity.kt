package com.dicoding.spicesens

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.dicoding.spicesens.databinding.ActivityMainBinding
import com.dicoding.spicesens.ui.auth.login.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Inisialisasi Firebase Authentication
        mAuth = FirebaseAuth.getInstance()

        // Periksa apakah pengguna sudah login
        checkUserLoginStatus()
    }

    private fun checkUserLoginStatus() {
        val currentUser: FirebaseUser? = mAuth.currentUser
        if (currentUser == null) {
            // Jika belum login, arahkan ke halaman login
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Hentikan aktivitas ini sehingga pengguna tidak dapat kembali ke MainActivity tanpa login
        } else {
            // Pengguna sudah login, lakukan tindakan yang diperlukan
            // Misalnya, tampilkan pesan selamat datang
        }
    }
}