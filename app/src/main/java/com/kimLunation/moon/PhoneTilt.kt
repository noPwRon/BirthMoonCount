// This is the package name for the code. It helps organize code in a project.
package com.kimLunation.moon

// Imports are used to bring in code from other parts of the project or from external libraries.
import android.content.Context // Provides access to application-specific resources and classes, including the sensor service.
import android.hardware.Sensor // Represents a sensor, like the accelerometer or gyroscope.
import android.hardware.SensorEvent // Provides the raw sensor data when a sensor reading changes.
import android.hardware.SensorEventListener // An interface for receiving notifications from the SensorManager when sensor values have changed.
import android.hardware.SensorManager // The class that lets you access the device's sensors.
import androidx.compose.runtime.Composable // An annotation that marks a function as a Jetpack Compose UI component.
import androidx.compose.runtime.DisposableEffect // A composable for side effects that need to be cleaned up when the composable leaves the screen.
import androidx.compose.runtime.State // A holder for a value that can be observed by Compose.
import androidx.compose.runtime.mutableFloatStateOf // Creates a mutable float state object that is observable by Compose.
import androidx.compose.runtime.remember // Remembers a value across recompositions.
import androidx.compose.runtime.rememberUpdatedState // Remembers a value and keeps it updated across recompositions, useful for effects.
import kotlin.math.atan2 // A mathematical function to calculate an angle.

/**
 * A composable function that provides the phone's roll angle in degrees.
 * Roll is the rotation of the phone as if it were a steering wheel.
 * The returned value is suitable for use with 'rotationZ' in a composable's modifier.
 *
 * @param context The application context, needed to get the sensor service.
 * @return A 'State' object containing the roll angle in degrees. The value will be between -180 and 180.
 */
@Composable
fun rememberPhoneRollDegrees(context: Context): State<Float> {
    // 'mutableFloatStateOf' creates a state object for a float value. When this value changes,
    // any composable that reads it will be recomposed.
    val rollDegState = remember { mutableFloatStateOf(0f) }
    // 'rememberUpdatedState' is used to make sure that the 'DisposableEffect' always has the latest context,
    // even if the composable is recomposed with a new one.
    val ctx = rememberUpdatedState(context)

    // 'DisposableEffect' is perfect for managing resources that need to be cleaned up, like sensor listeners.
    // The code inside the block is executed when the composable is first displayed.
    // The 'onDispose' block is executed when the composable is removed from the screen.
    DisposableEffect(Unit) {
        // Get the 'SensorManager' from the Android system.
        val sm = ctx.value.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // Get the default rotation vector sensor. This sensor is a fusion of the accelerometer, gyroscope,
        // and magnetometer, providing a robust orientation of the device in 3D space.
        val sensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        if (sensor == null) {
            // If the device doesn't have this sensor, we do nothing.
            onDispose { }
        } else {
            // These arrays will be used to store data from the sensor.
            val rotationMatrix = FloatArray(9)
            val remapped = FloatArray(9)
            val orientation = FloatArray(3)

            // Create a 'SensorEventListener' to listen for sensor changes.
            val listener = object : SensorEventListener {
                // This method is called whenever the sensor reports a new value.
                override fun onSensorChanged(event: SensorEvent) {
                    // Convert the rotation vector from the event into a 3x3 rotation matrix.
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                    // Remap the coordinate system. This is important to get stable roll values when the phone
                    // is in a portrait orientation.
                    SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_X,
                        SensorManager.AXIS_Z,
                        remapped
                    )

                    // Get the orientation angles (azimuth, pitch, roll) from the remapped rotation matrix.
                    SensorManager.getOrientation(remapped, orientation)

                    // The orientation array contains {azimuth, pitch, roll} in radians.
                    // We want the roll, which is the third element (index 2).
                    val rollRad = orientation[2]

                    // Convert the roll from radians to degrees.
                    val deg = (rollRad * 57.2957795f)

                    // Update the state with the new roll value, wrapped to be between -180 and 180.
                    rollDegState.floatValue = wrap180(deg)
                }

                // This method is called when the sensor's accuracy changes. We don't need to do anything here.
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }

            // Register our listener to receive updates from the sensor.
            // 'SENSOR_DELAY_GAME' is a good rate for UI updates.
            sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)

            // This 'onDispose' block is crucial. It unregisters the listener when the composable is no longer visible.
            // This prevents battery drain and memory leaks.
            onDispose {
                sm.unregisterListener(listener)
            }
        }
    }

    // Return the state object. The UI will automatically update when its value changes.
    return rollDegState
}

/**
 * A composable function that provides the phone's azimuth (heading) in degrees.
 * Azimuth is the direction the phone is pointing, relative to magnetic north.
 * 0 degrees is North, 90 is East, 180 is South, and 270 is West.
 *
 * @param context The application context, needed to get the sensor service.
 * @return A 'State' object containing the azimuth in degrees, from 0 to 360.
 */
@Composable
fun rememberPhoneAzimuthDegrees(context: Context): State<Float> {
    val azimuthDegState = remember { mutableFloatStateOf(0f) }
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
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_X,
                        SensorManager.AXIS_Z,
                        remapped
                    )
                    SensorManager.getOrientation(remapped, orientation)
                    // The azimuth (or yaw) is the first element (index 0) of the orientation array.
                    val azimuthRad = orientation[0]
                    // Convert from radians to degrees.
                    val deg = (azimuthRad * 57.2957795f)
                    // Update the state, wrapping the value to be between 0 and 360.
                    azimuthDegState.floatValue = wrap360(deg)
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }

            sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)

            onDispose {
                sm.unregisterListener(listener)
            }
        }
    }

    return azimuthDegState
}

/**
 * A helper function to wrap an angle in degrees to be within the range [-180, 180].
 */
private fun wrap180(deg: Float): Float {
    var v = deg % 360f
    if (v <= -180f) v += 360f
    if (v > 180f) v -= 360f
    return v
}

/**
 * A helper function to wrap an angle in degrees to be within the range [0, 360).
 */
private fun wrap360(deg: Float): Float {
    var v = deg % 360f
    if (v < 0f) v += 360f
    return v
}
