package com.anafthdev.mlkit.ui.face_detection

import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRect
import androidx.core.graphics.toRectF
import com.anafthdev.mlkit.data.FaceDetectionImageAnalyzer
import com.anafthdev.mlkit.data.model.FacePosition
import timber.log.Timber
import kotlin.math.ceil

@Composable
fun FaceDetectionScreen() {

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val density = LocalDensity.current
    val config = LocalConfiguration.current

    val facePositions = remember {
        mutableStateListOf<FacePosition>()
    }

    var cameraSelector by remember {
        mutableStateOf(CameraSelector.DEFAULT_FRONT_CAMERA)
    }

    var cameraProvider by remember {
        mutableStateOf<ProcessCameraProvider?>(null)
    }

    val preview = remember {
        Preview.Builder()
            .setTargetResolution(
                with(density) {
                    Size(ceil(config.screenWidthDp.dp.toPx()).toInt(), ceil(config.screenHeightDp.dp.toPx()).toInt())
                }
            )
            .build()
    }

    val faceDetectionImageAnalyzer = remember {
        FaceDetectionImageAnalyzer().apply {
            addOnCompleteListener { faces, outputTransform ->
                facePositions.clear()

                if (faces.isEmpty()) return@addOnCompleteListener

                for (face in faces) {
                    val boundingBox = face.boundingBox.toRectF()
                    Timber.i("rekt fes: ${face.boundingBox.toComposeRect()}")

                    facePositions.add(FacePosition(boundingBox.toRect().toComposeRect()))
                }
            }

            addOnFailureListener { e ->
                Timber.e(e)
            }
        }
    }

    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setTargetResolution(
                with(density) {
                    Size(ceil(config.screenWidthDp.dp.toPx()).toInt(), ceil(config.screenHeightDp.dp.toPx()).toInt()).also { Timber.i("rekt siz: ${it}") }
                }
            )
            .build()
            .apply {
                setAnalyzer(ContextCompat.getMainExecutor(context), faceDetectionImageAnalyzer)
            }
    }

    LaunchedEffect(Unit) {
        ProcessCameraProvider.getInstance(context).apply {
            addListener(
                {
                    cameraProvider = get()
                }, ContextCompat.getMainExecutor(context)
            )
        }
    }

    LaunchedEffect(cameraProvider, cameraSelector) {
        if (cameraProvider != null) {
            cameraProvider!!.unbindAll()
            var camera = cameraProvider!!.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)
        ) {
            for (face in facePositions) {

                Timber.i("rekt: ${size}")

                drawContext.canvas.drawRect(
                    rect = face.boxPosition.let {
                        it.copy(
                            left = (size.width - it.left - size.width / 4),
                            right = size.width - it.left + it.width - size.width / 4
                        )
                    },
                    paint = Paint().apply {
                        color = Color.Blue
                        style = PaintingStyle.Stroke
                        strokeWidth = 10f
                    }
                )
            }
        }

        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
//                    controller = cameraController
                    scaleType = PreviewView.ScaleType.FIT_CENTER
                    preview.setSurfaceProvider(surfaceProvider)
                }
            },
            modifier = Modifier
                .fillMaxSize()
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .zIndex(1f)
                .align(Alignment.BottomCenter)
        ) {
            IconButton(
                onClick = {
                    cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else CameraSelector.DEFAULT_BACK_CAMERA
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Cameraswitch,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(32.dp)
                )
            }
        }
    }
}
