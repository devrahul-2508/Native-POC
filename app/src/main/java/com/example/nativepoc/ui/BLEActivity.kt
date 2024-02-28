@file:Suppress("DEPRECATION")

package com.example.nativepoc.ui

import BluetoothService
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
import com.example.nativepoc.R
import com.example.nativepoc.constants.Constants
import com.example.nativepoc.controller.BluetoothController


class BLEActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothGatt: BluetoothGatt? = null

    private var bluetoothController: BluetoothController? = null

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
                    bluetoothController!!.stopScan()

                    bluetoothController!!.connectToDevice(deviceAddress)
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
                            bluetoothController?.setBluetoothGatt(gatt)
                            bluetoothController?.discoverServices()
                        }

                    }

                    BluetoothProfile.STATE_DISCONNECTED -> {
                        // We successfully disconnected on our own request
                        Log.d(TAG, "Disconnected")

                        bluetoothController?.disconnectDevice()
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

                    if (bluetoothController != null) {
                        bluetoothController?.writeAppConnect()

                        Handler(Looper.getMainLooper()).postDelayed({
                            bluetoothController?.getSystemFlags()

                        }, 300L)


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
                    if (characteristic.uuid == Constants.MPU_ACCELEROMETER_UUID) {
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

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {

                if (Constants.SYSTEM_FLAGS_UUID == characteristic.uuid) {
                    val newValue = characteristic.value

                    Log.d("BAM System Flag Values", newValue.contentToString())
                }
                if (Constants.MPU_ACCELEROMETER_UUID == characteristic.uuid) {
                    // Retrieve the new value of the characteristic
                    val newValue = characteristic.value

                    Log.d("BAM Accelerometer Values", newValue.contentToString())

                    // Process the new value as needed
                }

            }


        }

        bluetoothController = BluetoothController(this, scanCallback, gattCallback)


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

                bluetoothController!!.startScan()

            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                bluetoothController?.startScan()
            } else {
                println("Location permission denied")
            }
        }
    }


    companion object {
        private const val REQUEST_BLE_PERMISSION = 1001

        private const val TAG = "BAM"


    }
}