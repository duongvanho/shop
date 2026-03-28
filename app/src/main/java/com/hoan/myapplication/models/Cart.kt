package com.hoan.myapplication.models

data class Cart(
        var id: String? = null,
        val pid: String? = null,
        val uid: String? = null,
        val imageUrl: String? = null,
        val name: String? = null,
        val price: String? = null,
        val size: String? = null,
        var quantity: Int? = 1
)
