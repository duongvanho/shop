package com.hoan.myapplication.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hoan.myapplication.R
import com.hoan.myapplication.activities.ProductActivity
import com.hoan.myapplication.adapters.LikeAdapter
import com.hoan.myapplication.adapters.LikedOnClickInterface
import com.hoan.myapplication.adapters.LikedProductOnClickInterface
import com.hoan.myapplication.databinding.FragmentLikepageBinding
import com.hoan.myapplication.models.Like
import com.hoan.myapplication.utils.Extensions.toast

class LikeFragment() :
        Fragment(R.layout.fragment_likepage), LikedProductOnClickInterface, LikedOnClickInterface {

    private lateinit var binding: FragmentLikepageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: LikeAdapter
    private lateinit var likedProductList: ArrayList<Like>

    private val likeDBRef = FirebaseDatabase.getInstance().getReference("LikedProducts")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentLikepageBinding.bind(view)
        auth = FirebaseAuth.getInstance()
        likedProductList = ArrayList()
        adapter = LikeAdapter(requireContext(), likedProductList, this, this)

        val productLayoutManager = GridLayoutManager(context, 2)
        binding.rvLikedProducts.layoutManager = productLayoutManager
        binding.rvLikedProducts.adapter = adapter

        displayLikedProducts()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            displayLikedProducts()
        }
    }

    private fun displayLikedProducts() {
        val uid = auth.currentUser?.uid ?: return
        likeDBRef
                .orderByChild("uid")
                .equalTo(uid)
                .addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                likedProductList.clear()
                                if (snapshot.exists()) {
                                    for (dataSnapshot in snapshot.children) {
                                        val likedProduct = dataSnapshot.getValue(Like::class.java)
                                        likedProduct?.let { likedProductList.add(it) }
                                    }
                                    adapter.notifyDataSetChanged()
                                } else {
                                    adapter.notifyDataSetChanged()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                requireActivity()
                                        .toast("Failed to load liked products: ${error.message}")
                            }
                        }
                )
    }

    override fun onClickProduct(item: Like) {
        val intent = Intent(requireContext(), ProductActivity::class.java)

        intent.putExtra("productId", item.pid)

        startActivity(intent)
    }

    override fun onClickLike(item: Like) {
        likeDBRef
                .orderByChild("pid")
                .equalTo(item.pid)
                .addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (dataSnapshot in snapshot.children) {
                                    val like = dataSnapshot.getValue(Like::class.java)
                                    if (like?.uid == auth.currentUser!!.uid) {
                                        dataSnapshot.ref.removeValue()
                                        likedProductList.remove(like)
                                        adapter.notifyDataSetChanged()
                                        requireActivity().toast("Removed From the Liked Items")
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                requireActivity().toast("Failed To Remove From Liked Items")
                            }
                        }
                )
    }

    companion object {
        fun newInstance() = LikeFragment()
    }
}
