package com.hoan.myapplication.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.hoan.myapplication.databinding.ActivitySignupBinding
import com.hoan.myapplication.models.User

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnSignUp.setOnClickListener {
            if (binding.etEmailSignUp.text.isNotEmpty() &&
                            binding.etNameSignUp.text.isNotEmpty() &&
                            binding.etPasswordSignUp.text.isNotEmpty()
            ) {
                createUser(
                        binding.etEmailSignUp.text.toString(),
                        binding.etPasswordSignUp.text.toString()
                )
            }
        }
        binding.tvNavigateToSignIn.setOnClickListener {
            intent = Intent(this, SignInActivity::class.java)

            startActivity(intent)
            finish()
        }
    }

    private fun createUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser!!.uid
                val databaseReference = FirebaseDatabase.getInstance().getReference("Users")

                val user =
                        User(
                                id = userId,
                                name = binding.etNameSignUp.text.toString(),
                                email = email,
                                phone = "",
                                address = ""
                        )

                databaseReference
                        .child(userId)
                        .setValue(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "User created successfully", Toast.LENGTH_SHORT)
                                    .show()

                            intent = Intent(this, MainActivity::class.java)

                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
            } else {
                Toast.makeText(
                                this,
                                task.exception!!.localizedMessage ?: "Sign-up failed",
                                Toast.LENGTH_SHORT
                        )
                        .show()
            }
        }
    }
}
