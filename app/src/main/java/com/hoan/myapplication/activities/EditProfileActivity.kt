package com.hoan.myapplication.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hoan.myapplication.databinding.ActivityEditProfileBinding
import com.hoan.myapplication.models.User
import com.hoan.myapplication.utils.Extensions.toast

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var userRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        userRef = FirebaseDatabase.getInstance().getReference("Users")

        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            finish()
            return
        }

        loadProfile(uid)

        binding.btnSaveProfile.setOnClickListener {
            val newName = binding.etEditName.text?.toString()?.trim().orEmpty()
            if (newName.isBlank()) {
                toast("Name is required")
                return@setOnClickListener
            }

            val email = binding.etEditEmail.text?.toString()?.trim().orEmpty()
            val phone = binding.etEditPhone.text?.toString()?.trim().orEmpty()
            val address = binding.etEditAddress.text?.toString()?.trim().orEmpty()

            val updatedUser =
                    User(id = uid, name = newName, email = email, phone = phone, address = address)

            userRef.child(uid)
                    .setValue(updatedUser)
                    .addOnSuccessListener {
                        toast("Profile updated")
                        finish()
                    }
                    .addOnFailureListener { e ->
                        toast(e.localizedMessage ?: "Failed to update profile")
                    }
        }
    }

    private fun loadProfile(uid: String) {
        userRef.child(uid)
                .addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val user = snapshot.getValue(User::class.java)
                                binding.etEditName.setText(user?.name.orEmpty())
                                binding.etEditEmail.setText(user?.email.orEmpty())
                                binding.etEditPhone.setText(user?.phone.orEmpty())
                                binding.etEditAddress.setText(user?.address.orEmpty())
                            }

                            override fun onCancelled(error: DatabaseError) {
                                toast(error.message)
                            }
                        }
                )
    }
}
