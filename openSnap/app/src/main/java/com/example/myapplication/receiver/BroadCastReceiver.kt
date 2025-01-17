package com.example.myapplication.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class StopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val message = intent?.getStringExtra("STOP")
        if (message != null) {
            Log.d("HERE", "Stop action received")
        }
        Log.d("HERE", "Stop action not received")
    }
}