package com.example.servicesamples

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class MyService : Service() {

    override fun onCreate() {
        Log.d(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        // Simulate work that takes 10 seconds
        Thread.sleep(10000)
        stopSelf() // Stop the service after the work is done
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "Service destroyed")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        private const val TAG = "MyService"
    }
}
