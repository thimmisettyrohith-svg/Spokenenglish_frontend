package com.freelance.speakflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(
    title: String,
    content: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = content,
                fontSize = 14.sp,
                color = Color(0xFF444444),
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Last updated: January 2024",
                fontSize = 12.sp,
                color = Color(0xFFE91E63) // Pinkish accent color
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

// Helper to provide standard dummy text
object LegalContent {
    const val PRIVACY_POLICY = """
This Privacy Policy describes how Spoken English Learning with Gamification ("we," "us," or "our") collects, uses, and shares your information when you use our mobile application (the "App").

Information We Collect
We collect the following types of information:
- Personal Information: This includes your name, email address, and any other information you provide to us directly.
- Usage Data: We collect information about how you use the App, such as the features you use, the content you view, and the time you spend in the App.

How We Use Your Information
We use your information for the following purposes:
- To provide and improve the App's functionality.
- To personalize your experience.
- To communicate with you about updates, promotions, and other relevant information.

Data Security
We take reasonable measures to protect your information from unauthorized access, use, or disclosure.
    """

    const val TERMS_CONDITIONS = """
Welcome to Spoken English Learning with Gamification.

1. Acceptance of Terms
By using our app, you agree to these terms. If you do not agree, please do not use the app.

2. User Accounts
You are responsible for maintaining the confidentiality of your account information.

3. Content
All content provided in the app is for educational purposes. We are not responsible for any errors or omissions.

4. Termination
We reserve the right to terminate or suspend your account at any time, without notice, for conduct that violates these terms.
    """
}