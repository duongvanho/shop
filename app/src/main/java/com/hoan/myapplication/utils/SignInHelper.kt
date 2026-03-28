package com.hoan.myapplication.utils

import com.google.firebase.auth.FirebaseAuth

class SignInHelper(private val auth: FirebaseAuth) {

    sealed class SignInState {
        object Success : SignInState()
        data class Error(val message: String) : SignInState()
    }

    fun validateInput(email: String, password: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty()
    }

    fun signInUser(email: String, password: String, onResult: (SignInState) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(SignInState.Success)
            } else {
                onResult(SignInState.Error(task.exception?.localizedMessage ?: "Sign-in failed"))
            }
        }
    }
    fun isAlreadyLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}
