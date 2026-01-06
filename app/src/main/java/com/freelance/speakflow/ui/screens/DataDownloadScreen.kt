package com.freelance.speakflow.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataDownloadScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Download", fontWeight = FontWeight.Bold) },
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
                .padding(24.dp)
        ) {
            Text(
                "Request a copy of your learning data, including progress, XP, achievements, and activity history.",
                color = Color.Gray,
                lineHeight = 22.sp,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(32.dp))

            // --- OPTION 1: FULL DATA ---
            DownloadOptionCard(
                title = "Download Full Data",
                subtitle = "All your learning data",
                color = Color(0xFFE3F2FD), // Light Blue
                accentColor = Color(0xFF2196F3)
            ) {
                Toast.makeText(context, "Request Sent", Toast.LENGTH_SHORT).show()
            }

            // --- OPTION 2: ACTIVITY ONLY ---
            DownloadOptionCard(
                title = "Download Activity Only",
                subtitle = "Only your activity history",
                color = Color(0xFFFFF3E0), // Light Orange
                accentColor = Color(0xFFFF9800)
            ) {
                Toast.makeText(context, "Request Sent", Toast.LENGTH_SHORT).show()
            }

            // --- OPTION 3: PROFILE INFO ---
            DownloadOptionCard(
                title = "Download Profile Info",
                subtitle = "Only your profile information",
                color = Color(0xFFF3E5F5), // Light Purple
                accentColor = Color(0xFF9C27B0)
            ) {
                Toast.makeText(context, "Request Sent", Toast.LENGTH_SHORT).show()
            }

            Spacer(Modifier.weight(1f))

            Text(
                "You will receive a downloadable file via email.",
                color = Color(0xFFE91E63), // Pink
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun DownloadOptionCard(
    title: String,
    subtitle: String,
    color: Color,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .height(80.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(subtitle, color = Color(0xFFE91E63), fontSize = 12.sp) // Using Pink for subtitle as per design
            }

            // Abstract Art Placeholder
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .background(color.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(4.dp, accentColor.copy(alpha = 0.2f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(accentColor, CircleShape)
                )
            }
        }
    }
}