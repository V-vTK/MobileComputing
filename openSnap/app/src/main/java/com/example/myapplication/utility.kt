package com.example.myapplication

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

fun handleBackPress(activity: Activity?, backPressedTime: Long, onBackPressedTimeUpdated: (Long) -> Unit) {
    val currentTime = System.currentTimeMillis()
    if (currentTime - backPressedTime < 500) {
        Log.d("BackPress2", "Exiting activity, currentTime: $currentTime, backPressedTime: $backPressedTime")
        activity?.finish()
    } else {
        onBackPressedTimeUpdated(currentTime)
        Log.d("BackPress1", "Exiting activity, currentTime: $currentTime, backPressedTime: $backPressedTime")
    }
}

@Composable
fun BackPressHandler() {
    var backPressedTime by remember { mutableLongStateOf(0) }
    val activity = (LocalContext.current as? Activity)

    BackHandler {
        handleBackPress(activity, backPressedTime) { newBackPressedTime ->
            backPressedTime = newBackPressedTime
        }
    }
}

@Composable
fun imagePicker(selectedImageUri: Uri?, onImageSelected: (Uri?) -> Unit) {
    // https://medium.com/@yogesh_shinde/implementing-image-video-documents-picker-in-jetpack-compose-73ef846cfffb
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        onImageSelected(uri)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally,) {
        Button(onClick = {
            launcher.launch(
                PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }) {
            Text(text = "Select Image")
        }

        selectedImageUri?.let { image ->
            val painter = rememberAsyncImagePainter(
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(data = image)
                    .build()
            )
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .size(250.dp, 250.dp)
                    .padding(16.dp)
            )
        }
    }
}