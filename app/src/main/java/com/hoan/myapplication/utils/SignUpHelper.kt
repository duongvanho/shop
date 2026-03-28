package com.hoan.myapplication.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class SignUpHelper(private val auth: FirebaseAuth, private val database: DatabaseReference) {

    sealed class SignUpState {
        object Success : SignUpState()
        data class Error(val message: String) : SignUpState()
    }

    fun validateInput(email: String, name: String, password: String): Boolean {
        if (email.isBlank() || name.isBlank() || password.isBlank()) return false

        // Dùng regex thay Patterns.EMAIL_ADDRESS
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (!emailRegex.matches(email)) return false

        if (password.length < 6) return false

        return true
    }
    fun createUser(email: String, password: String, name: String, onResult: (SignUpState) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser!!.uid
                val userMap = mapOf("id" to userId, "name" to name, "email" to email)
                database.child(userId)
                        .setValue(userMap)
                        .addOnSuccessListener { onResult(SignUpState.Success) }
                        .addOnFailureListener { e ->
                            onResult(SignUpState.Error(e.message ?: "Database error"))
                        }
            } else {
                onResult(SignUpState.Error(task.exception?.localizedMessage ?: "Sign-up failed"))
            }
        }
    }
}
