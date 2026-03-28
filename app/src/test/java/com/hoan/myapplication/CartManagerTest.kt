package com.hoan.myapplication

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.hoan.myapplication.models.Order
import com.hoan.myapplication.utils.CartManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CartManagerTest {
    @Mock private lateinit var ordersRef: DatabaseReference
    @Mock private lateinit var newEntryRef: DatabaseReference
    @Mock private lateinit var addTask: Task<Void>

    private lateinit var cartManager: CartManager

    @Before
    fun setup() {
        cartManager = CartManager(ordersRef)
    }

    @Test
    fun `addToCart success`() {
        val order = Order(uid = "u1", pid = "p1", name = "Product", price = "100", quantity = 2)

        `when`(ordersRef.push()).thenReturn(newEntryRef)
        `when`(newEntryRef.key).thenReturn("order123")
        `when`(ordersRef.child(anyString())).thenReturn(newEntryRef)
        `when`(newEntryRef.setValue(any())).thenReturn(addTask)
        `when`(addTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnSuccessListener<Void>>(0)
            listener.onSuccess(null)
            addTask
        }
        `when`(addTask.addOnFailureListener(any())).thenReturn(addTask)

        var resultStatus = false
        var resultMessage = ""

        cartManager.addToCart(order) { success, message ->
            resultStatus = success
            resultMessage = message
        }

        assertTrue(resultStatus)
        assertEquals("Added to cart", resultMessage)
        assertEquals("order123", order.id)
    }

    @Test
    fun `addToCart failure`() {
        val order = Order(uid = "u1", pid = "p1", name = "Product", price = "100", quantity = 2)

        `when`(ordersRef.push()).thenReturn(newEntryRef)
        `when`(newEntryRef.key).thenReturn("order123")
        `when`(ordersRef.child(anyString())).thenReturn(newEntryRef)
        `when`(newEntryRef.setValue(any())).thenReturn(addTask)
        `when`(addTask.addOnSuccessListener(any())).thenReturn(addTask)
        `when`(addTask.addOnFailureListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnFailureListener>(0)
            listener.onFailure(Exception("Database error"))
            addTask
        }

        var resultStatus = true
        var resultMessage = ""

        cartManager.addToCart(order) { success, message ->
            resultStatus = success
            resultMessage = message
        }

        assertFalse(resultStatus)
        assertEquals("Database error", resultMessage)
    }
}
