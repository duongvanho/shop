package com.hoan.myapplication.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hoan.myapplication.R
import com.hoan.myapplication.activities.CheckoutActivity
import com.hoan.myapplication.adapters.CartAdapter
import com.hoan.myapplication.databinding.FragmentCartpageBinding
import com.hoan.myapplication.models.Cart
import com.hoan.myapplication.utils.Extensions.toast
import com.hoan.myapplication.utils.SwipeToDelete

class CartFragment :
        Fragment(R.layout.fragment_cartpage),
        CartAdapter.OnLongClickRemove,
        CartAdapter.OnQuantityChanged {

    private lateinit var binding: FragmentCartpageBinding
    private lateinit var cartList: ArrayList<Cart>
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: CartAdapter
    private var subTotalPrice = 0
    private var totalPrice = 0

    private var orderDatabaseReference = FirebaseDatabase.getInstance().getReference("orders")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentCartpageBinding.bind(view)
        auth = FirebaseAuth.getInstance()

        val layoutManager = LinearLayoutManager(context)

        cartList = ArrayList()

        retrieveCartItems()

        adapter = CartAdapter(requireContext(), cartList, this, this)
        binding.rvCartItems.adapter = adapter
        binding.rvCartItems.layoutManager = layoutManager

        val itemTouchHelper =
                ItemTouchHelper(
                        object : SwipeToDelete() {
                            override fun onSwiped(
                                    viewHolder: RecyclerView.ViewHolder,
                                    direction: Int
                            ) {
                                deleteProductCart(viewHolder)
                            }
                        }
                )

        itemTouchHelper.attachToRecyclerView(binding.rvCartItems)

        binding.btnCartCheckout.setOnClickListener {
            if (cartList.isEmpty()) {
                updatePrices()
                return@setOnClickListener
            }

            updatePrices()
            startActivity(Intent(requireContext(), CheckoutActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        retrieveCartItems()
    }

    private fun deleteProductCart(viewHolder: RecyclerView.ViewHolder) {
        val position = viewHolder.adapterPosition
        val itemToRemove = cartList[position]

        val id = itemToRemove.id
        if (!id.isNullOrBlank()) {
            orderDatabaseReference
                    .child(id)
                    .removeValue()
                    .addOnSuccessListener {
                        cartList.removeAt(position)
                        adapter.notifyItemRemoved(position)
                        requireActivity().toast("Removed Successfully!")
                        updatePrices()
                    }
                    .addOnFailureListener {
                        requireActivity().toast("Failed to remove")
                        adapter.notifyItemChanged(position)
                    }
            return
        }

        orderDatabaseReference
                .orderByChild("uid")
                .equalTo(itemToRemove.uid)
                .addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (orderSnapshot in snapshot.children) {
                                    val order = orderSnapshot.getValue(Cart::class.java)
                                    if (order != null &&
                                                    order.pid == itemToRemove.pid &&
                                                    order.size == itemToRemove.size
                                    ) {
                                        orderSnapshot
                                                .ref
                                                .removeValue()
                                                .addOnSuccessListener {
                                                    cartList.removeAt(position)
                                                    adapter.notifyItemRemoved(position)
                                                    requireActivity().toast("Removed Successfully!")

                                                    updatePrices()
                                                }
                                                .addOnFailureListener {
                                                    requireActivity().toast("Failed to remove")
                                                    adapter.notifyItemChanged(position)
                                                }
                                        return
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                requireActivity().toast("Error: ${error.message}")
                                adapter.notifyItemChanged(position)
                            }
                        }
                )
    }

    private fun updatePrices() {
        var subTotal = 0
        for (item in cartList) {
            val price = item.price?.toIntOrNull() ?: 0
            val qty = item.quantity ?: 1
            subTotal += price * qty
        }
        subTotalPrice = subTotal
        totalPrice = subTotal

        binding.tvLastSubTotalprice.text = subTotalPrice.toString()
        binding.tvLastTotalPrice.text =
                if (cartList.isEmpty()) {
                    binding.tvLastTotalPrice.setTextColor(Color.RED)
                    "Min 1 product is Required"
                } else {
                    binding.tvLastTotalPrice.setTextColor(Color.BLACK)
                    totalPrice.toString()
                }
        binding.tvLastSubTotalItems.text = "Subtotal Items(${cartList.size})"
    }

    private fun retrieveCartItems() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("orders")

        databaseReference
                .orderByChild("uid")
                .equalTo(auth.currentUser?.uid)
                .addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                cartList.clear()
                                if (snapshot.exists()) {
                                    for (data in snapshot.children) {
                                        val order = data.getValue(Cart::class.java)
                                        if (order != null) {
                                            if (order.id.isNullOrBlank()) {
                                                order.id = data.key
                                            }
                                            if (order.quantity == null || order.quantity!! < 1) {
                                                order.quantity = 1
                                            }
                                            cartList.add(order)
                                        }
                                    }
                                }

                                adapter.notifyDataSetChanged()
                                updatePrices()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                requireActivity().toast("Error: ${error.message}")
                            }
                        }
                )
    }

    override fun onLongRemove(item: Cart, position: Int) {
        val id = item.id
        if (!id.isNullOrBlank()) {
            orderDatabaseReference
                    .child(id)
                    .removeValue()
                    .addOnSuccessListener {
                        cartList.removeAt(position)
                        adapter.notifyItemRemoved(position)
                        requireActivity().toast("Removed Successfully!")
                        updatePrices()
                    }
                    .addOnFailureListener { requireActivity().toast("Failed to remove") }
            return
        }

        orderDatabaseReference
                .orderByChild("uid")
                .equalTo(item.uid)
                .addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (orderSnapshot in snapshot.children) {
                                    val order = orderSnapshot.getValue(Cart::class.java)

                                    if (order != null &&
                                                    order.pid == item.pid &&
                                                    order.size == item.size
                                    ) {
                                        orderSnapshot
                                                .ref
                                                .removeValue()
                                                .addOnSuccessListener {
                                                    cartList.removeAt(position)
                                                    adapter.notifyItemRemoved(position)
                                                    requireActivity().toast("Removed Successfully!")
                                                    updatePrices()
                                                }
                                                .addOnFailureListener {
                                                    requireActivity().toast("Failed to remove")
                                                }
                                        return
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                requireActivity().toast("Error: ${error.message}")
                            }
                        }
                )
    }

    override fun onQuantityChanged(item: Cart, newQuantity: Int, position: Int) {
        val id = item.id
        if (id.isNullOrBlank()) {
            updatePrices()
            return
        }

        orderDatabaseReference
                .child(id)
                .child("quantity")
                .setValue(newQuantity)
                .addOnSuccessListener { updatePrices() }
                .addOnFailureListener {
                    requireActivity().toast("Failed to update quantity")
                    updatePrices()
                }
    }

    companion object {
        fun newInstance() = CartFragment()
    }
}
