package com.hoan.myapplication

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.hoan.myapplication.utils.SignInHelper
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
class SigninTest {

    @Mock private lateinit var mockFirebaseUser: FirebaseUser
    @Mock private lateinit var mockAuth: FirebaseAuth

    @Mock private lateinit var mockTask: Task<AuthResult>

    private lateinit var signInHelper: SignInHelper

    @Before
    fun setup() {
        signInHelper = SignInHelper(mockAuth)
    }

    // ─── Test validateInput() ───────────────────────────

    @Test
    fun `validate tra ve true khi email va password hop le`() {
        assertTrue(signInHelper.validateInput("test@gmail.com", "123456"))
    }

    @Test
    fun `validate tra ve false khi email rong`() {
        assertFalse(signInHelper.validateInput("", "123456"))
    }

    @Test
    fun `validate tra ve false khi password rong`() {
        assertFalse(signInHelper.validateInput("test@gmail.com", ""))
    }

    @Test
    fun `validate tra ve false khi ca hai rong`() {
        assertFalse(signInHelper.validateInput("", ""))
    }

    // ─── Test signInUser() ──────────────────────────────

    @Test
    fun `signInUser thanh cong tra ve Success`() {
        `when`(mockAuth.signInWithEmailAndPassword(anyString(), anyString())).thenReturn(mockTask)
        `when`(mockTask.isSuccessful).thenReturn(true)
        `when`(mockTask.addOnCompleteListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnCompleteListener<AuthResult>>(0)
            listener.onComplete(mockTask)
            mockTask
        }

        var result: SignInHelper.SignInState? = null
        signInHelper.signInUser("test@gmail.com", "123456") { result = it }

        assertEquals(SignInHelper.SignInState.Success, result)
    }

    @Test
    fun `signInUser that bai tra ve Error co message`() {
        val exception = Exception("Wrong password")
        `when`(mockAuth.signInWithEmailAndPassword(anyString(), anyString())).thenReturn(mockTask)
        `when`(mockTask.isSuccessful).thenReturn(false)
        `when`(mockTask.exception).thenReturn(exception)
        `when`(mockTask.addOnCompleteListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnCompleteListener<AuthResult>>(0)
            listener.onComplete(mockTask)
            mockTask
        }

        var result: SignInHelper.SignInState? = null
        signInHelper.signInUser("test@gmail.com", "wrongpass") { result = it }

        val error = result as SignInHelper.SignInState.Error
        assertEquals("Wrong password", error.message)
    }

    @Test
    fun `signInUser that bai khong co exception tra ve message mac dinh`() {
        `when`(mockAuth.signInWithEmailAndPassword(anyString(), anyString())).thenReturn(mockTask)
        `when`(mockTask.isSuccessful).thenReturn(false)
        `when`(mockTask.exception).thenReturn(null)
        `when`(mockTask.addOnCompleteListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnCompleteListener<AuthResult>>(0)
            listener.onComplete(mockTask)
            mockTask
        }

        var result: SignInHelper.SignInState? = null
        signInHelper.signInUser("test@gmail.com", "wrongpass") { result = it }

        val error = result as SignInHelper.SignInState.Error
        assertEquals("Sign-in failed", error.message)
    }

    @Test
    fun `isAlreadyLoggedIn tra ve true khi da dang nhap`() {
        `when`(mockAuth.currentUser).thenReturn(mockFirebaseUser)

        assertTrue(signInHelper.isAlreadyLoggedIn())
    }

    @Test
    fun `isAlreadyLoggedIn tra ve false khi chua dang nhap`() {
        `when`(mockAuth.currentUser).thenReturn(null)

        assertFalse(signInHelper.isAlreadyLoggedIn())
    }
}
