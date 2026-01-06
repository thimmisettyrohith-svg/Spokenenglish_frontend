package com.freelance.speakflow.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freelance.speakflow.data.RetrofitInstance
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecurityScreen(
    userId: Int,
    onBack: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToDataDownload: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- State for Delete Account Dialog ---
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    // --- Delete Confirmation Dialog ---
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
            title = { Text("Delete Account?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This action cannot be undone. All your progress, XP, and streaks will be permanently lost.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleting = true
                        scope.launch {
                            try {
                                val response = RetrofitInstance.api.deleteAccount(userId)
                                if (response.success) {
                                    Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_LONG).show()
                                    showDeleteDialog = false
                                    onAccountDeleted() // Navigate to Login
                                } else {
                                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Error connecting to server", Toast.LENGTH_SHORT).show()
                            } finally {
                                isDeleting = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text("Delete Forever")
                    }
                }
            },
            dismissButton = {
                if (!isDeleting) {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel", color = Color.Black)
                    }
                }
            },
            containerColor = Color.White
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy & Security", fontWeight = FontWeight.Bold) },
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
            // ================= SECTION 1: SECURITY =================
            SettingsSectionTitle("ACCOUNT SECURITY")

            // 1. Change Password (Real)
            SecurityItem(Icons.Default.Lock, "Change Password") {
                onNavigateToChangePassword()
            }

            // 2. Login Activity (Placeholder)
            SecurityItem(Icons.Default.Timer, "Login Activity") {
                Toast.makeText(context, "Coming Soon in v1.1", Toast.LENGTH_SHORT).show()
            }

            Spacer(Modifier.height(24.dp))

            // ================= SECTION 2: DATA =================
            SettingsSectionTitle("DATA & PERMISSIONS")

            // 3. App Permissions (Real System Intent)
            SecurityItem(Icons.Default.VpnKey, "App Permissions") {
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Could not open settings", Toast.LENGTH_SHORT).show()
                }
            }

            // 4. Data Download (Real Screen)
            SecurityItem(Icons.Default.CloudDownload, "Data Download") {
                onNavigateToDataDownload()
            }

            // 5. Delete Account (Real Dialog)
            SecurityItem(Icons.Default.Warning, "Delete Account", isDestructive = true) {
                showDeleteDialog = true
            }

            Spacer(Modifier.height(24.dp))

            // ================= SECTION 3: INFO =================
            SettingsSectionTitle("INFORMATION")

            // 6. Privacy Policy
            SecurityItem(Icons.Default.Policy, "Privacy Policy") {
                onNavigateToPrivacyPolicy()
            }

            // 7. Terms & Conditions
            SecurityItem(Icons.Default.Description, "Terms & Conditions") {
                onNavigateToTerms()
            }
        }
    }
}

// ================= HELPER COMPOSABLES =================

@Composable
fun SecurityItem(
    icon: ImageVector,
    title: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDestructive) Color.Red else Color.Black
                )
            }

            Spacer(Modifier.width(16.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDestructive) Color.Red else Color.Black,
                modifier = Modifier.weight(1f)
            )

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
    )
}