package com.anafthdev.mlkit.ui.home

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anafthdev.mlkit.data.MlKitScreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    navigateTo: (MlKitScreen) -> Unit
) {

    FlowRow(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .systemBarsPadding()
    ) {
        Button(
            onClick = {
                navigateTo(MlKitScreen.FaceDetection)
            }
        ) {
            Text("Face detection")
        }
    }
}
