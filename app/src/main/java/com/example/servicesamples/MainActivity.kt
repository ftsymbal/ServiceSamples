package com.example.servicesamples

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var myService: MyService? = null
    private var isServiceBound = false
    private lateinit var countdownTextView: TextView
    private lateinit var startServiceButton: Button
    private val handler = Handler(Looper.getMainLooper())

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MyService.LocalBinder
            myService = binder.getService()
            isServiceBound = true
            if (myService!!.isRunning) {
                startServiceButton.text = getString(R.string.restart_service)
                startPolling()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        countdownTextView = findViewById(R.id.countdownTextView)
        startServiceButton = findViewById(R.id.startServiceButton)

        startService()

        startServiceButton.setOnClickListener {
            if (myService?.isRunning == true) {
                countdownTextView.text = myService?.restartCountdown().toString()
            }
            else {
                startServiceButton.text = getString(R.string.restart_service)
                countdownTextView.text = myService?.startCountdown().toString()
                startPolling()
            }
        }
    }

    private fun startService() {
        val serviceIntent = Intent(this, MyService::class.java)
        this.startService(serviceIntent)
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun startPolling() {
        handler.post(object : Runnable {
            override fun run() {
                val counter = myService?.getCurrentProgress()
                if (counter!! > 0) {
                    countdownTextView.text = counter.toString()
                    handler.postDelayed(this, 1000)
                }
                else {
                    startServiceButton.text = getString(R.string.start_service)
                    countdownTextView.text = ""
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService()
    }

    private fun unbindService() {
        if (isServiceBound) {
            unbindService(connection)
            isServiceBound = false
        }
    }
}
