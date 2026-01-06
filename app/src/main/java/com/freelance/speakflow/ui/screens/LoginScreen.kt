package com.freelance.speakflow.ui.screens

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.freelance.speakflow.data.LoginRequest
import com.freelance.speakflow.data.RetrofitInstance
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (Int) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit // ✅ 1. Callback received from Navigation
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun doLogin() {
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        scope.launch {
            try {
                // Call API
                val response = RetrofitInstance.api.login(
                    LoginRequest(email.trim(), password.trim())
                )

                // Pass user_id upward on success
                onLoginSuccess(response.userId)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Login failed: Check email/password", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    // Call the UI Layout
    LoginLayout(
        email = email,
        onEmailChange = { email = it },
        password = password,
        onPasswordChange = { password = it },
        isLoading = isLoading,
        onLoginClick = { doLogin() },
        onNavigateToRegister = onNavigateToRegister,
        // ✅ 2. Passing the callback to the UI parameter
        onForgotPasswordClick = onNavigateToForgotPassword
    )
}