package com.freelance.speakflow.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    onBack: () -> Unit,
    onContactSupport: () -> Unit,
    onNavigateToFaqs: () -> Unit ,// ✅ Added Callback
    onNavigateToFeedback: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & Support", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
        ) {
            // --- SECTION 1 ---
            SettingsSectionTitle("SUPPORT OPTIONS")
            // ✅ WIRED UP
            SecurityItem(Icons.AutoMirrored.Filled.Help, "FAQs") {
                onNavigateToFaqs()
            }
            SecurityItem(Icons.AutoMirrored.Filled.Chat, "Contact Support") {
                onContactSupport()
            }

            Spacer(Modifier.height(24.dp))

            // --- SECTION 2 ---
            SettingsSectionTitle("COMMUNITY")
            SecurityItem(Icons.Default.Edit, "Give Feedback") {
                onNavigateToFeedback()
            }

            Spacer(Modifier.height(24.dp))

            // --- SECTION 3 ---
            SettingsSectionTitle("APP INFO")
            SecurityItem(Icons.Default.Info, "About the App") {
                Toast.makeText(context, "Version 1.0.0", Toast.LENGTH_SHORT).show()
            }

            Spacer(Modifier.weight(1f))

            Text(
                "Version 1.0.0",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}