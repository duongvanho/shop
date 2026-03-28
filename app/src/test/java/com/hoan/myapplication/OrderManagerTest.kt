package com.hoan.myapplication

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.hoan.myapplication.models.Cart
import com.hoan.myapplication.models.User
import com.hoan.myapplication.utils.OrderManager
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
class OrderManagerTest {
    @Mock private lateinit var orderHistoryRef: DatabaseReference
    @Mock private lateinit var uidChildRef: DatabaseReference
    @Mock private lateinit var orderPushRef: DatabaseReference
    @Mock private lateinit var setTask: Task<Void>

    private lateinit var orderManager: OrderManager

    @Before
    fun setup() {
        orderManager = OrderManager(orderHistoryRef)
    }

    @Test
    fun `placeOrder with empty cart returns error`() {
        orderManager.placeOrder("uid1", User(id = "uid1", name = "A"), emptyList()) {
                success,
                message ->
            assertFalse(success)
            assertEquals("Cart is empty", message)
        }
    }

    @Test
    fun `placeOrder success scenario`() {
        val user =
                User(
                        id = "uid1",
                        name = "A",
                        email = "a@example.com",
                        phone = "012345",
                        address = "Address"
                )
        val cartItems =
                listOf(Cart(id = "c1", pid = "p1", uid = "uid1", price = "50", quantity = 2))

        `when`(orderHistoryRef.child(anyString())).thenReturn(uidChildRef)
        `when`(uidChildRef.push()).thenReturn(orderPushRef)
        `when`(orderPushRef.key).thenReturn("ord123")
        `when`(orderPushRef.setValue(any())).thenReturn(setTask)
        `when`(setTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnSuccessListener<Void>>(0)
            listener.onSuccess(null)
            setTask
        }
        `when`(setTask.addOnFailureListener(any())).thenReturn(setTask)

        var resultStatus = false
        var resultMessage = ""

        orderManager.placeOrder("uid1", user, cartItems) { success, message ->
            resultStatus = success
            resultMessage = message
        }

        assertTrue(resultStatus)
        assertEquals("Order placed successfully", resultMessage)
    }

    @Test
    fun `placeOrder failure scenario`() {
        val user =
                User(
                        id = "uid1",
                        name = "A",
                        email = "a@example.com",
                        phone = "012345",
                        address = "Address"
                )
        val cartItems =
                listOf(Cart(id = "c1", pid = "p1", uid = "uid1", price = "50", quantity = 2))

        `when`(orderHistoryRef.child(anyString())).thenReturn(uidChildRef)
        `when`(uidChildRef.push()).thenReturn(orderPushRef)
        `when`(orderPushRef.key).thenReturn("ord123")
        `when`(orderPushRef.setValue(any())).thenReturn(setTask)
        `when`(setTask.addOnSuccessListener(any())).thenReturn(setTask)
        `when`(setTask.addOnFailureListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnFailureListener>(0)
            listener.onFailure(Exception("Write failed"))
            setTask
        }

        var resultStatus = true
        var resultMessage = ""

        orderManager.placeOrder("uid1", user, cartItems) { success, message ->
            resultStatus = success
            resultMessage = message
        }

        assertFalse(resultStatus)
        assertEquals("Write failed", resultMessage)
    }
}
