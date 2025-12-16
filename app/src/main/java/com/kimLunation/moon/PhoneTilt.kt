package com.kimLunation.moon

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlin.math.atan2

/**
 * Returns phone roll angle in degrees suitable for rotationZ.
 * Roll is how much the phone is rotated like a steering wheel.
 */
@Composable
fun rememberPhoneRollDegrees(context: Context): State<Float> {
    val rollDegState = remember { mutableFloatStateOf(0f) }
    val ctx = rememberUpdatedState(context)

    DisposableEffect(Unit) {
        val sm = ctx.value.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        if (sensor == null) {
            onDispose { }
        } else {
            val rotationMatrix = FloatArray(9)
            val remapped = FloatArray(9)
            val orientation = FloatArray(3)

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    // Rotation vector -> rotation matrix
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                    // Remap so our roll is stable for portrait usage
                    SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_X,
                        SensorManager.AXIS_Z,
                        remapped
                    )

                    SensorManager.getOrientation(remapped, orientation)

                    // orientation[] = { azimuth(yaw), pitch, roll } in radians
                    val rollRad = orientation[2]

                    // Convert to degrees
                    val deg = (rollRad * 57.2957795f)

                    // Keep it in [-180, 180]
                    rollDegState.floatValue = wrap180(deg)
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }

            sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)

            onDispose {
                sm.unregisterListener(listener)
            }
        }
    }

    return rollDegState
}

private fun wrap180(deg: Float): Float {
    var v = deg % 360f
    if (v <= -180f) v += 360f
    if (v > 180f) v -= 360f
    return v
}
