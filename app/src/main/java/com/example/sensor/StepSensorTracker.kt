package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.sqrt

class StepSensorTracker(
    context: Context,
    private val onStepDetected: (Int) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepDetectorSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    private val stepCounterSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val accelSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastCounterValue: Float = -1f
    private var isTracking = false

    // Accelerometer-based step detection fallback
    private var lastAccelMagnitude = 0f
    private var lastStepTimestamp = 0L
    private val stepThreshold = 11.8f // G-force threshold for steps

    fun startTracking() {
        if (isTracking || sensorManager == null) return

        var registered = false

        // Prefer Hardware Step Detector
        stepDetectorSensor?.let {
            registered = sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        // If Step Detector not available, try Step Counter
        if (!registered && stepCounterSensor != null) {
            registered = sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
        }

        // Fallback to Accelerometer motion peak detector
        if (!registered && accelSensor != null) {
            sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_GAME)
        }

        isTracking = true
        Log.d("StepSensorTracker", "Step tracking started with sensor: ${if (stepDetectorSensor != null) "StepDetector" else if (stepCounterSensor != null) "StepCounter" else "Accelerometer"}")
    }

    fun stopTracking() {
        if (!isTracking || sensorManager == null) return
        sensorManager.unregisterListener(this)
        isTracking = false
        lastCounterValue = -1f
        Log.d("StepSensorTracker", "Step tracking stopped")
    }

    fun isSensorAvailable(): Boolean {
        return stepDetectorSensor != null || stepCounterSensor != null || accelSensor != null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_DETECTOR -> {
                if (event.values.isNotEmpty() && event.values[0] > 0f) {
                    onStepDetected(1)
                }
            }
            Sensor.TYPE_STEP_COUNTER -> {
                if (event.values.isNotEmpty()) {
                    val currentTotal = event.values[0]
                    if (lastCounterValue >= 0f) {
                        val delta = (currentTotal - lastCounterValue).toInt()
                        if (delta in 1..1000) {
                            onStepDetected(delta)
                        }
                    }
                    lastCounterValue = currentTotal
                }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                // Motion peak step detection
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt(x * x + y * y + z * z)
                val now = System.currentTimeMillis()

                if (magnitude > stepThreshold && lastAccelMagnitude <= stepThreshold && (now - lastStepTimestamp) > 300) {
                    lastStepTimestamp = now
                    onStepDetected(1)
                }
                lastAccelMagnitude = magnitude
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
