package com.anafthdev.mlkit.ui.face_detection

import android.util.Size
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
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

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            val faceDetectionImageAnalyzer = FaceDetectionImageAnalyzer().apply {
                addOnCompleteListener { faces, outputTransform ->
                    facePositions.clear()

                    if (faces.isEmpty()) return@addOnCompleteListener

                    val face = faces[0]
                    val boundingBox = face.boundingBox.toRectF()

                    facePositions.add(FacePosition(boundingBox.toRect().toComposeRect()))
                }

                addOnFailureListener { e ->
                    Timber.e(e)
                }
            }

            // Change default camera to front
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            imageAnalysisTargetSize = CameraController.OutputSize(
                with(density) {
                    Size(ceil(config.screenWidthDp.dp.toPx()).toInt(), ceil(config.screenHeightDp.dp.toPx()).toInt())
                }
            )

            bindToLifecycle(lifecycleOwner)

            // Set image analyzer
            setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(context), faceDetectionImageAnalyzer)
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
        }
    }

    var cameraSelector by remember {
        mutableStateOf(CameraSelector.DEFAULT_FRONT_CAMERA)
    }

    var cameraProvider by remember {
        mutableStateOf<ProcessCameraProvider?>(null)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
//                    controller = cameraController
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            update = { view ->
                if (cameraProvider != null) {
                    val preview = Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build()

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setDefaultResolution(
                            with(density) {
                                Size(ceil(config.screenWidthDp.dp.toPx()).toInt(), ceil(config.screenHeightDp.dp.toPx()).toInt())
                            }
                        )
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build()
                        .apply {
                            val faceDetectionImageAnalyzer = FaceDetectionImageAnalyzer().apply {
                                addOnCompleteListener { faces, outputTransform ->
                                    facePositions.clear()

                                    if (faces.isEmpty()) return@addOnCompleteListener

                                    val face = faces[0]
                                    val boundingBox = face.boundingBox.toRectF()

                                    facePositions.add(FacePosition(boundingBox.toRect().toComposeRect()))
                                }

                                addOnFailureListener { e ->
                                    Timber.e(e)
                                }
                            }

                            setAnalyzer(ContextCompat.getMainExecutor(context), faceDetectionImageAnalyzer)
                        }

                    preview.setSurfaceProvider(view.surfaceProvider)

                    var camera = cameraProvider!!.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()

                    for (face in facePositions) {

                        Timber.i("rekt: ${face.boxPosition.topLeft} | ${face.boxPosition.topRight}")

                        drawContext.canvas.drawRect(
                            rect = face.boxPosition.let {
                                it.copy(
                                    left = size.width - it.left - size.width / 4,
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
        )
    }
}
