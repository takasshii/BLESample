package com.example.ble.presenter

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.ble.BuildConfig
import com.example.ble.presenter.domain.GattState
import com.example.ble.printGattTable
import com.example.ble.toHexString
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@SuppressLint("MissingPermission")
class GattClient @Inject constructor(
    @ApplicationContext val context : Context
) : BluetoothGattCallback() {

    private var bluetoothGatt: BluetoothGatt? = null

    private var mWriteCharacteristic: BluetoothGattCharacteristic? = null

    fun getBluetoothGatt(): BluetoothGatt? = bluetoothGatt
    fun getWriteCharacteristic() : BluetoothGattCharacteristic? = mWriteCharacteristic

    private val _state = MutableStateFlow<GattState>(GattState.GattError(""))
    val state: SharedFlow<GattState> = _state.asStateFlow()

    private fun notifyGattState(state: GattState) {
        _state.value = state
    }

    fun connectGatt(bluetoothDevice: BluetoothDevice) {
        bluetoothDevice.connectGatt(context, false, this)
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        val deviceAddress = gatt.device.address

        // device is connected
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                // Store a reference to BluetoothGatt
                bluetoothGatt = gatt
                Handler(Looper.getMainLooper()).post {
                    bluetoothGatt?.discoverServices()
                }
                notifyGattState(GattState.GattConnect("Successfully connected"))
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                gatt.close()
                notifyGattState(GattState.GattDisconnect("disconnected"))

                val characteristic = gatt.getService(UUID.fromString(BuildConfig.SERVICE_UUID))
                    .getCharacteristic(UUID.fromString(BuildConfig.CHARACTERISTIC_UUID))
            }
        } else {
            Log.w(
                "BluetoothGattCallback",
                "Error $status encountered for $deviceAddress! Disconnecting..."
            )
            gatt.close()

            notifyGattState(GattState.GattError("\"Error $status\""))
        }
    }

    // gatt's service is discovered
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        with(gatt) {
            Log.w(
                "BluetoothGattCallback",
                "Discovered ${services.size} services for ${device.address}"
            )
            val characteristic = gatt.getService(UUID.fromString(BuildConfig.SERVICE_UUID))
                .getCharacteristic(UUID.fromString(BuildConfig.CHARACTERISTIC_UUID))

            val gattServices = gatt.services
            for (gattService in gattServices) {
                if (gattService.uuid == UUID.fromString(BuildConfig.SERVICE_UUID)) { // YOUR_SERVICE_UUIDには、使用しているサービスのUUIDを指定してください。
                    val characteristics = gattService.characteristics
                    for (characteristic in characteristics) {
                        if (characteristic.uuid == UUID.fromString(BuildConfig.CHARACTERISTIC_UUID)) { // YOUR_CHARACTERISTIC_UUIDには、使用しているキャラクタリスティックのUUIDを指定してください。
                            mWriteCharacteristic = characteristic
                            break
                        }
                    }
                }
            }
            printGattTable() // See implementation just above this section
            // Consider connection setup as complete here
            notifyGattState(GattState.ServiceDiscovered("Discovered Service"))
        }
    }

    override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
        Log.d(
            "BluetoothGattCallback",
            "ATT MTU changed to $mtu, success: ${status == BluetoothGatt.GATT_SUCCESS}"
        )
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        with(characteristic) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.i(
                        "BluetoothGattCallback",
                        "Wrote to characteristic $uuid | value: ${value.toHexString()}"
                    )
                    notifyGattState(GattState.CharacteristicWriteSuccess("wrote ${value.toHexString()}"))
                }
                // ATTRIBUTE_LENGTHを超えてしまっている場合
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                    Log.e("BluetoothGattCallback", "Write exceeded connection ATT MTU!")
                    notifyGattState(GattState.CharacteristicWriteError("wrote exceeded connection ATT MTU!"))
                }
                // 書き込みが許可されていない場合
                // チェックする前にfalseを返すことがあるのであまりお勧めされない
                BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                    Log.e("BluetoothGattCallback", "Write not permitted for $uuid!")
                    notifyGattState(GattState.CharacteristicWriteError("Write not permitted for $uuid!"))
                }
                else -> {
                    Log.e(
                        "BluetoothGattCallback",
                        "Characteristic write failed for $uuid, error: $status"
                    )
                    notifyGattState(GattState.CharacteristicWriteError("Characteristic write failed for $uuid, error: $status"))
                }
            }
        }
    }

    // if call readCharacteristic, this method will be called
    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        with(characteristic) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.i(
                        "BluetoothGattCallback",
                        "Read characteristic $uuid:\n${value.toHexString()}"
                    )
                    notifyGattState(GattState.CharacteristicReadSuccess("Read characteristic $uuid:\n${value.toHexString()}"))
                }
                BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                    Log.e("BluetoothGattCallback", "Read not permitted for $uuid!")
                    notifyGattState(GattState.CharacteristicReadError("Read not permitted"))
                }
                else -> {
                    Log.e(
                        "BluetoothGattCallback",
                        "Characteristic read failed for $uuid, error: $status"
                    )
                    notifyGattState(GattState.CharacteristicReadError("Characteristic read failed for $uuid, error: $status"))
                }
            }
        }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        with(characteristic) {
            Log.i(
                "BluetoothGattCallback",
                "Characteristic $uuid changed | value: ${value.toHexString()}"
            )
        }
    }
}