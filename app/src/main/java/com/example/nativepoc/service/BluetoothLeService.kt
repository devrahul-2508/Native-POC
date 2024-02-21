//package com.example.nativepoc.service
//
//import android.annotation.SuppressLint
//import android.app.Service
//import android.bluetooth.BluetoothDevice
//import android.bluetooth.BluetoothGatt
//import android.bluetooth.BluetoothGattCallback
//import android.bluetooth.BluetoothProfile
//import android.bluetooth.le.ScanSettings
//import android.content.Intent
//import android.os.Binder
//import android.os.IBinder
//import android.util.Log
//import com.example.nativepoc.BLEActivity
//
//class BluetoothLeService : Service(){
//
//    private val binder = LocalBinder()
//
//    override fun onBind(intent: Intent): IBinder? {
//        return binder
//    }
//
//    inner class LocalBinder : Binder() {
//        fun getService() : BluetoothLeService {
//            return this@BluetoothLeService
//        }
//    }
//
//
//
//    @SuppressLint("MissingPermission")
//    private fun startBleScan() {
//        Log.d("BAMSCAN", "Starting Scan")
//        val scanSettings =
//            ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
//
//        bluetoothAdapter.bluetoothLeScanner.startScan(null, scanSettings, bleScanCallback)
//
//        handler.postDelayed({
//            stopBleScan()
//        }, BLEActivity.SCAN_PERIOD)
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun stopBleScan() {
//        bluetoothAdapter.bluetoothLeScanner.stopScan(bleScanCallback)
//    }
//
//
//    @SuppressLint("MissingPermission")
//    private fun connectToDevice(device: BluetoothDevice) {
//        val gattCallback = object : BluetoothGattCallback() {
//            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
//                if (newState == BluetoothProfile.STATE_CONNECTED) {
//                    // Connected to the device, now discover services
//                    gatt.discoverServices()
//                }
//            }
//
//            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
//                if (status == BluetoothGatt.GATT_SUCCESS) {
//                    // Services discovered, you can now access them
//                    val services = gatt.services
//                    for (service in services) {
//                        Log.d("BAM BLE", "Service UUID: ${service.uuid}")
//                        // You can further explore characteristics, descriptors, etc. here
//                    }
//                } else {
//                    Log.e("BAM BLE", "Service discovery failed with status: $status")
//                }
//            }
//        }
//
//        // Connect to the device
//        val gatt = device.connectGatt(, false, gattCallback)
//        // Remember to handle disconnection and other necessary scenarios
//    }
//
//    override fun onBind(intent: Intent?): IBinder? {
//        TODO("Not yet implemented")
//    }
//}