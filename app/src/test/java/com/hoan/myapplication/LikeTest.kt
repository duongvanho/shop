package com.hoan.myapplication

import org.junit.Assert.*
import org.junit.Test

class LikeTest {

    @Test
    fun wishlist_add_success() {
        val wishlist = mutableListOf<String>()

        val added = addToWishlist(wishlist, "Nike")

        assertTrue(added)
        assertEquals(1, wishlist.size)
    }

    @Test
    fun wishlist_add_duplicate() {
        val wishlist = mutableListOf("Nike")

        val added = addToWishlist(wishlist, "Nike")

        assertFalse(added)
        assertEquals(1, wishlist.size)
    }

    @Test
    fun wishlist_add_multipleProducts() {
        val wishlist = mutableListOf<String>()

        addToWishlist(wishlist, "Nike")
        addToWishlist(wishlist, "Adidas")

        assertEquals(2, wishlist.size)
        assertTrue(wishlist.contains("Nike"))
        assertTrue(wishlist.contains("Adidas"))
    }

    @Test
    fun wishlist_add_duplicate_multiple() {
        val wishlist = mutableListOf("Nike", "Adidas")

        val addNike = addToWishlist(wishlist, "Nike")
        val addAdidas = addToWishlist(wishlist, "Adidas")

        assertFalse(addNike)
        assertFalse(addAdidas)
        assertEquals(2, wishlist.size)
    }

    @Test
    fun wishlist_initialEmpty() {
        val wishlist = mutableListOf<String>()
        assertTrue(wishlist.isEmpty())
    }
    private fun addToWishlist(wishlist: MutableList<String>, product: String): Boolean {
        return if (!wishlist.contains(product)) {
            wishlist.add(product)
            true
        } else false
    }
}
