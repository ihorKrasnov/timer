package com.example.timerapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.timer.R

class MainActivity : AppCompatActivity() {

    private lateinit var startButton: Button
    private lateinit var pauseButton: Button
    private lateinit var stopButton: Button
    private lateinit var textViewTimer: TextView
    private lateinit var editTextTime: EditText
    private var isTimerRunning = false
    private var remainingTime = 0
    private var time = 0
    private lateinit var timerServiceIntent: Intent

    private val handler = Handler(Looper.getMainLooper())
    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val time = intent?.getIntExtra(TimerService.EXTRA_TIME, 0) ?: 0
            textViewTimer.text = formatTime(time)
            remainingTime = time

            // Таймер завершився
            if (time == 0) {
                stopTimer()
                showTimerFinishedNotification()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton = findViewById(R.id.startButton)
        pauseButton = findViewById(R.id.pauseButton)
        stopButton = findViewById(R.id.stopButton)
        textViewTimer = findViewById(R.id.textViewTimer)
        editTextTime = findViewById(R.id.editTextTime)

        // Реєстрація приймача
        LocalBroadcastManager.getInstance(this).registerReceiver(
            timerReceiver,
            android.content.IntentFilter(TimerService.ACTION_TIMER_UPDATE)
        )

        // Кнопка старт
        startButton.setOnClickListener {
            time = if (time > 0)
                time
            else (editTextTime.text.toString().toIntOrNull() ?: return@setOnClickListener)
            startTimer(time)
        }

        // Кнопка пауза
        pauseButton.setOnClickListener {
            pauseTimer()
        }

        // Кнопка стоп
        stopButton.setOnClickListener {
            stopTimer()
        }
    }

    private fun startTimer(time: Int) {
        if (!isTimerRunning) {
            isTimerRunning = true
            remainingTime = time
            timerServiceIntent = Intent(this, TimerService::class.java)
            timerServiceIntent.putExtra(TimerService.EXTRA_TIME, time)
            startService(timerServiceIntent)
        } else {
            timerServiceIntent.putExtra(TimerService.EXTRA_TIME, remainingTime)
            startService(timerServiceIntent)
        }
    }

    private fun pauseTimer() {
        if (isTimerRunning) {
            time = remainingTime
            isTimerRunning = false
            stopService(timerServiceIntent)
        }
    }

    private fun stopTimer() {
        isTimerRunning = false
        remainingTime = 0
        time = 0
        textViewTimer.text = formatTime(remainingTime)
        stopService(timerServiceIntent)
    }

    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secondsLeft = seconds % 60
        return String.format("%02d:%02d", minutes, secondsLeft)
    }

    private fun showTimerFinishedNotification() {
        Toast.makeText(this, "Таймер завершено", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(timerReceiver)
    }
}
