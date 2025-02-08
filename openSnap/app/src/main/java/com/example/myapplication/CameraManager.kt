package com.example.myapplication

import android.content.Context
import android.util.Log
import android.view.Surface
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

// https://aboyi.medium.com/how-to-make-your-own-android-camera-app-without-knowing-how-aca3364358b
// https://medium.com/androiddevelopers/getting-started-with-camerax-in-jetpack-compose-781c722ca0c4
// https://developer.android.com/media/camera/camerax/orientation-rotation

class CameraPreviewViewModel : ViewModel() {
    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest
    private var cameraControl: CameraControl? = null

    // Docs:
    // This will affect the EXIF rotation metadata in images saved by takePicture calls and the ImageInfo.
    // getRotationDegrees() value of the ImageProxy returned by ImageCapture. OnImageCapturedCallback.
    // These will be set to be the rotation, which if applied to the output image data,
    // will make the image match the target rotation specified here.
    val imageCapture = ImageCapture.Builder().setTargetRotation(Surface.ROTATION_0).build()

    private val cameraPreviewUseCase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _surfaceRequest.update { newSurfaceRequest }
        }
    }

    suspend fun bindToCamera(
        appContext: Context,
        lifecycleOwner: LifecycleOwner,
    ) {
        val processCameraProvider = ProcessCameraProvider.awaitInstance(appContext)

        // Unbind first to avoid collisions
        processCameraProvider.unbind(cameraPreviewUseCase, imageCapture)

        val camera = processCameraProvider.bindToLifecycle(
            lifecycleOwner, DEFAULT_BACK_CAMERA, cameraPreviewUseCase, imageCapture
        )

        cameraControl = camera.cameraControl

        try { awaitCancellation() } finally {
            processCameraProvider.unbindAll()
            cameraControl = null
        }
    }
}