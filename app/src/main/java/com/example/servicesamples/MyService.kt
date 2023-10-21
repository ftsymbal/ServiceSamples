package com.example.servicesamples

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class MyService : Service() {

    private val binder = LocalBinder()
    private var counter = 11
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    inner class LocalBinder : Binder() {
        fun getService(): MyService = this@MyService
    }

    override fun onBind(intent: Intent): IBinder {
        if (!isRunning) {
            startCountdown()
            isRunning = true
        }
        return binder
    }

    fun getCurrentProgress(): Int {
        return counter
    }

    fun restartCountdown(): Int {
        counter = 10
        return counter
    }

    private fun startCountdown() {
        handler.post(object : Runnable {
            override fun run() {
                if (counter > 0) {
                    handler.postDelayed(this, 1000)
                    counter--
                }
                else {
                    isRunning = false
                }
            }
        })
    }
}
