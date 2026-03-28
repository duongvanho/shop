package com.hoan.myapplication

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.hoan.myapplication.utils.SignUpHelper
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.argumentCaptor

@RunWith(MockitoJUnitRunner::class)
class SignUpTest {
    @Mock private lateinit var mockAuth: FirebaseAuth
    @Mock private lateinit var mockDatabase: DatabaseReference
    @Mock private lateinit var mockAuthTask: Task<AuthResult>
    @Mock private lateinit var mockDatabaseTask: Task<Void>
    @Mock private lateinit var mockFirebaseUser: FirebaseUser

    private lateinit var signUpHelper: SignUpHelper

    @Before
    fun setup() {
        signUpHelper = SignUpHelper(mockAuth, mockDatabase)
    }

    // ─── Test validateInput() ───────────────────────────
    @Test
    fun `validate false khi email khong hop le`() {
        assertFalse(signUpHelper.validateInput("notanemail", "Hoan", "123456"))
    }

    @Test
    fun `validate false khi password qua ngan`() {
        assertFalse(signUpHelper.validateInput("test@gmail.com", "Hoan", "123"))
    }

    @Test
    fun `validate false khi name chi co khoang trang`() {
        assertFalse(signUpHelper.validateInput("test@gmail.com", "   ", "123456"))
    }

    @Test
    fun `validate false khi email chi co khoang trang`() {
        assertFalse(signUpHelper.validateInput("   ", "Hoan", "123456"))
    }

    @Test
    fun `validate true khi tat ca truong hop le`() {
        assertTrue(signUpHelper.validateInput("test@gmail.com", "Hoan", "123456"))
    }

    @Test
    fun `validate false khi email rong`() {
        assertFalse(signUpHelper.validateInput("", "Hoan", "123456"))
    }

    @Test
    fun `validate false khi name rong`() {
        assertFalse(signUpHelper.validateInput("test@gmail.com", "", "123456"))
    }

    @Test
    fun `validate false khi password rong`() {
        assertFalse(signUpHelper.validateInput("test@gmail.com", "Hoan", ""))
    }

    @Test
    fun `validate false khi tat ca rong`() {
        assertFalse(signUpHelper.validateInput("", "", ""))
    }

    // ─── Test createUser() ──────────────────────────────

    @Test
    fun `createUser thanh cong tra ve Success`() {
        `when`(mockAuth.createUserWithEmailAndPassword(anyString(), anyString()))
                .thenReturn(mockAuthTask)
        `when`(mockAuthTask.isSuccessful).thenReturn(true)
        `when`(mockAuth.currentUser).thenReturn(mockFirebaseUser)
        `when`(mockFirebaseUser.uid).thenReturn("uid_123")
        `when`(mockDatabase.child(anyString())).thenReturn(mockDatabase)
        `when`(mockDatabase.setValue(any())).thenReturn(mockDatabaseTask)

        `when`(mockAuthTask.addOnCompleteListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnCompleteListener<AuthResult>>(0)
            listener.onComplete(mockAuthTask)
            mockAuthTask
        }
        `when`(mockDatabaseTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnSuccessListener<Void>>(0)
            listener.onSuccess(null)
            mockDatabaseTask
        }
        `when`(mockDatabaseTask.addOnFailureListener(any())).thenReturn(mockDatabaseTask)

        var result: SignUpHelper.SignUpState? = null
        signUpHelper.createUser("test@gmail.com", "123456", "Hoan") { result = it }

        assertEquals(SignUpHelper.SignUpState.Success, result)
    }

    @Test
    fun `createUser that bai auth tra ve Error`() {
        val exception = Exception("Email already in use")
        `when`(mockAuth.createUserWithEmailAndPassword(anyString(), anyString()))
                .thenReturn(mockAuthTask)
        `when`(mockAuthTask.isSuccessful).thenReturn(false)
        `when`(mockAuthTask.exception).thenReturn(exception)
        `when`(mockAuthTask.addOnCompleteListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnCompleteListener<AuthResult>>(0)
            listener.onComplete(mockAuthTask)
            mockAuthTask
        }

        var result: SignUpHelper.SignUpState? = null
        signUpHelper.createUser("test@gmail.com", "123456", "Hoan") { result = it }

        val error = result as SignUpHelper.SignUpState.Error
        assertEquals("Email already in use", error.message)
    }

    @Test
    fun `createUser that bai database tra ve Error`() {
        val dbException = Exception("Database error")
        `when`(mockAuth.createUserWithEmailAndPassword(anyString(), anyString()))
                .thenReturn(mockAuthTask)
        `when`(mockAuthTask.isSuccessful).thenReturn(true)
        `when`(mockAuth.currentUser).thenReturn(mockFirebaseUser)
        `when`(mockFirebaseUser.uid).thenReturn("uid_123")
        `when`(mockDatabase.child(anyString())).thenReturn(mockDatabase)
        `when`(mockDatabase.setValue(any())).thenReturn(mockDatabaseTask)

        `when`(mockAuthTask.addOnCompleteListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnCompleteListener<AuthResult>>(0)
            listener.onComplete(mockAuthTask)
            mockAuthTask
        }
        `when`(mockDatabaseTask.addOnSuccessListener(any())).thenReturn(mockDatabaseTask)
        `when`(mockDatabaseTask.addOnFailureListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnFailureListener>(0)
            listener.onFailure(dbException)
            mockDatabaseTask
        }

        var result: SignUpHelper.SignUpState? = null
        signUpHelper.createUser("test@gmail.com", "123456", "Hoan") { result = it }

        val error = result as SignUpHelper.SignUpState.Error
        assertEquals("Database error", error.message)
    }

    @Test
    fun `createUser that bai khong co exception tra ve message mac dinh`() {
        `when`(mockAuth.createUserWithEmailAndPassword(anyString(), anyString()))
                .thenReturn(mockAuthTask)
        `when`(mockAuthTask.isSuccessful).thenReturn(false)
        `when`(mockAuthTask.exception).thenReturn(null)
        `when`(mockAuthTask.addOnCompleteListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnCompleteListener<AuthResult>>(0)
            listener.onComplete(mockAuthTask)
            mockAuthTask
        }

        var result: SignUpHelper.SignUpState? = null
        signUpHelper.createUser("test@gmail.com", "123456", "Hoan") { result = it }

        val error = result as SignUpHelper.SignUpState.Error
        assertEquals("Sign-up failed", error.message)
    }

    @Test
    fun `createUser voi email da ton tai tra ve Error`() {
        val exception = Exception("The email address is already in use by another account")
        `when`(mockAuth.createUserWithEmailAndPassword(anyString(), anyString()))
                .thenReturn(mockAuthTask)
        `when`(mockAuthTask.isSuccessful).thenReturn(false)
        `when`(mockAuthTask.exception).thenReturn(exception)
        `when`(mockAuthTask.addOnCompleteListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnCompleteListener<AuthResult>>(0)
            listener.onComplete(mockAuthTask)
            mockAuthTask
        }

        var result: SignUpHelper.SignUpState? = null
        signUpHelper.createUser("existed@gmail.com", "123456", "Hoan") { result = it }

        val error = result as SignUpHelper.SignUpState.Error
        assertEquals("The email address is already in use by another account", error.message)
    }

    @Test
    fun `createUser voi password yeu tra ve Error`() {
        val exception = Exception("The given password is invalid")
        `when`(mockAuth.createUserWithEmailAndPassword(anyString(), anyString()))
                .thenReturn(mockAuthTask)
        `when`(mockAuthTask.isSuccessful).thenReturn(false)
        `when`(mockAuthTask.exception).thenReturn(exception)
        `when`(mockAuthTask.addOnCompleteListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnCompleteListener<AuthResult>>(0)
            listener.onComplete(mockAuthTask)
            mockAuthTask
        }

        var result: SignUpHelper.SignUpState? = null
        signUpHelper.createUser("test@gmail.com", "123", "Hoan") { result = it }

        val error = result as SignUpHelper.SignUpState.Error
        assertEquals("The given password is invalid", error.message)
    }

    @Test
    fun `createUser voi email sai dinh dang tra ve Error`() {
        val exception = Exception("The email address is badly formatted")
        `when`(mockAuth.createUserWithEmailAndPassword(anyString(), anyString()))
                .thenReturn(mockAuthTask)
        `when`(mockAuthTask.isSuccessful).thenReturn(false)
        `when`(mockAuthTask.exception).thenReturn(exception)
        `when`(mockAuthTask.addOnCompleteListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnCompleteListener<AuthResult>>(0)
            listener.onComplete(mockAuthTask)
            mockAuthTask
        }

        var result: SignUpHelper.SignUpState? = null
        signUpHelper.createUser("notanemail", "123456", "Hoan") { result = it }

        val error = result as SignUpHelper.SignUpState.Error
        assertEquals("The email address is badly formatted", error.message)
    }

    @Test
    fun `createUser thanh cong luu dung thong tin user`() {
        val captor = argumentCaptor<Map<String, String>>()

        `when`(mockAuth.createUserWithEmailAndPassword(anyString(), anyString()))
                .thenReturn(mockAuthTask)
        `when`(mockAuthTask.isSuccessful).thenReturn(true)
        `when`(mockAuth.currentUser).thenReturn(mockFirebaseUser)
        `when`(mockFirebaseUser.uid).thenReturn("uid_123")
        `when`(mockDatabase.child(anyString())).thenReturn(mockDatabase)
        `when`(mockDatabase.setValue(captor.capture())).thenReturn(mockDatabaseTask)

        `when`(mockAuthTask.addOnCompleteListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnCompleteListener<AuthResult>>(0)
            listener.onComplete(mockAuthTask)
            mockAuthTask
        }
        `when`(mockDatabaseTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnSuccessListener<Void>>(0)
            listener.onSuccess(null)
            mockDatabaseTask
        }
        `when`(mockDatabaseTask.addOnFailureListener(any())).thenReturn(mockDatabaseTask)

        signUpHelper.createUser("test@gmail.com", "123456", "Hoan") {}

        // Kiểm tra dữ liệu lưu vào database đúng không
        val savedData = captor.firstValue
        assertEquals("uid_123", savedData["id"])
        assertEquals("Hoan", savedData["name"])
        assertEquals("test@gmail.com", savedData["email"])
    }
}
