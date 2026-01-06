package com.freelance.speakflow.ui.screens

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.freelance.speakflow.data.DashboardResponse
import com.freelance.speakflow.data.RetrofitInstance
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    userId: Int, // Passed from Login
    onNavigateToGame: (String) -> Unit
) {
    var dashboardData by remember { mutableStateOf<DashboardResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Fetch data when screen loads
    LaunchedEffect(userId) {
        scope.launch {
            try {
                dashboardData = RetrofitInstance.api.getDashboard(userId)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    context,
                    "Error loading: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                isLoading = false
            }
        }
    }

    HomeLayout(
        data = dashboardData,
        isLoading = isLoading,
        onModuleClick = { moduleId ->
            when (moduleId) {
                "vocab" -> {
                    onNavigateToGame("vocab")
                }

                "speaking" -> {
                    onNavigateToGame("speaking")
                }

                "grammar" -> {
                    onNavigateToGame("grammar")
                }

                // ✅ UPDATED: Now navigates to Situations
                "situations" -> {
                    onNavigateToGame("situations")
                }

                else -> {
                    Toast.makeText(
                        context,
                        "Unknown module: $moduleId",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    )
}