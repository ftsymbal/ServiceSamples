package com.example.servicesamples

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var myService: Messenger? = null
    private lateinit var myMessenger: Messenger
    private var isServiceBound = false
    private var isCounterRunning = false
    private lateinit var countdownTextView: TextView
    private lateinit var startServiceButton: Button
    private val handler = Handler(Looper.getMainLooper())

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            myService = Messenger(service)
            isServiceBound = true
            sendMessageToService(MSG_IS_RUNNING)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            myService = null
            isServiceBound = false
        }
    }

    inner class ResponseHandler(
    ) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_START -> {
                    //Counting Started
                    isCounterRunning = true;
                    startPolling()
                    startServiceButton.text = getString(R.string.restart_service)
                    updateScreenCounter(msg.arg1)
                }
                MSG_RESTART ->
                    //Counting Restarted
                    updateScreenCounter(msg.arg1)
                MSG_GET_COUNTER ->
                    //Got new counter value
                    updateScreenCounter(msg.arg1)
                MSG_IS_RUNNING ->
                    //Got response to is_service_running?
                    if (msg.arg1 == 1) {
                        isCounterRunning = true
                        startPolling()
                        startServiceButton.text = getString(R.string.restart_service)
                    } else {
                        isCounterRunning = false
                        startServiceButton.text = getString(R.string.start_service)
                    }
                else -> super.handleMessage(msg)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        countdownTextView = findViewById(R.id.countdownTextView)
        startServiceButton = findViewById(R.id.startServiceButton)

        myMessenger = Messenger(ResponseHandler())
        startService()

        startServiceButton.setOnClickListener {
            if (isCounterRunning) {
                sendMessageToService(MSG_RESTART)
            } else {
                sendMessageToService(MSG_START)
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
                sendMessageToService(MSG_GET_COUNTER)
                //Keep polling if counter still running
                if (isCounterRunning)
                    handler.postDelayed(this, 1000)
            }
        })
    }

    private fun updateScreenCounter(counter: Int) {
        if (counter > 0) {
            countdownTextView.text = counter.toString()
        }
        else {
            //Counter run out. Stop polling
            startServiceButton.text = getString(R.string.start_service)
            countdownTextView.text = ""
            isCounterRunning = false
        }
    }

    private fun sendMessageToService(messageId : Int){
        val msg: Message = Message.obtain(null, messageId, 0, 0)
        msg.replyTo = myMessenger
        try {
            myService?.send(msg)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
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
