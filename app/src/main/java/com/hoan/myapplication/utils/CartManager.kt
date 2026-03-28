package com.hoan.myapplication.utils

import com.google.firebase.database.DatabaseReference
import com.hoan.myapplication.models.Order

class CartManager(private val ordersRef: DatabaseReference) {

    fun addToCart(order: Order, callback: (Boolean, String) -> Unit) {
        val pushRef = ordersRef.push()
        val orderId = pushRef.key
        if (orderId.isNullOrBlank()) {
            callback(false, "Could not generate order ID")
            return
        }

        order.id = orderId

        ordersRef
                .child(orderId)
                .setValue(order)
                .addOnSuccessListener { callback(true, "Added to cart") }
                .addOnFailureListener { e -> callback(false, e.message ?: "Failed to add to cart") }
    }
}
