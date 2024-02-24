package com.example.nativepoc.controller

import BluetoothService
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.le.ScanCallback
import android.content.Context
import com.example.nativepoc.constants.Constants

class BluetoothController(
    private val context: Context,
    private val scanCallback: ScanCallback,
    private val gattCallback: BluetoothGattCallback
) {

    private val bluetoothService: BluetoothService =
        BluetoothService(context, scanCallback, gattCallback)

    fun startScan() {
        bluetoothService.startScan()
    }

    fun stopScan() {
        bluetoothService.stopScan()
    }

    fun connectToDevice(deviceAddress: String) {
        bluetoothService.connectToDevice(deviceAddress)
    }

    fun disconnectDevice() {
        bluetoothService.disconnectDevice()
    }

    fun discoverServices() {
        bluetoothService.discoverServices()
    }

    fun setBluetoothGatt(gatt: BluetoothGatt) {
        bluetoothService.setBluetoothGatt(gatt)
    }

    // For App Connect
    fun writeAppConnect(): Boolean {
        val data = "qwerty12"
       return bluetoothService.writeCharacteristic(
            Constants.DIAGNOSTIC_SERVICE_UUID,
            Constants.APP_CONNECT_UUID,
            data.toByteArray(Charsets.UTF_8)
        )
    }


    fun getSystemFlags() :Boolean{
       return bluetoothService.enableCharacteristicNotification(
            Constants.DEVICE_INFORMATION_SERVICE_UUID,
            Constants.SYSTEM_FLAGS_UUID,
            Constants.DESCRIPTOR_UUID
        )
    }

    fun getMpuAccelerometerData():Boolean {
        return bluetoothService.enableCharacteristicNotification(
            Constants.SENSOR_SERVICE_UUID,
            Constants.MPU_ACCELEROMETER_UUID,
            Constants.DESCRIPTOR_UUID
        )
    }
}