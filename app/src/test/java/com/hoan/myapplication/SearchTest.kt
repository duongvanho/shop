package com.hoan.myapplication

import org.junit.Assert.*
import org.junit.Test

class SearchTest {

    private val products = listOf("Nike Air", "Adidas Ultra", "Nike Jordan")

    @Test
    fun search_found() {
        val result = products.filter { it.contains("Nike", true) }
        assertEquals(2, result.size)
    }

    @Test
    fun search_caseInsensitive() {
        val result = products.filter { it.contains("nike", true) }
        assertEquals(2, result.size)
    }

    @Test
    fun search_partialMatch() {
        val result = products.filter { it.contains("Air", true) }
        assertEquals(1, result.size)
        assertEquals("Nike Air", result[0])
    }

    @Test
    fun search_notFound() {
        val result = products.filter { it.contains("Puma", true) }
        assertTrue(result.isEmpty())
    }

    @Test
    fun search_emptyKeyword() {
        val keyword = ""
        val result =
                if (keyword.isBlank()) emptyList()
                else products.filter { it.contains(keyword, true) }

        assertTrue(result.isEmpty())
    }

    @Test
    fun search_spaceKeyword() {
        val keyword = "   "
        val result =
                if (keyword.isBlank()) emptyList()
                else products.filter { it.contains(keyword, true) }

        assertTrue(result.isEmpty())
    }

    @Test
    fun search_specialCharacter() {
        val result = products.filter { it.contains("@@@", true) }
        assertTrue(result.isEmpty())
    }

    @Test
    fun search_longKeyword() {
        val keyword = "aaaaaaaaaaaaaaaaaaaa"
        val result = products.filter { it.contains(keyword, true) }
        assertTrue(result.isEmpty())
    }
}
