package com.example.myapplication

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import com.example.myapplication.services.AuthResponse
import com.example.myapplication.services.Messages


fun handleBackPress(activity: Activity?, backPressedTime: Long, onBackPressedTimeUpdated: (Long) -> Unit) {
    val currentTime = System.currentTimeMillis()
    if (currentTime - backPressedTime < 500) {
        activity?.finish()
    } else {
        onBackPressedTimeUpdated(currentTime)
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
fun Middleware(
    isAuthenticated: Boolean,
    undirect: () -> Unit,
    content: @Composable () -> Unit
) {
    if (isAuthenticated) {
        content()
    } else {
        undirect()
    }
}

fun isAutenticated(authResponse: MutableState<AuthResponse?>): Boolean {
    return !authResponse?.value?.token.isNullOrEmpty()
}
fun isAutenticated(authResponse: AuthResponse?): Boolean {
    return !authResponse?.token.isNullOrEmpty()
}

// https://stackoverflow.com/questions/77752066/jetpackcompose-mutablestate-not-saving-the-state
class MessageViewModel : ViewModel() {
    val messagesState = mutableStateOf<Messages?>(null)
}