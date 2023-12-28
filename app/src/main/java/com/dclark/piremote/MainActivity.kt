package com.dclark.piremote

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private lateinit var vibrator: Vibrator

    private lateinit var indicatorAnimation: AnimationDrawable
    private lateinit var indicatorImage: ImageView

    private lateinit var deviceName: String
    private lateinit var ipAddress: String
    private lateinit var port: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        indicatorImage = findViewById(R.id.indicator)
        indicatorImage.setBackgroundResource(R.drawable.power_settings_white)

        initOnClickListeners()
        initSettings()
        initVibrator()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initOnClickListeners() {
        var buttonLayout = findViewById<GridLayout>(R.id.buttonLayout)
        for (i in 0 until buttonLayout.childCount) {
            var button: View = buttonLayout.getChildAt(i)
            val operation: String = button.tag.toString()
            button.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        button.isPressed = true
                        indicatorImage.setBackgroundResource(R.drawable.power_settings_blue)
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        button.isPressed = false
                        vibrate()
                        sendRequest(operation)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun initSettings() {
        deviceName = getString(R.string.tibo_plus3)
        ipAddress = getString(R.string.ip_address)
        port = getString(R.string.port)
    }

    private fun initVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun startIndicatorAnimation(animation: Int) {
        indicatorImage.setBackgroundResource(animation)
        indicatorAnimation = indicatorImage.background as AnimationDrawable
        indicatorAnimation.start()
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, 1))
        } else {
            // backward compatibility for Android API < 26
            @Suppress("DEPRECATION")
            vibrator.vibrate(20)
        }
    }

    private fun sendRequest(operation: String) {
        RemoteControl.sendRequest(ipAddress, port, deviceName, operation) { result: Boolean ->
            startIndicatorAnimation(
                if (result)
                    R.drawable.ir_animation_success
                else
                    R.drawable.ir_animation_error
            )
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            sendRequest(getString(R.string.tibo_plus3_volume_inc))
            return true
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            sendRequest(getString(R.string.tibo_plus3_volume_dec))
            return true
        }

        // Continue with the default behavior for other keys
        return super.onKeyDown(keyCode, event)
    }
}