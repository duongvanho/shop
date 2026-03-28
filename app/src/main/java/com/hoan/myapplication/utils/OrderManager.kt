package com.hoan.myapplication.utils

import com.google.firebase.database.DatabaseReference
import com.hoan.myapplication.models.Cart
import com.hoan.myapplication.models.OrderHistory
import com.hoan.myapplication.models.User

class OrderManager(private val orderHistoryRef: DatabaseReference) {

    fun placeOrder(
            uid: String,
            user: User?,
            cartItems: List<Cart>,
            callback: (Boolean, String) -> Unit
    ) {
        if (user == null) {
            callback(false, "User must be logged in")
            return
        }

        if (cartItems.isEmpty()) {
            callback(false, "Cart is empty")
            return
        }

        val pushRef = orderHistoryRef.child(uid).push()
        val orderId = pushRef.key
        if (orderId.isNullOrBlank()) {
            callback(false, "Could not create order")
            return
        }

        val totalPrice = cartItems.sumOf { (it.price?.toIntOrNull() ?: 0) * (it.quantity ?: 1) }
        val totalItems = cartItems.sumOf { it.quantity ?: 1 }

        val now = System.currentTimeMillis()

        val orderHistory =
                OrderHistory(
                        id = orderId,
                        uid = uid,
                        name = user.name,
                        email = user.email,
                        phone = user.phone,
                        address = user.address,
                        totalPrice = totalPrice,
                        totalItems = totalItems,
                        createdAt = now,
                        items = cartItems
                )

        pushRef.setValue(orderHistory)
                .addOnSuccessListener { callback(true, "Order placed successfully") }
                .addOnFailureListener { e -> callback(false, e.message ?: "Failed to place order") }
    }
}
