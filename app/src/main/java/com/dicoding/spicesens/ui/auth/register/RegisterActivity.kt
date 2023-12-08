package com.dicoding.spicesens.ui.auth.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.spicesens.data.model.DataUsers
import com.dicoding.spicesens.databinding.ActivityRegisterBinding
import com.dicoding.spicesens.ui.auth.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("users")



        binding.btnSignUp.setOnClickListener {
            var nama = binding.editTextName.text.toString()
            var email = binding.editTextEmail.text.toString()
            var password = binding.editTextPassword.text.toString()
            var noHp = binding.editTextNoHp.text.toString()



            createAccount(nama,email,password,noHp)
        }
    }

    private fun createAccount(nama: String, email: String, password: String, noHp: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registrasi berhasil, simpan informasi tambahan ke Firebase Realtime Database
                    val userId = auth.currentUser?.uid
                    userId?.let {
                        val pengguna = DataUsers(nama, email, noHp)
                        databaseReference.child(it).setValue(pengguna)
                    }
                    Intent(this@RegisterActivity, LoginActivity::class.java).also { intent ->
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }

                    Toast.makeText(
                        this,
                        "Authentication success.",
                        Toast.LENGTH_SHORT,
                    ).show()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }

    }

    companion object {
        private const val TAG = "EmailPassword"
    }
}