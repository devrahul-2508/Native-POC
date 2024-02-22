@file:Suppress("DEPRECATION")

package com.example.nativepoc

import BluetoothScanner
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.UUID


class BLEActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothGatt: BluetoothGatt? = null

    private var bluetoothScanner: BluetoothScanner? = null

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        if (bluetoothGatt != null) {
            bluetoothGatt?.disconnect()

        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bleactivity)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()


        val scanCallback = object : ScanCallback() {
            @SuppressLint("MissingPermission")
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                // Process scan result here
                val deviceName = result.device.name ?: "Unknown Device"
                val deviceAddress = result.device.address

                if (deviceName.contains("Altor Helmet")) {
                    Log.d(TAG, deviceName)
                    bluetoothScanner!!.stopScan()

                    bluetoothScanner!!.connectToDevice(deviceAddress)
                }
                println("Found BLE Device: $deviceName ($deviceAddress)")
            }

            override fun onScanFailed(errorCode: Int) {
                // Handle scan failure here
                println("BLE Scan failed with error code: $errorCode")
            }
        }

        val gattCallback = object : BluetoothGattCallback() {
            @SuppressLint("MissingPermission")
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                // Handle connection state changes here
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.d(TAG, "State Connected")
                        if (gatt != null) {
                            bluetoothScanner?.setBluetoothGatt(gatt)
                            bluetoothScanner?.discoverServices()
                        }

                    }

                    BluetoothProfile.STATE_DISCONNECTED -> {
                        // We successfully disconnected on our own request
                        Log.d(TAG, "Disconnected")

                        bluetoothScanner?.disconnectDevice()
                    }

                    BluetoothProfile.STATE_CONNECTING -> {
                        // We're CONNECTING or DISCONNECTING, ignore for now
                        Log.d(TAG, "Connecting...")

                    }

                    BluetoothProfile.STATE_DISCONNECTING -> {
                        Log.d(TAG, "Disconnecting....")

                    }
                }

            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    for (gattService: BluetoothGattService in gatt!!.services) {
                        Log.d(TAG, "Service Name Found: " + gattService.uuid.toString())
                    }

                    val service = gatt.getService(SENSOR_SERVICE_UUID)
                    Log.d("BAM SERVICE", service.toString())

                    for (gattCharacteristic in service.characteristics) {

                        Log.d(
                            "BAM Characteristics",
                            "Characteristic Name Found: " + gattCharacteristic.uuid.toString()
                        )


                    }
                    val characteristic = service?.getCharacteristic(SENSOR_CHARACTERISTIC_UUID)
                    characteristic?.let {
                        val data = "qwerty12"

                        bluetoothScanner?.writeCharacteristic(
                            DIAGNOSTIC_SERVICE_UUID,
                            APPCONNECT_CHARACTERISTIC_UUID, data.toByteArray(Charsets.UTF_8)
                        )

                        Handler(Looper.getMainLooper()).postDelayed({
                            bluetoothScanner?.enableCharacteristicNotification(
                                SENSOR_SERVICE_UUID,
                                SENSOR_CHARACTERISTIC_UUID,
                                DESCRIPTOR_UUID
                            )
                        }, 300L)


                        // bluetoothScanner?.readCharacteristic(SERVICE_UUID, CHARACTERISTIC_UUID)
                    }


                } else {
                    Log.d("BAM BLE", "Service discovery failed with status: $status")
                }


            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray,
                status: Int
            ) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (characteristic.uuid == SENSOR_CHARACTERISTIC_UUID) {
                        // Handle characteristic read
                        val value = characteristic.value
                        // Process the value as needed
                        Log.d("BAM BLE", "Custom Characteristic Value: ${value?.contentToString()}")
                    }
                } else {
                    Log.d("BAM BLE", "Characteristic read failed with status: $status")
                }
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // Characteristic write successful
                    // You can perform any additional actions here if needed
                    Log.d("BAM CHARAC", "Characteristic write successful")
                } else {
                    // Characteristic write failed
                    Log.d("BAM CHARAC", "Characteristic write failed with status: $status")
                }
            }

//            override fun onCharacteristicChanged(
//                gatt: BluetoothGatt,
//                characteristic: BluetoothGattCharacteristic,
//                value: ByteArray
//            ) {
//                Log.d("BAM System Flag Values", "Inside")
//                if (CHARACTERISTIC_UUID == characteristic.uuid) {
//                    // Retrieve the new value of the characteristic
//                    val newValue = characteristic.value
//
//                    Log.d("BAM System Flag Values", newValue.contentToString())
//
//                    // Process the new value as needed
//                }
//            }

            @Deprecated("Deprecated in Java")
            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
            ) {
                super.onCharacteristicChanged(gatt, characteristic)

                Log.d("BAM System Flag Values", "Inside")
                if (SENSOR_CHARACTERISTIC_UUID == characteristic.uuid) {
                    // Retrieve the new value of the characteristic
                    val newValue = characteristic.value

                    Log.d("BAM System Flag Values", newValue.contentToString())

                    // Process the new value as needed
                }
            }


        }

        bluetoothScanner = BluetoothScanner(this, scanCallback, gattCallback)


        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_CONNECT
                ), REQUEST_BLE_PERMISSION
            )
        } else {
            if (bluetoothAdapter == null) {
                println("Device doesn't support Bluetooth")
                return
            }

            if (!bluetoothAdapter.isEnabled) {
                println("Bluetooth is not enabled")
                return
            } else {
                // startBleScan

                bluetoothScanner!!.startScan()

            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                bluetoothScanner?.startScan()
            } else {
                println("Location permission denied")
            }
        }
    }


    companion object {
        private const val REQUEST_BLE_PERMISSION = 1001

        private const val TAG = "BAM"

        private val SENSOR_SERVICE_UUID = UUID.fromString("0000a000-0000-1000-8000-00805f9b34fb")

        private val SENSOR_CHARACTERISTIC_UUID =
            UUID.fromString("0000a001-0000-1000-8000-00805f9b34fb")

        private val DIAGNOSTIC_SERVICE_UUID =
            UUID.fromString("0000ff00-0000-1000-8000-00805f9b34fb")

        private val APPCONNECT_CHARACTERISTIC_UUID =
            UUID.fromString("0000ff03-0000-1000-8000-00805f9b34fb")


        private val DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    }
}