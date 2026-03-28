package com.hoan.myapplication.models

data class OrderHistory(
        var id: String? = null,
        val uid: String? = null,
        val name: String? = null,
        val email: String? = null,
        val phone: String? = null,
        val address: String? = null,
        val totalPrice: Int? = null,
        val totalItems: Int? = null,
        val createdAt: Long? = null,
        val items: List<Cart>? = null
)
