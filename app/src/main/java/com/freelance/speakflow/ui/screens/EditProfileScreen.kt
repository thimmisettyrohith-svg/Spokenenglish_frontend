package com.freelance.speakflow.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.freelance.speakflow.data.RetrofitInstance
import com.freelance.speakflow.data.UpdateProfileRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    userId: Int, // ✅ PASSED FROM NAVIGATION
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Form State
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(true) }

    // 🚀 FETCH DATA ON LOAD
    LaunchedEffect(Unit) {
        try {
            val profile = RetrofitInstance.api.getProfile(userId)
            fullName = profile.fullName ?: ""
            username = profile.username
            email = profile.email
            phone = profile.phoneNumber ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    fun saveChanges() {
        if (username.isBlank() || email.isBlank()) {
            Toast.makeText(context, "Username and Email are required", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            try {
                val request = UpdateProfileRequest(
                    fullName = fullName,
                    username = username,
                    email = email,
                    phoneNumber = phone
                )
                val response = RetrofitInstance.api.updateProfile(userId, request)

                if (response.success) {
                    Toast.makeText(context, "Changes Saved Successfully!", Toast.LENGTH_SHORT).show()
                    onBack()
                } else {
                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error saving profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- AVATAR SECTION ---
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    AsyncImage(
                        model = "https://img.freepik.com/free-vector/woman-profile-cartoon_18591-58480.jpg", // Still placeholder for now
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Edit",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = fullName.ifBlank { username },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "@$username",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(Modifier.height(32.dp))

                // --- FORM FIELDS ---
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("ACCOUNT DETAILS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))

                    ProfileTextField("Full Name", fullName) { fullName = it }
                    ProfileTextField("Username", username) { username = it }
                    ProfileTextField("Email", email, KeyboardType.Email) { email = it }
                    ProfileTextField("Phone Number", phone, KeyboardType.Phone) { phone = it }
                }

                Spacer(Modifier.height(40.dp))

                // --- SAVE BUTTON ---
                Button(
                    onClick = { saveChanges() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Save Changes", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun ProfileTextField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}