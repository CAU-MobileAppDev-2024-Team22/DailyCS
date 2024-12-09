package com.example.firebaseexample.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = false,
            onClick = {
                navController.navigate("main") {
                    popUpTo("main") { inclusive = true } // 중복 쌓이지 않도록 설정
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Analytics, contentDescription = "Analytics") },
            label = { Text("Analytics") },
            selected = false,
            onClick = { /* Analytics 클릭 이벤트 */ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = false,
            onClick = { /* Profile 클릭 이벤트 */ }
        )
    }
}