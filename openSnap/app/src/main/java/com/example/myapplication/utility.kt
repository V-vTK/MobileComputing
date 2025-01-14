package com.example.myapplication

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

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