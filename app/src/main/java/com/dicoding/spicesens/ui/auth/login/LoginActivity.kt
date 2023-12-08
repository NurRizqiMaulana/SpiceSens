package com.dicoding.spicesens.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.spicesens.MainActivity
import com.dicoding.spicesens.databinding.ActivityLoginBinding
import com.dicoding.spicesens.ui.auth.register.RegisterActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        binding.btnTvRegister.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            var email = binding.editTextEmail.text.toString()
            var password = binding.editTextPassword.text.toString()

            signIn(email,password)
        }
    }

    private fun signIn(email: String, password: String) {
        // [START sign_in_with_email]
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Intent(this@LoginActivity, MainActivity::class.java).also { intent ->
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }

                    Toast.makeText(
                        this,
                        "Berhasil Masuk.",
                        Toast.LENGTH_SHORT,
                    ).show()
                } else {
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        // [END sign_in_with_email]
    }

//    override fun onStart() {
//        super.onStart()
//        if (auth.currentUser != null){
//            Intent(this@LoginActivity, MainActivity::class.java).also { intent ->
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                startActivity(intent)
//            }
//        }
//    }
}