package com.example.myapplication.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.MutableState

class StopNotificationReceiver(private val stopFlag: MutableState<Boolean>) : BroadcastReceiver() {
    // Had to change the minimum compile version and activation and it magically started working...
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("INTENT", "INTENT STOP PRESSED")
        stopFlag.value = true
    }
}
