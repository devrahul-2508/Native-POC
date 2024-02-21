package com.example.nativepoc.controller

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanSettings
import android.net.wifi.ScanResult
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.nativepoc.BLEActivity
import java.util.UUID

class BleController {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothGatt: BluetoothGatt? = null
    private val handler = Handler(Looper.getMainLooper())
    @SuppressLint("MissingPermission")
    fun searchForNearbyDevices(){
        Log.d(TAG, "Starting Scan")
        val scanSettings =
            ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

        bluetoothAdapter.bluetoothLeScanner.startScan(null, scanSettings, bleScanCallback)


        handler.postDelayed({
            stopBleScan()
        }, SCAN_PERIOD)
    }


    public fun concat(str1:String, str2:String):String{

        return "${str1}${str2}"


    }

    @SuppressLint("MissingPermission")
    private fun stopBleScan() {
        bluetoothAdapter.bluetoothLeScanner.stopScan(bleScanCallback)
    }



    private val bleScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let {
                val device: BluetoothDevice = it.device
                // You can process the scanned BLE device here


                if (device.name != null) {
                    if (device.name.contains("Altor Helmet")) {
                        Log.d("BAM BLE", "Name:${device.name} MacID:${device.address}")

                        connectToGattService(device)
                        stopBleScan()

                    }


                }

            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToGattService(device: BluetoothDevice) {

        val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // Connected to the device, now discover services
                    Log.d("BAM SERVICE", "Connected")

//                    var bondState: Int = device.bondState
//                    if(bondState == BluetoothDevice.BOND_NONE || bondState == BluetoothDevice.BOND_BONDED){
//                        gatt.discoverServices()
//                    }
                    gatt.discoverServices()


                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // We successfully disconnected on our own request
                    Log.d("BAM Status", "Disconnected")
                    gatt.close()
                } else if (newState == BluetoothProfile.STATE_CONNECTING) {
                    // We're CONNECTING or DISCONNECTING, ignore for now
                    Log.d("BAM Status", "Connecting....")

                } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
                    Log.d("BAM Status", "Disconnecting....")

                }


            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // Services discovered, you can now access them

                    Log.d("BAM SERVICE", "Discovered")

                    for (gattService: BluetoothGattService in gatt.services) {
                        Log.d("BAM SERVICES", "Service Name Found: " + gattService.uuid.toString())
                    }

                    // Get the custom characteristic

                    val service = gatt.getService(SERVICE_UUID)
                    Log.d("BAM SERVICE", service.toString())

                    for (gattCharacteristic in service.characteristics) {

                        Log.d(
                            "BAM Characteristics",
                            "Characteristic Name Found: " + gattCharacteristic.uuid.toString()
                        )


//                        val characteristic = service.getCharacteristic(gattCharacteristic.uuid)
//
//                        characteristic.let {
//                            gatt.readCharacteristic(it)
//                        }

                    }
                    val characteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)
                    characteristic?.let {
                        // Read or write to the custom characteristic as needed
                        // Example: Read the value
                        // gatt.readCharacteristic(it)
                        // Example: Write to the characteristic
                        // it.value = byteArrayOf(0x01, 0x02) // Data to write

                        // val valueToWrite = "qwerty12"

                        // Write the value to the characteristic
                        // it.setValue(valueToWrite)
                        // gatt.writeCharacteristic(it)

                        gatt.setCharacteristicNotification(it, true)


                        // Descriptor for enabling notifications (this is typical for most BLE devices)
                        val descriptor = it.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        descriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE

                        gatt.writeDescriptor(descriptor)
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
                    if (characteristic.uuid == CHARACTERISTIC_UUID) {
                        // Handle characteristic read
                        val value = characteristic.value
                        // Process the value as needed
                        Log.d("BAM BLE", "Custom Characteristic Value: ${value?.contentToString()}")
                    }

//                    val value = characteristic.value
//                    // Process the value as needed
//                    Log.d("BAM BLE", "Custom Characteristic Value: ${value?.contentToString()}")
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
                    Log.e("BAM CHARAC", "Characteristic write failed with status: $status")
                }
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
                Log.d("BAM System Flag Values", "Inside")
                if (CHARACTERISTIC_UUID == characteristic.uuid) {
                    // Retrieve the new value of the characteristic
                    val newValue = characteristic.value

                    Log.d("BAM System Flag Values", newValue.contentToString())

                    // Process the new value as needed
                }
            }


        }

        // Connect to the device
        bluetoothGatt = device.connectGatt(this, false, gattCallback)

        // Remember to handle disconnection and other necessary scenarios
    }

    companion object {
        const val TAG = "BleController"
        private const val SCAN_PERIOD: Long = 60000 // 6 SECONDS

        private val SERVICE_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")

        private val CHARACTERISTIC_UUID = UUID.fromString("00000006-0000-1000-8000-00805f9b34fb")
    }
}