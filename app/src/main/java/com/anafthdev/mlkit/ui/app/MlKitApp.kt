package com.anafthdev.mlkit.ui.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anafthdev.mlkit.data.MlKitScreen
import com.anafthdev.mlkit.ui.face_detection.FaceDetectionScreen
import com.anafthdev.mlkit.ui.home.HomeScreen

@Composable
fun MlKitApp() {

    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = MlKitScreen.Home.name
    ) {
        composable(MlKitScreen.Home.name) {
            HomeScreen(
                navigateTo = { screen ->
                    navController.navigate(screen.name)
                }
            )
        }

        composable(MlKitScreen.FaceDetection.name) {
            FaceDetectionScreen()
        }
    }

}
