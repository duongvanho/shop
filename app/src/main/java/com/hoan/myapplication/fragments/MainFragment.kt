package com.hoan.myapplication.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.hoan.myapplication.R
import com.hoan.myapplication.activities.ProductActivity
import com.hoan.myapplication.adapters.CategoryOnClickInterface
import com.hoan.myapplication.adapters.LikeOnClickInterface
import com.hoan.myapplication.adapters.MainCategoryAdapter
import com.hoan.myapplication.adapters.ProductDisplayAdapter
import com.hoan.myapplication.adapters.ProductOnClickInterface
import com.hoan.myapplication.databinding.FragmentMainpageBinding
import com.hoan.myapplication.models.Like
import com.hoan.myapplication.models.Product
import com.hoan.myapplication.utils.Extensions.toast


class MainFragment : Fragment(R.layout.fragment_mainpage),
    CategoryOnClickInterface,
    ProductOnClickInterface, LikeOnClickInterface {


    private lateinit var binding: FragmentMainpageBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var productList: ArrayList<Product>
    private lateinit var categoryList: ArrayList<String>
    private lateinit var productsAdapter: ProductDisplayAdapter
    private lateinit var categoryAdapter: MainCategoryAdapter
    private var likedProducts = mutableSetOf<String>()
    private lateinit var auth: FirebaseAuth
    private val likeDBRef = FirebaseDatabase.getInstance().getReference("LikedProducts")



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentMainpageBinding.bind(view)
        categoryList = ArrayList()
        productList = ArrayList()
        databaseReference = FirebaseDatabase.getInstance().getReference("products")
        auth = FirebaseAuth.getInstance()

        categoryList.add("Trending")
        binding.rvMainCategories.setHasFixedSize(true)
        val categoryLayoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.rvMainCategories.layoutManager = categoryLayoutManager
        categoryAdapter = MainCategoryAdapter(categoryList, this)
        binding.rvMainCategories.adapter = categoryAdapter
        setCategoryList()


        val productLayoutManager = GridLayoutManager(context, 2)
        productsAdapter = ProductDisplayAdapter(requireContext(), productList, this,this)
        binding.rvMainProductsList.layoutManager = productLayoutManager
        binding.rvMainProductsList.adapter = productsAdapter
        setProductsData()
        loadLikedProducts()



    }

    private fun setCategoryList() {
        val uniqueCategories = LinkedHashSet<String>()
        uniqueCategories.add("Trending")

        val valueEvent = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val product = dataSnapshot.getValue(Product::class.java)
                        product?.brand?.let { uniqueCategories.add(it) }
                    }
                }

                categoryList.clear()
                categoryList.addAll(uniqueCategories)
                categoryAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "$error", Toast.LENGTH_SHORT).show()
            }
        }

        databaseReference.addValueEventListener(valueEvent)
    }




    private fun setProductsData() {

        val valueEvent = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                productList.clear()

                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val products = dataSnapshot.getValue(Product::class.java)
                        productList.add(products!!)
                    }
                    productsAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "$error", Toast.LENGTH_SHORT).show()
            }

        }

        databaseReference.addValueEventListener(valueEvent)
    }




    private fun loadLikedProducts() {
        auth.currentUser?.let { user ->
            likeDBRef.orderByChild("uid").equalTo(user.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    likedProducts.clear()
                    if (snapshot.exists()) {
                        for (dataSnapshot in snapshot.children) {
                            val likedItem = dataSnapshot.getValue(Like::class.java)
                            likedItem?.pid?.let { likedProducts.add(it) }
                        }
                    }
                    productsAdapter.updateLikedProducts(likedProducts)
                }

                override fun onCancelled(error: DatabaseError) {
                    requireActivity().toast(error.message)
                }
            })
        }
    }



    override fun onClickCategory(button: Button) {
        binding.tvMainCategories.text = button.text

        val valueEvent = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                productList.clear()

                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val products = dataSnapshot.getValue(Product::class.java)

                        if (products!!.brand == button.text) {
                            productList.add(products)
                        }

                        if (button.text == "Trending") {
                            productList.add(products)
                        }

                    }

                    productsAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "$error", Toast.LENGTH_SHORT).show()
            }

        }

        databaseReference.addValueEventListener(valueEvent)
    }


    override fun onClickProduct(item: Product) {
        val intent = Intent(requireContext(), ProductActivity::class.java)

        intent.putExtra("productId", item.id)

        startActivity(intent)
    }

    override fun onClickLike(item: Product) {

            likeDBRef.orderByChild("pid").equalTo(item.id)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var alreadyLiked = false
                        for (dataSnapshot in snapshot.children) {
                            val likedItem = dataSnapshot.getValue(Like::class.java)
                            if (likedItem?.uid == auth.currentUser!!.uid) {
                                alreadyLiked = true
                                break
                            }
                        }

                        if (!alreadyLiked) {
                            likeDBRef.push().setValue(
                                Like(
                                    item.id,
                                    auth.currentUser!!.uid,
                                    item.brand,
                                    item.description,
                                    item.imageUrl,
                                    item.name,
                                    item.price
                                )
                            ).addOnSuccessListener {
                                requireActivity().toast("Added to Liked Items")
                            }.addOnFailureListener {
                                requireActivity().toast("Failed to Add to Liked")
                            }
                        } else {
                            requireActivity().toast("You have already liked this item")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

    override fun onUnlike(item: Product) {
        likeDBRef.orderByChild("pid").equalTo(item.id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        val like = data.getValue(Like::class.java)
                        if (like?.uid == auth.currentUser!!.uid) {
                            data.ref.removeValue()
                                .addOnSuccessListener {
                                    requireActivity().toast("Removed from Liked Items")
                                }
                                .addOnFailureListener {
                                    requireActivity().toast("Failed to remove from Liked Items")
                                }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    companion object {
        fun newInstance() = MainFragment()
    }

}




