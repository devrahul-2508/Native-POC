import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.math.sign

class BluetoothScanner(
    private val context: Context,
    private val scanCallback: ScanCallback,
    private val gattCallback: BluetoothGattCallback
) {
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()

        Log.d("BAM","Starting Scan")

        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
        bluetoothLeScanner?.startScan(null, scanSettings, scanCallback)

        // Stop scanning after a specified period
        mainHandler.postDelayed({
            stopScan()
        }, SCAN_PERIOD)
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        bluetoothLeScanner?.stopScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(deviceAddress: String) {
        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
        bluetoothGatt = device?.connectGatt(context, false, gattCallback)
        // Handle BluetoothGatt object for further operations, e.g., service discovery, characteristic read/write, etc.
    }

    @SuppressLint("MissingPermission")
    fun disconnectDevice() {
        bluetoothGatt?.close()
    }

    @SuppressLint("MissingPermission")
    fun discoverServices() {
        bluetoothGatt?.discoverServices()
    }

    fun setBluetoothGatt(gatt: BluetoothGatt) {
        bluetoothGatt = gatt
    }

    @SuppressLint("MissingPermission")
    fun readCharacteristic(serviceUuid: UUID, characteristicUuid: UUID) {
        val service = bluetoothGatt?.getService(serviceUuid)
        val characteristic = service?.getCharacteristic(characteristicUuid)
        bluetoothGatt?.readCharacteristic(characteristic)
    }

    @SuppressLint("MissingPermission")
    fun writeCharacteristic(serviceUuid: UUID, characteristicUuid: UUID, data: ByteArray) {
        val service = bluetoothGatt?.getService(serviceUuid)
        val characteristic = service?.getCharacteristic(characteristicUuid)
        characteristic?.value = data
        bluetoothGatt?.writeCharacteristic(characteristic)
    }

    @SuppressLint("MissingPermission")
    fun enableCharacteristicNotification(
        serviceUuid: UUID,
        characteristicUuid: UUID,
        descriptorUuid: UUID
    ) {
        val service = bluetoothGatt?.getService(serviceUuid)


        val characteristic = service?.getCharacteristic(characteristicUuid)
        bluetoothGatt?.setCharacteristicNotification(characteristic, true)

        val descriptor = characteristic?.getDescriptor(descriptorUuid)
        descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        val result = bluetoothGatt?.writeDescriptor(descriptor)
        Log.d("BAM", result.toString())

        // Delay execution using Handler
    }


    companion object {
        private const val SCAN_PERIOD: Long = 60000 // Scan for 10 seconds
    }
}
