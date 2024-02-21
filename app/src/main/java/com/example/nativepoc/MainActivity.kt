package com.example.nativepoc

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.io.File
import java.io.FileWriter
import java.io.IOException


class MainActivity : AppCompatActivity(),SensorEventListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private lateinit var accelerometerValuesTextView: TextView
    private lateinit var magnetometerValuesTextView: TextView
    private lateinit var gyroscopeValuesTextView: TextView
    private  lateinit var locationTextView: TextView
    private lateinit var startButton: Button
    private lateinit var deleteButton: Button
    private lateinit var bleButton: Button

    private var accelerometerCount = 0
    private var magnetometerCount = 0
    private var gyroscopeCount = 0
    private var locationCount = 0

    private val sensorDataFolderName = "SensorData"
    private var folderTimestamp = ""
    private val accelerometerFileName = "accelerometer.csv"
    private val magnetometerFileName = "magnetometer.csv"
    private val gyroscopeFileName = "gyroscope.csv"

    private var accelerometerData: String = ""
    private var magnetometerData:  String = ""
    private var gyroscopeData: String = ""

    private var accelerometerArray: FloatArray = FloatArray(3)
    private var magnetometerArray: FloatArray = FloatArray(3)
    private var gyroscopeArray: FloatArray = FloatArray(3)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)


        accelerometerValuesTextView = findViewById(R.id.accelerometerValues)
        magnetometerValuesTextView = findViewById(R.id.magnetometerValues)
        gyroscopeValuesTextView = findViewById(R.id.gyroscopeValues)
        locationTextView = findViewById(R.id.locationTextView)
        startButton = findViewById(R.id.startButton)
        deleteButton = findViewById(R.id.deleteButton)
        bleButton = findViewById(R.id.bleButton)

        checkPermission()



        startButton.setOnClickListener {
            startDumpingData()
        }

        deleteButton.setOnClickListener {
            deleteFolders()
        }

        bleButton.setOnClickListener {
            startActivity(Intent(this,BLEActivity::class.java))
        }

      //  createSensorDataFolder()

    }

    private fun startDumpingData(){
        // creating the base folder
        createSensorDataFolder()

        //start 50 milisecond timer
        startFiftyMillisecondTimer()
    }

    private fun deleteFolders(){
        val directory = File(this.getExternalFilesDir(null),"")

        if (directory.exists() && directory.isDirectory) {
            val files = directory.listFiles()

            files?.forEach { file ->
                if (file.isDirectory) {
                    file.deleteRecursively()
                    println("Folder ${file.name} deleted successfully.")
                }
            }

            println("All folders within deleted successfully.")
        } else {
            println("Does not exist or is not a directory.")
        }
    }

    private fun checkPermission(){
        if (ActivityCompat.checkSelfPermission(this,
              android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request permissions if not granted
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_LOCATION_PERMISSION)
            return
        }

        // Permissions are already granted, fetch and display location
        startLocationUpdates()
    }



    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
            interval = 100 // Update location every 100 ms
            fastestInterval = 50 // Fastest update 50 ms
            priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        // Request location updates
        fusedLocationClient.requestLocationUpdates(locationRequest,
            object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult ?: return
                    for (location in locationResult.locations) {
                        // Handle location updates
                        val latLong = "Latitude: ${location.latitude}, Longitude: ${location.longitude}"
                        val speed = location.speed
                        val altitude = location.altitude
                        locationCount++
                        locationTextView.text = "Location: $latLong"
                    }
                }
            }, null)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Check if location permission is granted
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, fetch and display location
                startLocationUpdates()
            } else {
                // Permission denied, inform the user
                locationTextView.text = "Location permission denied."
            }
        }
    }

    private fun createSensorDataFolder() {
        folderTimestamp = System.currentTimeMillis().toString()
        val folder = File(this.getExternalFilesDir(null), folderTimestamp)
        if (!folder.exists()) {
            folder.mkdirs()

        }

    }

    private fun writeDataToFile(fileName: String, values: FloatArray) {
        val file = File(getExternalFilesDir(null)?.absolutePath + "/$folderTimestamp/$fileName")
        try {
            val writer = FileWriter(file, true) // Append mode
            writer.write("${values[0]}, ${values[1]}, ${values[2]}\n")
            writer.flush()
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun startFiftyMillisecondTimer() {
        val handler = Handler(Looper.getMainLooper())
        val task = object : Runnable {
            override fun run() {
                // Your logging code here
                Log.d("BAM AccCount", "Accelerometer data: $accelerometerData")
                Log.d("BAM MagnoCount", "Magnetometer data: $magnetometerData")
                Log.d("BAM GyroCount", "Gyroscope data: $gyroscopeData")

                writeDataToFile(accelerometerFileName,accelerometerArray)
                writeDataToFile(magnetometerFileName,magnetometerArray)
                writeDataToFile(gyroscopeFileName,gyroscopeArray)

                // Schedule the next execution after 50 milliseconds
                handler.postDelayed(this, 50)
            }
        }
        // Schedule the initial execution
        handler.postDelayed(task, 50)
    }




    override fun onResume() {
        super.onResume()

        accelerometer?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        magnetometer?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        gyroscope?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    val values = "X: ${it.values[0]}, Y: ${it.values[1]}, Z: ${it.values[2]}"
                    accelerometerCount++
                    accelerometerArray = floatArrayOf(it.values[0],it.values[1],it.values[2])
                    accelerometerValuesTextView.text = values
                    accelerometerData = values
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    val values = "X: ${it.values[0]}, Y: ${it.values[1]}, Z: ${it.values[2]}"
                    magnetometerCount++
                    magnetometerArray = floatArrayOf(it.values[0],it.values[1],it.values[2])
                    magnetometerData = values
                    magnetometerValuesTextView.text = values

                }
                Sensor.TYPE_GYROSCOPE -> {
                    val values = "X: ${it.values[0]}, Y: ${it.values[1]}, Z: ${it.values[2]}"
                    gyroscopeCount++
                    gyroscopeArray = floatArrayOf(it.values[0],it.values[1],it.values[2])
                    gyroscopeData = values
                    gyroscopeValuesTextView.text = values

                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }




    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1001
    }
}