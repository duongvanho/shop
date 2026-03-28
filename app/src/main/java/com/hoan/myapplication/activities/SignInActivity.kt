package com.hoan.myapplication.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.hoan.myapplication.databinding.ActivitySigninBinding

class SignInActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySigninBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySigninBinding.inflate(layoutInflater)

        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            intent = Intent(this, MainActivity::class.java)
        }
        binding.btnSignIn.setOnClickListener {

            if (
                binding.etEmailSignIn.text.isNotEmpty() &&
                binding.etPasswordSignIn.text.isNotEmpty()
            ) {

                signInUser(binding.etEmailSignIn.text.toString(),
                    binding.etPasswordSignIn.text.toString())

            } else {
                Toast.makeText(this, "Some Fields are Empty", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvNavigateToSignUp.setOnClickListener {
            intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }



    private fun signInUser(email: String, password: String) {

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    Toast.makeText(this, "Sign In Successful", Toast.LENGTH_SHORT).show()

                    intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, task.exception!!.localizedMessage!!, Toast.LENGTH_SHORT).show()
                }
            }

    }
}