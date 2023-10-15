package com.anafthdev.mlkit.data

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.transform.ImageProxyTransformFactory
import androidx.camera.view.transform.OutputTransform
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import timber.log.Timber

class FaceDetectionImageAnalyzer: ImageAnalysis.Analyzer {

    private var onCompleteListener: OnCompleteListener? = null
    private var onFailureListener: OnFailureListener? = null

    private val highAccuracyOpts = FaceDetectorOptions.Builder().apply {
        setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
//        setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
//        // Detect face landmarks (eye, mouth, ect)
//        setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
    }.build()

    private val detector = FaceDetection.getClient(highAccuracyOpts)

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image

        mediaImage?.let { img ->
            val outputTransform = ImageProxyTransformFactory().getOutputTransform(imageProxy)

            // InputImage from ml kit
            val image = InputImage.fromMediaImage(img, imageProxy.imageInfo.rotationDegrees)
            Timber.i("rekt imeg: ${image.width} x ${image.height}")
            detector.process(image)
                .addOnCompleteListener { task ->
                    onCompleteListener?.onComplete(task.result, outputTransform)

                    mediaImage.close()
                    imageProxy.close()
                }
                .addOnFailureListener { exception ->
                    onFailureListener?.onFailure(exception)
                }
        }
    }

    fun addOnCompleteListener(listener: OnCompleteListener) {
        onCompleteListener = listener
    }

    fun addOnFailureListener(listener: OnFailureListener) {
        onFailureListener = listener
    }

    fun interface OnCompleteListener {
        fun onComplete(faces: List<Face>, outputTransform: OutputTransform)
    }

    fun interface OnFailureListener {
        fun onFailure(exception: Exception)
    }

}