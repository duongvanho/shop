package com.hoan.myapplication.models

data class Order(
    var id: String? = null,
    val uid: String? = null,
    val pid: String? = null,
    val imageUrl: String? = null,
    val name: String? = null,
    val size: String? = null,
    val quantity: Int? = null,
    val price: String? = null
)
