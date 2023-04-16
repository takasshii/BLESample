package com.example.ble.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ble.*
import com.example.ble.domain.BleUIState
import com.example.ble.domain.BleUseCase
import com.example.ble.presenter.GattClient
import com.example.ble.presenter.ScanClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@HiltViewModel
@SuppressLint("MissingPermission")
class BleViewModel @Inject constructor(
    private val useCase: BleUseCase,
    private val gattClient: GattClient,
    private val scanClient: ScanClient,
    private val scanSettings: ScanSettings,
    private val scanFilter: ScanFilter,
    private val bluetoothManager: BluetoothManager
) : ViewModel() {

    val scanResults = mutableListOf<ScanResult>()
    private var isScanning = false

    val scanResultAdapter: BLEListAdapter by lazy {
        BLEListAdapter(scanResults) { result ->
            // User tapped on a scan result
            if (isScanning) {
                clearScanResult(isScan = false)
            }
            with(result.device) {
                Log.w("ScanResultAdapter", "Connecting to $address")
                gattClient.connectGatt(this)
            }
        }
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        bluetoothManager.adapter
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    val bleState: SharedFlow<BleUIState> = useCase().shareIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly
    )

    fun reflectScanResult(result: ScanResult) {
        val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
        if (indexQuery != -1) { // A scan result already exists with the same address
            scanResults[indexQuery] = result
            scanResultAdapter.notifyItemChanged(indexQuery)
        } else {
            with(result.device) {
                Log.i(
                    "ScanCallback",
                    "Found BLE device! Name: ${name ?: "Unnamed"}, address: $address"
                )
            }
            scanResults.add(result)
            scanResultAdapter.notifyItemInserted(scanResults.size)
        }
    }

    fun clearScanResult(isScan : Boolean) {
        scanResults.clear()
        scanResultAdapter.notifyDataSetChanged()
        isScanning = isScan

        if(isScan) {
            bleScanner.startScan(mutableListOf(scanFilter), scanSettings, scanClient)
        } else {
            bleScanner.stopScan(scanClient)
        }
    }

    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic?, payload: ByteArray) {
        characteristic?.let {
            val writeType = when {
                characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                characteristic.isWritableWithoutResponse() -> {
                    BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                }
                else -> error("Characteristic ${characteristic.uuid} cannot be written to")
            }

            gattClient.getBluetoothGatt()?.let { gatt ->
                characteristic.writeType = writeType
                characteristic.value = payload
                gatt.writeCharacteristic(characteristic)
            } ?: error("Not connected to a BLE device!")
        }
    }

    fun readBatteryLevel() {
        val batteryServiceUuid = UUID.fromString(BuildConfig.SERVICE_UUID)
        val batteryLevelCharUuid = UUID.fromString(BuildConfig.CHARACTERISTIC_UUID)
        gattClient.getBluetoothGatt()?.let {
            val batteryLevelChar = it
                .getService(batteryServiceUuid)?.getCharacteristic(batteryLevelCharUuid)
            if (batteryLevelChar?.isReadable() == true) {
                it.readCharacteristic(batteryLevelChar)
            }
        }
    }
}