package com.example.ble.presenter

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.util.Log
import com.example.ble.*
import java.util.*
import javax.inject.Inject

@SuppressLint("MissingPermission")
class BlePresenterImpl @Inject constructor(
    private val gattCallbackImpl: GattClient
) : BlePresenter {

    override fun writeDescriptor(descriptor: BluetoothGattDescriptor, payload: ByteArray) {
        gattCallbackImpl.getBluetoothGatt()?.let { gatt ->
            descriptor.value = payload
            gatt.writeDescriptor(descriptor)
        } ?: error("Not connected to a BLE device!")
    }

    override fun enableNotifications(characteristic: BluetoothGattCharacteristic) {
        val cccdUuid = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")
        val payload = when {
            characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> {
                Log.e(
                    "ConnectionManager",
                    "${characteristic.uuid} doesn't support notifications/indications"
                )
                return
            }
        }

        characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
            if (gattCallbackImpl.getBluetoothGatt()?.setCharacteristicNotification(characteristic, true) == false) {
                Log.e(
                    "ConnectionManager",
                    "setCharacteristicNotification failed for ${characteristic.uuid}"
                )
                return
            }
            writeDescriptor(cccDescriptor, payload)
        } ?: Log.e(
            "ConnectionManager",
            "${characteristic.uuid} doesn't contain the CCC descriptor!"
        )
    }

    override fun disableNotifications(characteristic: BluetoothGattCharacteristic) {
        if (!characteristic.isNotifiable() && !characteristic.isIndicatable()) {
            Log.e(
                "ConnectionManager",
                "${characteristic.uuid} doesn't support indications/notifications"
            )
            return
        }

        val cccdUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
            if (gattCallbackImpl.getBluetoothGatt()?.setCharacteristicNotification(characteristic, false) == false) {
                Log.e(
                    "ConnectionManager",
                    "setCharacteristicNotification failed for ${characteristic.uuid}"
                )
                return
            }
            writeDescriptor(cccDescriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
        } ?: Log.e(
            "ConnectionManager",
            "${characteristic.uuid} doesn't contain the CCC descriptor!"
        )
    }
}