package com.hoan.myapplication.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hoan.myapplication.databinding.ActivityCheckoutBinding
import com.hoan.myapplication.models.Cart
import com.hoan.myapplication.models.OrderHistory
import com.hoan.myapplication.models.User
import com.hoan.myapplication.utils.Extensions.toast

class CheckoutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var auth: FirebaseAuth

    private lateinit var userRef: DatabaseReference
    private lateinit var ordersRef: DatabaseReference
    private lateinit var orderHistoryRef: DatabaseReference

    private var totalPrice: Int = 0
    private var itemCount: Int = 0

    private var cachedUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        userRef = FirebaseDatabase.getInstance().getReference("Users")
        ordersRef = FirebaseDatabase.getInstance().getReference("orders")
        orderHistoryRef = FirebaseDatabase.getInstance().getReference("OrderHistory")

        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            finish()
            return
        }

        loadUser(uid)
        loadCart(uid)

        binding.btnConfirmOrder.setOnClickListener {
            val user = cachedUser
            val phone = user?.phone?.trim().orEmpty()
            val address = user?.address?.trim().orEmpty()

            if (phone.isBlank() || address.isBlank()) {
                toast("Please update phone and address before ordering")
                return@setOnClickListener
            }

            if (itemCount <= 0) {
                toast("Cart is empty")
                return@setOnClickListener
            }

            placeOrderAndClearCart(uid)
        }
    }

    private fun loadUser(uid: String) {
        userRef.child(uid)
                .addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val user = snapshot.getValue(User::class.java)
                                cachedUser = user

                                binding.tvCheckoutName.text = user?.name.orEmpty()
                                binding.tvCheckoutEmail.text = user?.email.orEmpty()
                                binding.tvCheckoutPhone.text = user?.phone.orEmpty()
                                binding.tvCheckoutAddress.text = user?.address.orEmpty()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                toast(error.message)
                            }
                        }
                )
    }

    private fun loadCart(uid: String) {
        ordersRef
                .orderByChild("uid")
                .equalTo(uid)
                .addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                var total = 0
                                var count = 0

                                for (data in snapshot.children) {
                                    val cartItem = data.getValue(Cart::class.java)
                                    if (cartItem != null) {
                                        val price = cartItem.price?.toIntOrNull() ?: 0
                                        val qty = cartItem.quantity ?: 1
                                        total += price * qty
                                        count += qty
                                    }
                                }

                                totalPrice = total
                                itemCount = count

                                binding.tvCheckoutItems.text = count.toString()
                                binding.tvCheckoutTotal.text = total.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                toast(error.message)
                            }
                        }
                )
    }

    private fun placeOrderAndClearCart(uid: String) {
        ordersRef
                .orderByChild("uid")
                .equalTo(uid)
                .addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val cartItems = mutableListOf<Cart>()
                                val removals = snapshot.children.toList()

                                for (data in snapshot.children) {
                                    val cartItem = data.getValue(Cart::class.java)
                                    if (cartItem != null) {
                                        if (cartItem.id.isNullOrBlank()) {
                                            cartItem.id = data.key
                                        }
                                        cartItems.add(cartItem)
                                    }
                                }

                                if (cartItems.isEmpty()) {
                                    toast("Cart is empty")
                                    finish()
                                    return
                                }

                                val user = cachedUser
                                val now = System.currentTimeMillis()
                                val total =
                                        cartItems.sumOf {
                                            (it.price?.toIntOrNull() ?: 0) * (it.quantity ?: 1)
                                        }
                                val count = cartItems.sumOf { it.quantity ?: 1 }

                                val newOrderRef = orderHistoryRef.child(uid).push()
                                val orderId = newOrderRef.key
                                if (orderId.isNullOrBlank()) {
                                    toast("Could not create order")
                                    return
                                }

                                val order =
                                        OrderHistory(
                                                id = orderId,
                                                uid = uid,
                                                name = user?.name,
                                                email = user?.email,
                                                phone = user?.phone,
                                                address = user?.address,
                                                totalPrice = total,
                                                totalItems = count,
                                                createdAt = now,
                                                items = cartItems
                                        )

                                newOrderRef
                                        .setValue(order)
                                        .addOnSuccessListener {
                                            removals.forEach { it.ref.removeValue() }
                                            toast("Order placed successfully")
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            toast(e.message ?: "Failed to place order")
                                        }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                toast(error.message)
                            }
                        }
                )
    }
}
