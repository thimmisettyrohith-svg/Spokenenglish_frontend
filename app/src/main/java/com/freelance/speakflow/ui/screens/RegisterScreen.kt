package com.freelance.speakflow.ui.screens

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.freelance.speakflow.data.RegisterRequest
import com.freelance.speakflow.data.RetrofitInstance
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    // 1. State
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // 2. Helpers
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 3. Logic Function
    fun doRegister() {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        scope.launch {
            try {
                val request = RegisterRequest(username = name, email = email, password = password)

                // Call API
                val response = RetrofitInstance.api.register(request)

                Toast.makeText(context, "Welcome, ${response.username}!", Toast.LENGTH_SHORT).show()
                onRegisterSuccess() // Navigate to Home or Login

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    // 4. Render Dumb Layout
    RegisterLayout(
        name = name,
        onNameChange = { name = it },
        email = email,
        onEmailChange = { email = it },
        password = password,
        onPasswordChange = { password = it },
        isLoading = isLoading,
        onRegisterClick = { doRegister() },
        onNavigateToLogin = onNavigateToLogin
    )
}