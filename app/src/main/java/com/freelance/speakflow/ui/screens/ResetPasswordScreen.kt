package com.freelance.speakflow.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freelance.speakflow.data.ResetPasswordRequest
import com.freelance.speakflow.data.RetrofitInstance
import com.freelance.speakflow.ui.theme.InputDark
import com.freelance.speakflow.ui.theme.PurplePrimary
import com.freelance.speakflow.ui.theme.TextDark
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    email: String, // Received from previous screen
    onBack: () -> Unit,
    onResetSuccess: () -> Unit
) {
    // ✅ REMOVED OTP STATE
    var newPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun resetPassword() {
        if (newPassword.isBlank() || newPassword.length < 6) {
            Toast.makeText(context, "Password must be at least 6 chars", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        scope.launch {
            try {
                // ✅ SENDING EMPTY OTP ("") - Backend ignores it
                val response = RetrofitInstance.api.resetPassword(
                    ResetPasswordRequest(email, "", newPassword)
                )

                if (response.success) {
                    Toast.makeText(context, "Password Changed Successfully!", Toast.LENGTH_LONG).show()
                    onResetSuccess()
                } else {
                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error resetting password", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                "Create New Password",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Set a new password for $email",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(32.dp))

            // ✅ ONLY PASSWORD FIELD NOW
            Text("New Password", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            TextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                placeholder = { Text("Min 6 characters") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = InputDark,
                    unfocusedContainerColor = InputDark,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { resetPassword() },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White)
                else Text("Reset Password", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}