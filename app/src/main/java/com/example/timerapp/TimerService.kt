package com.example.timerapp

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.Looper
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class TimerService : Service() {

    companion object {
        const val ACTION_TIMER_UPDATE = "com.example.timerapp.TIMER_UPDATE"
        const val EXTRA_TIME = "extra_time"
    }

    private var timeLeft = 0
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isRunning && timeLeft > 0) {
                timeLeft--
                sendTimeUpdate()
                handler.postDelayed(this, 1000) // Повторюється кожну секунду
            }
        }
    }

    private fun sendTimeUpdate() {
        val intent = Intent(ACTION_TIMER_UPDATE).apply {
            putExtra(EXTRA_TIME, timeLeft)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val startTime = intent.getIntExtra(EXTRA_TIME, 0)
            if (startTime > 0) {
                timeLeft = startTime
            }
        }

        // Якщо таймер не працює, запускаємо його
        if (!isRunning) {
            isRunning = true
            handler.post(timerRunnable)
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): Binder {
        return Binder()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        handler.removeCallbacks(timerRunnable)
    }
}
