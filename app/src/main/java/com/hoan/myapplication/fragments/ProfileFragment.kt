package com.hoan.myapplication.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hoan.myapplication.R
import com.hoan.myapplication.activities.EditProfileActivity
import com.hoan.myapplication.activities.OrderHistoryActivity
import com.hoan.myapplication.activities.SignInActivity
import com.hoan.myapplication.databinding.FragmentAccountBinding
import com.hoan.myapplication.models.User
import com.hoan.myapplication.utils.Extensions.toast

class ProfileFragment : Fragment(R.layout.fragment_account) {
    private lateinit var binding: FragmentAccountBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var userRef: DatabaseReference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentAccountBinding.bind(view)
        auth = FirebaseAuth.getInstance()
        userRef = FirebaseDatabase.getInstance().getReference("Users")

        binding.ivEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.btnOrderHistory.setOnClickListener {
            startActivity(Intent(requireContext(), OrderHistoryActivity::class.java))
        }

        loadProfile()

        binding.btnLogOut.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), SignInActivity::class.java)

            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onResume() {
        super.onResume()
        loadProfile()
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            binding.tvProfileName.text = ""
            binding.tvProfileEmail.text = ""
            binding.tvProfilePhone.text = ""
            binding.tvProfileAddress.text = ""
            return
        }

        userRef.child(uid)
                .addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val user = snapshot.getValue(User::class.java)
                                binding.tvProfileName.text = user?.name.orEmpty()
                                binding.tvProfileEmail.text = user?.email.orEmpty()
                                binding.tvProfilePhone.text = user?.phone.orEmpty()
                                binding.tvProfileAddress.text = user?.address.orEmpty()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                requireActivity().toast(error.message)
                            }
                        }
                )
    }

    companion object {
        fun newInstance() = ProfileFragment()
    }
}
