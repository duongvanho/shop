package com.hoan.myapplication.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hoan.myapplication.R
import com.hoan.myapplication.activities.ProductActivity
import com.hoan.myapplication.adapters.LikeOnClickInterface
import com.hoan.myapplication.adapters.ProductDisplayAdapter
import com.hoan.myapplication.adapters.ProductOnClickInterface
import com.hoan.myapplication.databinding.FragmentSearchBinding
import com.hoan.myapplication.models.Like
import com.hoan.myapplication.models.Product
import com.hoan.myapplication.utils.Extensions.toast

class SearchFragment :
        Fragment(R.layout.fragment_search), ProductOnClickInterface, LikeOnClickInterface {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private val allProducts = ArrayList<Product>()
    private val filteredProducts = ArrayList<Product>()
    private lateinit var adapter: ProductDisplayAdapter

    private var likedProducts = mutableSetOf<String>()
    private val likeDBRef = FirebaseDatabase.getInstance().getReference("LikedProducts")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentSearchBinding.bind(view)
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("products")

        adapter = ProductDisplayAdapter(requireContext(), filteredProducts, this, this)
        binding.rvSearchResults.layoutManager = GridLayoutManager(context, 2)
        binding.rvSearchResults.adapter = adapter

        loadLikedProducts()
        loadProducts()
        setupSearch()
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                    ) = Unit
                    override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                    ) = Unit
                    override fun afterTextChanged(s: Editable?) {
                        filterProducts(s?.toString().orEmpty())
                    }
                }
        )
    }

    private fun loadProducts() {
        databaseReference.addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        allProducts.clear()
                        if (snapshot.exists()) {
                            for (dataSnapshot in snapshot.children) {
                                val product = dataSnapshot.getValue(Product::class.java)
                                if (product != null) allProducts.add(product)
                            }
                        }

                        filterProducts(binding.etSearch.text?.toString().orEmpty())
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                    }
                }
        )
    }

    private fun filterProducts(queryRaw: String) {
        val query = queryRaw.trim().lowercase()
        filteredProducts.clear()

        if (query.isEmpty()) {
            filteredProducts.addAll(allProducts)
        } else {
            for (product in allProducts) {
                val haystack =
                        listOfNotNull(product.brand, product.name, product.description)
                                .joinToString(" ")
                                .lowercase()
                if (haystack.contains(query)) {
                    filteredProducts.add(product)
                }
            }
        }

        adapter.notifyDataSetChanged()
    }

    private fun loadLikedProducts() {
        auth.currentUser?.let { user ->
            likeDBRef
                    .orderByChild("uid")
                    .equalTo(user.uid)
                    .addListenerForSingleValueEvent(
                            object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    likedProducts.clear()
                                    if (snapshot.exists()) {
                                        for (dataSnapshot in snapshot.children) {
                                            val likedItem = dataSnapshot.getValue(Like::class.java)
                                            likedItem?.pid?.let { likedProducts.add(it) }
                                        }
                                    }
                                    adapter.updateLikedProducts(likedProducts)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    requireActivity().toast(error.message)
                                }
                            }
                    )
        }
    }

    companion object {
        fun newInstance() = SearchFragment()
    }

    override fun onClickProduct(item: Product) {
        if (item.id.isNullOrBlank()) return
        val intent = Intent(requireContext(), ProductActivity::class.java)
        intent.putExtra("productId", item.id)
        startActivity(intent)
    }

    override fun onClickLike(item: Product) {
        val currentUser = auth.currentUser ?: return
        val pid = item.id ?: return

        likeDBRef
                .orderByChild("pid")
                .equalTo(pid)
                .addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                var alreadyLiked = false
                                for (dataSnapshot in snapshot.children) {
                                    val likedItem = dataSnapshot.getValue(Like::class.java)
                                    if (likedItem?.uid == currentUser.uid) {
                                        alreadyLiked = true
                                        break
                                    }
                                }

                                if (alreadyLiked) {
                                    requireActivity().toast("You have already liked this item")
                                    return
                                }

                                likeDBRef
                                        .push()
                                        .setValue(
                                                Like(
                                                        pid,
                                                        currentUser.uid,
                                                        item.brand,
                                                        item.description,
                                                        item.imageUrl,
                                                        item.name,
                                                        item.price
                                                )
                                        )
                                        .addOnSuccessListener {
                                            likedProducts.add(pid)
                                            adapter.updateLikedProducts(likedProducts)
                                            requireActivity().toast("Added to Liked Items")
                                        }
                                        .addOnFailureListener {
                                            requireActivity().toast("Failed to Add to Liked")
                                        }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(
                                                requireContext(),
                                                "Error: ${error.message}",
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                            }
                        }
                )
    }

    override fun onUnlike(item: Product) {
        val currentUser = auth.currentUser ?: return
        val pid = item.id ?: return

        likeDBRef
                .orderByChild("pid")
                .equalTo(pid)
                .addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (data in snapshot.children) {
                                    val like = data.getValue(Like::class.java)
                                    if (like?.uid == currentUser.uid) {
                                        data.ref
                                                .removeValue()
                                                .addOnSuccessListener {
                                                    likedProducts.remove(pid)
                                                    adapter.updateLikedProducts(likedProducts)
                                                    requireActivity()
                                                            .toast("Removed from Liked Items")
                                                }
                                                .addOnFailureListener {
                                                    requireActivity()
                                                            .toast(
                                                                    "Failed to remove from Liked Items"
                                                            )
                                                }
                                        return
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(
                                                requireContext(),
                                                "Error: ${error.message}",
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                            }
                        }
                )
    }
}
